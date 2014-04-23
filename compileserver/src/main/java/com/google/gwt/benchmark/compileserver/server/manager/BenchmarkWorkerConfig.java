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
package com.google.gwt.benchmark.compileserver.server.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BenchmarkWorkerConfig contains the moduleName for a worker to compile and the configurations for
 * runners that the worker should launch.
 */
public class BenchmarkWorkerConfig {
  public static BenchmarkWorkerConfig from(BenchmarkRun run) {
    return new BenchmarkWorkerConfig(run.getModuleName(), run.getRunConfigs());
  }

  private final String moduleName;
  private final List<RunnerConfig> runnerConfigs;

  public BenchmarkWorkerConfig(String moduleName, List<RunnerConfig> runners) {
    this.moduleName = moduleName;
    this.runnerConfigs = new ArrayList<>(runners);
  }

  public String getModuleName() {
    return moduleName;
  }

  public List<RunnerConfig> getRunners() {
    return Collections.unmodifiableList(runnerConfigs);
  }
}
