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
package com.google.gwt.benchmark.framework.shared.ui;

import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;
import com.google.gwt.benchmark.framework.shared.BenchmarkResult;

/**
 * Java implementation of {@link BenchmarkResult}.
 */
public class BenchmarkResultJavaImpl implements BenchmarkResult {

  private int numberOfRuns;
  private double totalTimePassedMs;
  private JavaScriptArrayNumber timesForIndividualRunsMs;
  private String name;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getNumberOfRuns() {
    return numberOfRuns;
  }

  @Override
  public JavaScriptArrayNumber getTimesForIndividualRunsMs() {
    return timesForIndividualRunsMs;
  }

  @Override
  public double getTotalTimePassedMs() {
    return totalTimePassedMs;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setNumberOfRuns(int numberOfRuns) {
    this.numberOfRuns = numberOfRuns;
  }

  @Override
  public void setTimesForIndividualRunsMs(JavaScriptArrayNumber timesForIndividualRunsMs) {
    this.timesForIndividualRunsMs = timesForIndividualRunsMs;
  }

  @Override
  public void setTotalTimePassed(double time) {
    this.totalTimePassedMs = time;
  }
}
