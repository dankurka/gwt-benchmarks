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
 * Benchmark to monitor performance for multiplying longs in GWT.
 */
public class LongMultiplyBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new LongMultiplyBenchmark();
    }
  }

  private long[] array;

  public LongMultiplyBenchmark() {
    super("LongMultiply");
  }

  @Override
  public Object run() {
    long product = 1;
    for (int i = 0; i < array.length; i++) {
      product *= array[i];
    }
    if(product != 7114059635456803793L) {
      throw new RuntimeException();
    }
    return Long.valueOf(product);
  }

  @Override
  public void setupOneTime() {
    array = new long[1000];
    for (int i = 0; i < array.length; i++) {
      // only use odd numbers so we are not overflowing into zero.
      array[i] = 2 * i + 1;
    }
  }
}
