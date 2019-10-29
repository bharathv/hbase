/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Optional;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.RegionLocations;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.Waiter;
import org.apache.hadoop.hbase.Waiter.ExplainingPredicate;
import org.apache.hadoop.hbase.master.MetaRegionLocationCache;

final class RegionReplicaTestHelper {

  private RegionReplicaTestHelper() {
  }

  // waits for all replicas to have region location
  static void waitUntilAllMetaReplicasHavingRegionLocation(Configuration conf,
      AsyncRegistry registry, int regionReplication) throws IOException {
    Waiter.waitFor(conf, conf.getLong("hbase.client.sync.wait.timeout.msec", 60000), 200, true,
      new ExplainingPredicate<IOException>() {
        @Override
        public String explainFailure() throws IOException {
          return "Not all meta replicas get assigned";
        }

        @Override
        public boolean evaluate() throws IOException {
          try {
            RegionLocations locs = registry.getMetaRegionLocation().get();
            if (locs.size() < regionReplication) {
              return false;
            }
            for (int i = 0; i < regionReplication; i++) {
              if (locs.getRegionLocation(i) == null) {
                return false;
              }
            }
            return true;
          } catch (Exception e) {
            TestZKAsyncRegistry.LOG.warn("Failed to get meta region locations", e);
            return false;
          }
        }
      });
  }

  static Optional<ServerName> getRSCarryingReplica(HBaseTestingUtility util, TableName tableName,
      int replicaId) {
    return util.getHBaseCluster().getRegionServerThreads().stream().map(t -> t.getRegionServer())
      .filter(rs -> rs.getRegions(tableName).stream()
        .anyMatch(r -> r.getRegionInfo().getReplicaId() == replicaId))
      .findAny().map(rs -> rs.getServerName());
  }

  /**
   * Return the new location.
   */
  static ServerName moveRegion(HBaseTestingUtility util, HRegionLocation currentLoc)
      throws Exception {
    ServerName serverName = currentLoc.getServerName();
    RegionInfo regionInfo = currentLoc.getRegion();
    TableName tableName = regionInfo.getTable();
    int replicaId = regionInfo.getReplicaId();
    ServerName newServerName = util.getHBaseCluster().getRegionServerThreads().stream()
      .map(t -> t.getRegionServer().getServerName()).filter(sn -> !sn.equals(serverName)).findAny()
      .get();
    util.getAdmin().move(regionInfo.getEncodedNameAsBytes(), newServerName);
    if (regionInfo.isMetaRegion()) {
      // Invalidate the meta cache forcefully to avoid test races. Otherwise there might be a
      // delay in master receiving the change event and the cache could be stale in that window.
      MetaRegionLocationCache metaCache =
          util.getMiniHBaseCluster().getMaster().getMetaRegionLocationCache();
      metaCache.invalidateMetaReplica(replicaId);
    }
    util.waitFor(30000, new ExplainingPredicate<Exception>() {

      @Override
      public boolean evaluate() throws Exception {
        Optional<ServerName> newServerName = getRSCarryingReplica(util, tableName, replicaId);
        return newServerName.isPresent() && !newServerName.get().equals(serverName);
      }

      @Override
      public String explainFailure() throws Exception {
        return regionInfo.getRegionNameAsString() + " is still on " + serverName;
      }
    });
    return newServerName;
  }

  interface Locator {
    RegionLocations getRegionLocations(TableName tableName, int replicaId, boolean reload)
        throws Exception;

    void updateCachedLocationOnError(HRegionLocation loc, Throwable error) throws Exception;
  }

  static void testLocator(HBaseTestingUtility util, TableName tableName, Locator locator)
      throws Exception {
    RegionLocations locs =
      locator.getRegionLocations(tableName, RegionReplicaUtil.DEFAULT_REPLICA_ID, false);
    assertEquals(3, locs.size());
    for (int i = 0; i < 3; i++) {
      HRegionLocation loc = locs.getRegionLocation(i);
      assertNotNull(loc);
      ServerName serverName = getRSCarryingReplica(util, tableName, i).get();
      assertEquals(serverName, loc.getServerName());
    }
    ServerName newServerName = moveRegion(util, locs.getDefaultRegionLocation());
    // The cached location should not be changed
    assertEquals(locs.getDefaultRegionLocation().getServerName(),
      locator.getRegionLocations(tableName, RegionReplicaUtil.DEFAULT_REPLICA_ID, false)
        .getDefaultRegionLocation().getServerName());
    // should get the new location when reload = true
    assertEquals(newServerName,
      locator.getRegionLocations(tableName, RegionReplicaUtil.DEFAULT_REPLICA_ID, true)
        .getDefaultRegionLocation().getServerName());
    // the cached location should be replaced
    assertEquals(newServerName,
      locator.getRegionLocations(tableName, RegionReplicaUtil.DEFAULT_REPLICA_ID, false)
        .getDefaultRegionLocation().getServerName());

    ServerName newServerName1 = moveRegion(util, locs.getRegionLocation(1));
    ServerName newServerName2 = moveRegion(util, locs.getRegionLocation(2));

    // The cached location should not be change
    assertEquals(locs.getRegionLocation(1).getServerName(),
      locator.getRegionLocations(tableName, 1, false).getRegionLocation(1).getServerName());
    // clear the cached location for replica 1
    locator.updateCachedLocationOnError(locs.getRegionLocation(1), new NotServingRegionException());
    // the cached location for replica 2 should not be changed
    assertEquals(locs.getRegionLocation(2).getServerName(),
      locator.getRegionLocations(tableName, 2, false).getRegionLocation(2).getServerName());
    // should get the new location as we have cleared the old location
    assertEquals(newServerName1,
      locator.getRegionLocations(tableName, 1, false).getRegionLocation(1).getServerName());
    // as we will get the new location for replica 2 at once, we should also get the new location
    // for replica 2
    assertEquals(newServerName2,
      locator.getRegionLocations(tableName, 2, false).getRegionLocation(2).getServerName());
  }
}
