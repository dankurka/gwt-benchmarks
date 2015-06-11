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
package com.google.j2cl.benchmark.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobResult;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;
import com.google.j2cl.benchmark.common.util.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * RunZip can upload a zip file containing a benchmark to the benchmark infratructure and display
 * results.
 */
public class RunZip {

  @Option(name="-runnerServerUrl",usage="the url to the runner server")
  String runnerServerUrl = "http://localhost:8080";

  @Option(name="-benchmark", usage="The zip file that contains your benchmark", required = true)
  File benchmark;

  @Option(name="-runners", usage="The browsers to run your benchmark against (Default all)")
  List<String> runners = Lists.newArrayList();

  @Option(name="-zipFolder")
  boolean zipFolder;

  private List<RunnerConfig> runnerConfigs;

  public static void main(String[] args) {
    new RunZip().doMain(args);
  }

  @VisibleForTesting
  void doMain(String[] args) {
    if (!parse(args)) {
      return;
    }

    File zipFile = null;
    try {
      if (zipFolder) {
        zipFile = ZipUtil.zipFolder(benchmark, UUID.randomUUID().toString());
      }
      BenchmarkUploader benchmarkUploader =
          createBenchmarkUploader(runnerServerUrl, zipFolder? zipFile : benchmark, runnerConfigs);
      Job job = benchmarkUploader.run(true);
      System.out.println("Results:");
      for (JobResult result : job.getJobResults()) {
        if (result.isSucceded()) {
          System.out.println(result.getConfig() + ": " + result.getResult());
        } else {
          System.out.println(result.getConfig() + ": Failed (" + result.getErrorMessage() + ")");
        }
      }
    } catch (IOException e) {
      System.err.println("Error running benchmark");
      e.printStackTrace(System.err);
    } catch (InterruptedException e) {
      // probably a CTRL-c
      System.out.println("quitting");
    } finally {
      FileUtils.deleteQuietly(zipFile);
    }
  }

  private List<RunnerConfig> parseRunner(CmdLineParser parser, List<String> commandLineRunners)
      throws CmdLineException {
    List<RunnerConfig> runnerConfigs = Lists.newArrayList();
    for (String runner : commandLineRunners) {
      switch (runner) {
        case "chrome":
          runnerConfigs.add(RunnerConfigs.CHROME_LINUX);
          break;
        case "firefox":
          runnerConfigs.add(RunnerConfigs.FIREFOX_LINUX);
          break;
        case "ie10":
          runnerConfigs.add(RunnerConfigs.IE10_WIN);
          break;
        case "ie11":
          runnerConfigs.add(RunnerConfigs.IE11_WIN);
          break;
        default:
          throw new CmdLineException(parser, "Invalid value for -runners: " + runner
              + ". Allowed values: chrome, firefox, ie10, ie11", null);
      }
    }
    return runnerConfigs;
  }

  private boolean parse(String[] args) {
    ParserProperties parserProperties = ParserProperties.defaults()
    // Console width of 80
        .withUsageWidth(80);

    CmdLineParser parser = new CmdLineParser(this, parserProperties);

    try {
      parser.parseArgument(args);
      runnerConfigs =
          runners.isEmpty() ? RunnerConfigs.getAllRunners() : parseRunner(parser, runners);
      return true;
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      System.err.println("run_zip.sh [options...] arguments...");
      // print the list of available options
      parser.printUsage(System.err);
      System.err.println();
      // print option sample. This is useful some time
      System.err.println("  Example: run_zip.sh " + parser.printExample(OptionHandlerFilter.ALL));
      return false;
    }
  }

  @VisibleForTesting
  BenchmarkUploader createBenchmarkUploader(String serverUrl, File zip,
      List<RunnerConfig> runnerConfigs) {
    return new BenchmarkUploader(serverUrl, zip, runnerConfigs);
  }
}
