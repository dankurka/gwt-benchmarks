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
 * Benchmark for copying operation of a {@link HashMap}.
 */
public class HashMapCloneManualObjectKeysBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapCloneManualObjectKeysBenchmark();
    }
  }

  private int length;

  private HashMap<Object, Object> map;

  public HashMapCloneManualObjectKeysBenchmark() {
    super("ManualCloneObjectHashMap");
  }

  @Override
  public Object run() {
    Map<Object, Object> clone = new HashMap<Object, Object>();
    for( Entry<Object, Object> entries : map.entrySet())
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

    map = new HashMap<Object, Object>();

    for (int i = 0; i < length; i++) {
      Integer key = i;
      Integer value =  i + 1;
      map.put(key, value);
    }
  }
}