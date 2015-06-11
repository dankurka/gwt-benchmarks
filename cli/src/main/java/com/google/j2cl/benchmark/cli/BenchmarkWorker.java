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
package com.google.j2cl.benchmark.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.google.j2cl.benchmark.cli.BenchmarkRun.State;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.util.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * BenchmarkWorker compiles a single module, writes the host page and asks a Runner to execute the
 * benchmark handing back results.
 */
public class BenchmarkWorker implements Callable<BenchmarkWorker.WorkResult> {

  public static class WorkResult {

    public WorkResult(State state) {
      this(state, "");
    }

    public WorkResult(State state, String reason) {
      this.state = state;
      this.job = null;
      this.reason = reason;
    }

    public WorkResult(Job job) {
      this.job = job;
      this.state = State.DONE;
      this.reason = null;
    }

    public final Job job;
    public final State state;
    public final String reason;

    public boolean isFailed() {
      switch(state) {
        case DONE:
        case NOT_RUN:
          return false;
        default:
          return true;
      }
    }
  }

  public interface Factory {
    BenchmarkWorker create(BenchmarkWorkerConfig benchmarkWorkerConfig);
  }

  private final BenchmarkCompiler compiler;
  private final BenchmarkWorkerConfig benchmarkData;
  private final String moduleTemplate;
  private File compilerOutputDir;
  private Provider<String> randomStringProvider;
  private BenchmarkUploader.Factory benchmarkUploderFactory;

  @Inject
  public BenchmarkWorker(
      BenchmarkCompiler compiler,
      @Named("moduleTemplate") String moduleTemplate,
      @Assisted BenchmarkWorkerConfig benchmarkData,
      @Named("compilerOutputDir") File compilerOutputDir,
      @Named("randomStringProvider") Provider<String> randomStringProvider,
      BenchmarkUploader.Factory benchmarkUploderFactory) {
    this.compiler = compiler;
    this.moduleTemplate = moduleTemplate;
    this.benchmarkData = benchmarkData;
    this.compilerOutputDir = compilerOutputDir;
    this.randomStringProvider = randomStringProvider;
    this.benchmarkUploderFactory = benchmarkUploderFactory;
  }

  @Override
  public WorkResult call() {
    // create working dir
    String randomDirName = randomStringProvider.get();
    File outputDir = new File(compilerOutputDir, randomDirName);
    if (!outputDir.mkdirs()) {
      return new WorkResult(State.FAILED_TO_CREATE_DIR);
    }

    try {
      compiler.compile(benchmarkData.getModuleName(), outputDir, benchmarkData.getDevJar(),
          benchmarkData.getUserJar(), benchmarkData.getCompilerArgs());
    } catch (CliException e) {
      cleanupDirectory(outputDir);
      return new WorkResult(State.FAILED_COMPILE, e.getMessage());
    }

    try {
      writeHostPage(outputDir, benchmarkData.getModuleName());
    } catch (IOException e) {
      cleanupDirectory(outputDir);
      return new WorkResult(State.FAILED_TO_GENERATE_HOST_PAGE, e.getMessage());
    }

    File zipFile = null;
    try {
      zipFile = ZipUtil.zipFolder(outputDir, randomStringProvider.get());
    } catch (IOException e) {
      return new WorkResult(State.FAILED_TO_ZIP_BENCHMARK, e.getMessage());
    } finally {
      cleanupDirectory(outputDir);
    }

    BenchmarkUploader benchmarkUploader =
        benchmarkUploderFactory.create(zipFile, benchmarkData.getRunners());

    try {
      Job job = benchmarkUploader.run(false);
      return new WorkResult(job);
    } catch (IOException e) {
      return new WorkResult(
          State.FAILED_TO_RUN_ON_RUNNER, "Failed to get results for benchmark: " + e.getMessage());
    } catch (InterruptedException e) {
      return new WorkResult(
          State.FAILED_TO_RUN_ON_RUNNER, "Failed to get results for benchmark: " + e.getMessage());
    } finally {
      FileUtils.deleteQuietly(zipFile);
    }
  }

  @VisibleForTesting
  void writeHostPage(File outputDir, String moduleName) throws IOException {
    String tpl =
        moduleTemplate.replace("{module_nocache}", moduleName + "/" + moduleName + ".nocache.js");
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(new File(outputDir, "index.html"));
      IOUtils.write(tpl.getBytes("UTF-8"), stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  @VisibleForTesting
  void cleanupDirectory(File outputDir) {
    FileUtils.deleteQuietly(outputDir);
  }
}
