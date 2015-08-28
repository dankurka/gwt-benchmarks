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
 * Benchmark to monitor performance for multiplying longs in GWT.
 *
 * This benchmarks will only incur values that do not need more than 31 bits to be
 * represented and thus will potentially fit in a simple JavaScript double.
 */
public class LongDivisionBigNumbersBenchmark extends AbstractBenchmark {

  private static final long BITS_63 = 1L << 63;
  private static final long BITS_55 = 1L << 54;

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new LongDivisionBigNumbersBenchmark();
    }
  }

  private long[] array;
  private long[] result;

  public LongDivisionBigNumbersBenchmark() {
    super("LongDivisionBigNumbersBenchmark");
  }

  @Override
  public Object run() {
    for (int i = 0; i < array.length; i++) {
      result[i] = BITS_63 / array[i];
    }
    if(result[0] != -9223372036854775808L) {
      throw new RuntimeException("Expected value -9223372036854775808L but was: " + result[0]);
    }

    if(result[result.length - 1] != 20L) {
      throw new RuntimeException("Expected value 20L but was: " + result[result.length - 1]);
    }
    return result;
  }

  @Override
  public void setupOneTime() {
    array = new long[1000];
    result = new long[1000];
    for (int i = 0; i < array.length; i++) {
      array[i] = BITS_55 * i + 1;
    }
  }
}
