/*
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
package org.apache.hadoop.hbase.master;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.StartMiniClusterOption;
import org.apache.hadoop.hbase.testclassification.MasterTests;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({MediumTests.class, MasterTests.class})
public class TestAlwaysStandByHMaster {

  @ClassRule
  public static final HBaseClassTestRule CLASS_RULE =
      HBaseClassTestRule.forClass(TestAlwaysStandByHMaster.class);
  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();

  @BeforeClass
  public static void setup() throws Exception {
    StartMiniClusterOption option = StartMiniClusterOption.builder().
        numAlwaysStandByMasters(1).numMasters(1).numRegionServers(1).build();
    TEST_UTIL.startMiniCluster(option);
  }

  public static void teardown() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
  }

  /**
   * Tests that the AlwaysStandByHMaster does not transition to active state even if no active
   * master exists.
   */
  @Test  public void testAlwaysStandBy() throws Exception {
    // Make sure there is an active master.
    assertNotNull(TEST_UTIL.getMiniHBaseCluster().getMaster());
    assertEquals(2, TEST_UTIL.getMiniHBaseCluster().getMasterThreads().size());
    // Kill the only active master.
    TEST_UTIL.getMiniHBaseCluster().stopMaster(0).join();
    // Wait for 5s to make sure the always standby doesn't transition to active state.
    assertFalse(TEST_UTIL.getMiniHBaseCluster().waitForActiveAndReadyMaster(5000));
    // Add a new master.
    HMaster newActive = TEST_UTIL.getMiniHBaseCluster().startMaster().getMaster();
    assertTrue(TEST_UTIL.getMiniHBaseCluster().waitForActiveAndReadyMaster(5000));
    // Newly added master should be the active.
    assertEquals(newActive.getServerName(),
        TEST_UTIL.getMiniHBaseCluster().getMaster().getServerName());
  }
}
