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
package com.google.gwt.benchmark.framework.client;

/**
 * Injector provides a method that a benchmark's entry point can use to set up a JavaScript
 * benchmark.
 * <p>
 * Note: Injection is done using eval because in D8 we do not have a head element to add script
 * tags to (GWT ScriptInjector works that way). This class is intended to be used with original
 * Benchmarks that have been ported from JS (Octane). It allows us to keep the original file as is
 * and simply inject it into the benchmarking system.
 */
public class Injector {

  /**
   * Maps a JavaScript value stored in a local variable to a global variable.
   */
  public static class GlobalMapping {
    private String globalName;
    private String localName;

    public GlobalMapping(String globalName, String localName) {
      this.globalName = globalName;
      this.localName = localName;
    }

    public String getGlobalName() {
      return globalName;
    }

    public String getLocalName() {
      return localName;
    }
  }

  /**
   * Inject JavaScript into a a private scope.
   *
   * @param js the main JavaScript to inject
   * @param mappings An array of mappings to publish from the scope
   */
  public void injectJavaScript(String js, GlobalMapping... mappings) {
    StringBuilder builder = new StringBuilder();
    builder.append("(function() {\n");
    builder.append(js);
    for (int i = 0; i < mappings.length; i++) {
      builder.append(
          "$wnd." + mappings[i].getGlobalName() + " = " + mappings[i].getLocalName() + ";\n");
    }
    builder.append("})();");

    eval(builder.toString());
  }

  private native void eval(String js) /*-{
    eval(js);
  }-*/;
}
