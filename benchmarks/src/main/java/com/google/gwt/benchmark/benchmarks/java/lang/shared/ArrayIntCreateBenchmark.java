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
 * Benchmark for integer array creation performance
 */
public class ArrayIntCreateBenchmark extends AbstractBenchmark {

  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new ArrayIntCreateBenchmark();
    }
  }

  private Object[] array;

  public ArrayIntCreateBenchmark() {
    super("ArrayIntCreateBenchmark");
  }

  @Override
  public Object run() {
    // Note: we are reusing the same array every time, since every run allocates
    // about 8MB of memory. Not reusing would mean running out of memory very fast
    // on faster JavaScript VMs
    for (int i = 0; i < array.length; i++) {
      array[i] = new int[1000];
    }
    return array;
  }

  @Override
  public void setupOneTime() {
    array = new Object[1000];
  }
}
