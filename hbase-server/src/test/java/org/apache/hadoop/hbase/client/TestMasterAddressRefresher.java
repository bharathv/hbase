package org.apache.hadoop.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.Waiter;
import org.apache.hadoop.hbase.util.EnvironmentEdgeManager;
import org.junit.Assert;
import org.junit.Test;
import org.apache.hbase.thirdparty.com.google.common.util.concurrent.Uninterruptibles;

public class TestMasterAddressRefresher {

  private class DummyMasterRegistry extends MasterRegistry {

    private final AtomicInteger getMastersCallCounter = new AtomicInteger(0);
    private final List<Long> callTimeStamps = new ArrayList<>();

    DummyMasterRegistry(Configuration conf) throws IOException {
      super(conf);
    }

    @Override
    public CompletableFuture<List<ServerName>> getMasters() {
      getMastersCallCounter.incrementAndGet();
      callTimeStamps.add(EnvironmentEdgeManager.currentTime());
      return CompletableFuture.completedFuture(new ArrayList<>());
    }

    public int getMastersCount() {
      return getMastersCallCounter.get();
    }

    public List<Long> getCallTimeStamps() {
      return callTimeStamps;
    }
  }

  @Test
  public void testPeriodicMasterEndPointRefresh() throws IOException {
    Configuration conf = HBaseConfiguration.create();
    // Refresh every 1 second.
    conf.setLong(MasterAddressRefresher.PERIODIC_REFRESH_INTERVAL_SECS, 1);
    conf.setLong(MasterAddressRefresher.MIN_SECS_BETWEEN_REFRESHES, 0);
    try (DummyMasterRegistry registry = new DummyMasterRegistry(conf)) {
      // Wait for > 3 seconds to see that at least 3 getMasters() RPCs have been made.
      Waiter.waitFor(
          conf, 5000, (Waiter.Predicate<Exception>) () -> registry.getMastersCount() > 3);
    }
  }

  @Test
  public void testDurationBetweenRefreshes() throws IOException {
    Configuration conf = HBaseConfiguration.create();
    // Disable periodic refresh
    conf.setLong(MasterAddressRefresher.PERIODIC_REFRESH_INTERVAL_SECS, Integer.MAX_VALUE);
    // A minimum duration of 1s between refreshes
    conf.setLong(MasterAddressRefresher.MIN_SECS_BETWEEN_REFRESHES, 1);
    try (DummyMasterRegistry registry = new DummyMasterRegistry(conf)) {
      // Issue a ton of manual refreshes.
      for (int i = 0; i < 10000; i++) {
        registry.masterAddressRefresher.refreshNow();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
      // Overall wait time is 10000 ms, so the number of requests should be <=10
      List<Long> callTimeStamps = registry.getCallTimeStamps();
      // Actual calls to getMasters() should be much lower than the refresh count.
      Assert.assertTrue(String.valueOf(registry.getMastersCount()), registry.getMastersCount() <= 20);
      Assert.assertTrue(callTimeStamps.size() > 0);
      // Verify that the delta between subsequent RPCs is at least 1sec as configured.
      for (int i = 1; i < callTimeStamps.size() - 1; i++) {
        long delta = callTimeStamps.get(i) - callTimeStamps.get(i - 1);
        // Few ms cushion to account for any env jitter.
        Assert.assertTrue(callTimeStamps.toString(), delta > 990);
      }
    }

  }
}
