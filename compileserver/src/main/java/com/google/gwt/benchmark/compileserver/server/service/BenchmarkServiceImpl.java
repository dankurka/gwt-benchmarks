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
package com.google.gwt.benchmark.compileserver.server.service;

import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkManager;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkRun;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkRun.Result;
import com.google.gwt.benchmark.compileserver.server.manager.RunnerConfig;
import com.google.gwt.benchmark.compileserver.shared.Service;
import com.google.gwt.benchmark.compileserver.shared.ServiceException;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewEntryDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewEntryDTO.BenchmarState;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewResponseDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkRunDTO;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Implementation for GWT service BenchmarkService.
 */
public class BenchmarkServiceImpl extends RemoteServiceServlet implements Service {

  private static BenchmarkRunDTO.State convertStatus(BenchmarkRun.Result.State state) {
    switch (state) {
      case NOT_RUN:
        return BenchmarkRunDTO.State.NOT_RUN;
      case DONE:
        return BenchmarkRunDTO.State.DONE;
      default:
        return BenchmarkRunDTO.State.FAILED_RUN;
    }
  }

  private static BenchmarkOverviewEntryDTO.BenchmarState convertStatus(BenchmarkRun.State state) {
    switch (state) {
      case COMPILING:
      case NOT_RUN:
        return BenchmarState.RUNNING;
      case DONE:
        return BenchmarState.DONE;
      case FAILED_TO_RUN_ON_RUNNER:
        return BenchmarState.AT_LEAST_ONE_FAILED;
      default:
        return BenchmarState.FATAL_ERROR;
    }
  }

  private static ArrayList<BenchmarkOverviewEntryDTO> createBenchmarkOverviewEntryDTOs(
      Map<String, BenchmarkRun> latestRun) {
    ArrayList<BenchmarkOverviewEntryDTO> list = new ArrayList<BenchmarkOverviewEntryDTO>();
    for (Entry<String, BenchmarkRun> mapEntry : latestRun.entrySet()) {
      BenchmarkRun serverBenchmarkRun = mapEntry.getValue();

      BenchmarkOverviewEntryDTO entry = new BenchmarkOverviewEntryDTO();
      entry.setStatus(convertStatus(serverBenchmarkRun.getState()));
      entry.setBenchmarkName(mapEntry.getKey());
      if (serverBenchmarkRun.isFailed()) {
        entry.setErrorMessage(serverBenchmarkRun.getErrorMessage());
      }

      ArrayList<BenchmarkRunDTO> benchmarkRuns = new ArrayList<BenchmarkRunDTO>();
      for (Entry<RunnerConfig, Result> runEntries : serverBenchmarkRun.getResults().entrySet()) {
        Result result = runEntries.getValue();
        BenchmarkRunDTO benchmarkRun = new BenchmarkRunDTO();
        benchmarkRun.setState(convertStatus(result.getState()));
        benchmarkRun.setErrorMessage(result.getErrorMessage());
        if (result.getState() == Result.State.DONE) {
          benchmarkRun.setRunsPerMinute(result.getRunsPerSecond());
        }
        benchmarkRuns.add(benchmarkRun);
      }
      entry.setBenchmarkRuns(benchmarkRuns);
      list.add(entry);
    }
    return list;
  }

  private static ArrayList<String> createRunnerDTOs(List<RunnerConfig> allRunners) {
    ArrayList<String> runnerNames = new ArrayList<>();
    for (RunnerConfig rc : allRunners) {
      runnerNames.add(rc.getOS() + " " + rc.getBrowser());
    }
    return runnerNames;
  }

  private static final Logger logger = Logger.getLogger(BenchmarkServiceImpl.class.getName());

  private BenchmarkManager benchmarkManager;

  @Inject
  public BenchmarkServiceImpl(BenchmarkManager benchmarkManager) {
    this.benchmarkManager = benchmarkManager;
  }

  @Override
  public BenchmarkOverviewResponseDTO loadBenchmarkOverview() throws ServiceException {
    try {
      BenchmarkOverviewResponseDTO response = new BenchmarkOverviewResponseDTO();
      response.setRunnerNames(createRunnerDTOs(benchmarkManager.getAllRunners()));

      Map<String, BenchmarkRun> latestRun = benchmarkManager.getLatestRun();
      response.setExecutingBenchmarks(benchmarkManager.isRunning());

      if (latestRun == null || latestRun.isEmpty()) {
        response.setHasLatestRun(false);
        // early exit since we do not have any current benchmark to copy
        return response;
      }

      response.setHasLatestRun(true);
      response.setBenchmarks(createBenchmarkOverviewEntryDTOs(latestRun));
      return response;

    } catch (Exception e) {
      logger.log(Level.WARNING, "Error while executing service call", e);
      if (e instanceof ServiceException) {
        throw (ServiceException) e;
      }
      throw new ServiceException("Error while executing your request");
    }
  }

  @Override
  public void startServer() {
    benchmarkManager.start();
  }

  @Override
  public void stopServer() {
    benchmarkManager.stop();
  }
}
