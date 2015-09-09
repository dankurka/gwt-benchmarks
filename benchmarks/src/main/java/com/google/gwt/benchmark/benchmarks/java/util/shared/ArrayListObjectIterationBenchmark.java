/*
 * Copyright 2015 Google Inc.
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

import java.util.ArrayList;

/**
 * Benchmark ArrayList iteration performance.
 */
public class ArrayListObjectIterationBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new ArrayListObjectIterationBenchmark();
    }
  }

  private ArrayList<Object> arrayList;

  public ArrayListObjectIterationBenchmark() {
    super("ArrayListObjectIterationBenchmark");
  }

  @Override
  public Object run() {
    int sum = 0;

    for (Object object : arrayList) {
      if (object != null) {
        sum++;
      }
    }

    // compare value - disables opts and finds JIT bugs
    if (sum != 1000) {
      throw new RuntimeException();
    }

    return Integer.valueOf(sum);
  }

  @Override
  public void setupOneTime() {
    arrayList = new ArrayList<Object>();
    for (int i = 0; i < 1000; i++) {
      arrayList.add(":" + i);
    }
  }
}
