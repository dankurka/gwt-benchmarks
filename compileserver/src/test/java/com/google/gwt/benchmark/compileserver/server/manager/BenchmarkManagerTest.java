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

import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.ReportProgressHandler;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkWorker.Factory;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkWorker.ProgressHandler;
import com.google.inject.Provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationWithTimeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Test for {@link BenchmarkManager}.
 */
public class BenchmarkManagerTest {

  private interface Condition {
    boolean condition();
  }

  private static class BenchmarkManagerWithPublicAdd extends BenchmarkManager {
    public BenchmarkManagerWithPublicAdd(BenchmarkFinder collector,
        Factory benchmarkWorkerFactory,
        Provider<ExecutorService> poolProvider,
        com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.Factory reporterFactory,
        boolean useReporter,
        CliInteractor commitReader,
        Provider<Timer> timerProvider,
        MailReporter errorReporter) {
      super(collector,
          benchmarkWorkerFactory,
          poolProvider,
          reporterFactory,
          useReporter,
          commitReader,
          timerProvider,
          errorReporter);
    }

    @Override
    public void addBenchmarkRun(BenchmarkRun br) {
      super.addBenchmarkRun(br);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object a) {
    return (T) a;
  }

  private BenchmarkFinder collector;
  private BenchmarkWorker.Factory benchmarkWorkerFactory;
  private Provider<ExecutorService> poolProvider;
  private Provider<Timer> timerProvider;
  private BenchmarkReporter.Factory reporterFactory;
  private BenchmarkManager manager;
  private CliInteractor commitReader;
  private ThreadPoolExecutor threadPoolExecutor;
  private BenchmarkWorker benchmarkWorker;
  private BenchmarkReporter benchmarkReporter;
  private MailReporter errorReporter;
  private Timer timer;

  @Before
  public void setup() {
    collector = Mockito.mock(BenchmarkFinder.class);
    benchmarkWorkerFactory = Mockito.mock(BenchmarkWorker.Factory.class);
    poolProvider = cast(Mockito.mock(Provider.class));
    reporterFactory = Mockito.mock(BenchmarkReporter.Factory.class);
    commitReader = Mockito.mock(CliInteractor.class);
    timerProvider = cast(Mockito.mock(Provider.class));
    threadPoolExecutor = Mockito.mock(ThreadPoolExecutor.class);
    benchmarkWorker = Mockito.mock(BenchmarkWorker.class);
    benchmarkReporter = Mockito.mock(BenchmarkReporter.class);
    errorReporter = Mockito.mock(MailReporter.class);
    timer = Mockito.mock(Timer.class);
  }

  @Test
  public void testSuccessfulRun() throws BenchmarkManagerException, InterruptedException {

    Mockito.when(timerProvider.get()).thenReturn(timer);

    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.when(commitReader.getLastCommitId()).thenReturn("commit1");
    Mockito.when(commitReader.getCurrentCommitId()).thenReturn("commit2");
    Mockito.when(poolProvider.get()).thenReturn(threadPoolExecutor);
    Mockito.when(collector.get()).thenReturn(Arrays.asList("module1", "module2"));

    ArgumentCaptor<ProgressHandler> progressHandlerCaptor =
        ArgumentCaptor.forClass(ProgressHandler.class);

    ArgumentCaptor<BenchmarkWorkerConfig> workerConfigCapture =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(workerConfigCapture.capture(),
        progressHandlerCaptor.capture())).thenReturn(benchmarkWorker);

    ArgumentCaptor<Map<String, BenchmarkRun>> resultCaptor =
        cast(ArgumentCaptor.forClass(Map.class));

    ArgumentCaptor<ReportProgressHandler> reportProgressHandlerCaptor =
        ArgumentCaptor.forClass(ReportProgressHandler.class);

    Mockito.when(reporterFactory.create(resultCaptor.capture(), Mockito.anyString(),
        reportProgressHandlerCaptor.capture())).thenReturn(benchmarkReporter);

    Assert.assertFalse(manager.isRunning());

