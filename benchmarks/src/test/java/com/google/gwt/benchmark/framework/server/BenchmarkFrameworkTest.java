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
package com.google.gwt.benchmark.framework.server;

import com.google.gwt.benchmark.framework.shared.BenchmarkExecutor;
import com.google.gwt.benchmark.framework.shared.BenchmarkResult;
import com.google.gwt.benchmark.framework.shared.MockBenchmark;

import org.junit.Assert;
import org.junit.Test;

/**
 * IntegrationTest for the Benchmark framework running in Java.
 */
public class BenchmarkFrameworkTest {

  @Test
  public void test() {
    MockBenchmark benchmark = new MockBenchmark();
    BenchmarkExecutor executor = new BenchmarkExecutor();
    BenchmarkResult benchmarkResult = executor.executeWithFixedTime(benchmark);
    Assert.assertEquals(4000, benchmarkResult.getTotalTimePassedMs(), 100);
    Assert.assertEquals(80, benchmarkResult.getNumberOfRuns(), 2);
    Assert.assertTrue(benchmark.isTearDownOneTimeCalled());
  }
}
