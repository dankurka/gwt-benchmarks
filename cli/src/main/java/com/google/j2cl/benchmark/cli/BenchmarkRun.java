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
package com.google.j2cl.benchmark.cli;

import com.google.j2cl.benchmark.common.runner.RunnerConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BenchmarkRun contains all relevant information for one benchmark being run.
 *
 * <p>
 * It has a state to keep track of execution progress. Since one benchmark module will be run by
 * multiple Runners it also keeps track of each individual result.
 */
public class BenchmarkRun {

  /**
   * BenchmarkResult contains the result for one module being run on one exact runner.
   */
  public static class Result {

    private State state;
    private double runsPerSecond;
    private String errorMessage;

    public enum State {
      NOT_RUN, FAILED_RUN, SUCCESSFUL_RUN
    }

    public Result() {
      state = State.NOT_RUN;
    }

    public void setRunsPerSecond(double runsPerSecond) {
      state = State.SUCCESSFUL_RUN;
      this.runsPerSecond = runsPerSecond;
    }

    public double getRunsPerSecond() {
      return runsPerSecond;
    }

    public State getState() {
      return state;
    }

    public void setErrorMessage(String errorMessage) {
      state = State.FAILED_RUN;
      this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  public enum State {
    NOT_RUN, FAILED_COMPILE, FAILED_TO_GENERATE_HOST_PAGE, FAILED_TO_RUN_ON_RUNNER,
    DONE, FAILED_TO_CREATE_DIR, FAILED_TO_ZIP_BENCHMARK,
  }

  private State state;

  private List<RunnerConfig> runners = new ArrayList<>();

  private Map<RunnerConfig, Result> results = new LinkedHashMap<>();

  private final String moduleName;

  private final String commitId;

  private String errorMessage;

  private final long commitMsEpoch;

  private final String reportingName;

  public BenchmarkRun(String moduleName, String commitId, long commitMsEpoch, String reportingName) {
    this.moduleName = moduleName;
    this.commitId = commitId;
    this.commitMsEpoch = commitMsEpoch;
    this.reportingName = reportingName;
    state = State.NOT_RUN;
  }

  public void addRunner(RunnerConfig config) {
    runners.add(config);
    results.put(config, new Result());
  }

  public void addResult(RunnerConfig config, double runsPerSecond) {
    if (!runners.contains(config)) {
      throw new IllegalStateException();
    }
    if (!results.containsKey(config)) {
      throw new IllegalStateException();
    }

    Result result = results.get(config);
    result.setRunsPerSecond(runsPerSecond);
  }

  public long getCommitMsEpoch() {
    return commitMsEpoch;
  }

  public String getCommitId() {
    return commitId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getModuleName() {
    return moduleName;
  }

  public Map<RunnerConfig, Result> getResults() {
    return results;
  }

  public List<RunnerConfig> getRunConfigs() {
    return Collections.unmodifiableList(runners);
  }

  public State getState() {
    return state;
  }

  public boolean isFailed() {
    return !(state == State.DONE || state == State.NOT_RUN);
  }

  public void setFailedCompile(String message) {
    this.errorMessage = message;
    state = State.FAILED_COMPILE;
    setResultsToFailed();
  }

  public void setFailedHostPageGenerationFailed(String errorMessage) {
    this.errorMessage = errorMessage;
    state = State.FAILED_TO_GENERATE_HOST_PAGE;
    setResultsToFailed();
  }

  public void setFailedToCreateDirectory() {
    state = State.FAILED_TO_CREATE_DIR;
    setResultsToFailed();
  }

  public void setFailedToRunOnServer() {
    state = State.FAILED_TO_RUN_ON_RUNNER;
  }

  public void setRunEnded() {
    state = State.DONE;
  }

  private void setResultsToFailed() {
    for (Result r : results.values()) {
      r.state = Result.State.FAILED_RUN;
    }
  }

  public String getReportingName() {
    return reportingName;
  }
}
