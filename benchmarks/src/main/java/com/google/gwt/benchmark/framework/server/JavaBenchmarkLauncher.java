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

import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;
import com.google.gwt.benchmark.framework.shared.BenchmarkExecutor;
import com.google.gwt.benchmark.framework.shared.BenchmarkResult;

import java.lang.reflect.Constructor;

/**
 * Runs a benchmark directly on the JVM.
 */
public class JavaBenchmarkLauncher {

  public static void main(String[] args) {
    // Validate arguments
    if(args.length != 1) {
      System.out.println("Usage: JavaEntryPoint <BenchmarkClass>");
      System.exit(1);
    }
    String benchmarkClassName = args[0];

    // Instantiate Benchmark
    AbstractBenchmark benchmark = null;
    try {
      benchmark = getBenchmark(benchmarkClassName);
    } catch (Exception e) {
      System.out.println(String.format("Can't instantiate class %s", benchmarkClassName));
      e.printStackTrace();
      System.exit(1);
    }

    // Execute Benchmark
    BenchmarkExecutor executor = new BenchmarkExecutor();
    BenchmarkResult benchmarkResult = executor.executeWithFixedTime(benchmark);
    double runsPerSecond =
        (benchmarkResult.getNumberOfRuns() * 1000) / benchmarkResult.getTotalTimePassedMs();
    System.out.println(benchmark.getName() + ": " + runsPerSecond);
  }

  private static AbstractBenchmark getBenchmark(String className) throws Exception {
    Class<?> clazz = Class.forName(className);
    Constructor<?> ctor = clazz.getConstructor();
    AbstractBenchmark abstractBenchmark = (AbstractBenchmark) ctor.newInstance();
    return abstractBenchmark;
  }
}
