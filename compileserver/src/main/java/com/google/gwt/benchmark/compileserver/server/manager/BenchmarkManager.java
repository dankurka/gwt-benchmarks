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

import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.Factory;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.ReportProgressHandler;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkRun.Result;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkWorker.ProgressHandler;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * BenchmarkManager is the central place for starting and stopping benchmarks.
 *
 * <p>
 * BenchmarkManager interacts with several other objects to execute benchmarks, pull new changes
 * into the local repository, build the SDK and report results or errors.
 */
@Singleton
public class BenchmarkManager {

  private class EventLoop implements Runnable {
    @Override
    public void run() {
      runEventLoop();
    }
  }

  private class ThreadSafeProgressHandler implements ProgressHandler {

    private BenchmarkRun benchmarkRun;

    public ThreadSafeProgressHandler(BenchmarkRun benchmarkRun) {
      this.benchmarkRun = benchmarkRun;
    }

    @Override
    public void onCompilationFailed(String message) {
      synchronized (benchmarkRunsByNameLock) {
        benchmarkRun.setFailedCompile(message);
      }
    }

    @Override
    public void onResult(RunnerConfig config, double result) {
      synchronized (benchmarkRunsByNameLock) {
        benchmarkRun.addResult(config, result);
      }
    }

    @Override
    public void failedToRunBenchmark(RunnerConfig config, String errorMessage) {
      synchronized (benchmarkRunsByNameLock) {
        benchmarkRun.getResults().get(config).setErrorMessage(errorMessage);
        benchmarkRun.setFailedToRunOnServer();
      }
    }

    @Override
    public void onHostPageGenerationFailed(String errorMessage) {
      synchronized (benchmarkRunsByNameLock) {
        benchmarkRun.setFailedHostPageGenerationFailed(errorMessage);
      }
    }

    @Override
    public void onRunEnded() {
      synchronized (benchmarkRunsByNameLock) {
        if (!benchmarkRun.isFailed()) {
          benchmarkRun.setRunEnded();
        }
      }
      if (workCount.decrementAndGet() == 0) {
        maybeReportResults(benchmarkRun.getCommitId(), benchmarkRun.getCommitMsEpoch());
      }
    }

    @Override
    public void onCompileDirCreationFailed() {
      synchronized (benchmarkRunsByNameLock) {
        benchmarkRun.setFailedToCreateDirectory();
      }
    }
  }

  private static final Logger logger = Logger.getLogger(BenchmarkManager.class.getName());

  private static final long TICK_INTERVAL = 10 * 1000L;

  private static Map<String, BenchmarkRun> deepClone(Map<String, BenchmarkRun> runMap) {
    Map<String, BenchmarkRun> map = new HashMap<>();
    for (Map.Entry<String, BenchmarkRun> entry : runMap.entrySet()) {
      map.put(entry.getKey(), BenchmarkRun.from(entry.getValue()));
    }
    return map;
  }

  private static Collection<String> getNonSuccessfulRuns(Map<String, BenchmarkRun> results) {

    List<String> list = new ArrayList<>();
    for (BenchmarkRun benchmarkRun : results.values()) {

      if (benchmarkRun.isFailed()) {
        for (Entry<RunnerConfig, Result> result : benchmarkRun.getResults().entrySet()) {
          list.add(benchmarkRun.getModuleName() + " " + result.getKey());
        }
      } else {
        for (Entry<RunnerConfig, Result> result : benchmarkRun.getResults().entrySet()) {
          if (result.getValue().getState() != Result.State.DONE) {
            list.add(benchmarkRun.getModuleName() + " " + result.getKey());
          }
        }
      }
    }
    return list;
  }

  private enum Command {
    EXIT, CHECK_FOR_UPDATES, RUN_BENCHMARKS, SUCCESSFUL_RUN, FAILED_RUN
  }

  private enum State {
    IDLE, RUNNING_BENCHMARKS
  }

  private BenchmarkFinder benchmarkFinder;

  private Object benchmarkRunsByNameLock = new Object();

  private Map<String, BenchmarkRun> benchmarkRunsByName = new HashMap<>();

  private BenchmarkWorker.Factory benchmarkWorkerFactory;

  private long currentCommitDateMsEpoch;

  private boolean currentlyRunning = false;

  private BlockingQueue<Command> commands = new LinkedBlockingQueue<>();

  private String currentCommitId;

  private MailReporter errorReporter;

  private Thread eventLoop;

  private String lastSuccessfulCommitId;

  private ExecutorService pool;

