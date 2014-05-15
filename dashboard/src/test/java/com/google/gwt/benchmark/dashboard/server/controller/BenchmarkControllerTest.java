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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.gwt.benchmark.common.shared.json.BenchmarkRunJson;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkGraph;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkResult;
import com.google.gwt.benchmark.dashboard.server.domain.BenchmarkRun;
import com.google.gwt.benchmark.dashboard.server.servlets.AddBenchmarkResultServletTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link BenchmarkController}.
 */
public class BenchmarkControllerTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalDatastoreServiceTestConfig(), new LocalTaskQueueTestConfig().setQueueXmlPath(
          System.getProperty("user.dir") + "/src/test/resources/queue.xml"));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testControllerPersists() throws ControllerException, EntityNotFoundException {

    BenchmarkRunJson benchmarkRunJSON = AddBenchmarkResultServletTest.buildBenchmarkRunJSON();

    BenchmarkController controller = new BenchmarkController();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    Assert.assertEquals(0, ds.prepare(new Query(BenchmarkRun.NAME)).countEntities(withLimit(10)));
    Assert.assertEquals(0, ds.prepare(new Query(BenchmarkGraph.NAME)).countEntities(withLimit(10)));
    Assert.assertEquals(0,
        ds.prepare(new Query(BenchmarkResult.NAME)).countEntities(withLimit(10)));
    controller.addBenchmarkResult(benchmarkRunJSON);

    Assert.assertEquals(1, ds.prepare(new Query(BenchmarkRun.NAME)).countEntities(withLimit(10)));

    String commitId = benchmarkRunJSON.getCommitId();

    long runtime = Math.round(benchmarkRunJSON.getCommitTimeMsEpoch());

    Key benchmarkRunKey = BenchmarkRun.createKey(commitId);
    Key module1FireFox = BenchmarkResult.createKey(benchmarkRunKey, "module1", "firefox_linux");
    Key module1Chrome = BenchmarkResult.createKey(benchmarkRunKey, "module1", "chrome_linux");
    Key module2FireFox = BenchmarkResult.createKey(benchmarkRunKey, "module2", "firefox_linux");
    Key module2Chrome = BenchmarkResult.createKey(benchmarkRunKey, "module2", "chrome_linux");

    Entity entity = ds.get(benchmarkRunKey);

    BenchmarkRun benchmarkRun = new BenchmarkRun(entity);

    Assert.assertEquals(commitId, benchmarkRun.getCommitId());
    Assert.assertEquals(runtime, benchmarkRun.getCommitTimeMsEpoch());

    Assert.assertEquals(2, benchmarkRun.getRunnerIds().size());
    Assert.assertTrue(benchmarkRun.getRunnerIds().contains("firefox_linux"));
    Assert.assertTrue(benchmarkRun.getRunnerIds().contains("chrome_linux"));

    Assert.assertEquals(4, benchmarkRun.getResults().size());
    Assert.assertTrue(benchmarkRun.getResults().contains(module1FireFox));
    Assert.assertTrue(benchmarkRun.getResults().contains(module1Chrome));
    Assert.assertTrue(benchmarkRun.getResults().contains(module2FireFox));
    Assert.assertTrue(benchmarkRun.getResults().contains(module2Chrome));

    entity = ds.get(module1FireFox);
    BenchmarkResult benchmarkResult = new BenchmarkResult(entity);
    Assert.assertEquals("module1", benchmarkResult.getBenchmarkName());
    Assert.assertEquals("firefox_linux", benchmarkResult.getRunnerId());
    Assert.assertEquals(3, benchmarkResult.getRunsPerMinute(), 0.0001);

    entity = ds.get(module1Chrome);
    benchmarkResult = new BenchmarkResult(entity);
    Assert.assertEquals("module1", benchmarkResult.getBenchmarkName());
    Assert.assertEquals("chrome_linux", benchmarkResult.getRunnerId());
    Assert.assertEquals(4, benchmarkResult.getRunsPerMinute(), 0.0001);

    entity = ds.get(module2FireFox);
    benchmarkResult = new BenchmarkResult(entity);
    Assert.assertEquals("module2", benchmarkResult.getBenchmarkName());
    Assert.assertEquals("firefox_linux", benchmarkResult.getRunnerId());
    Assert.assertEquals(3, benchmarkResult.getRunsPerMinute(), 0.0001);

    entity = ds.get(module2Chrome);
    benchmarkResult = new BenchmarkResult(entity);
    Assert.assertEquals("module2", benchmarkResult.getBenchmarkName());
    Assert.assertEquals("chrome_linux", benchmarkResult.getRunnerId());
    Assert.assertEquals(4, benchmarkResult.getRunsPerMinute(), 0.0001);

    LocalTaskQueue taskQueue = LocalTaskQueueTestConfig.getLocalTaskQueue();

    Map<String, QueueStateInfo> queueStateInfo = taskQueue.getQueueStateInfo();
    QueueStateInfo info = queueStateInfo.get("graph-queue");
    Assert.assertEquals(4, info.getCountTasks());
  }

  @Test
  public void testGraphUpdate() throws ControllerException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    long commitTime_20th_march_2014 = 1400610555950L;
    long oneWeekInMs = 604800000L;

    List<String> runnerIds = Arrays.asList("linux_ff", "linux_chrome");

    // put in one entity for the current week
    BenchmarkRun benchmarkRun = new BenchmarkRun("commitId0", commitTime_20th_march_2014);
    benchmarkRun.setRunnerIds(runnerIds);
    BenchmarkResult benchmarkResult1 =
        new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_ff");
    benchmarkResult1.setRunsPerMinute(100);
    ds.put(benchmarkResult1.getEntity());
    BenchmarkResult benchmarkResult2 =
        new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_chrome");
    benchmarkResult2.setRunsPerMinute(200);
    ds.put(benchmarkResult2.getEntity());
    BenchmarkResult benchmarkResult3 =
        new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_ff");
    benchmarkResult3.setRunsPerMinute(300);
    ds.put(benchmarkResult3.getEntity());
    BenchmarkResult benchmarkResult4 =
        new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_chrome");
    benchmarkResult4.setRunsPerMinute(400);
    ds.put(benchmarkResult4.getEntity());

    benchmarkRun.setResults(Arrays.asList(benchmarkResult1.getKey(), benchmarkResult2.getKey(),
        benchmarkResult3.getKey(), benchmarkResult4.getKey()));

    ds.put(benchmarkRun.getEntity());

    // put in another entity for the current week (a little bit later)
    benchmarkRun = new BenchmarkRun("commitId1", commitTime_20th_march_2014 + 5000L);
    benchmarkRun.setRunnerIds(runnerIds);
    benchmarkResult1 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_ff");
    benchmarkResult1.setRunsPerMinute(101);
    ds.put(benchmarkResult1.getEntity());
    benchmarkResult2 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_chrome");
    benchmarkResult2.setRunsPerMinute(201);
    ds.put(benchmarkResult2.getEntity());
    benchmarkResult3 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_ff");
    benchmarkResult3.setRunsPerMinute(301);
    ds.put(benchmarkResult3.getEntity());
    benchmarkResult4 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_chrome");
    benchmarkResult4.setRunsPerMinute(401);
    ds.put(benchmarkResult4.getEntity());

    benchmarkRun.setResults(Arrays.asList(benchmarkResult1.getKey(), benchmarkResult2.getKey(),
        benchmarkResult3.getKey(), benchmarkResult4.getKey()));

    ds.put(benchmarkRun.getEntity());

    // put in one entity the week before
    benchmarkRun = new BenchmarkRun("commitId2", commitTime_20th_march_2014 - oneWeekInMs);
    benchmarkRun.setRunnerIds(runnerIds);
    benchmarkResult1 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_ff");
    benchmarkResult1.setRunsPerMinute(102);
    ds.put(benchmarkResult1.getEntity());
    benchmarkResult2 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_chrome");
    benchmarkResult2.setRunsPerMinute(202);
    ds.put(benchmarkResult2.getEntity());
    benchmarkResult3 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_ff");
    benchmarkResult3.setRunsPerMinute(302);
    ds.put(benchmarkResult3.getEntity());
    benchmarkResult4 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_chrome");
    benchmarkResult4.setRunsPerMinute(402);
    ds.put(benchmarkResult4.getEntity());

    benchmarkRun.setResults(Arrays.asList(benchmarkResult1.getKey(), benchmarkResult2.getKey(),
        benchmarkResult3.getKey(), benchmarkResult4.getKey()));

    ds.put(benchmarkRun.getEntity());

    // put in one entity the week after
    benchmarkRun = new BenchmarkRun("commitId3", commitTime_20th_march_2014 + oneWeekInMs);
    benchmarkRun.setRunnerIds(runnerIds);
    benchmarkResult1 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_ff");
    benchmarkResult1.setRunsPerMinute(103);
    ds.put(benchmarkResult1.getEntity());
    benchmarkResult2 = new BenchmarkResult(benchmarkRun.getKey(), "module1", "linux_chrome");
    benchmarkResult2.setRunsPerMinute(203);
    ds.put(benchmarkResult2.getEntity());
    benchmarkResult3 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_ff");
    benchmarkResult3.setRunsPerMinute(303);
    ds.put(benchmarkResult3.getEntity());
    benchmarkResult4 = new BenchmarkResult(benchmarkRun.getKey(), "module2", "linux_chrome");
    benchmarkResult4.setRunsPerMinute(403);
    ds.put(benchmarkResult4.getEntity());

    benchmarkRun.setResults(Arrays.asList(benchmarkResult1.getKey(), benchmarkResult2.getKey(),
        benchmarkResult3.getKey(), benchmarkResult4.getKey()));

    ds.put(benchmarkRun.getEntity());

    BenchmarkController controller = new BenchmarkController();

    // update the first graph
    controller.updateGraph(commitTime_20th_march_2014, "module1", "linux_ff");
    // get an verify
    Query query = new Query(BenchmarkGraph.NAME);
    List<Entity> list = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
    Assert.assertEquals(1, list.size());

    BenchmarkGraph benchmarkGraph = new BenchmarkGraph(list.get(0));
    Assert.assertEquals("module1", benchmarkGraph.getModule());
    Assert.assertEquals("linux_ff", benchmarkGraph.getRunnerId());
    Assert.assertEquals(2, benchmarkGraph.getCommitIds().size());
    Assert.assertEquals("commitId0", benchmarkGraph.getCommitIds().get(0));
    Assert.assertEquals("commitId1", benchmarkGraph.getCommitIds().get(1));
    Assert.assertEquals(21, benchmarkGraph.getWeek());
    Assert.assertEquals(2014, benchmarkGraph.getYear());
    Assert.assertEquals(2, benchmarkGraph.getRunsPerSecond().size());
    Assert.assertEquals(100, benchmarkGraph.getRunsPerSecond().get(0), 0.0001);
    Assert.assertEquals(101, benchmarkGraph.getRunsPerSecond().get(1), 0.0001);
    // delete the entity
    ds.delete(benchmarkGraph.getEntity().getKey());

    // update the second graph
    controller.updateGraph(commitTime_20th_march_2014, "module2", "linux_ff");
    // get an verify
    query = new Query(BenchmarkGraph.NAME);
    list = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
    Assert.assertEquals(1, list.size());

    benchmarkGraph = new BenchmarkGraph(list.get(0));
    Assert.assertEquals("module2", benchmarkGraph.getModule());
    Assert.assertEquals("linux_ff", benchmarkGraph.getRunnerId());
    Assert.assertEquals(2, benchmarkGraph.getCommitIds().size());
    Assert.assertEquals("commitId0", benchmarkGraph.getCommitIds().get(0));
    Assert.assertEquals("commitId1", benchmarkGraph.getCommitIds().get(1));
    Assert.assertEquals(21, benchmarkGraph.getWeek());
    Assert.assertEquals(2014, benchmarkGraph.getYear());
    Assert.assertEquals(2, benchmarkGraph.getRunsPerSecond().size());
    Assert.assertEquals(300, benchmarkGraph.getRunsPerSecond().get(0), 0.0001);
    Assert.assertEquals(301, benchmarkGraph.getRunsPerSecond().get(1), 0.0001);
    // delete the entity
    ds.delete(benchmarkGraph.getEntity().getKey());

    // update the third graph
    controller.updateGraph(commitTime_20th_march_2014, "module1", "linux_chrome");
    // get an verify
    query = new Query(BenchmarkGraph.NAME);
    list = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
    Assert.assertEquals(1, list.size());

    benchmarkGraph = new BenchmarkGraph(list.get(0));
    Assert.assertEquals("module1", benchmarkGraph.getModule());
    Assert.assertEquals("linux_chrome", benchmarkGraph.getRunnerId());
    Assert.assertEquals(2, benchmarkGraph.getCommitIds().size());
    Assert.assertEquals("commitId0", benchmarkGraph.getCommitIds().get(0));
    Assert.assertEquals("commitId1", benchmarkGraph.getCommitIds().get(1));
    Assert.assertEquals(21, benchmarkGraph.getWeek());
    Assert.assertEquals(2014, benchmarkGraph.getYear());
    Assert.assertEquals(2, benchmarkGraph.getRunsPerSecond().size());
    Assert.assertEquals(200, benchmarkGraph.getRunsPerSecond().get(0), 0.0001);
    Assert.assertEquals(201, benchmarkGraph.getRunsPerSecond().get(1), 0.0001);
    // delete the entity
    ds.delete(benchmarkGraph.getEntity().getKey());

    // update the third graph
    controller.updateGraph(commitTime_20th_march_2014, "module2", "linux_chrome");
    // get an verify
    query = new Query(BenchmarkGraph.NAME);
    list = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
    Assert.assertEquals(1, list.size());

    benchmarkGraph = new BenchmarkGraph(list.get(0));
    Assert.assertEquals("module2", benchmarkGraph.getModule());
    Assert.assertEquals("linux_chrome", benchmarkGraph.getRunnerId());
    Assert.assertEquals(2, benchmarkGraph.getCommitIds().size());
    Assert.assertEquals("commitId0", benchmarkGraph.getCommitIds().get(0));
    Assert.assertEquals("commitId1", benchmarkGraph.getCommitIds().get(1));
    Assert.assertEquals(21, benchmarkGraph.getWeek());
    Assert.assertEquals(2014, benchmarkGraph.getYear());
    Assert.assertEquals(2, benchmarkGraph.getRunsPerSecond().size());
    Assert.assertEquals(400, benchmarkGraph.getRunsPerSecond().get(0), 0.0001);
    Assert.assertEquals(401, benchmarkGraph.getRunsPerSecond().get(1), 0.0001);
    // delete the entity
    ds.delete(benchmarkGraph.getEntity().getKey());
  }
}
