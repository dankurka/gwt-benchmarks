/*
 * Copyright 2015 Google Inc.
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
package com.google.j2cl.benchmark.common.runner;

/**
 * Represents one result of a {@link Job}. There are results for each browser / configuration
 * being run.
 */
public class JobResult {
  private String errorMessage;
  private boolean succeded;
  private double result;
  private boolean ran;
  private final RunnerConfig runnerConfig;

  public JobResult(RunnerConfig runnerConfig) {
    this.runnerConfig = runnerConfig;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setResult(double result) {
    this.result = result;
  }

  public double getResult() {
    return result;
  }

  public boolean isSucceded() {
    return succeded;
  }

  public void setSucceded(boolean succeded) {
    this.succeded = succeded;
  }

  public void setRan(boolean ran) {
    this.ran = ran;
  }

  public boolean isRan() {
    return ran;
  }

  @Override
  public JobResult clone() {
    JobResult jobResult = new JobResult(runnerConfig);
    jobResult.errorMessage = errorMessage;
    jobResult.ran = ran;
    jobResult.result = result;
    jobResult.succeded = succeded;
    return jobResult;
  }

  @Override
  public String toString() {
    return "JobResult [errorMessage=" + errorMessage + ", succeded=" + succeded + ", result="
        + result + ", ran=" + ran + "]";
  }

  public RunnerConfig getConfig() {
    return runnerConfig;
  }
}
