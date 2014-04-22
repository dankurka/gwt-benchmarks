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

import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;

/**
 * A benchmark result contains all information for an executed benchmark.
 */
public interface BenchmarkResult {
  /**
   * Returns the name of the benchmark.
   */
  String getName();

  /**
   * Returns the number of times the benchmark was run.
   */
  int getNumberOfRuns();

  /**
   * Returns the {@link JavaScriptArrayNumber} containing times of individual runs.
   */
  JavaScriptArrayNumber getTimesForIndividualRunsMs();

  /**
   * Returns the time it took to execute the benchmark in milliseconds
   */
  double getTotalTimePassedMs();

  /**
   * Sets the name of the benchmark.
   */
  void setName(String name);

  /**
   * Sets how many times a benchmark was run.
   */
  void setNumberOfRuns(int runs);

  /**
   * Sets the {@link JavaScriptArrayNumber} that contains all times for individual runs.
   */
  void setTimesForIndividualRunsMs(JavaScriptArrayNumber numbers);

  /**
   * Sets how much time it took to execute the benchmark in milliseconds.
   */
  void setTotalTimePassed(double time);
}
