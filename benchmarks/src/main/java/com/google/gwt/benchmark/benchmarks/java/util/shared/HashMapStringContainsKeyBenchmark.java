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
 * Benchmark for {@link HashMap} get performance for String keys.
 */
public class HashMapStringContainsKeyBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapStringContainsKeyBenchmark();
    }
  }

  private JavaScriptArray<String> keysInMap;

  private JavaScriptArray<String> keysNotInMap;

  private int length;

  private HashMap<String, String> map;

  public HashMapStringContainsKeyBenchmark() {
    super("StringContainsHashMapBenchmark");
  }

  @Override
  public Object run() {
    JavaScriptArrayBoolean inMap = CollectionFactory.createBoolean(length);
    JavaScriptArrayBoolean notInMap = CollectionFactory.createBoolean(length);

    for (int i = 0; i < length; i++) {
      inMap.set(i, map.containsKey(keysInMap.get(i)));
      notInMap.set(i, map.containsKey(keysNotInMap.get(i)));
    }
    for (int i = 0; i < length; i++) {
      if (!inMap.get(i)) {
        throw new RuntimeException("Value was not in map at index " + i);
      }
      if (notInMap.get(i)) {
        throw new RuntimeException("Value was in map at index " + i);
      }
    }

    return new Object[] {inMap, notInMap};

  }

  @Override
  public void setupOneTime() {
    length = 1000;
    keysInMap = CollectionFactory.create();
    keysNotInMap = CollectionFactory.create();

    map = new HashMap<String, String>();

    for (int i = 0; i < length; i++) {
      String key = "thisissomekey" + i;
      String value = "thisissomevalue" + i;

      keysInMap.push(key);
      keysNotInMap.push(value);
      map.put(key, value);
    }
  }
}
