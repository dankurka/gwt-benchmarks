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
package com.google.gwt.benchmark.benchmarks.java.lang.shared;

import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

/**
 * Benchmark to monitor performance for adding longs in GWT.
 *
 * This benchmark in particular only uses numbers that can not be represented in a double anymore
 * (bigger than 54 bits).
 */
public class LongAddBigNumbersBenchmark extends AbstractBenchmark {

  private static final long BITS_55 = 1L << 55;

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new LongAddBigNumbersBenchmark();
    }
  }

  private long[] array;

  public LongAddBigNumbersBenchmark() {
    super("LongAddBigNumbersBenchmark");
  }

  @Override
  public Object run() {
    long sum = 0;
    for (int i = 0; i < array.length; i++) {
      sum += array[i];
    }

    if (sum != -8646911284501347320L) {
      throw new RuntimeException("Sum should be -8646911284501347320, but is: " + sum);
    }

    return Long.valueOf(sum);
  }

  @Override
  public void setupOneTime() {
    array = new long[10000];
    for (int i = 0; i < array.length; i++) {
      array[i] = (i + 1) + BITS_55;
    }
  }
}
