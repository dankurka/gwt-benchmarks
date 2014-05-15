package com.google.gwt.benchmark.dashboard.shared.service;

import com.google.gwt.benchmark.common.shared.service.ServiceException;
import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

/**
 * Service interface implemented server side.
 */
@RemoteServiceRelativePath("data/service")
public interface DashboardService extends RemoteService {
  ArrayList<String> getLatestBenchmarkNames() throws ServiceException;

  BenchmarkResultsTable getLatestGraphs(String benchmarkName) throws ServiceException;

  BenchmarkResultsTable getGraphs(String benchmarkName, int week, int year)
      throws ServiceException;

}
