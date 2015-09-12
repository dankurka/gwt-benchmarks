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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.j2cl.benchmark.cli.BenchmarkWorker.WorkResult;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;

/**
 * Test for {@link Manager}.
 */
public class ManagerTest {

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object a) {
    return (T) a;
  }

  private BenchmarkFinder collector;
  private BenchmarkWorker.Factory benchmarkWorkerFactory;
  private Provider<ExecutorService> poolProvider;
  private BenchmarkReporter.Factory reporterFactory;
  private Manager manager;
  private CliInteractor commitReader;
  private ThreadPoolExecutor threadPoolExecutor;
  private BenchmarkWorker benchmarkWorker;
  private BenchmarkReporter benchmarkReporter;
  private MailReporter errorReporter;
  private File devJar;
  private File userJar;

  @Before
  public void setup() {
    collector = Mockito.mock(BenchmarkFinder.class);
    benchmarkWorkerFactory = Mockito.mock(BenchmarkWorker.Factory.class);
    poolProvider = cast(Mockito.mock(Provider.class));
    reporterFactory = Mockito.mock(BenchmarkReporter.Factory.class);
    commitReader = Mockito.mock(CliInteractor.class);
    threadPoolExecutor = Mockito.mock(ThreadPoolExecutor.class);
    benchmarkWorker = Mockito.mock(BenchmarkWorker.class);
    benchmarkReporter = Mockito.mock(BenchmarkReporter.class);
    errorReporter = Mockito.mock(MailReporter.class);
    devJar = mock(File.class);
    userJar = mock(File.class);

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSuccessfulDeamonRun() throws CliException, InterruptedException, ExecutionException {

    final List<String> filterInput = Lists.newArrayList();

    Predicate<String> filterPredicate = new Predicate<String>() {
        @Override
      public boolean apply(@Nullable String input) {
        filterInput.add(input);
        if (input.equals("ignoredBenchmark")) {
          return false;
        }
        return true;
      }
    };

    manager = new Manager(collector, benchmarkWorkerFactory, poolProvider, reporterFactory, true,
        commitReader, errorReporter, filterPredicate, true, false, devJar, userJar, "", false) {
      @Override
      void sleep(long timeInMs) throws InterruptedException {
        // we use this to end the test!
        throw new InterruptedException("endtest");
      }

      @Override
      void sleepWaitingForJobs() throws InterruptedException {
      }
    };

    when(commitReader.getLastCommitId()).thenReturn("commit1");
    when(commitReader.getCurrentCommitId()).thenReturn("commit2");
    when(poolProvider.get()).thenReturn(threadPoolExecutor);
    when(collector.get()).thenReturn(Arrays.asList("module1", "module2", "ignoredBenchmark"));

    ArgumentCaptor<BenchmarkWorkerConfig> workerConfigCapture =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(workerConfigCapture.capture())).thenReturn(
        benchmarkWorker);

    ArgumentCaptor<List<BenchmarkRun>> resultCaptor = cast(ArgumentCaptor.forClass(List.class));

    when(reporterFactory.create(resultCaptor.capture(), eq("commit2"))).thenReturn(
        benchmarkReporter);


    Future<WorkResult> workResultFuture1 = cast(mock(Future.class));
    Future<WorkResult> workResultFuture2 = cast(mock(Future.class));
    Future<WorkResult> workResultFuture3 = cast(mock(Future.class));
    Future<WorkResult> workResultFuture4 = cast(mock(Future.class));

    when(workResultFuture1.isDone()).thenReturn(false, true);
    when(workResultFuture2.isDone()).thenReturn(false, true);
    when(workResultFuture3.isDone()).thenReturn(false, true);
    when(workResultFuture4.isDone()).thenReturn(false, true);

    Job job1 = new Job(new JobId("jobId1"), RunnerConfigs.getAllRunners(), 1);
    job1.addResult(RunnerConfigs.CHROME_LINUX, 1);
    job1.addResult(RunnerConfigs.FIREFOX_LINUX, 2);
    job1.addResult(RunnerConfigs.IE11_WIN, 3);
    WorkResult workResult1 = new WorkResult(job1);
    when(workResultFuture1.get()).thenReturn(workResult1);

    Job job2 = new Job(new JobId("jobId2"), RunnerConfigs.getAllRunners(), 2);
    job2.addResult(RunnerConfigs.CHROME_LINUX, 4);
    job2.addResult(RunnerConfigs.FIREFOX_LINUX,5);
    job2.addResult(RunnerConfigs.IE11_WIN, 6);
    WorkResult workResult2 = new WorkResult(job2);
    when(workResultFuture2.get()).thenReturn(workResult2);

    Job job3 = new Job(new JobId("jobId3"), RunnerConfigs.getAllRunners(), 3);
    job3.addResult(RunnerConfigs.CHROME_LINUX, 7);
    job3.addResult(RunnerConfigs.FIREFOX_LINUX, 8);
    job3.addResult(RunnerConfigs.IE11_WIN, 9);
    WorkResult workResult3 = new WorkResult(job3);
    when(workResultFuture3.get()).thenReturn(workResult3);

    Job job4 = new Job(new JobId("jobId4"), RunnerConfigs.getAllRunners(), 4);
    job4.addResult(RunnerConfigs.CHROME_LINUX, 10);
    job4.addResult(RunnerConfigs.FIREFOX_LINUX, 11);
    job4.addResult(RunnerConfigs.IE11_WIN, 12);
    WorkResult workResult4 = new WorkResult(job4);
    when(workResultFuture4.get()).thenReturn(workResult4);

    when(threadPoolExecutor.submit(benchmarkWorker))
        .thenReturn(workResultFuture1, workResultFuture2, workResultFuture3, workResultFuture4);

    when(benchmarkReporter.report()).thenReturn(true);

    try {
      manager.execute();
    } catch (InterruptedException e) {
      // make sure we throw the thing
      assertThat(e.getMessage()).isEqualTo("endtest");
    }

    assertThat(filterInput).containsExactly("module1", "module2", "ignoredBenchmark");

    verify(commitReader).checkout("commit1");
    verify(commitReader).maybeCheckoutNextCommit("commit1");
    verify(poolProvider).get();
    verify(collector).get();
    verify(threadPoolExecutor, times(4)).submit(benchmarkWorker);

    List<BenchmarkWorkerConfig> workerConfigs = workerConfigCapture.getAllValues();
    assertThat(workerConfigs.size()).isEqualTo(4);

    assertBenchmarkWorkerConfig(workerConfigs.get(0), "module1");
    assertBenchmarkWorkerConfig(workerConfigs.get(1), "module1");

    assertBenchmarkWorkerConfig(workerConfigs.get(2), "module2");
    assertBenchmarkWorkerConfig(workerConfigs.get(3), "module2");

    verify(benchmarkReporter).report();

    List<BenchmarkRun> capturedBenchmarkResults = resultCaptor.getValue();

    assertThat(capturedBenchmarkResults.size()).isEqualTo(4);

    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(0), RunnerConfigs.CHROME_LINUX, 1.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(0), RunnerConfigs.FIREFOX_LINUX, 2.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(0), RunnerConfigs.IE11_WIN, 3.0);

    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(1), RunnerConfigs.CHROME_LINUX, 4.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(1), RunnerConfigs.FIREFOX_LINUX, 5.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(1), RunnerConfigs.IE11_WIN, 6.0);

    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(2), RunnerConfigs.CHROME_LINUX, 7.0);
    assertSuccessfulBenchmarkRun(
        capturedBenchmarkResults.get(2), RunnerConfigs.FIREFOX_LINUX, 8.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(2), RunnerConfigs.IE11_WIN, 9.0);

    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(3), RunnerConfigs.CHROME_LINUX, 10.0);
    assertSuccessfulBenchmarkRun(
        capturedBenchmarkResults.get(3), RunnerConfigs.FIREFOX_LINUX, 11.0);
    assertSuccessfulBenchmarkRun(capturedBenchmarkResults.get(3), RunnerConfigs.IE11_WIN, 12.0);

    verifyZeroInteractions(errorReporter);
  }

  private void assertSuccessfulBenchmarkRun(
      BenchmarkRun benchmarkRun, RunnerConfig config, double result) {
    assertThat(benchmarkRun.isFailed()).isFalse();
    assertThat(
        benchmarkRun.getResults().get(config).getState())
        .isEqualTo(BenchmarkRun.Result.State.SUCCESSFUL_RUN);
    assertThat(benchmarkRun.getResults().get(config)
        .getRunsPerSecond()).isEqualTo(result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFailingDeamonRun() throws CliException, InterruptedException, ExecutionException {
    final List<String> filterInput = Lists.newArrayList();

    Predicate<String> filterPredicate = new Predicate<String>() {
        @Override
      public boolean apply(@Nullable String input) {
        filterInput.add(input);
        if (input.equals("ignoredBenchmark")) {
          return false;
        }
        return true;
      }
    };

    manager = new Manager(collector, benchmarkWorkerFactory, poolProvider, reporterFactory, true,
        commitReader, errorReporter, filterPredicate, true, false, devJar, userJar, "", false) {
      @Override
      void sleep(long timeInMs) throws InterruptedException {
        // we use this to end the test!
        throw new InterruptedException("endtest");
      }

      @Override
      void sleepWaitingForJobs() throws InterruptedException {
      }
    };

    when(commitReader.getLastCommitId()).thenReturn("commit1");
    when(commitReader.getCurrentCommitId()).thenReturn("commit2");
    when(poolProvider.get()).thenReturn(threadPoolExecutor);
    when(collector.get()).thenReturn(Arrays.asList("module1", "module2", "ignoredBenchmark"));

    ArgumentCaptor<BenchmarkWorkerConfig> workerConfigCapture =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(workerConfigCapture.capture())).thenReturn(
        benchmarkWorker);

    ArgumentCaptor<List<BenchmarkRun>> resultCaptor = cast(ArgumentCaptor.forClass(List.class));

    when(reporterFactory.create(resultCaptor.capture(), eq("commit2"))).thenReturn(
        benchmarkReporter);


    Future<WorkResult> workResultFuture1 = cast(mock(Future.class));
    Future<WorkResult> workResultFuture2 = cast(mock(Future.class));

    when(workResultFuture1.isDone()).thenReturn(false, true);
    when(workResultFuture2.isDone()).thenReturn(false, true);

    Job job1 = new Job(new JobId("jobId1"), RunnerConfigs.getAllRunners(), 1);
    job1.addResult(RunnerConfigs.CHROME_LINUX, 1);
    job1.addResult(RunnerConfigs.FIREFOX_LINUX, 2);
    job1.addResult(RunnerConfigs.IE11_WIN, 3);
    WorkResult workResult1 = new WorkResult(job1);
    when(workResultFuture1.get()).thenReturn(workResult1);
    Job job2 = new Job(new JobId("jobId2"), RunnerConfigs.getAllRunners(), 2);
    job2.setRunFailed(RunnerConfigs.CHROME_LINUX, "just testing");
    job2.addResult(RunnerConfigs.FIREFOX_LINUX,5);
    job2.addResult(RunnerConfigs.IE11_WIN, 6);
    WorkResult workResult2 = new WorkResult(job2);
    when(workResultFuture2.get()).thenReturn(workResult2);

    when(threadPoolExecutor.submit(benchmarkWorker)).thenReturn(workResultFuture1, workResultFuture2);

    when(benchmarkReporter.report()).thenReturn(true);

    try {
      manager.execute();
    } catch (InterruptedException e) {
      // make sure we throw the thing
      assertThat(e.getMessage()).isEqualTo("endtest");
    }

    assertThat(filterInput).containsExactly("module1", "module2", "ignoredBenchmark");

    verify(commitReader).checkout("commit1");
    verify(commitReader).maybeCheckoutNextCommit("commit1");
    verify(poolProvider).get();
    verify(collector).get();
    verify(threadPoolExecutor, times(4)).submit(benchmarkWorker);

    List<BenchmarkWorkerConfig> workerConfigs = workerConfigCapture.getAllValues();
    assertThat(workerConfigs.size()).isEqualTo(4);

    assertBenchmarkWorkerConfig(workerConfigs.get(0), "module1");
    assertBenchmarkWorkerConfig(workerConfigs.get(1), "module1");

    assertBenchmarkWorkerConfig(workerConfigs.get(2), "module2");
    assertBenchmarkWorkerConfig(workerConfigs.get(3), "module2");

    verifyZeroInteractions(benchmarkReporter);

    ArgumentCaptor<String> argCaptorEmail = ArgumentCaptor.forClass(String.class);
    verify(errorReporter).sendEmail(argCaptorEmail.capture());

    System.out.println(argCaptorEmail.getValue());

    assertThat(argCaptorEmail.getValue()).isEqualTo(
        "Benchmarks failed executing - stopping system\n" +
        "\n" +
        "Failed Benchmarks: \n" +
        "module1 linux chrome FullOptimized \n" +
        "module2 linux chrome Normal \n" +
        "module2 linux chrome FullOptimized \n" +
        "");
  }

  private void assertBenchmarkWorkerConfig(BenchmarkWorkerConfig config, String module) {
    assertThat(config.getModuleName()).isEqualTo(module);
    assertThat(config.getDevJar()).isSameAs(devJar);
    assertThat(config.getUserJar()).isSameAs(userJar);
    assertThat(config.getRunners()).containsExactly(
        RunnerConfigs.getAllRunners().toArray());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRunInSingleMode() throws InterruptedException, ExecutionException {
    final List<String> filterInput = Lists.newArrayList();

    Predicate<String> filterPredicate = new Predicate<String>() {
        @Override
      public boolean apply(@Nullable String input) {
        filterInput.add(input);
        if (input.equals("ignoredBenchmark")) {
          return false;
        }
        return true;
      }
    };

    when(poolProvider.get()).thenReturn(threadPoolExecutor);
    when(collector.get()).thenReturn(Arrays.asList("module1", "module2", "ignoredBenchmark"));

    ArgumentCaptor<BenchmarkWorkerConfig> workerConfigCapture =
        ArgumentCaptor.forClass(BenchmarkWorkerConfig.class);

    Mockito.when(benchmarkWorkerFactory.create(workerConfigCapture.capture())).thenReturn(
        benchmarkWorker);

    ArgumentCaptor<List<BenchmarkRun>> resultCaptor = cast(ArgumentCaptor.forClass(List.class));

    when(reporterFactory.create(resultCaptor.capture(), eq("commit2"))).thenReturn(
        benchmarkReporter);


    Future<WorkResult> workResultFuture1 = cast(mock(Future.class));
    Future<WorkResult> workResultFuture2 = cast(mock(Future.class));

    when(workResultFuture1.isDone()).thenReturn(false, true);
    when(workResultFuture2.isDone()).thenReturn(false, true);

    Job job1 = new Job(new JobId("jobId1"), RunnerConfigs.getAllRunners(), 1);
    job1.addResult(RunnerConfigs.CHROME_LINUX, 1);
    job1.addResult(RunnerConfigs.FIREFOX_LINUX, 2);
    job1.addResult(RunnerConfigs.IE11_WIN, 3);
    WorkResult workResult1 = new WorkResult(job1);
    when(workResultFuture1.get()).thenReturn(workResult1);
    Job job2 = new Job(new JobId("jobId2"), RunnerConfigs.getAllRunners(), 2);
    job2.addResult(RunnerConfigs.CHROME_LINUX, 4);
    job2.addResult(RunnerConfigs.FIREFOX_LINUX,5);
    job2.addResult(RunnerConfigs.IE11_WIN, 6);
    WorkResult workResult2 = new WorkResult(job2);
    when(workResultFuture2.get()).thenReturn(workResult2);

    when(threadPoolExecutor.submit(benchmarkWorker)).thenReturn(workResultFuture1, workResultFuture2);

    manager = new Manager(collector, benchmarkWorkerFactory, poolProvider, reporterFactory, false,
        commitReader, errorReporter, filterPredicate, false, false, devJar, userJar, "", false) {

        @Override
      void sleepWaitingForJobs() throws InterruptedException {
      }

        @Override
      void printOutput(String output) {
        assertThat(output).isEqualTo("Results:\n" + "  module1\n"
            + "    linux firefox: 2.000000 runs/second\n"
            + "    linux chrome: 1.000000 runs/second\n"
            + "    windows ie IE11: 3.000000 runs/second\n" + "  module2\n"
            + "    linux firefox: 5.000000 runs/second\n"
            + "    linux chrome: 4.000000 runs/second\n"
            + "    windows ie IE11: 6.000000 runs/second\n");
      }
    };

    manager.execute();

    verifyZeroInteractions(errorReporter);
    verifyZeroInteractions(benchmarkReporter);

  }
}
