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
package com.google.gwt.benchmark.dashboard.server.controller;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gwt.benchmark.common.shared.json.BenchmarkResultJson;
import com.google.gwt.benchmark.common.shared.json.BenchmarkRunJson;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkGraph;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkResult;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkRun;
import com.google.gwt.benchmark.dashboard.server.guice.DashboardServletGuiceModule;
import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This controller is responsible for adding and retrieving benchmark data.
 */
public class BenchmarkController {

  private static final Logger logger = Logger.getLogger(BenchmarkController.class.getName());

  private static class ToPersist {

    private final long commitTimeMsEpoch;
    private final BenchmarkRun benchmarkRun;
    private final List<BenchmarkResult> benchmarkResults;

    public ToPersist(BenchmarkRun benchmarkRun, List<BenchmarkResult> benchmarkResults,
        long commitTimeMsEpoch) {
      this.benchmarkRun = benchmarkRun;
      this.benchmarkResults = Collections.unmodifiableList(benchmarkResults);
      this.commitTimeMsEpoch = commitTimeMsEpoch;
    }
  }

  private static class WeekSpan {
    private final int commitWeek;
    private final int commitYear;
    private final long weekStartMsEpoch;
    private final long weekEndMsEpoch;

    public WeekSpan(int commitWeek, int commitYear, long weekStartMsEpoch, long weekEndMsEpoch) {
      this.commitWeek = commitWeek;
      this.commitYear = commitYear;
      this.weekStartMsEpoch = weekStartMsEpoch;
      this.weekEndMsEpoch = weekEndMsEpoch;
    }
  }

  private static class BenchmarkGraphData {
    private final List<String> commitIds;
    private final List<Double> runsPerSecond;

    public BenchmarkGraphData(List<String> commitIds, List<Double> runsPerSecond) {
      this.commitIds = Collections.unmodifiableList(commitIds);
      this.runsPerSecond = Collections.unmodifiableList(runsPerSecond);
    }
  }

  /**
   * Returns the name of each benchmark included in the most
   * recent benchmark run.
   */
  public List<String> getLatestBenchmarkNames() throws ControllerException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Query query = BenchmarkResult.createQueryLastest();

    PreparedQuery prepare = ds.prepare(query);
    List<Entity> list = prepare.asList(FetchOptions.Builder.withLimit(1));

    if (list.size() == 0) {
      throw new ControllerException("No entries in datastore");
    }

    BenchmarkRun run = new BenchmarkRun(list.get(0));
    List<Key> results = run.getResults();
    Map<Key, Entity> map = ds.get(results);
    Set<String> modules = new HashSet<>();

    for (Entry<Key, Entity> entry : map.entrySet()) {
      BenchmarkResult result = new BenchmarkResult(entry.getValue());
      modules.add(result.getBenchmarkName());
    }