  private Provider<ExecutorService> poolProvider;

  private Factory reporterFactory;

  private CliInteractor cliInteractor;

  private State state = State.IDLE;

  private Timer timer;

  private Provider<Timer> timerProvider;

  private boolean useReporter;

  private AtomicInteger workCount = new AtomicInteger();

  @Inject
  public BenchmarkManager(BenchmarkFinder collector,
      BenchmarkWorker.Factory benchmarkWorkerFactory,
      @Named("managerPoolSize") Provider<ExecutorService> poolProvider,
      BenchmarkReporter.Factory reporterFactory,
      @Named("useReporter") boolean useReporter,
      CliInteractor commitReader,
      Provider<Timer> timerProvider,
      MailReporter errorReporter) {
    this.benchmarkFinder = collector;
    this.benchmarkWorkerFactory = benchmarkWorkerFactory;
    this.poolProvider = poolProvider;
    this.reporterFactory = reporterFactory;
    this.useReporter = useReporter;
    this.cliInteractor = commitReader;
    this.errorReporter = errorReporter;
    this.timerProvider = timerProvider;
  }

  public String getLastCommitId() {
    synchronized (benchmarkRunsByNameLock) {
      return lastSuccessfulCommitId;
    }
  }

  public Map<String, BenchmarkRun> getLatestRun() {
    synchronized (benchmarkRunsByNameLock) {
      return getLatestRunSynchronized(benchmarkRunsByName);
    }
  }

  public List<RunnerConfig> getAllRunners() {
    return Arrays.asList(RunnerConfigs.FIREFOX_LINUX);
  }

  public synchronized boolean isRunning() {
    return currentlyRunning;
  }

