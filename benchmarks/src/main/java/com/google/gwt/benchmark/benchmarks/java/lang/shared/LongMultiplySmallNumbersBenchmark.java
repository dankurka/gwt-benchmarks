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
public class LongMultiplySmallNumbersBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new LongMultiplySmallNumbersBenchmark();
    }
  }

  private long[] array;
  private long[] result;

  public LongMultiplySmallNumbersBenchmark() {
    super("LongMultiplySmallNumbersBenchmark");
  }

  @Override
  public Object run() {
    for (int i = 0; i < array.length - 1; i++) {
      result[i] = array[i] * array[i + 1];
    }
    if(result[0] != 2L) {
      throw new RuntimeException("Expected value 2lL but was: " + result[0]);
    }

    if(result[result.length - 1] != 999000L) {
      throw new RuntimeException("Expected value 999000L but was: " + result[result.length - 1]);
    }
    return result;
  }

  @Override
  public void setupOneTime() {
    array = new long[1000];
    result = new long[999];
    for (int i = 0; i < array.length; i++) {
      array[i] = i + 1;
    }
  }
}
