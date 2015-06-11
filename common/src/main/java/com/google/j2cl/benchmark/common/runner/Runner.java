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
package com.google.j2cl.benchmark.common.runner;

/**
 * A Runner executes a compiled benchmark and makes the result available.
 */
public interface Runner extends Runnable {

  /**
   * Factory for creating runners.
   */
  public interface Factory {
    /**
     * Create a new runner with a certain config and a url to invoke.
     */
    Runner create(RunnerConfig runnerConfig, String url);
  }

  /**
   * Get the configuration of this runner.
   *
   * @return the configuration of the runner.
   */
  RunnerConfig getConfig();

  /**
   * Get the error message describing the error that happened while running the benchmark.
   *
   * @return the error message if {@link #isFailed()} returns true, otherwise null
   */
  String getErrorMessage();

  /**
   * The result of the run (runs per second)
   * <p>
   * Note: this is only available if {@link #isDone()} returns true.
   *
   * @return the result of the run
   */
  double getResult();

  /**
   * Is the runner done with execution of the benchmark.
   *
   * @return true if runner is done with the execution of the benchmark
   */
  boolean isDone();

  /**
   * Is the run failed
   *
   * @return true if the run failed
   */
  boolean isFailed();
}