  public synchronized void start() {

    if (isRunning()) {
      throw new IllegalStateException();
    }
    commands.clear();
    eventLoop = new Thread(new EventLoop());
    eventLoop.start();

    timer = timerProvider.get();
    timer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        commands.add(Command.CHECK_FOR_UPDATES);
      }
    }, TICK_INTERVAL, TICK_INTERVAL);

    currentlyRunning = true;
  }

  public synchronized void stop() {
    if (!isRunning()) {
      throw new IllegalStateException();
    }

    commands.add(Command.EXIT);
    try {
      eventLoop.join();
    } catch (InterruptedException e) {
      // Our framework does not make use of thread.interrupt() so this must mean the JVM is trying
      // to gracefully shut down in response to an external signal. Let it happen.
    }

    timer.cancel();
    commands.clear();

    currentlyRunning = false;
  }

  protected void addBenchmarkRun(BenchmarkRun br) {
    synchronized (benchmarkRunsByNameLock) {
      addBenchmarkRunSynchronized(benchmarkRunsByName, br);
    }
  }

  private BenchmarkRun createBenchmarkRunForModule(String moduleName, String commitId,
      long currentCommitDateMsEpoch) {
    BenchmarkRun br = new BenchmarkRun(moduleName, commitId, currentCommitDateMsEpoch);
    br.addRunner(RunnerConfigs.CHROME_LINUX);
    return br;
  }

  private void maybeReportResults(String commitId, long commitMsEpoch) {
    Map<String, BenchmarkRun> results;
    synchronized (benchmarkRunsByNameLock) {
      results = deepClone(benchmarkRunsByName);
    }

    Collection<String> runs = getNonSuccessfulRuns(results);
    if (!runs.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Failed Benchmarks: \n");
      for (String errorModule : runs) {
        builder.append(String.format("%s \n", errorModule));
      }
      logger.severe(
          String.format("Benchmarks failed executing - stopping system\n%s", builder.toString()));
      reportError("Benchmarks failed executing - stopping system");
      stop();
      return;
    }

    if (!useReporter) {
      commands.add(Command.SUCCESSFUL_RUN);
      return;
    }

    ReportProgressHandler p = new ReportProgressHandler() {

      @Override
      public void onPermanentFailure() {
        reportError("Reporter failed to report results, shutting down the system");
        stop();
      }

      @Override
      public void onCommitReported() {
        commands.add(Command.SUCCESSFUL_RUN);
      }
    };
    new Thread(reporterFactory.create(results, commitId, commitMsEpoch, p)).start();
  }

  private Command getNextCommand() {
    try {
      return commands.take();
    } catch (InterruptedException e) {
      // Our framework does not make use of thread.interrupt() so this must mean the JVM is trying
      // to gracefully shut down in response to an external signal. Let it happen.
      return Command.EXIT;
    }
  }

  private void runEventLoop() {
    state = State.IDLE;
    // check out last successful commit
    try {
      logger.info("Getting last commit");
      setLastCommit(cliInteractor.getLastCommitId());
      currentCommitId = getLastCommitId();
      logger.info(String.format("Last commit was %s", currentCommitId));
      currentCommitDateMsEpoch = cliInteractor.getDateForCommitInMsEpoch(currentCommitId);
      logger.info("Checking out last commit");
      cliInteractor.checkout(getLastCommitId());

    } catch (BenchmarkManagerException e) {
      logger.log(Level.WARNING, "Can not checkout commit - shutting down", e);
      reportError("Can not update git repo");
      new Thread(new Runnable() {

        @Override
        public void run() {
          stop();
        }
      }).start();
      return;
    }

    while (true) {

      Command command = getNextCommand();
      switch (command) {
        case CHECK_FOR_UPDATES:
          // do not check for updates if we are already running a benchmark
          if (state == State.RUNNING_BENCHMARKS) {
            continue;
          }

          boolean hasUpdates = false;
          try {
            cliInteractor.maybeCheckoutNextCommit(getLastCommitId());
            String commitId = cliInteractor.getCurrentCommitId();
            hasUpdates = !currentCommitId.equals(commitId);
            currentCommitId = commitId;

          } catch (BenchmarkManagerException e) {
            logger.log(Level.WARNING, "Can not update repository", e);
            reportError("Can not update git repo");
            break;
          }

          try {
            if (hasUpdates) {
              logger.info(String.format("found a new commit %s", currentCommitId));

              logger.info("Getting its commit date");
              currentCommitDateMsEpoch = cliInteractor.getDateForCommitInMsEpoch(currentCommitId);

              logger.info("Building SDK");
              cliInteractor.buildSDK();
              logger.info("Starting benchmark runners");
              startBenchmarkingAllForCommit(currentCommitId, currentCommitDateMsEpoch);
              state = State.RUNNING_BENCHMARKS;
            }

          } catch (BenchmarkManagerException e) {
            logger.log(Level.WARNING, "Can not build SDK", e);
            reportError("Can not build SDK");
            break;
          }
          break;
        case EXIT:
          if (state == State.RUNNING_BENCHMARKS) {
            try {
              pool.shutdown();
              pool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
          }
          // end this thread
          commands.clear();
          return;

        case RUN_BENCHMARKS:
          if (state == State.RUNNING_BENCHMARKS) {
            logger.warning(
                "asked to run benchmarks, but we are already running them, should not happen");
            continue;
          }
          state = State.RUNNING_BENCHMARKS;
          startBenchmarkingAllForCommit(currentCommitId, currentCommitDateMsEpoch);
          break;

        case SUCCESSFUL_RUN:
          setLastCommit(currentCommitId);
          state = State.IDLE;
          break;
        default:
          logger.severe("Hit default case");
          break;
      }
    }
  }

  private void reportError(String message) {
    errorReporter.sendEmail(message);
  }

  private void setLastCommit(String commitId) {
    synchronized (benchmarkRunsByNameLock) {
      lastSuccessfulCommitId = commitId;
    }
  }

  private void startBenchmarkingAllForCommit(String commitId, long currentCommitDateMsEpoch) {

    pool = poolProvider.get();

    List<String> benchmarkModuleNames = benchmarkFinder.get();

    for (String benchmarkModuleName : benchmarkModuleNames) {

      // we are currently ignoring D8 benchmarks until we have a V8 runner.
      if (benchmarkModuleName.endsWith("D8")) {
        continue;
      }

      BenchmarkRun br = createBenchmarkRunForModule(benchmarkModuleName, commitId,
          currentCommitDateMsEpoch);
      addBenchmarkRun(br);

      ProgressHandler progressHandler = new ThreadSafeProgressHandler(br);

      BenchmarkWorker worker =
          benchmarkWorkerFactory.create(BenchmarkWorkerConfig.from(br), progressHandler);
      workCount.incrementAndGet();

      pool.execute(worker);
    }
  }

  // Visible for testings
  void addBenchmarkRunSynchronized(Map<String, BenchmarkRun> runsByName, BenchmarkRun br) {
    runsByName.put(br.getModuleName(), br);
  }

  // Visible for testings
  Map<String, BenchmarkRun> getLatestRunSynchronized(Map<String, BenchmarkRun> runsByName) {
    return deepClone(runsByName);
  }

  // Visible for testings
  boolean isEventLoopAlive() {
    return eventLoop.isAlive();
  }
}