    manager.start();

    Assert.assertTrue(manager.isRunning());
    Assert.assertTrue(manager.isEventLoopAlive());

    ArgumentCaptor<TimerTask> captor = ArgumentCaptor.forClass(TimerTask.class);

    Mockito.verify(timer).scheduleAtFixedRate(captor.capture(), Mockito.anyLong(),
        Mockito.anyLong());

    TimerTask timerTask = captor.getValue();
    timerTask.run();

    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(commitReader, timeout).checkout("commit1");
    Mockito.verify(commitReader, timeout).maybeCheckoutNextCommit("commit1");
    Mockito.verify(poolProvider, timeout).get();
    Mockito.verify(collector, timeout).get();
    Mockito.verify(threadPoolExecutor, timeout.times(2)).execute(benchmarkWorker);

    List<ProgressHandler> progressHandlers = progressHandlerCaptor.getAllValues();
    Assert.assertEquals(2, progressHandlers.size());
    List<BenchmarkWorkerConfig> workerConfigs = workerConfigCapture.getAllValues();
    Assert.assertEquals(2, workerConfigs.size());

    // TODO right now there is only one runner, needs updating
    // simulate benchmarks done
    progressHandlers.get(0).onResult(workerConfigs.get(0).getRunners().get(0), 1);
    progressHandlers.get(0).onResult(workerConfigs.get(0).getRunners().get(1), 2);
    progressHandlers.get(0).onResult(workerConfigs.get(0).getRunners().get(2), 3);
    progressHandlers.get(0).onResult(workerConfigs.get(0).getRunners().get(3), 4);
    progressHandlers.get(0).onRunEnded();

    progressHandlers.get(1).onResult(workerConfigs.get(0).getRunners().get(0), 5);
    progressHandlers.get(1).onResult(workerConfigs.get(0).getRunners().get(1), 6);
    progressHandlers.get(1).onResult(workerConfigs.get(0).getRunners().get(2), 7);
    progressHandlers.get(1).onResult(workerConfigs.get(0).getRunners().get(3), 8);
    progressHandlers.get(1).onRunEnded();

    Mockito.verify(benchmarkReporter, timeout).run();

    Map<String, BenchmarkRun> map = resultCaptor.getValue();
    Assert.assertEquals(2, map.size());

    Assert.assertTrue(map.containsKey("module1"));
    Assert.assertTrue(map.containsKey("module2"));

    BenchmarkRun benchmarkRun = map.get("module1");

