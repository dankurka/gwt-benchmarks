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
package com.google.gwt.benchmark.benchmarks.octane.client.deltablue.js;

import com.google.gwt.benchmark.benchmarks.octane.client.OctaneJS;
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.client.Injector;
import com.google.gwt.benchmark.framework.client.Injector.GlobalMapping;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class DeltaBlueJSBenchmark extends AbstractBenchmark {
  static {
    Resource r = GWT.create(Resource.class);
    new Injector().injectJavaScript(OctaneJS.PREAMBLE +  r.deltaBlueJavaScript().getText(),
        new GlobalMapping("__octane__deltablue_js_run", "deltaBlue"));
  }

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {

    @Override
    protected AbstractBenchmark getBenchmark() {
      return new DeltaBlueJSBenchmark();
    }
  }

  interface Resource extends ClientBundle {
    @Source("deltablue.js")
    TextResource deltaBlueJavaScript();
  }

  public DeltaBlueJSBenchmark() {
    super("Octane_deltablue_js");
  }

  @Override
  public native Object run() /*-{
    return $wnd.__octane__deltablue_js_run();
  }-*/;
}
