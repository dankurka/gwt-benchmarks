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
package com.google.gwt.benchmark.benchmarks.octane.client.richards.js;

import com.google.gwt.benchmark.benchmarks.octane.client.OctaneJS;
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.client.Injector;
import com.google.gwt.benchmark.framework.client.Injector.GlobalMapping;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class RichardsJSBenchmark extends AbstractBenchmark {

  /**
   * EntryPoint or RichardJS benchmark.
   */
  public static class EntryPoint extends AbstractBenchmarkEntryPoint {

    @Override
    protected AbstractBenchmark getBenchmark() {
      return new RichardsJSBenchmark();
    }
  }

  interface Resource extends ClientBundle {
    @Source("richards.js")
    TextResource richardsJavaScript();
  }

  public RichardsJSBenchmark() {
    super("RichardsBenchmarkJS");
  }

  static {
    Resource r = GWT.create(Resource.class);
    new Injector().injectJavaScript(OctaneJS.PREAMBLE + r.richardsJavaScript().getText(),
        new GlobalMapping("__octane__richards_js_run", "runRichards"));
  }

  @Override
  public native Object run() /*-{
    return $wnd.__octane__richards_js_run();
  }-*/;
}
