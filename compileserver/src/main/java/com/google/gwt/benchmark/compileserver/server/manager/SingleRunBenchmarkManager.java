/*
 * Copyright 2015 Google Inc.
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

import com.google.inject.Provider;
import com.google.inject.name.Named;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * SingleRunBenchmarkManager allows for a single (updloaded) versino of the SDK to be benchmarked.
 */
public class SingleRunBenchmarkManager extends BenchmarkManager {

  private static final Logger logger = Logger.getLogger(BenchmarkManager.class.getName());
  private File sdkFolder;

  @Inject
  public SingleRunBenchmarkManager(BenchmarkFinder collector,
      BenchmarkWorker.Factory benchmarkWorkerFactory,
      @Named("managerPoolSize") Provider<ExecutorService> poolProvider,
      BenchmarkReporter.Factory reporterFactory,
      @Named("useReporter") boolean useReporter,
      CliInteractor commitReader,
      Provider<Timer> timerProvider,
      MailReporter errorReporter,
      @Named("gwtSourceLocation") File gwtSourceLocation) {
    super(collector, benchmarkWorkerFactory, poolProvider, reporterFactory, useReporter,
        commitReader, timerProvider, errorReporter, gwtSourceLocation);
  }

  @Override
  protected void runEventLoop() {
    logger.info("Starting benchmark runners");
    startBenchmarkingAllForCommit("no_commit", System.currentTimeMillis());
  }

  public void setSDKDir(File sdkFolder) {
    // This is a dirty hack to get rid of files of an old run
    // but for now this is good enough...
    if (this.sdkFolder != null) {
      FileUtils.deleteQuietly(this.sdkFolder);
    }

    this.sdkFolder = sdkFolder;
    if (!sdkFolder.isDirectory()) {
      throw new IllegalArgumentException(
          "sdkFolder does not point to a folder: " + sdkFolder.getAbsolutePath());
    }

    devJar = new File(sdkFolder, "gwt-dev.jar");
    if (!devJar.exists()) {
      throw new IllegalArgumentException("Can not find dev jar");
    }
    userJar = new File(sdkFolder, "gwt-user.jar");
    if (!userJar.exists()) {
      throw new IllegalArgumentException("Can not find user jar");
    }
  }

  @Override
  protected void maybeReportResults(String commitId, long commitMsEpoch) {
    currentlyRunning = false;
  }
}
