package org.apache.hadoop.hbase.ipc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.util.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hbase.thirdparty.com.google.common.base.Preconditions;
import org.apache.hbase.thirdparty.com.google.protobuf.Descriptors;
import org.apache.hbase.thirdparty.com.google.protobuf.Message;
import org.apache.hbase.thirdparty.com.google.protobuf.RpcCallback;
import org.apache.hbase.thirdparty.com.google.protobuf.RpcChannel;
import org.apache.hbase.thirdparty.com.google.protobuf.RpcController;

/**
 * A non-blocking implementation of RpcChannel that hedges requests to multiple service end points.
 * First received response is returned to the caller. This abstracts out the logic needed to batch
 * requests to multiple end points underneath and presents itself as a single logical RpcChannel to
 * the client.
 *
 * Hedging Details:
 * ---------------
 * - Hedging of RPCs happens in multiple batches. In each iteration, we select a 'batch' of address
 * end points to make the call to. We do multiple iterations until we get a proper response to the
 * rpc call or all the service addresses are exhausted, which ever happens first.
 *
 * - We randomize the addresses up front so that the batch order per client is non deterministic.
 * This avoids hot spots on the service side. The size of each batch is controlled via 'fanOutSize'.
 * Higher fanOutSize implies we make more rpc calls in a single batch. One needs to mindful of the
 * load on the client and server side when configuring the fan out.
 *
 * - In a happy case, once we receive a response from one end point, we interrupt and cancel the
 * other inflight rpcs in the same batch and return the response to the client. If we do not get a
 * valid response from any address end point, we propagate the error back to the caller.
 *
 * - Rpc timeouts are applied to every hedged rpc.
 *
 * - Callers need to be careful about what rpcs they are trying to hedge. Not every kind of call can
 * be hedged (for example: cluster state changing rpcs).
 *
 * (TODO) Retries and Adaptive hedging policy:
 * ------------------------------------------
 *
 * - No retries are handled at the channel level. Retries can be built in upper layers. However the
 * question is, do we even need retries? Hedging in fact is a substitute for retries.
 *
 * - Clearly hedging puts more load on the service side. To mitigate this, we can make the hedging
 * policy more adaptive. In most happy cases, the rpcs from the first few end points should return
 * right away (especially short lived rpcs, that do not take up much time). In such cases, hedging
 * is not needed. So, the idea is to make this request pattern pluggable so that the requests are
 * hedged only when needed.
 */
class HedgedRpcChannel implements RpcChannel {
  private static final Logger LOG = LoggerFactory.getLogger(HedgedRpcChannel.class);

  private final AbstractRpcClient rpcClient;
  // List of service addresses to hedge the requests to.
  private final List<InetSocketAddress> addrs;
  private final User ticket;
  private final int rpcTimeout;
  // Controls the size of
  private final int fanOutSize;

  private class RunnableRpc implements Runnable {
    private final BatchRpcCtx batchRpcCtx;
    private final Descriptors.MethodDescriptor md;
    private final HBaseRpcController hrc;
    private final Message param;
    private final Message returnType;
    private final User ticket;
    private final InetSocketAddress isa;

    RunnableRpc(BatchRpcCtx batchRpcCtx, Descriptors.MethodDescriptor md, HBaseRpcController hrc,
        Message param, Message returnType, final User ticket, final InetSocketAddress isa) {
      this.batchRpcCtx = batchRpcCtx;
      this.md = md;
      this.hrc = hrc;
      this.param = param;
      this.returnType = returnType;
      this.ticket = ticket;
      this.isa = isa;
    }

    @Override
    public void run() {
      BlockingRpcCallback<Message> done = new BlockingRpcCallback<>();
      rpcClient.callMethod(md, hrc, param, returnType, ticket, isa, done);
      Message val = null;
      try {
        val = done.get();
      } catch (IOException e) {
        LOG.trace("Interrupted hedged RPC call.", e);
      }
      if (hrc.failed()) {
        LOG.debug("Failed request {}.", md.getName(), hrc.getFailed());
        batchRpcCtx.incrementFailedRpcs(hrc.getFailed());
      } else {
        batchRpcCtx.setResultIfNotSet(val);
      }
    }
  }

  /**
   * A shared RPC context between a batch of hedged RPCs. Tracks the state and helpers needed to
   * synchronize on multiple RPCs to different end points fetching the result. All the methods are
   * thread-safe.
   */
  private static class BatchRpcCtx {
    // Result set by the thread finishing first. Set only once.
    private final AtomicReference<Message> result = new AtomicReference<>();
    // Caller waits on this latch being set.
    // We set this to 1, so that the first successful RPC result is returned to the client.
    private CountDownLatch resultsReady = new CountDownLatch(1);
    // Failed rpc book-keeping.
    private AtomicInteger failedRpcCount = new AtomicInteger();

    // List of running RPCs in this batch.
    private final List<Future<?>> pendingRpcFutures =
        Collections.synchronizedList(new ArrayList<>());

    // Target addresses.
    private final List<InetSocketAddress> addresses;
    // Called when the result is ready.
    private final RpcCallback<Message> callBack;
    // Last failed rpc's exception. Used to propagate the reason to the controller.
    private IOException lastFailedRpcReason;

