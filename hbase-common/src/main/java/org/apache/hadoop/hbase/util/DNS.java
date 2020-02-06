/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.util;

import java.lang.reflect.Method;
import java.net.UnknownHostException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.yetus.audience.InterfaceAudience;

/**
 * Wrapper around Hadoop's DNS class to hide reflection.
 */
@InterfaceAudience.Private
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="REC_CATCH_EXCEPTION",
  justification="If exception, presume HAS_NEW_DNS_GET_DEFAULT_HOST_API false")
public final class DNS {
  private static boolean HAS_NEW_DNS_GET_DEFAULT_HOST_API;
  private static Method GET_DEFAULT_HOST_METHOD;

  static {
    try {
      GET_DEFAULT_HOST_METHOD = org.apache.hadoop.net.DNS.class
          .getMethod("getDefaultHost", String.class, String.class, boolean.class);
      HAS_NEW_DNS_GET_DEFAULT_HOST_API = true;
    } catch (Exception e) {
      HAS_NEW_DNS_GET_DEFAULT_HOST_API = false; // FindBugs: Causes REC_CATCH_EXCEPTION. Suppressed
    }
  }

  private DNS() {}

  /**
   * Wrapper around DNS.getDefaultHost(String, String), calling
   * DNS.getDefaultHost(String, String, boolean) when available.
   *
   * @param strInterface The network interface to query.
   * @param nameserver The DNS host name.
   * @return The default host names associated with IPs bound to the network interface.
   */
  public static String getDefaultHost(String strInterface, String nameserver)
      throws UnknownHostException {
    if (HAS_NEW_DNS_GET_DEFAULT_HOST_API) {
      try {
        // Hadoop-2.8 includes a String, String, boolean variant of getDefaultHost
        // which properly handles multi-homed systems with Kerberos.
        return (String) GET_DEFAULT_HOST_METHOD.invoke(null, strInterface, nameserver, true);
      } catch (Exception e) {
        // If we can't invoke the method as it should exist, throw an exception
        throw new RuntimeException("Failed to invoke DNS.getDefaultHost via reflection", e);
      }
    } else {
      return org.apache.hadoop.net.DNS.getDefaultHost(strInterface, nameserver);
    }
  }

  /**
   * Get the configured hostname for master/regionserver. Gets the default hostname if not specified
   * in the configuration.
   * @param conf Configuration to look up.
   * @param isMaster True if master hostname needs to be looked up and false for regionserver.
   */
  public static String getHostname(Configuration conf, boolean isMaster)
      throws UnknownHostException {
    String hostname = conf.get(isMaster? HConstants.MASTER_HOSTNAME_KEY :
        HConstants.RS_HOSTNAME_KEY);
    if (hostname == null || hostname.isEmpty()) {
      String masterOrRS = isMaster ? "master" : "regionserver";
      return Strings.domainNamePointerToHostName(getDefaultHost(
          conf.get("hbase." + masterOrRS + ".dns.interface", "default"),
          conf.get("hbase." + masterOrRS + ".dns.nameserver", "default")));
    } else {
      return hostname;
    }
  }
}
