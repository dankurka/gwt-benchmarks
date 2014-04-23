/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.compileserver.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Contains all information of the current run being executed on the compile server.
 */
public class BenchmarkOverviewResponseDTO implements Serializable {

  private ArrayList<BenchmarkOverviewEntryDTO> benchmarks;
  private ArrayList<String> runnerNames;
  private boolean executingBenchmarks;
  private boolean hasLatestRun;

  public BenchmarkOverviewResponseDTO() {
  }

  public ArrayList<BenchmarkOverviewEntryDTO> getBenchmarks() {
    return benchmarks;
  }

  public void setBenchmarks(ArrayList<BenchmarkOverviewEntryDTO> benchmarks) {
    this.benchmarks = benchmarks;
  }

  public ArrayList<String> getRunnerNames() {
    return runnerNames;
  }

  public void setRunnerNames(ArrayList<String> runnerNames) {
    this.runnerNames = runnerNames;
  }

  public void setExecutingBenchmarks(boolean executingBenchmarks) {
    this.executingBenchmarks = executingBenchmarks;
  }

  public boolean isExecutingBenchmarks() {
    return executingBenchmarks;
  }

  public void setHasLatestRun(boolean hasLatestRun) {
    this.hasLatestRun = hasLatestRun;
  }

  public boolean isHasLatestRun() {
    return hasLatestRun;
  }
}
