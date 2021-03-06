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
package com.google.gwt.benchmark.benchmarks.java.lang.shared;

import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

/**
 * Benchmark to monitor performance for adding longs in GWT.
 */
public class LongAddBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new LongAddBenchmark();
    }
  }

  private long[] array;

  public LongAddBenchmark() {
    super("LongAdd");
  }

  @Override
  public Object run() {
    long sum = 0;
    for (int i = 0; i < array.length; i++) {
      sum += array[i];
    }

    if (sum != 49995000L) {
      throw new RuntimeException();
    }

    return Long.valueOf(sum);
  }

  @Override
  public void setupOneTime() {
    array = new long[10000];
    for (int i = 0; i < array.length; i++) {
      array[i] = i;
    }
  }
}
