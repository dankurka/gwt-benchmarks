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
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Benchmark for iteration on a {@link HashMap} with String keys.
 */
public class HashMapStringIterationBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapStringIterationBenchmark();
    }
  }

  private int length;

  private HashMap<String, String> map;

  public HashMapStringIterationBenchmark() {
    super("StringHashMapIterationBenchmark");
  }

  @Override
  public Object run() {
    JavaScriptArray<String> array = CollectionFactory.create(length * 2);

    int count = 0;
    for (Entry<String, String> entry : map.entrySet()) {
      array.set(count++, entry.getKey());
      array.set(count++, entry.getValue());
    }

    if(array.length() != length * 2) {
      throw new RuntimeException();
    }

    return array;
  }

  @Override
  public void setupOneTime() {
    length = 1000;

    map = new HashMap<String, String>();

    for (int i = 0; i < length; i++) {
      String key = "thisissomekey" + i;
      String value = "thisissomevalue" + i;
      map.put(key, value);
    }
  }
}
