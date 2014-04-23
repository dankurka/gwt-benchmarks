/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.compileserver.server.runners.settings;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Util {
  public static InetAddress getFirstNonLoopbackAddress() throws Exception {
    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
    while (en.hasMoreElements()) {
      NetworkInterface i = en.nextElement();
      for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
        InetAddress addr = en2.nextElement();
        if (!addr.isLoopbackAddress()) {
          if (addr instanceof Inet4Address) {
            return addr;
          }
        }
      }
    }
    throw new Exception("could not find any interface");
  }
}
