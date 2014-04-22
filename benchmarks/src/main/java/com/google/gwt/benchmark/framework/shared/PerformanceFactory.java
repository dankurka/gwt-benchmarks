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
package com.google.gwt.benchmark.framework.shared;

import com.google.gwt.core.client.GWT;

/**
 * Factory to create {@link Performance}.
 * <p>
 * When running on a JVM an implementation is used based on {@link System#currentTimeMillis()},
 * when running in JavaScript a feature check for window.performance.now is made and might
 * get polyfilled using {@link System#currentTimeMillis()}.
 */
public final class PerformanceFactory {

  private static class PerformanceDefaultImpl implements Performance {
    public double now() {
      return System.currentTimeMillis();
    }
  }

  private static class PerformanceWindowPerformanceImpl implements Performance {
    public native double now() /*-{
      return $wnd.performance.now();
    }-*/;
  }

  public static Performance create() {
    if (GWT.isScript() && hasPerformance()) {
      return new PerformanceWindowPerformanceImpl();
    } else {
      return new PerformanceDefaultImpl();
    }
  }

  private static native boolean hasPerformance() /*-{
    return !!$wnd.performance && !!$wnd.performance.now;
  }-*/;

  private PerformanceFactory() {}
}
