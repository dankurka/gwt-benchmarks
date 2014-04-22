/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.framework.shared;

import com.google.gwt.core.client.GWT;

public class Util {

  /**
   * Placing results of a benchmark on a global static field should effectly disable any bad
   * optimizations since the field could be read by another class that might get loaded later.
   */
  public static Object disableOptField;

  public static void disableOpt(Object o) {
    if(GWT.isScript()) {
      disableOptGwt(o);
    } else {
      disableOptField = o;
    }
  }

  private static native void disableOptGwt(Object o) /*-{
    if(!$wnd.__private_disableOpt) {
      $wnd.__private_disableOpt = function(){};
    }
    $wnd.__private_disableOpt(o);
  }-*/;
}