    BatchRpcCtx(List<InetSocketAddress> addresses, RpcCallback<Message> callBack) {
      this.addresses = addresses;
      this.callBack = Preconditions.checkNotNull(callBack);
    }

    /**
     * Sets the result only if it is not already set by another thread. Thread that successfully
     * sets the result also count downs the latch.
     * @param result Result to be set.
     */
    public void setResultIfNotSet(Message result) {
      if (result != null && this.result.compareAndSet(null, result)) {
        resultsReady.countDown();
        // Now that the results are ready, we interrupt the pending callback threads.
        for (Future rpcFuture: pendingRpcFutures) {
          // Call back thread waiting on the rpc result's get() would be interrupted.
          rpcFuture.cancel(true);
        }
      }
    }

    /**
     * Waits until the results are populated and calls the callback if the call is successful.
     * @return true for successful rpc and false otherwise.
     */
    public boolean waitForResults() {
      try {
        // We do not set a timeout on await() because we rely on the underlying RPCs to timeout if
        // something on the remote is broken. Worst case we should wait for rpc time out to kick in.
        resultsReady.await();
      } catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for batched master RPC results. Aborting wait.", e);
      }
      Message message = result.get();
      if (message != null) {
        callBack.run(message);
        return true;
      }
      return false;
    }

    public void addToPendingRpcFutures(Future<?> rpcFuture) {
      pendingRpcFutures.add(rpcFuture);
    }

    public void incrementFailedRpcs(IOException reason) {
      if (failedRpcCount.incrementAndGet() == addresses.size()) {
        lastFailedRpcReason = reason;
        // All the rpcs in this batch have failed. Invoke the waiting threads.
        resultsReady.countDown();
      }
    }

    public IOException getLastFailedRpcReason() {
      return lastFailedRpcReason;
    }

    @Override
    public String toString() {
      return String.format("Batched rpc for target(s) %s", PrettyPrinter.toString(addresses));
    }
  }

  public HedgedRpcChannel(AbstractRpcClient<?> rpcClient, List<InetSocketAddress> addrs,
      User ticket, int rpcTimeout, int fanOutSize) {
    this.rpcClient = rpcClient;
    this.addrs = Preconditions.checkNotNull(addrs);
    Preconditions.checkArgument(this.addrs.size() >= 1);
    // For non-deterministic client query pattern. Not all clients want to hedge RPCs in the same
    // order, creating hot spots on the service end points.
    Collections.shuffle(this.addrs);
    this.ticket = ticket;
    this.rpcTimeout = rpcTimeout;
    this.fanOutSize = fanOutSize;
  }

  private HBaseRpcController applyRpcTimeout(RpcController controller) {
    // There is no reason to use any other implementation of RpcController.
    Preconditions.checkState(controller instanceof HBaseRpcController);
    HBaseRpcController hBaseRpcController = (HBaseRpcController) controller;
    int rpcTimeoutToSet =
        hBaseRpcController.hasCallTimeout() ? hBaseRpcController.getCallTimeout() : rpcTimeout;
    HBaseRpcController response = new HBaseRpcControllerImpl();
    response.setCallTimeout(rpcTimeoutToSet);
    return response;
  }

  public void doCallMethod(Descriptors.MethodDescriptor method, RpcController controller,
      Message request, Message responsePrototype, RpcCallback<Message> done) {
    int i = 0;
    BatchRpcCtx lastBatchCtx = null;
    while (i < addrs.size()) {
      // Each iteration picks fanOutSize addresses to run as batch.
      int batchEnd = Math.min(addrs.size(), i + fanOutSize);
      List<InetSocketAddress> addrSubList = addrs.subList(i, batchEnd);
      BatchRpcCtx batchRpcCtx = new BatchRpcCtx(addrSubList, done);
      lastBatchCtx = batchRpcCtx;
      for (InetSocketAddress address : addrSubList) {
        batchRpcCtx.addToPendingRpcFutures(rpcClient.submitToHedgedRpcPool(new RunnableRpc(
            batchRpcCtx, method, applyRpcTimeout(controller), request, responsePrototype,
            ticket, address)));
      }
      LOG.debug("Attempting request {}, {}", method.getName(), batchRpcCtx);
      if (batchRpcCtx.waitForResults()) {
        return;
      }
      // (TODO): Sleep between batches?
      // Entire batch has failed, lets try the next batch.
      LOG.debug("Failed request {}, {}.", method.getName(), batchRpcCtx);
      i = batchEnd;
    }
    // All the batches failed, mark it a failed rpc.
    // Propagate the failure reason. We propagate the last batch's last failing rpc reason.
    // Can we do something better?
    ((HBaseRpcController) controller).setFailed(lastBatchCtx.getLastFailedRpcReason());
    done.run(null);
  }

  @Override
  public void callMethod(Descriptors.MethodDescriptor method, RpcController controller,
      Message request, Message responsePrototype, RpcCallback<Message> done) {
    // To make the channel non-blocking, we run the actual doCalMethod() async. The call back is
    // called once the hedging finishes.
    CompletableFuture.runAsync(
      () -> doCallMethod(method, controller, request, responsePrototype, done));
  }
}
