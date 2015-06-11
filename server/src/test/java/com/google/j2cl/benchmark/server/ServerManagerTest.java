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
package com.google.j2cl.benchmark.server;

import static com.google.common.truth.Truth.assertThat;

import com.google.inject.Provider;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.JobResult;
import com.google.j2cl.benchmark.common.runner.Runner;
import com.google.j2cl.benchmark.common.runner.Runner.Factory;
import com.google.j2cl.benchmark.server.JobNotFoundException;
import com.google.j2cl.benchmark.server.ServerManager;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test for {@link ServerManager}.
 */
public class ServerManagerTest {

  private ServerManager manager;
  private Provider<Long> timeProvider;
  private Provider<String> randomStringProvider;
  private ExecutorService executorService;
  private Factory runnerFactory;
  private AtomicBoolean stopSleeping;
  private AtomicBoolean fileShouldHaveBeenDeleted;
  private File extractDir;

  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    randomStringProvider = mock(Provider.class);

    executorService = mock(ExecutorService.class);
    timeProvider = mock(Provider.class);
    runnerFactory = mock(Runner.Factory.class);
    stopSleeping = new AtomicBoolean(false);
    fileShouldHaveBeenDeleted = new AtomicBoolean(false);
    extractDir = new File("target/");

  }

  @Test
  public void testSuccessfulRun() throws JobNotFoundException {

    manager = new ServerManager(randomStringProvider, runnerFactory, executorService, "myhost",
        7777, timeProvider, extractDir);

    when(randomStringProvider.get()).thenReturn("r1", "r2");

    InputStream benchmarkZip = this.getClass().getResourceAsStream("benchmark.zip");

    when(timeProvider.get()).thenReturn(0l, 1l, 2l);

    Runner chromeRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.CHROME_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(chromeRunner);
    when(chromeRunner.isDone()).thenReturn(true);
    when(chromeRunner.isFailed()).thenReturn(false);
    when(chromeRunner.getResult()).thenReturn(2.0);

    Runner ffRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.FIREFOX_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(ffRunner);
    when(ffRunner.isDone()).thenReturn(true);
    when(ffRunner.isFailed()).thenReturn(false);
    when(ffRunner.getResult()).thenReturn(3.0);

    JobId jobId = manager.submitJob(benchmarkZip,
        Arrays.asList(RunnerConfigs.CHROME_LINUX, RunnerConfigs.FIREFOX_LINUX));

    ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);

    verify(executorService, times(2)).submit(argumentCaptor.capture());

    List<Runnable> allRunnables = argumentCaptor.getAllValues();
    assertThat(allRunnables.size()).isEqualTo(2);

    // execute chrome
    allRunnables.get(0).run();

    Job job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.RUNNING);
    JobResult jobResultChrome = job.getResult(RunnerConfigs.CHROME_LINUX);
    assertThat(jobResultChrome).isNotNull();
    assertThat(jobResultChrome.isRan()).isTrue();
    assertThat(jobResultChrome.isSucceded()).isTrue();
    assertThat(jobResultChrome.getResult()).isLessThan(2.00001);
    assertThat(jobResultChrome.getResult()).isGreaterThan(1.99999);

    // execute firefox
    allRunnables.get(1).run();

    job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.FINISHED);
    JobResult jobResultFF = job.getResult(RunnerConfigs.FIREFOX_LINUX);
    assertThat(jobResultFF).isNotNull();
    assertThat(jobResultFF.isRan()).isTrue();
    assertThat(jobResultFF.isSucceded()).isTrue();
    assertThat(jobResultFF.getResult()).isLessThan(3.00001);
    assertThat(jobResultFF.getResult()).isGreaterThan(2.99999);

    FileUtils.deleteQuietly(job.getFolder());
  }

  @Test
  public void testFailingRun() throws JobNotFoundException {

    manager = new ServerManager(randomStringProvider, runnerFactory, executorService, "myhost",
        7777, timeProvider, extractDir);

    when(randomStringProvider.get()).thenReturn("r1", "r2");

    InputStream benchmarkZip = this.getClass().getResourceAsStream("benchmark.zip");

    when(timeProvider.get()).thenReturn(0l, 1l, 2l);

    Runner chromeRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.CHROME_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(chromeRunner);
    when(chromeRunner.isDone()).thenReturn(true);
    when(chromeRunner.isFailed()).thenReturn(true);
    when(chromeRunner.getErrorMessage()).thenReturn("error message");

    Runner ffRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.FIREFOX_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(ffRunner);
    when(ffRunner.isDone()).thenReturn(true);
    when(ffRunner.isFailed()).thenReturn(false);
    when(ffRunner.getResult()).thenReturn(3.0);

    JobId jobId = manager.submitJob(benchmarkZip,
        Arrays.asList(RunnerConfigs.CHROME_LINUX, RunnerConfigs.FIREFOX_LINUX));

    ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);

    verify(executorService, times(2)).submit(argumentCaptor.capture());

    List<Runnable> allRunnables = argumentCaptor.getAllValues();
    assertThat(allRunnables.size()).isEqualTo(2);

    // execute chrome
    allRunnables.get(0).run();

    Job job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.RUNNING);
    JobResult jobResultChrome = job.getResult(RunnerConfigs.CHROME_LINUX);
    assertThat(jobResultChrome).isNotNull();
    assertThat(jobResultChrome.isRan()).isTrue();
    assertThat(jobResultChrome.isSucceded()).isFalse();
    assertThat(jobResultChrome.getErrorMessage()).isEqualTo("error message");

    // execute firefox
    allRunnables.get(1).run();

    job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.FAILED);
    JobResult jobResultFF = job.getResult(RunnerConfigs.FIREFOX_LINUX);
    assertThat(jobResultFF).isNotNull();
    assertThat(jobResultFF.isRan()).isTrue();
    assertThat(jobResultFF.isSucceded()).isTrue();
    assertThat(jobResultFF.getResult()).isLessThan(3.00001);
    assertThat(jobResultFF.getResult()).isGreaterThan(2.99999);

    FileUtils.deleteQuietly(job.getFolder());
  }

  @Test
  public void testCleanup() throws InterruptedException, JobNotFoundException {

    manager = new ServerManager(randomStringProvider, runnerFactory, executorService, "myhost",
        7777, timeProvider, extractDir) {
        @Override
      void sleep(long timeInMs) throws InterruptedException {
        while (!stopSleeping.get()) {
          Thread.sleep(100l);
        }
      }

        @Override
      synchronized void cleanup() {
        super.cleanup();
        fileShouldHaveBeenDeleted.set(true);
      }

    };

    when(randomStringProvider.get()).thenReturn("r1", "r2");

    InputStream benchmarkZip = this.getClass().getResourceAsStream("benchmark.zip");

    when(timeProvider.get()).thenReturn(0l, 1l, 2l);

    Runner chromeRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.CHROME_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(chromeRunner);
    when(chromeRunner.isDone()).thenReturn(true);
    when(chromeRunner.isFailed()).thenReturn(true);
    when(chromeRunner.getErrorMessage()).thenReturn("error message");

    Runner ffRunner = mock(Runner.class);
    when(runnerFactory.create(RunnerConfigs.FIREFOX_LINUX,
        "http://myhost:7777/__bench/r1/index.html")).thenReturn(ffRunner);
    when(ffRunner.isDone()).thenReturn(true);
    when(ffRunner.isFailed()).thenReturn(false);
    when(ffRunner.getResult()).thenReturn(3.0);

    JobId jobId = manager.submitJob(benchmarkZip,
        Arrays.asList(RunnerConfigs.CHROME_LINUX, RunnerConfigs.FIREFOX_LINUX));

    ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);

    verify(executorService, times(2)).submit(argumentCaptor.capture());

    List<Runnable> allRunnables = argumentCaptor.getAllValues();
    assertThat(allRunnables.size()).isEqualTo(2);

    // execute chrome
    allRunnables.get(0).run();

    Job job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.RUNNING);
    JobResult jobResultChrome = job.getResult(RunnerConfigs.CHROME_LINUX);
    assertThat(jobResultChrome).isNotNull();
    assertThat(jobResultChrome.isRan()).isTrue();
    assertThat(jobResultChrome.isSucceded()).isFalse();
    assertThat(jobResultChrome.getErrorMessage()).isEqualTo("error message");

    // execute firefox
    allRunnables.get(1).run();

    job = manager.getStatus(jobId);

    assertThat(job.getJobId()).isSameAs(jobId);
    assertThat(job.getStatus()).isEqualTo(Job.Status.FAILED);
    JobResult jobResultFF = job.getResult(RunnerConfigs.FIREFOX_LINUX);
    assertThat(jobResultFF).isNotNull();
    assertThat(jobResultFF.isRan()).isTrue();
    assertThat(jobResultFF.isSucceded()).isTrue();
    assertThat(jobResultFF.getResult()).isLessThan(3.00001);
    assertThat(jobResultFF.getResult()).isGreaterThan(2.99999);

    stopSleeping.set(true);

    // wait for the cleanup thread to delete the folder
    Thread.sleep(400);
    assertThat(fileShouldHaveBeenDeleted.get()).isTrue();
    assertThat(job.getFolder().exists()).isFalse();
  }
}
