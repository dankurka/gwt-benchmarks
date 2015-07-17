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

import com.google.gwt.benchmark.benchmarks.java.util.shared.helper.Key;
import com.google.gwt.benchmark.benchmarks.java.util.shared.helper.Value;
import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

import java.util.HashMap;

/**
 * Benchmark for object insertion (not String) into {@link HashMap}.
 */
public class HashMapObjectPutBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new HashMapObjectPutBenchmark();
    }
  }

  private JavaScriptArray<Key> keys;
  private int length;
  private JavaScriptArray<Value> values;

  public HashMapObjectPutBenchmark() {
    super("HashMapObjectPutBenchmark");
  }

  @Override
  public Object run() {
    HashMap<Key, Value> map = new HashMap<Key,Value>();

    for (int i = 0; i < keys.length(); i++) {
      map.put(keys.get(i), values.get(i));
    }

    if(map.size() != length) {
      throw new RuntimeException();
    }

    return map;
  }

  @Override
  public void setupOneTime() {
    length = 1000;
    keys = CollectionFactory.create();
    values = CollectionFactory.create();

    for (int i = 0; i < length; i++) {
      keys.push(new Key());
      values.push(new Value());
    }
  }
}
