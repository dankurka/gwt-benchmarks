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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.j2cl.benchmark.cli.BenchmarkRun.Result;
import com.google.j2cl.benchmark.cli.BenchmarkRun.Result.State;
import com.google.j2cl.benchmark.cli.BenchmarkWorker.WorkResult;
import com.google.j2cl.benchmark.common.runner.JobResult;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Manager compiles and runs a series of benchmarks.
 */
public class Manager {

  private static final Logger logger = Logger.getLogger(Manager.class.getName());

  private static final long TICK_INTERVAL = 10 * 1000L;

  private static final String NEWLINE = "\n";

  private static List<String> getNonSuccessfulRuns(List<BenchmarkRun> results) {
    List<String> list = new ArrayList<>();
    for (BenchmarkRun benchmarkRun : results) {
      if (benchmarkRun.isFailed()) {
        for (Entry<RunnerConfig, Result> result : benchmarkRun.getResults().entrySet()) {
          if (result.getValue().getState() == BenchmarkRun.Result.State.FAILED_RUN) {
            list.add(benchmarkRun.getModuleName() + " " + result.getKey() + " "
                + benchmarkRun.getReportingName());
          }
        }
      } else {
        for (Entry<RunnerConfig, Result> result : benchmarkRun.getResults().entrySet()) {
          if (result.getValue().getState() != Result.State.SUCCESSFUL_RUN) {
            list.add(benchmarkRun.getModuleName() + " " + result.getKey() + " "
                + benchmarkRun.getReportingName());
          }
        }
      }
    }
    return list;
  }

  private final BenchmarkFinder benchmarkFinder;

  private final BenchmarkWorker.Factory benchmarkWorkerFactory;

  private long currentCommitDateMsEpoch;

  private String currentCommitId;

  private final MailReporter errorReporter;

  private String lastSuccessfulCommitId;

  private ExecutorService pool;

  private Provider<ExecutorService> poolProvider;

  private final BenchmarkReporter.Factory reporterFactory;

  protected final CliInteractor cliInteractor;

  private boolean reportResults;

  protected final File devJar;

  protected final File userJar;

  private final Predicate<String> benchmarkFilter;

  private final boolean deamonMode;

  private final boolean skipSDKBuild;

  private final String compilerArgs;

  @Inject
  public Manager(BenchmarkFinder collector, BenchmarkWorker.Factory benchmarkWorkerFactory,
      @Named("managerPoolSize") Provider<ExecutorService> poolProvider,
      BenchmarkReporter.Factory reporterFactory, @Named("reportResults") boolean reportResults,
      CliInteractor commitReader, MailReporter errorReporter,
      @Named("benchmarkFilter") Predicate<String> benchmarkFilter,
      @Named("deamonMode") boolean deamonMode, @Named("skipSDKBuild") boolean skipSDKBuild,
      @Named("gwtDevJar") File devJar, @Named("gwtUserJar") File userJar,
      @Named("compilerArgs") String compilerArgs) {
    this.benchmarkFinder = collector;
    this.benchmarkWorkerFactory = benchmarkWorkerFactory;
    this.poolProvider = poolProvider;
    this.reporterFactory = reporterFactory;
    this.reportResults = reportResults;
    this.cliInteractor = commitReader;
    this.errorReporter = errorReporter;
    this.benchmarkFilter = benchmarkFilter;
    this.deamonMode = deamonMode;
    this.skipSDKBuild = skipSDKBuild;
    this.devJar = devJar;
    this.userJar = userJar;
    this.compilerArgs = compilerArgs;
  }

  private BenchmarkRun createBenchmarkRunForModule(
      String moduleName, String commitId, long currentCommitDateMsEpoch, String reportingName) {
    BenchmarkRun br =
        new BenchmarkRun(moduleName, commitId, currentCommitDateMsEpoch, reportingName);
    for (RunnerConfig config : RunnerConfigs.getAllRunners()) {
      br.addRunner(config);
    }
    return br;
  }

