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

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * BenchmarkWorker compiles a single module, writes the host page and asks a Runner to execute the
 * benchmark handing back results.
 */
public class BenchmarkWorker implements Runnable {

  public interface Factory {
    BenchmarkWorker create(BenchmarkWorkerConfig benchmarkWorkerConfig,
        ProgressHandler progressHandler);
  }

  public interface ProgressHandler {
    void onCompilationFailed(String message);

    void onResult(RunnerConfig config, double result);

    void failedToRunBenchmark(RunnerConfig config, String errorMessage);

    void onHostPageGenerationFailed(String errorMessage);

    void onRunEnded();

    void onCompileDirCreationFailed();
  }

  private final BenchmarkCompiler compiler;
  private final Runner.Factory runnerProvider;
  private final BenchmarkWorkerConfig benchmarkData;
  private final ProgressHandler progressHandler;
  private final String ip;
  private final String moduleTemplate;
  private File compilerOutputDir;
  private int port;
  private Provider<String> randomStringProvider;

  @Inject
  public BenchmarkWorker(BenchmarkCompiler compiler,
      Runner.Factory runnerProvider,
      @Named("moduleTemplate") String moduleTemplate,
      @Assisted BenchmarkWorkerConfig benchmarkData,
      @Assisted ProgressHandler progressHandler,
      @Named("ip") String ip,
      @Named("port") int port,
      @Named("compilerOutputDir") File compilerOutputDir,
      @Named("randomStringProvider") Provider<String> randomStringProvider) {
    this.compiler = compiler;
    this.runnerProvider = runnerProvider;
    this.moduleTemplate = moduleTemplate;
    this.benchmarkData = benchmarkData;
    this.progressHandler = progressHandler;
    this.ip = ip;
    this.port = port;
    this.compilerOutputDir = compilerOutputDir;
    this.randomStringProvider = randomStringProvider;
  }

  @Override
  public void run() {
    // create working dir
    String randomDirName = randomStringProvider.get();
    File outputDir = new File(compilerOutputDir, randomDirName);
    if (!outputDir.mkdirs()) {
      progressHandler.onCompileDirCreationFailed();
      progressHandler.onRunEnded();
      return;
    }

    try {
      compiler.compile(benchmarkData.getModuleName(), outputDir, benchmarkData.getDevJar(),
          benchmarkData.getUserJar());
    } catch (BenchmarkCompilerException e) {
      cleanupDirectory(outputDir);
      progressHandler.onCompilationFailed(e.getMessage() + " " + e.getOutput());
      progressHandler.onRunEnded();
      return;
    }

    try {
      writeHostPage(outputDir, benchmarkData.getModuleName());
    } catch (IOException e) {
      cleanupDirectory(outputDir);
      progressHandler.onHostPageGenerationFailed("Failed to write host Page: " + e.getMessage());
      progressHandler.onRunEnded();
      return;
    }

    List<Runner> runners = new ArrayList<>();

    for (RunnerConfig config : benchmarkData.getRunners()) {
      String url = getUrl(this.port, randomDirName, benchmarkData.getModuleName());
      Runner r = runnerProvider.create(config, url);
      runners.add(r);
      // TODO currently executing runners in sequence, switch to parallel execution.
      r.run();
    }
    for (Runner runner : runners) {
      if (runner.isFailed()) {
        progressHandler.failedToRunBenchmark(runner.getConfig(), runner.getErrorMessage());
      } else {
        progressHandler.onResult(runner.getConfig(), runner.getResult());
      }
    }
    cleanupDirectory(outputDir);
    progressHandler.onRunEnded();
  }

  private void writeHostPage(File outputDir, String moduleName) throws IOException {
    String tpl =
        moduleTemplate.replace("{module_nocache}", moduleName + "/" + moduleName + ".nocache.js");
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(new File(outputDir, moduleName + ".html"));
      IOUtils.write(tpl.getBytes("UTF-8"), stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private String getUrl(int port, String randomDirName, String module) {
    String url = "http://{host}:{port}/__bench/{dirName}/{module}.html";
    url = url.replace("{host}", ip);
    url = url.replace("{port}", "" + port);
    url = url.replace("{dirName}", randomDirName);
    url = url.replace("{module}", module);
    return url;
  }

  // Visible for Testing
  void cleanupDirectory(File outputDir) {
    FileUtils.deleteQuietly(outputDir);
  }
}
