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
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkRun.Result;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BenchmarkReporter reports benchmark results to a remote URL.
 * <p>
 * It does a HTTP Put with JSON in the body using the AutoBeans interfaces from the common project.
 * On Failure it retries a couple of times before reporting a permanent failure.
 */
public class BenchmarkReporter implements Runnable {

  public interface HttpURLConnectionFactory {
    HttpURLConnection create() throws IOException;
  }

  public interface ReportProgressHandler {
    void onCommitReported();

    void onPermanentFailure();
  }

  public interface Factory {
    BenchmarkReporter create(Map<String, BenchmarkRun> results,
        @Assisted("commitId") String commitId, @Assisted("commitDate") long commitMsEpoch,
        ReportProgressHandler reportProgressHandler);
  }

  public static final int[] WAITING_TIME_SECONDS = new int[] {1, 10, 100, 1000, 1000};

  private static final int HTTP_OK = 200;
  private static Logger logger = Logger.getLogger(BenchmarkReporter.class.getName());

  private final Map<String, BenchmarkRun> results;
  private final String commitId;
  private final HttpURLConnectionFactory httpURLConnectionFactory;

  private final ReportProgressHandler reportProgressHandler;

  private final long commitMsEpoch;

  private String reporterSecret;

  @Inject
  public BenchmarkReporter(HttpURLConnectionFactory httpURLConnectionFactory,
      @Named("reporterSecret") String reporterSecret, @Assisted Map<String, BenchmarkRun> results,
      @Assisted("commitId") String commitId, @Assisted("commitDate") long commitMsEpoch,
      @Assisted ReportProgressHandler reportProgressHandler) {
    this.httpURLConnectionFactory = httpURLConnectionFactory;
    this.reporterSecret = reporterSecret;
    this.results = results;
    this.commitId = commitId;
    this.commitMsEpoch = commitMsEpoch;
    this.reportProgressHandler = reportProgressHandler;
  }

  @Override
  public void run() {
    AutoBean<BenchmarkRunJson> bean = AutoBeanUtils.getAutoBean(createBenchmarkRunJson());
    String jsonString = AutoBeanCodex.encode(bean).getPayload();

    boolean sent = false;
    for (int count = 0; count < WAITING_TIME_SECONDS.length; count++) {
      if (postResultToServer(jsonString)) {
        sent = true;

        break;
      }
      logger.warning(String.format("Could not post results to dashboard retrying in %d seconds.",
          WAITING_TIME_SECONDS[count]));
      if (!sleep(WAITING_TIME_SECONDS[count])) {
        break;
      }
    }

    if (!sent) {
      reportProgressHandler.onPermanentFailure();
    } else {
      reportProgressHandler.onCommitReported();
    }
  }

  private BenchmarkRunJson createBenchmarkRunJson() {
    JsonFactory.Factory factory = JsonFactory.get();

    BenchmarkRunJson runJSON = factory.run().as();
    runJSON.setCommitId(commitId);
    runJSON.setCommitTimeMsEpoch(commitMsEpoch);
    Map<String, List<BenchmarkResultJson>> results = new LinkedHashMap<>();

    for (Entry<String, BenchmarkRun> br : this.results.entrySet()) {

      String moduleName = br.getKey();
      List<BenchmarkResultJson> list = new ArrayList<>();
      results.put(moduleName, list);

      for (Entry<RunnerConfig, Result> entry : br.getValue().getResults().entrySet()) {
        Result result = entry.getValue();
        RunnerConfig runnerConfig = entry.getKey();
        BenchmarkResultJson resultJSON = factory.result().as();
        resultJSON.setBenchmarkName(moduleName);
        resultJSON.setRunnerId(runnerConfig.toString());
        resultJSON.setRunsPerSecond(result.getRunsPerSecond());
        list.add(resultJSON);
      }

    }
    runJSON.setResultByBenchmarkName(results);
    return runJSON;
  }

  private boolean postResultToServer(String json) {
    OutputStream out = null;
    try {

      HttpURLConnection httpCon = httpURLConnectionFactory.create();
      httpCon.addRequestProperty("auth", reporterSecret);
      httpCon.setDoOutput(true);
      httpCon.setRequestMethod("PUT");

      out = httpCon.getOutputStream();
      out.write(json.getBytes("UTF-8"));

      if (httpCon.getResponseCode() == HTTP_OK) {
        return true;
      }

    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not post results to server", e);
    } finally {
      IOUtils.closeQuietly(out);
    }
    return false;
  }

  // Visible for testing
  boolean sleep(int seconds) {
    try {
      Thread.sleep(1000L * seconds);
      return true;
    } catch (InterruptedException e) {
      // Our framework does not make use of thread.interrupt() so this must mean the JVM is trying
      // to gracefully shut down in response to an external signal. Let it happen.
      return false;
    }
  }
}