  protected void reportNonSuccesulRuns(List<BenchmarkRun> results) {
    Collection<String> runs = getNonSuccessfulRuns(results);
    if (!runs.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Benchmarks failed executing - stopping system\n\n");
      builder.append("Failed Benchmarks: \n");
      for (String errorModule : runs) {
        builder.append(String.format("%s \n", errorModule));
      }
      logger.severe(
          String.format("Benchmarks failed executing - stopping system\n%s", builder.toString()));
      reportError(builder.toString());
    }
  }

  protected boolean maybeReportResults(String commitId, List<BenchmarkRun> results) {
    if (!reportResults) {
      return true;
    }

    BenchmarkReporter benchmarkReporter = reporterFactory.create(results, commitId);

    if (!benchmarkReporter.report()) {
      reportError("Reporter failed to report results, shutting down the system");
      return false;
    }

    return true;
  }

  protected boolean checkoutLastCommit() {
    try {
      logger.info("Getting last commit");
      setLastCommit(cliInteractor.getLastCommitId());
      currentCommitId = lastSuccessfulCommitId;
      logger.info(String.format("Last commit was %s", currentCommitId));
      currentCommitDateMsEpoch = cliInteractor.getDateForCommitInMsEpoch(currentCommitId);
      logger.info("Checking out last commit");
      cliInteractor.checkout(lastSuccessfulCommitId);
      logger.info(String.format("found a new commit %s", currentCommitId));
      logger.info("Getting its commit date");
      currentCommitDateMsEpoch = cliInteractor.getDateForCommitInMsEpoch(currentCommitId);
      return true;
    } catch (CliException e) {
      logger.log(Level.WARNING, "Can not checkout commit - shutting down", e);
      reportError("Can not update git repo");
      return false;
    }
  }

  protected boolean hasUpdates;

  protected boolean checkForPossibleUpdates() {
    hasUpdates = false;
    try {
      cliInteractor.maybeCheckoutNextCommit(lastSuccessfulCommitId);
      String commitId = cliInteractor.getCurrentCommitId();
      logger.info("Repo is now at commit: " + commitId);
      hasUpdates = !currentCommitId.equals(commitId);
      logger.info("Do we have a new commit: " + hasUpdates);
      currentCommitId = commitId;
      return true;

    } catch (CliException e) {
      logger.log(Level.WARNING, "Can not update repository", e);
      reportError("Can not update git repo");
      return false;
    }
  }



  protected boolean buildSDK() {
    try {
      logger.info("Building SDK");
      cliInteractor.buildSDK();
      logger.info("Successfully build SDK");
      return true;
    } catch (CliException e) {
      logger.log(Level.WARNING, "Can not build SDK", e);
      return false;
    }
  }

  public void execute() throws InterruptedException {
    if (deamonMode) {
      runAsDeamon();
    } else {
      runAsSingleBenchmarRun();
    }
  }

  protected void runAsSingleBenchmarRun() throws InterruptedException {
    if (!skipSDKBuild) {
      if(!buildSDK()) {
        System.err.println("Can not build SDK");
        return;
      }
    }

    List<RunInfo> jobFutures = startBenchmarking("no commit id", System.currentTimeMillis());
    waitForJobsAndUpdate(Lists.newArrayList(jobFutures));
    ImmutableList<BenchmarkRun> finishedRuns = convertToBenchmarkRuns(jobFutures);
    String output = convertRunToString(finishedRuns);
    printOutput(output);
  }

  @VisibleForTesting
  void printOutput(String output) {
    System.out.print(output);
  }

