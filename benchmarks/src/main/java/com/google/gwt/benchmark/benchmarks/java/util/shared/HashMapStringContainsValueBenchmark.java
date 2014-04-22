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

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayBoolean;
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

import java.util.HashMap;

/**
 * Benchmark for {@link HashMap} contains with String values.
 */
public class HashMapStringContainsValueBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapStringContainsValueBenchmark();
    }
  }

  private JavaScriptArray<String> valuesInMap;

  private JavaScriptArray<String> valuesNotInMap;

  private int length;

  private HashMap<String, String> map;

  public HashMapStringContainsValueBenchmark() {
    super("StringContainsValueHashMapBenchmark");
  }

  @Override
  public Object run() {
    JavaScriptArrayBoolean arrayInMap = CollectionFactory.createBoolean(length);
    JavaScriptArrayBoolean arrayNotInMap = CollectionFactory.createBoolean(length);

    for (int i = 0; i < length; i++) {
      arrayInMap.set(i, map.containsValue(valuesInMap.get(i)));
      arrayNotInMap.set(i, map.containsValue(valuesNotInMap.get(i)));
    }

    for (int i = 0; i < length; i++) {
      if (!arrayInMap.get(i)) {
        throw new RuntimeException("Value was not in map at index " + i);
      }
      if (arrayNotInMap.get(i)) {
        throw new RuntimeException("Value was in map at index " + i);
      }
    }

    return new Object[] {arrayInMap, arrayNotInMap};
  }

  @Override
  public void setupOneTime() {
    length = 1000;
    valuesInMap = CollectionFactory.create();
    valuesNotInMap = CollectionFactory.create();

    map = new HashMap<String, String>();

    for (int i = 0; i < length; i++) {
      String key = "thisissomekey" + i;
      String value = "thisissomevalue" + i;

      valuesInMap.push(value);
      valuesNotInMap.push(key);
      map.put(key, value);
    }
  }
}
