package com.google.gwt.benchmark.compileserver.server.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    public static Result copyOf(Result other) {
      Result result = new Result();
      result.runsPerSecond = other.runsPerSecond;
      result.state = other.state;
      result.errorMessage = other.errorMessage;
      return result;
    }

    private State state;
    private double runsPerSecond;
    private String errorMessage;

    public enum State {
      NOT_RUN, FAILED_RUN, DONE
    }

    public Result() {
      state = State.NOT_RUN;
    }

    public void setRunsPerSecond(double runsPerSecond) {
      state = State.DONE;
      this.runsPerSecond = runsPerSecond;
    }

    public double getRunsPerSecond() {
      return runsPerSecond;
    }

    public State getState() {
      return state;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  public enum State {
    NOT_RUN, COMPILING, FAILED_COMPILE, FAILED_TO_GENERATE_HOST_PAGE, FAILED_TO_RUN_ON_RUNNER,
    DONE, FAILED_TO_CREATE_DIR,
  }

  public static BenchmarkRun from(BenchmarkRun other) {
    BenchmarkRun clone = new BenchmarkRun(other.moduleName, other.commitId, other.commitDate);
    clone.runners = other.getRunConfigs();
    clone.results = deepClone(other.results);
    clone.state = other.state;
    clone.errorMessage = other.errorMessage;
    return clone;
  }

  private static Map<RunnerConfig, Result> deepClone(Map<RunnerConfig, Result> runMap) {
    Map<RunnerConfig, Result> map = new HashMap<>();
    for (Map.Entry<RunnerConfig, Result> entry : runMap.entrySet()) {
      map.put(entry.getKey(), Result.copyOf(entry.getValue()));
    }
    return map;
  }

  private State state;

  private List<RunnerConfig> runners = new ArrayList<>();

  private Map<RunnerConfig, Result> results = new LinkedHashMap<>();

  private final String moduleName;

  private final String commitId;

  private String errorMessage;

  private final String commitDate;

  public BenchmarkRun(String moduleName, String commitId, String commitDate) {
    this.moduleName = moduleName;
    this.commitId = commitId;
    this.commitDate = commitDate;
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

  public String getCommitDate() {
    return commitDate;
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
  }

  public void setFailedHostPageGenerationFailed(String errorMessage) {
    this.errorMessage = errorMessage;
    state = State.FAILED_TO_GENERATE_HOST_PAGE;
  }

  public void setFailedToCreateDirectory() {
    state = State.FAILED_TO_CREATE_DIR;
  }

  public void setFailedToRunOnServer() {
    state = State.FAILED_TO_RUN_ON_RUNNER;
  }

  public void setRunEnded() {
    state = State.DONE;
  }
}
