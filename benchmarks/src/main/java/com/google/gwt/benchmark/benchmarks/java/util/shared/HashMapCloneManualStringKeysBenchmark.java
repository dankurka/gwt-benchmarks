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
package com.google.gwt.benchmark.benchmarks.java.util.shared;

import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Benchmark for {@link HashMap} clone performance.
 */
public class HashMapCloneManualStringKeysBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapCloneManualStringKeysBenchmark();
    }
  }

  private int length;

  private HashMap<String, String> map;

  public HashMapCloneManualStringKeysBenchmark() {
    super("ManualCloneStringHashMap");
  }

  @Override
  public Object run() {
    Map<String, String> clone = new HashMap<String,String>();
    for( Entry<String, String> entries : map.entrySet())
    {
      clone.put(entries.getKey(), entries.getValue());
    }

    if(clone.size() != length) {
      throw new RuntimeException();
    }

    return clone;
  }

  @Override
  public void setupOneTime() {
    length = 1000;

    map = new HashMap<String,String>();

    for (int i = 0; i < length; i++) {
      String key = "thisissomekey" + i;
      String value = "thisissomevalue" + i;
      map.put(key, value);
    }
  }
}