    ArrayList<String> resultList = new ArrayList<>(modules);
    Collections.sort(resultList);
    return resultList;
  }

  public BenchmarkResultsTable getGraphs(String benchmarkName, int week, int year) {
    Query query = BenchmarkGraph.createQuery(benchmarkName, week, year);
    List<BenchmarkGraph> graphs = executeQuery(query);
    return createResponse(benchmarkName, graphs, week, year);
  }

  public void addBenchmarkResult(BenchmarkRunJson benchmarkRunJSON) throws ControllerException {
    ToPersist toPersist = createDomainObjects(benchmarkRunJSON);
    persistBenchmarkRun(toPersist, 3);
    addUpdateRequestToTaskQueue(toPersist);
  }

  public void updateGraph(long commitTimeMsEpoch, String benchmarkName, String runnerId)
      throws ControllerException {

    WeekSpan weekSpan = createWeekSpan(commitTimeMsEpoch);

    BenchmarkGraphData benchmarkGraphData = calculateNewGraphData(benchmarkName, runnerId,
        weekSpan.weekStartMsEpoch, weekSpan.weekEndMsEpoch);

    putBenchmarkGraph(benchmarkName, runnerId, weekSpan.commitWeek, weekSpan.commitYear,
        benchmarkGraphData.commitIds, benchmarkGraphData.runsPerSecond);
  }

  private BenchmarkResultsTable createResponse(String benchmarkName,
      List<BenchmarkGraph> graphs, int week, int year) {
    Collections.sort(graphs, new Comparator<BenchmarkGraph>() {

      @Override
      public int compare(BenchmarkGraph o1, BenchmarkGraph o2) {
        return o1.getRunnerId().compareTo(o2.getRunnerId());
      }
    });
    String weekName = String.format("Week: %d Year: %d", week, year);
    List<String> allRunnerIds = new ArrayList<String>();

    List<String> commitIds = new ArrayList<String>();
    List<double[]> runnerResultList = new ArrayList<>();

    boolean first = true;

    for (BenchmarkGraph benchmarkGraph : graphs) {
      if (first) {
        commitIds.addAll(benchmarkGraph.getCommitIds());
        first = false;
      } else {
        // if an update is in progress some graph entities might have been updated
        // and some not. In this case we are just going to return an empty response
        if (commitIds.size() != benchmarkGraph.getCommitIds().size()) {
          return BenchmarkResultsTable.create(benchmarkName, weekName, year, week,
              Collections.<String>emptyList(),Collections.<String>emptyList(),
              Collections.<double[]>emptyList());
        }
      }

      allRunnerIds.add(benchmarkGraph.getRunnerId());
      Double[] runsPerSecond = benchmarkGraph.getRunsPerSecond().toArray(new Double[] {});
      double[] runs = ArrayUtils.toPrimitive(runsPerSecond);
      runnerResultList.add(runs);
    }

    return BenchmarkResultsTable.create(benchmarkName, weekName, year, week, commitIds,
        allRunnerIds, runnerResultList);
  }

  private List<BenchmarkGraph> executeQuery(Query query) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepare = ds.prepare(query);

    List<Entity> entityList = prepare.asList(FetchOptions.Builder.withDefaults());

    List<BenchmarkGraph> list = new ArrayList<>();
    for (Entity entity : entityList) {
      list.add(new BenchmarkGraph(entity));
    }
    return list;
  }



  private BenchmarkGraphData calculateNewGraphData(String benchmarkName, String runnerId,
      long weekStartMsEpoch, long weekEndMsEpoch) {

    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(BenchmarkRun.NAME).addSort("commitTimeMsEpoch", SortDirection.ASCENDING);
    Filter startFilter = new Query.FilterPredicate("commitTimeMsEpoch",
        FilterOperator.GREATER_THAN_OR_EQUAL, weekStartMsEpoch);
    Filter endFilter =
        new Query.FilterPredicate("commitTimeMsEpoch", FilterOperator.LESS_THAN, weekEndMsEpoch);

    Filter compositeFilter = Query.CompositeFilterOperator.and(startFilter, endFilter);
    query.setFilter(compositeFilter);
    PreparedQuery prepare = dataStore.prepare(query);

    List<Entity> entityList = prepare.asList(FetchOptions.Builder.withDefaults());

    ArrayList<Key> keys = new ArrayList<Key>(entityList.size());
    List<String> commitIds = new ArrayList<>(entityList.size());
    for (Entity entity : entityList) {
      keys.add(BenchmarkResult.createKey(entity.getKey(), benchmarkName, runnerId));
      commitIds.add(new BenchmarkRun(entity).getCommitId());
    }

    Map<Key, Entity> brMap = dataStore.get(keys);
    List<BenchmarkResult> results = new ArrayList<>(brMap.size());

    for (Key key : keys) {
      results.add(new BenchmarkResult(brMap.get(key)));
    }

    List<Double> runsPerSecond = new ArrayList<>(entityList.size());
    for (BenchmarkResult benchmarkResult : results) {
      runsPerSecond.add(benchmarkResult.getRunsPerMinute());
    }

    return new BenchmarkGraphData(commitIds, runsPerSecond);
  }

  private void putBenchmarkGraph(String benchmarkName, String runnerId, int week, int year,
      List<String> commitIds, List<Double> runsPerSecond) throws ControllerException {
    BenchmarkGraph graph = new BenchmarkGraph(benchmarkName, runnerId, week, year);
    graph.setRunsPerSecond(runsPerSecond);
    graph.setCommitIds(commitIds);
    try {
      DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
      dataStore.put(graph.getEntity());
    } catch (DatastoreFailureException | ConcurrentModificationException e) {
      throw new ControllerException("Can not persist BenchmarkGraph", e);
    }
  }

  private WeekSpan createWeekSpan(long commitTimeMsEpoch) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(commitTimeMsEpoch));
    int week = cal.get(Calendar.WEEK_OF_YEAR);
    int year = cal.get(Calendar.YEAR);

    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(Calendar.WEEK_OF_YEAR, week);
    calendar.set(Calendar.YEAR, year);
    long startSearch = calendar.getTimeInMillis();

    calendar.add(Calendar.DAY_OF_YEAR, 7);
    long endSearch = calendar.getTimeInMillis();

    return new WeekSpan(week, year, startSearch, endSearch);
  }

  private void addUpdateRequestToTaskQueue(ToPersist toPersist) {
    Queue queue = QueueFactory.getQueue("graph-queue");
    for (BenchmarkResult benchmarkResult : toPersist.benchmarkResults) {
      TaskOptions taskOptions = TaskOptions.Builder.withUrl(
          DashboardServletGuiceModule.GRAPH_QUEUE_URL).param("commitTimeMsEpoch",
          String.valueOf(toPersist.commitTimeMsEpoch)).param("benchmarkName",
          benchmarkResult.getBenchmarkName()).param("runnerId", benchmarkResult.getRunnerId());
      queue.add(taskOptions);
    }
  }

  private ToPersist createDomainObjects(BenchmarkRunJson benchmarkRunJSON) {
    String commitId = benchmarkRunJSON.getCommitId();
    long commitTimeMsEpoch = Math.round(benchmarkRunJSON.getCommitTimeMsEpoch());
    BenchmarkRun benchmarkRun = new BenchmarkRun(commitId, commitTimeMsEpoch);
    List<BenchmarkResult> brToPersist = new ArrayList<>();

    HashSet<String> runnerIds = new HashSet<>();
    Map<String, List<BenchmarkResultJson>> results = benchmarkRunJSON.getResultByBenchmarkName();

    for (Entry<String, List<BenchmarkResultJson>> entry : results.entrySet()) {

      String moduleName = entry.getKey();
      List<BenchmarkResultJson> listResults = entry.getValue();
      for (BenchmarkResultJson benchmarkResultJSON : listResults) {
        runnerIds.add(benchmarkResultJSON.getRunnerId());
        BenchmarkResult benchmarkResult = new BenchmarkResult(benchmarkRun.getKey(), moduleName,
            benchmarkResultJSON.getRunnerId());
        benchmarkResult.setRunsPerSecond(benchmarkResultJSON.getRunsPerSecond());
        brToPersist.add(benchmarkResult);
      }
    }

    benchmarkRun.setRunnerIds(new ArrayList<>(runnerIds));

    return new ToPersist(benchmarkRun, brToPersist, commitTimeMsEpoch);
  }

  private void persistBenchmarkRun(ToPersist toPersist, int retryCount) throws ControllerException {
    for(int i = 0; i < retryCount; i++) {
        logger.info(String.format("persistBenchmarkRun try %d", (i+1)));
        if (persistBenchmarkRun(toPersist)) {
          logger.info(String.format("persistBenchmarkRun try %d succeded", (i+1)));
          return;
        }
        logger.info(String.format("persistBenchmarkRun try %d failed", (i+1)));
    }

    logger.warning((String.format("persistBenchmarkRun gave up after %d retries", retryCount)));
    throw new ControllerException(
        (String.format("persistBenchmarkRun gave up after %d retries", retryCount)));
  }

  private boolean persistBenchmarkRun(ToPersist toPersist) {
    // If a client will try to persist the same benchmark run twice, we will simply
    // update the existing entry in the datastore, effectively meaning last entry wins.
    // There is an uncovered edge case: If a benchmark run is reuploaded with fewer executed
    // benchmarks these entries will be left in the datastore.

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Transaction transaction = null;

    try {
      transaction = datastore.beginTransaction();
      List<Entity> entities = new ArrayList<>();
      for (BenchmarkResult br : toPersist.benchmarkResults) {
        entities.add(br.getEntity());
      }
      List<Key> list = datastore.put(entities);
      BenchmarkRun benchmarkRun = toPersist.benchmarkRun;
      benchmarkRun.setResults(list);
      datastore.put(benchmarkRun.getEntity());
      transaction.commit();
      return true;
    } catch (DatastoreTimeoutException | DatastoreFailureException
        | ConcurrentModificationException e) {
      logger.log(Level.WARNING, "Can not persist benchmark results", e);
      return false;
    } finally {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
    }
  }
}
