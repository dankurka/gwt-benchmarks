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
package com.google.gwt.benchmark.framework.shared;

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;

/**
 * The BenchmarkExecutor executes benchmarks and measures their performance.
 */
public class BenchmarkExecutor {

  private Performance performance;

  public BenchmarkExecutor() {
    performance = PerformanceFactory.create();
  }

  public BenchmarkResult executeWithFixedTime(AbstractBenchmark benchmark) {

    benchmark.setupOneTime();
    // Give the vm a chance to warm up
    runBenchmarkForAtLeast(benchmark, 100, 2);

    // At least 2 seconds and 2 runs
    BenchmarkResult benchmarkResult = runBenchmarkForAtLeast(benchmark, 4000, 10);
    benchmark.tearDownOneTime();

    return benchmarkResult;
  }

  private BenchmarkResult runBenchmarkForAtLeast(AbstractBenchmark benchmark,
      double minimalTime, int minimalRuns) {

    double startMs = performance.now();
    double minimalEnd = startMs + minimalTime;

    int runs = 0;
    JavaScriptArray<Object> array = CollectionFactory.create();
    JavaScriptArrayNumber times = CollectionFactory.createNumber();

    while (runs <= minimalRuns || performance.now() < minimalEnd) {
      benchmark.setup();
      double currentMs = performance.now();
      Object result = benchmark.run();
      array.push(result);
      times.push(performance.now() - currentMs);
      runs++;
      benchmark.tearDown();
    }

    double tookMs = performance.now() - startMs;

    // Keep GWT compiler / JavaScript engine from removing code
    Util.disableOpt(array);

    BenchmarkResult benchmarkResult = BenchmarkResultFactory.create();
    benchmarkResult.setName(benchmark.getName());
    benchmarkResult.setNumberOfRuns(runs);
    benchmarkResult.setTimesForIndividualRunsMs(times);
    benchmarkResult.setTotalTimePassed(tookMs);
    return benchmarkResult;
  }
}