    Assert.assertEquals("commit2", benchmarkRun.getCommitId());
    Assert.assertEquals("module1", benchmarkRun.getModuleName());
    Assert.assertEquals(BenchmarkRun.State.DONE, benchmarkRun.getState());
    Assert.assertEquals(1,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(0)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(2,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(1)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(3,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(2)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(4,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(3)).getRunsPerSecond(),
        0.0001);

    benchmarkRun = map.get("module2");
    Assert.assertEquals("commit2", benchmarkRun.getCommitId());
    Assert.assertEquals("module2", benchmarkRun.getModuleName());
    Assert.assertEquals(BenchmarkRun.State.DONE, benchmarkRun.getState());
    Assert.assertEquals(5,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(0)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(6,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(1)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(7,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(2)).getRunsPerSecond(),
        0.0001);
    Assert.assertEquals(8,
        benchmarkRun.getResults().get(benchmarkRun.getRunConfigs().get(3)).getRunsPerSecond(),
        0.0001);

    // tell benchmark manager that we reported successfully
    ReportProgressHandler progressHandler = reportProgressHandlerCaptor.getValue();
    progressHandler.onCommitReported();

    // wait for benchmark manager to change to the next commit
    waitFor(new Condition() {
      @Override
      public boolean condition() {
        return manager.getLastCommitId().equals("commit2");
      }
    }, 200);

    Assert.assertTrue(manager.isRunning());
    Assert.assertTrue(manager.isEventLoopAlive());

    manager.stop();

    Assert.assertFalse(manager.isRunning());

    waitFor(new Condition() {
      @Override
      public boolean condition() {
        return !manager.isEventLoopAlive();
      }
    }, 200);
  }

  @Test
  public void testEventLoopFailsOnStartupWhileGettingCommitId() throws BenchmarkManagerException,
      InterruptedException {
    Mockito.when(timerProvider.get()).thenReturn(timer);
    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.when(commitReader.getLastCommitId()).thenThrow(new BenchmarkManagerException(""));

    Assert.assertFalse(manager.isRunning());
    manager.start();
    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(errorReporter, timeout).sendEmail("Can not update git repo");
    waitFor(new Condition() {

      @Override
      public boolean condition() {
        return !manager.isRunning();
      }
    }, 200);
    Assert.assertFalse(manager.isRunning());
    Assert.assertFalse(manager.isEventLoopAlive());
  }

  @Test
  public void testEventLoopFailsOnStartupWhileGettingCommitDate() throws BenchmarkManagerException,
      InterruptedException {
    Mockito.when(timerProvider.get()).thenReturn(timer);
    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.when(commitReader.getDateForCommitInMsEpoch(Mockito.anyString())).thenThrow(
        new BenchmarkManagerException(""));

    Assert.assertFalse(manager.isRunning());
    manager.start();
    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(errorReporter, timeout).sendEmail("Can not update git repo");
    waitFor(new Condition() {

      @Override
      public boolean condition() {
        return !manager.isRunning();
      }
    }, 200);
    Assert.assertFalse(manager.isRunning());
    Assert.assertFalse(manager.isEventLoopAlive());
  }

  @Test
  public void testEventLoopFailsOnStartupWhileCheckingOutCurrentCommit()
      throws BenchmarkManagerException, InterruptedException {
    Mockito.when(timerProvider.get()).thenReturn(timer);
    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.doThrow(new BenchmarkManagerException("")).when(commitReader)
        .checkout(Mockito.anyString());

    Assert.assertFalse(manager.isRunning());
    manager.start();
    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(errorReporter, timeout).sendEmail("Can not update git repo");
    waitFor(new Condition() {

      @Override
      public boolean condition() {
        return !manager.isRunning();
      }
    }, 200);
    Assert.assertFalse(manager.isRunning());
    Assert.assertFalse(manager.isEventLoopAlive());
  }

  @Test
  public void testFailedToCompileBenchmark() throws BenchmarkManagerException,
      InterruptedException {

    Mockito.when(timerProvider.get()).thenReturn(timer);

    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.when(commitReader.getLastCommitId()).thenReturn("commit1");
    Mockito.when(commitReader.getCurrentCommitId()).thenReturn("commit2");
    Mockito.when(poolProvider.get()).thenReturn(threadPoolExecutor);
    Mockito.when(collector.get()).thenReturn(Arrays.asList("module1", "module2"));

    ArgumentCaptor<ProgressHandler> progressHandlerCaptor =
        ArgumentCaptor.forClass(ProgressHandler.class);

    ArgumentCaptor<BenchmarkWorkerConfig> benchmarkWorkerConfigCaptor =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(benchmarkWorkerConfigCaptor.capture(),
        progressHandlerCaptor.capture())).thenReturn(benchmarkWorker);

    ArgumentCaptor<Map<String, BenchmarkRun>> resultCaptor =
        cast(ArgumentCaptor.forClass(Map.class));

    ArgumentCaptor<ReportProgressHandler> reportProgressHandlerCaptor =
        ArgumentCaptor.forClass(ReportProgressHandler.class);

    Mockito.when(reporterFactory.create(resultCaptor.capture(), Mockito.anyString(),
        reportProgressHandlerCaptor.capture())).thenReturn(benchmarkReporter);

    Assert.assertFalse(manager.isRunning());

    manager.start();

    Assert.assertTrue(manager.isRunning());
    Assert.assertTrue(manager.isEventLoopAlive());

    ArgumentCaptor<TimerTask> captor = ArgumentCaptor.forClass(TimerTask.class);

    Mockito.verify(timer).scheduleAtFixedRate(captor.capture(), Mockito.anyLong(),
        Mockito.anyLong());

    TimerTask timerTask = captor.getValue();
    timerTask.run();

    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(commitReader, timeout).checkout("commit1");
    Mockito.verify(commitReader, timeout).maybeCheckoutNextCommit("commit1");
    Mockito.verify(poolProvider, timeout).get();
    Mockito.verify(collector, timeout).get();
    Mockito.verify(threadPoolExecutor, timeout.times(2)).execute(benchmarkWorker);

    List<ProgressHandler> progressHandlers = progressHandlerCaptor.getAllValues();
    Assert.assertEquals(2, progressHandlers.size());
    List<BenchmarkWorkerConfig> workerConfigs = benchmarkWorkerConfigCaptor.getAllValues();
    Assert.assertEquals(2, workerConfigs.size());

    // TODO right now there is only one runner, needs updating
    // simulate benchmarks done
    progressHandlers.get(0).onResult(workerConfigs.get(0).getRunners().get(0), 1);
    progressHandlers.get(0).onCompilationFailed("bad module1");
    progressHandlers.get(0).onRunEnded();
    progressHandlers.get(1).onResult(workerConfigs.get(0).getRunners().get(0), 2);
    progressHandlers.get(1).onCompilationFailed("bad module2");
    progressHandlers.get(1).onRunEnded();

    Mockito.verifyZeroInteractions(benchmarkReporter);

    Map<String, BenchmarkRun> map = manager.getLatestRun();
    Assert.assertEquals(2, map.size());

    Assert.assertTrue(map.containsKey("module1"));
    Assert.assertTrue(map.containsKey("module2"));

    BenchmarkRun benchmarkRun = map.get("module1");
    Assert.assertEquals(BenchmarkRun.State.FAILED_COMPILE, benchmarkRun.getState());
    benchmarkRun = map.get("module2");
    Assert.assertEquals(BenchmarkRun.State.FAILED_COMPILE, benchmarkRun.getState());

    Mockito.verify(errorReporter).sendEmail(Mockito.anyString());

    waitFor(new Condition() {

      @Override
      public boolean condition() {
        return !manager.isRunning();
      }
    }, 200);
    Assert.assertFalse(manager.isRunning());
    Assert.assertFalse(manager.isEventLoopAlive());
  }

  @Test
  public void testOneFailedRunner() throws BenchmarkManagerException, InterruptedException {

    Mockito.when(timerProvider.get()).thenReturn(timer);

    manager = new BenchmarkManager(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter);

    Mockito.when(commitReader.getLastCommitId()).thenReturn("commit1");
    Mockito.when(commitReader.getCurrentCommitId()).thenReturn("commit2");
    Mockito.when(poolProvider.get()).thenReturn(threadPoolExecutor);
    Mockito.when(collector.get()).thenReturn(Arrays.asList("module1", "module2"));

    ArgumentCaptor<ProgressHandler> progressHandlerCaptor =
        ArgumentCaptor.forClass(ProgressHandler.class);

    ArgumentCaptor<BenchmarkWorkerConfig> workerConfigCaptor =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(workerConfigCaptor.capture(),
        progressHandlerCaptor.capture())).thenReturn(benchmarkWorker);

    ArgumentCaptor<Map<String, BenchmarkRun>> resultCaptor =
        cast(ArgumentCaptor.forClass(Map.class));

    ArgumentCaptor<ReportProgressHandler> reportProgressHandlerCaptor =
        ArgumentCaptor.forClass(ReportProgressHandler.class);

    Mockito.when(reporterFactory.create(resultCaptor.capture(), Mockito.anyString(),
        reportProgressHandlerCaptor.capture())).thenReturn(benchmarkReporter);

    Assert.assertFalse(manager.isRunning());

    manager.start();

    Assert.assertTrue(manager.isRunning());
    Assert.assertTrue(manager.isEventLoopAlive());

    ArgumentCaptor<TimerTask> captor = ArgumentCaptor.forClass(TimerTask.class);

    Mockito.verify(timer).scheduleAtFixedRate(captor.capture(), Mockito.anyLong(),
        Mockito.anyLong());

    TimerTask timerTask = captor.getValue();
    timerTask.run();

    VerificationWithTimeout timeout = Mockito.timeout(200);
    Mockito.verify(commitReader, timeout).checkout("commit1");
    Mockito.verify(commitReader, timeout).maybeCheckoutNextCommit("commit1");
    Mockito.verify(poolProvider, timeout).get();
    Mockito.verify(collector, timeout).get();
    Mockito.verify(threadPoolExecutor, timeout.times(2)).execute(benchmarkWorker);

    List<ProgressHandler> progressHandlers = progressHandlerCaptor.getAllValues();
    Assert.assertEquals(2, progressHandlers.size());
    List<BenchmarkWorkerConfig> workerConfig = workerConfigCaptor.getAllValues();
    Assert.assertEquals(2, workerConfig.size());

    // TODO right now there is only one runner, needs updating
    // simulate benchmarks done
    progressHandlers.get(0).onResult(workerConfig.get(0).getRunners().get(0), 1);
    progressHandlers.get(0).failedToRunBenchmark(workerConfig.get(0).getRunners().get(0),
        "testerror");
    progressHandlers.get(0).onRunEnded();
    progressHandlers.get(1).onResult(workerConfig.get(0).getRunners().get(0), 2);
    progressHandlers.get(1).onResult(workerConfig.get(1).getRunners().get(0), 23);
    progressHandlers.get(1).onRunEnded();

    Mockito.verifyZeroInteractions(benchmarkReporter);

    Map<String, BenchmarkRun> map = manager.getLatestRun();
    Assert.assertEquals(2, map.size());

    Assert.assertTrue(map.containsKey("module1"));
    Assert.assertTrue(map.containsKey("module2"));

    BenchmarkRun benchmarkRun = map.get("module1");
    Assert.assertEquals(BenchmarkRun.State.FAILED_TO_RUN_ON_RUNNER, benchmarkRun.getState());
    benchmarkRun = map.get("module2");
    Assert.assertEquals(BenchmarkRun.State.DONE, benchmarkRun.getState());

    Mockito.verify(errorReporter).sendEmail(Mockito.anyString());

    waitFor(new Condition() {

      @Override
      public boolean condition() {
        return !manager.isRunning();
      }
    }, 200);
    Assert.assertFalse(manager.isRunning());
    Assert.assertFalse(manager.isEventLoopAlive());
  }