  protected String convertRunToString(List<BenchmarkRun> finishedRuns) {
    StringBuilder builder = new StringBuilder();
    builder.append("Results:");
    builder.append(NEWLINE);

    for (BenchmarkRun benchmarkRun : finishedRuns) {
      builder.append("  "  + benchmarkRun.getModuleName());
      builder.append(NEWLINE);
      if (benchmarkRun.isFailed() && benchmarkRun.getState() != BenchmarkRun.State.FAILED_TO_RUN_ON_RUNNER) {
        // all of them failed!
        for (RunnerConfig config : benchmarkRun.getRunConfigs()) {
          String message = String.format("    %s: Failed (%s)", config, benchmarkRun.getErrorMessage());
          builder.append(message);
          builder.append(NEWLINE);
        }
      } else {
        for (RunnerConfig config : benchmarkRun.getRunConfigs()) {
          Result result = benchmarkRun.getResults().get(config);
          if (result.getState() == State.SUCCESSFUL_RUN) {
            String message = String.format("    %s: %f runs/second", config, result.getRunsPerSecond());
            builder.append(message);
            builder.append(NEWLINE);
          } else {
            String message = String.format("    %s: Failed (%s)", config, result.getErrorMessage());
            builder.append(message);
            builder.append(NEWLINE);
          }
        }
      }
    }
    return builder.toString();
  }

  protected void runAsDeamon() throws InterruptedException {
    // check out last successful commit
    if (!checkoutLastCommit()) {
      return;
    }

    while (true) {
      if (!checkForPossibleUpdates()) {
        return;
      }

      if (!hasUpdates) {
        sleep(TICK_INTERVAL);
        continue;
      }

      // we have a new commit
      if (!buildSDK()) {
        reportError("Can not build SDK");
        return;
      }

      List<RunInfo> jobFutures = startBenchmarking(currentCommitId, currentCommitDateMsEpoch);
      waitForJobsAndUpdate(Lists.newArrayList(jobFutures));
      ImmutableList<BenchmarkRun> finishedRuns = convertToBenchmarkRuns(jobFutures);
      boolean successfulRun = getNonSuccessfulRuns(finishedRuns).isEmpty();

      if (!successfulRun) {
        reportNonSuccesulRuns(finishedRuns);
        return;
      }

      if (!maybeReportResults(currentCommitId, finishedRuns)) {
        reportError("Unable to save results to spreadsheet");
        return;
      }

      if (!updateCurrentCommit(currentCommitId)) {
        reportError("Can not store last commitId - quitting");
        return;
      }
    }
  }

  protected ImmutableList<BenchmarkRun> convertToBenchmarkRuns(List<RunInfo> jobFutures) {
    ImmutableList<BenchmarkRun> finishedRuns =
        FluentIterable.from(jobFutures).transform(new Function<RunInfo, BenchmarkRun>() {

            @Override
          public BenchmarkRun apply(RunInfo input) {
            return input.benchmarkRun;
          }
        }).toList();
    return finishedRuns;
  }

  protected boolean updateCurrentCommit(String commit) {
    try {
      cliInteractor.storeCommitId(commit);
      setLastCommit(commit);
      return true;
    } catch (CliException e) {
      logger.log(Level.WARNING, "Can not store last commitId", e);
      return false;
    }
  }

  protected void waitForJobsAndUpdate(List<RunInfo> jobFutures) throws InterruptedException {
    while (!jobFutures.isEmpty()) {
      try {
        sleepWaitingForJobs();
      } catch (InterruptedException e) {
        throw e;
      }
      Iterables.removeIf(jobFutures, new Predicate<RunInfo>() {
          @Override
        public boolean apply(RunInfo info) {
          if (info.future.isDone()) {
            WorkResult workResult = null;
            try {
              workResult = info.future.get();
            } catch (InterruptedException | ExecutionException e) {
              // can not be thrown since work is done
            }

            if (workResult.isFailed() || !workResult.job.isSucceeded()) {
              if (workResult.job == null) {
                // the whole job failed
                switch(workResult.state) {
                  case FAILED_COMPILE:
                    info.benchmarkRun.setFailedCompile(workResult.reason);
                    break;

                  case FAILED_TO_CREATE_DIR:
                    info.benchmarkRun.setFailedToCreateDirectory();
                    break;
                  case FAILED_TO_GENERATE_HOST_PAGE:
                    info.benchmarkRun.setFailedHostPageGenerationFailed(workResult.reason);
                    break;
                  case FAILED_TO_RUN_ON_RUNNER:
                    info.benchmarkRun.setFailedToRunOnServer();
                    break;
                  case FAILED_TO_ZIP_BENCHMARK:
                    info.benchmarkRun.setFailedToCreateDirectory();
                    break;
                  default:
                    throw new RuntimeException();
                }
              } else {
                for (JobResult jobResult : workResult.job.getJobResults()) {
                  if (jobResult.isSucceded()) {
                    info.benchmarkRun.addResult(jobResult.getConfig(), jobResult.getResult());
                  } else {
                    info.benchmarkRun.getResults().get(jobResult.getConfig()).setErrorMessage(
                        jobResult.getErrorMessage());
                    info.benchmarkRun.setFailedToRunOnServer();
                  }
                }
              }
            } else {
              for (JobResult jobResult : workResult.job.getJobResults()) {
                info.benchmarkRun.addResult(jobResult.getConfig(), jobResult.getResult());
              }
            }
            return true;
          }
          return false;
        }
      });
    }
  }



