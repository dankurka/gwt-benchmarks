package com.google.gwt.benchmark.dashboard.shared.service;

import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

/**
 * Service interface to be injected into every object that needs data from the server.
 */
public interface DashboardServiceAsync {
  void getLatestBenchmarkNames(AsyncCallback<ArrayList<String>> callback);

  void getLatestGraphs(String module, AsyncCallback<BenchmarkResultsTable> callback);

  void getGraphs(String module, int week, int year,
      AsyncCallback<BenchmarkResultsTable> callback);
}
