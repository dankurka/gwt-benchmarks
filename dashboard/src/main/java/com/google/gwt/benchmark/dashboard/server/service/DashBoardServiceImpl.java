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
package com.google.gwt.benchmark.dashboard.server.service;

import com.google.gwt.benchmark.common.shared.service.ServiceException;
import com.google.gwt.benchmark.dashboard.server.controller.BenchmarkController;
import com.google.gwt.benchmark.dashboard.server.controller.ControllerException;
import com.google.gwt.benchmark.dashboard.shared.service.DashboardService;
import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation for GWT service.
 */
@Singleton
public class DashBoardServiceImpl extends RemoteServiceServlet implements DashboardService {

  private static final Logger logger = Logger.getLogger(DashBoardServiceImpl.class.getName());

  private BenchmarkController controller;

  @Inject
  public DashBoardServiceImpl(BenchmarkController controller) {
    this.controller = controller;
  }

  @Override
  public ArrayList<String> getLatestBenchmarkNames() throws ServiceException {
    try {
      return new ArrayList<>(controller.getLatestBenchmarkNames());
    } catch (ControllerException e) {
      logger.log(Level.WARNING, "Can not load modules", e);
      throw new ServiceException("Can not load modules");
    }
  }

  @Override
  public BenchmarkResultsTable getLatestGraphs(String benchmarkName) throws ServiceException {
    Calendar cal = Calendar.getInstance();
    int week = cal.get(Calendar.WEEK_OF_YEAR);
    int year = cal.get(Calendar.YEAR);
    return controller.getGraphs(benchmarkName, week, year);
  }

  @Override
  public BenchmarkResultsTable getGraphs(String benchmarkName, int week, int year)
      throws ServiceException {
      return controller.getGraphs(benchmarkName, week, year);
  }
}
