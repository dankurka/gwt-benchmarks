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

import com.google.gwt.benchmark.common.shared.json.BenchmarkResultJson;
import com.google.gwt.benchmark.common.shared.json.BenchmarkRunJson;
import com.google.gwt.benchmark.common.shared.json.JsonFactory;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.HttpURLConnectionFactory;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.ReportProgressHandler;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link BenchmarkReporter}.
 */
public class BenchmarkReporterTest {

  private BenchmarkReporter reporter;
  private HashMap<String, BenchmarkRun> results;
  private HttpURLConnectionFactory urlFactory;
  private HttpURLConnection urlConnection;
  private OutputStream outputStream;
  private String commitId;
  private String commitDate;
  private ReportProgressHandler reportProgressHandler;

  @Before
  public void setup() {
    commitId = "commitId1";
    commitDate = "my commit date";

    results = new HashMap<>();
    BenchmarkRun benchmarkRun = new BenchmarkRun("module1", commitId, commitDate);
    benchmarkRun.addRunner(RunnerConfigs.CHROME_LINUX);
    benchmarkRun.addRunner(RunnerConfigs.FIREFOX_LINUX);
    benchmarkRun.addResult(RunnerConfigs.CHROME_LINUX, 2);
    benchmarkRun.addResult(RunnerConfigs.FIREFOX_LINUX, 3);
    results.put("module1", benchmarkRun);
    BenchmarkRun benchmarkRun1 = new BenchmarkRun("module2", commitId, commitDate);
    benchmarkRun1.addRunner(RunnerConfigs.CHROME_LINUX);
    benchmarkRun1.addRunner(RunnerConfigs.FIREFOX_LINUX);
    benchmarkRun1.addResult(RunnerConfigs.CHROME_LINUX, 4);
    benchmarkRun1.addResult(RunnerConfigs.FIREFOX_LINUX, 5);
    results.put("module2", benchmarkRun1);

    urlFactory = Mockito.mock(BenchmarkReporter.HttpURLConnectionFactory.class);

    urlConnection = Mockito.mock(HttpURLConnection.class);

    outputStream = Mockito.mock(OutputStream.class);

    reportProgressHandler = Mockito.mock(ReportProgressHandler.class);

  }

  @Test
  public void testSuccessFulPostToServer() throws IOException {

    Mockito.when(urlFactory.create()).thenReturn(urlConnection);
    Mockito.when(urlConnection.getOutputStream()).thenReturn(outputStream);
    Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

    reporter =
        new BenchmarkReporter(urlFactory, results, commitId, commitDate, reportProgressHandler);

    reporter.run();

    Mockito.verify(outputStream).close();
    Mockito.verify(urlConnection).setRequestMethod("PUT");
    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
    Mockito.verify(outputStream).write(captor.capture());

    byte[] jsonBytes = captor.getValue();
    String jsonString = new String(jsonBytes, "UTF-8");

    AutoBean<BenchmarkRunJson> bean =
        AutoBeanCodex.decode(JsonFactory.get(), BenchmarkRunJson.class, jsonString);
    BenchmarkRunJson benchmarkRunJSON = bean.as();

    Assert.assertEquals(commitId, benchmarkRunJSON.getCommitId());
    Assert.assertEquals(commitDate, benchmarkRunJSON.getCommitTime());

    Map<String, List<BenchmarkResultJson>> resultsJSON =
        benchmarkRunJSON.getResultByBenchmarkName();

    Assert.assertEquals(2, resultsJSON.size());
    List<BenchmarkResultJson> module1List = resultsJSON.get("module1");
    Assert.assertEquals(2, module1List.size());
    Assert.assertEquals("module1", module1List.get(0).getBenchmarkName());
    Assert.assertEquals(2, module1List.get(0).getRunsPerMinute(), 0.0001);
    Assert.assertEquals(RunnerConfigs.CHROME_LINUX.toString(),
        module1List.get(0).getRunnerId().toString());
    Assert.assertEquals("module1", module1List.get(1).getBenchmarkName());
    Assert.assertEquals(3, module1List.get(1).getRunsPerMinute(), 0.0001);
    Assert.assertEquals(RunnerConfigs.FIREFOX_LINUX.toString(),
        module1List.get(1).getRunnerId().toString());

    List<BenchmarkResultJson> module1List2 = resultsJSON.get("module2");
    Assert.assertEquals("module2", module1List2.get(0).getBenchmarkName());
    Assert.assertEquals(4, module1List2.get(0).getRunsPerMinute(), 0.0001);
    Assert.assertEquals(RunnerConfigs.CHROME_LINUX.toString(),
        module1List2.get(0).getRunnerId().toString());
    Assert.assertEquals("module2", module1List2.get(1).getBenchmarkName());
    Assert.assertEquals(5, module1List2.get(1).getRunsPerMinute(), 0.0001);
    Assert.assertEquals(RunnerConfigs.FIREFOX_LINUX.toString(),
        module1List2.get(1).getRunnerId().toString());

    Mockito.verify(reportProgressHandler).onCommitReported();
  }

  @Test
  public void testFailingRetries() throws IOException {

    Mockito.when(urlFactory.create()).thenReturn(urlConnection);
    Mockito.when(urlConnection.getOutputStream()).thenReturn(outputStream);
    Mockito.when(urlConnection.getResponseCode()).thenReturn(500);

    final List<Integer> waitingTimes = new ArrayList<>();

    reporter =
        new BenchmarkReporter(urlFactory, results, commitId, commitDate, reportProgressHandler) {

          @Override
          boolean sleep(int seconds) {
            waitingTimes.add(seconds);
            return true;
          }
        };

    reporter.run();

    Mockito.verify(outputStream, Mockito.times(BenchmarkReporter.WAITING_TIME_SECONDS.length))
        .close();
    Mockito.verify(urlConnection, Mockito.times(BenchmarkReporter.WAITING_TIME_SECONDS.length))
        .setRequestMethod("PUT");

    Assert.assertEquals(BenchmarkReporter.WAITING_TIME_SECONDS.length, waitingTimes.size());

    for (int i = 0; i < BenchmarkReporter.WAITING_TIME_SECONDS.length; i++) {
      Assert.assertEquals(BenchmarkReporter.WAITING_TIME_SECONDS[i],
          waitingTimes.get(i).intValue());
    }

    Mockito.verify(reportProgressHandler).onPermanentFailure();
  }
}