  protected void reportError(String message) {
    errorReporter.sendEmail(message);
  }

  private void setLastCommit(String commitId) {
    lastSuccessfulCommitId = commitId;
  }

  protected static class RunInfo {
    public Future<BenchmarkWorker.WorkResult> future;
    public BenchmarkRun benchmarkRun;
  }

  protected List<RunInfo> startBenchmarking(String commitId, long currentCommitDateMsEpoch) {
    logger.info("Starting benchmark runners");
    pool = poolProvider.get();
    List<RunInfo> jobFutures = Lists.newArrayList();

    for (String benchmarkModuleName : benchmarkFinder.get()) {

      // we are currently ignoring D8 benchmarks until we have a V8 runner.
      if (benchmarkModuleName.endsWith("D8")) {
        continue;
      }

      // TODO(dankurka): fix navier and remove this
      if (benchmarkModuleName.endsWith("NavierStokesBenchmarkGWT")) {
        continue;
      }

      if (!benchmarkFilter.apply(benchmarkModuleName)) {
        logger.info(String.format("Removed benchmark due to filter: %s", benchmarkModuleName));
        continue;
      }

      if (deamonMode) {
        // in deamon mode we want to test different configs of the compiler
        for (CompilerSettings compilerSettings : COMPILER_SETINGS) {
          String actualArguments = compilerArgs + " " + compilerSettings.args;
          RunInfo runInfo = submitJob(benchmarkModuleName, commitId, currentCommitDateMsEpoch,
              actualArguments, compilerSettings.name);
          jobFutures.add(runInfo);
        }
      } else {
        RunInfo runInfo =
            submitJob(benchmarkModuleName, commitId, currentCommitDateMsEpoch, compilerArgs, "");
        jobFutures.add(runInfo);
      }
    }
    return jobFutures;
  }

  protected RunInfo submitJob(String benchmarkModuleName, String commitId,
      long currentCommitDateMsEpoch, String compilerArgs, String reportingName) {
    BenchmarkRun br = createBenchmarkRunForModule(
        benchmarkModuleName, commitId, currentCommitDateMsEpoch, reportingName);
    BenchmarkWorker worker = benchmarkWorkerFactory.create(
        BenchmarkWorkerConfig.from(br, devJar, userJar, compilerArgs));
    RunInfo runInfo = new RunInfo();
    runInfo.benchmarkRun = br;
    runInfo.future = pool.submit(worker);
    return runInfo;
  }

  @VisibleForTesting
  void sleep(long timeInMs) throws InterruptedException {
    Thread.sleep(timeInMs);
  }

  @VisibleForTesting
  void sleepWaitingForJobs() throws InterruptedException {
    Thread.sleep(5 * 1000l);
  }

  private static final ImmutableList<CompilerSettings> COMPILER_SETINGS = ImmutableList.of(
      // TODO(dankurka): Talk with Goktug about using precondistions here
      //new CompilerSettings("Normal", ""),
      new CompilerSettings("FullOptimized", "-optimize 9 -XnoclassMetadata " +
          "-XnocheckCasts")/*,*/
      //new CompilerSettings("NoneOptimized", "-style PRETTY -optimize 0")
      );

  private static class CompilerSettings {
    public final String args;
    public String name;
    public CompilerSettings(String name, String args) {
      this.name = name;
      this.args = args;
    }
  }

}
