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
package com.google.gwt.benchmark.framework.client;

import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;
import com.google.gwt.benchmark.framework.shared.BenchmarkExecutor;
import com.google.gwt.benchmark.framework.shared.BenchmarkResult;
import com.google.gwt.benchmark.framework.shared.Performance;
import com.google.gwt.benchmark.framework.shared.PerformanceFactory;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for all Benchmark entry points.
 */
public abstract class AbstractBenchmarkEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    // Make sure we depend on common collection classes so our benchmarks are more representative of
    // real world apps.
    dependOnAllCollections();

    try {
      maybePatchPerformanceNow();
      BenchmarkExecutor executor = new BenchmarkExecutor();
      BenchmarkResult benchmarkResult =
          executor.executeWithFixedTime(getBenchmark());
      double runsPerSecond =
          (benchmarkResult.getNumberOfRuns() * 1000) / benchmarkResult.getTotalTimePassedMs();
      update(runsPerSecond);
      if (hasDom()) {
        display("Result: " + runsPerSecond + " runs/second (Total runs: "
            + benchmarkResult.getNumberOfRuns() + ", time: "
            + benchmarkResult.getTotalTimePassedMs() + "ms)");
      }
    } catch (Exception e) {
      setFailed();
      if (hasDom()) {
        display("Failed, see console!");
      }
      GWT.log("Benchmark failed", e);
    }
  }

  protected abstract AbstractBenchmark getBenchmark();

  private native boolean hasWindowPerformanceNow() /*-{
    return !!$wnd.performance && !!$wnd.performance.now;
  }-*/;

  private void maybePatchPerformanceNow() {
    if(!hasWindowPerformanceNow()) {
      polyfill(PerformanceFactory.create());
    }
  }

  private native void polyfill(Performance p) /*-{
    var now = function() {
      return p.@com.google.gwt.benchmark.framework.shared.Performance::now()();
    };
    $wnd.performance = {};
    $wnd.performance.now = now;
    // If we are runninn with a linker that loads our code in an iframe
    // patch the local window object as well.
    window.performance = {};
    window.performance.now = now;
  }-*/;

  private native void update(double result) /*-{
    $wnd.__gwt__benchmarking__failed = false;
    $wnd.__gwt__benchmarking__result = result;
    $wnd.__gwt__benchmarking__ran = true;
  }-*/;

  private native void setFailed() /*-{
    $wnd.__gwt__benchmarking__failed = true;
    $wnd.__gwt__benchmarking__ran = true;
  }-*/;

  /**
   * Returns whether the current environment the code is executed in has a DOM.
   */
  private native boolean hasDom() /*-{
    return !!($doc.createElement && $doc.createElement('div'))
  }-*/;

  private native void display(String s) /*-{
    // This is intentionally not using GWT's DOM API since we do not want the
    // framework to drag in extra dependencies.
    var div = $doc.createElement('div');
    div.textContent = s;
    $doc.body.appendChild(div);
  }-*/;

  private static void dependOnAllCollections() {
    utilizeMap(new HashMap<String, String>());
    utilizeMap(new LinkedHashMap<String, String>());
    utilizeList(new ArrayList<String>());
    utilizeList(new LinkedList<String>());
    utilizeList(new ArrayList<String>() {
      int size = 0;

      @Override
      public boolean add(String item) {
        size++;
        return super.add(item);
      }

      @Override
      public String get(int index) {
        return super.get(index);
      }

      @Override
      public int size() {
        return size;
      }
    });
    utilizeList(new LinkedList<String>() {
      int size = 0;

      @Override
      public boolean add(String item) {
        size++;
        return super.add(item);
      }

      @Override
      public String get(int index) {
        return super.get(index);
      }

      @Override
      public int size() {
        return size;
      }
    });
  }

  private static void utilizeMap(Map<String, String> map) {
    if (addGet(map) && iterate(map) && remove(map)) {
      return;
    }
    throw new AssertionError();
  }

  private static boolean addGet(Map<String, String> map) {
    map.put("input", "output");
    return "output".equals(map.get("input")) && map.size() == 1;
  }

  private static boolean iterate(Map<String, String> map) {
    String result = "";
    for (Entry<String, String> entry : map.entrySet()) {
      result += entry.getKey() + entry.getValue();
    }
    return result.length() > 0;
  }

  private static boolean remove(Map<String, String> map) {
    return "output".equals(map.remove("input"));
  }

  private static void utilizeList(List<String> list) {
    if (addGet(list) && iterate(list) && remove(list)) {
      return;
    }
    throw new AssertionError();
  }

  private static boolean addGet(List<String> list) {
    list.add("input");
    return "input".equals(list.get(0)) && list.size() == 1;
  }

  private static boolean remove(List<String> list) {
    return "input".equals(list.remove(0));
  }

  private static boolean iterate(List<String> list) {
    String result = "";
    for (String entry : list) {
      result += entry;
    }
    return result.length() > 0;
  }
}