  @Test
  public void testMapIsLocked() throws InterruptedException {

    final List<String> list = new ArrayList<>();

    final BenchmarkManagerWithPublicAdd manager = new BenchmarkManagerWithPublicAdd(collector,
        benchmarkWorkerFactory,
        poolProvider,
        reporterFactory,
        true,
        commitReader,
        timerProvider,
        errorReporter) {

      @Override
      void addBenchmarkRunSynchronized(Map<String, BenchmarkRun> runsByName, BenchmarkRun br) {
        try {
          Thread.sleep(40);
        } catch (InterruptedException ignored) {
        }
        list.add("add");
      }

      @Override
      Map<String, BenchmarkRun> getLatestRunSynchronized(Map<String, BenchmarkRun> runsByName) {
        list.add("get");
        return null;
      }
    };

    Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        manager.addBenchmarkRun(null);
      }
    });

    thread.start();

    Thread.sleep(50);
    manager.getLatestRun();

    Assert.assertEquals(2, list.size());
    Assert.assertEquals("add", list.get(0));
    Assert.assertEquals("get", list.get(1));
  }

  private void waitFor(Condition c, long timeout) throws InterruptedException {
    long endMs = System.currentTimeMillis() + timeout;
    while (!c.condition()) {
      if (System.currentTimeMillis() > endMs) {
        throw new RuntimeException("timeout while waiting for condition");
      }
      Thread.sleep(5);
    }
  }
}
