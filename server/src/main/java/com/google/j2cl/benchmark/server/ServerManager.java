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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.Job.FailReason;
import com.google.j2cl.benchmark.common.runner.Job.Status;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.Runner;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.util.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Named;

/**
 * RunServerManager accepts JavaScript benchmarks (as a zip folder) and runs them using an
 * implementation of {@link Runner} and reports back with results.
 */
public class ServerManager {

  private static final Logger logger = Logger.getLogger(ServerManager.class.getCanonicalName());

  private final Provider<String> randomStringProvider;

  private final Map<String, Job> jobsById = new HashMap<>();

  private final Runner.Factory runnerProvider;

  private ExecutorService executorService;

  private final int port;

  private final String ip;

  private final Provider<Long> timeProvider;

  private final File extractDir;

  private class Worker implements Runnable {
    private final JobId jobId;
    private RunnerConfig config;
    private String url;
    private final Runner.Factory runnerFactory;

    public Worker(JobId jobId, RunnerConfig config, String url, Runner.Factory runnerFactory) {
      this.jobId = jobId;
      this.config = config;
      this.url = url;
      this.runnerFactory = runnerFactory;
    }

    @Override
    public void run() {
      Runner runner = runnerFactory.create(config, url);
      runner.run();
      if (runner.isFailed()) {
        failed(jobId, config, runner.getErrorMessage());
      } else {
        finished(jobId, config, runner.getResult());
      }
    }
  }

  @Inject
  public ServerManager(@Named("randomStringProvider") Provider<String> randomStringProvider,
      Runner.Factory runnerProvider, ExecutorService executorService, @Named("ip") String ip,
      @Named("port") int port, @Named("timeProvider") Provider<Long> timeProvider,
      @Named("extractDir") File extractDir) {
    this.randomStringProvider = randomStringProvider;
    this.runnerProvider = runnerProvider;
    this.executorService = executorService;
    this.ip = ip;
    this.port = port;
    this.timeProvider = timeProvider;
    this.extractDir = extractDir;

    new Thread(new Runnable() {
        @Override
      public void run() {
        long tenMinutesInMs = 1000l * 60 * 10;
        try {
          sleep(tenMinutesInMs);
        } catch (InterruptedException e) {
          return;
        }
        cleanup();
      }
    }).start();
  }

  public JobId submitJob(InputStream zipToBenchmark, List<RunnerConfig> runnerIds) {

    String randomString = randomStringProvider.get();

    File tempFile = null;
    FileOutputStream tempOutputStream = null;
    File folder = null;
    try {
      tempFile = File.createTempFile(randomString + "-to_bench", ".zip");
      tempOutputStream = new FileOutputStream(tempFile);
      folder = new File(extractDir, randomString);
      folder.mkdirs();
      logger.info("Using folder: " + folder.getAbsolutePath());

      IOUtils.copy(zipToBenchmark, tempOutputStream);
      ZipUtil.unzip(tempFile, folder);
    } catch (IOException e) {
      Job job = new Job(new JobId(randomString), runnerIds, timeProvider.get());
      job.setFailed(FailReason.CAN_NOT_EXTRACT_ZIP);
      addJob(job);
      return job.getJobId();
    } finally {
      IOUtils.closeQuietly(tempOutputStream);
      if (tempFile != null) {
        tempFile.delete();
      }
    }

    Job job = new Job(new JobId(randomString), runnerIds, timeProvider.get());
    job.setFolder(folder);

    for (RunnerConfig runnerConfig : runnerIds) {
      String url = getUrl(randomString);
      Worker worker = new Worker(job.getJobId(), runnerConfig, url, runnerProvider);
      executorService.submit(worker);
    }
    jobsById.put(job.getJobId().getId(), job);
    return job.getJobId();
  }

  public synchronized Job getStatus(JobId jobId) throws JobNotFoundException {
    Job job = jobsById.get(jobId.getId());
    if (job == null) {
      throw new JobNotFoundException();
    }
    return job.clone();
  }

  private synchronized void addJob(Job job) {
    jobsById.put(job.getJobId().getId(), job);
  }

  private String getUrl(String dirName) {
    String url = "http://{host}:{port}/__bench/{dirName}/index.html";
    url = url.replace("{host}", ip);
    url = url.replace("{port}", "" + port);
    url = url.replace("{dirName}", dirName);
    return url;
  }

  private synchronized void finished(JobId jobId, RunnerConfig config, double result) {
    Job job = jobsById.get(jobId.getId());
    if (job == null) {
      throw new IllegalStateException(
          "Job reports being finished, but can not be found. This should never happen. JobId: "
          + jobId.getId() + " config: " + config);
    }
    job.addResult(config, result);
    maybeCleanUp(job);
  }

  private synchronized void failed(JobId jobId, RunnerConfig config, String reason) {
    Job job = jobsById.get(jobId.getId());
    if (job == null) {
      throw new IllegalStateException(
          "Job reports being failed, but can not be found. This should never happen. JobId: "
          + jobId.getId() + " config: " + config);
    }
    job.setRunFailed(config, reason);
    maybeCleanUp(job);
  }

  private void maybeCleanUp(Job job) {
    if (job.getStatus() == Status.FINISHED || job.getStatus() == Status.FAILED) {
      FileUtils.deleteQuietly(job.getFolder());
    }
  }

  @VisibleForTesting
  void sleep(long timeInMs) throws InterruptedException {
    Thread.sleep(timeInMs);
  }

  @VisibleForTesting
  synchronized void cleanup() {
    Iterator<Job> iterator = jobsById.values().iterator();
    while (iterator.hasNext()) {
      Job job = iterator.next();
      if (job.isOld(timeProvider.get())) {
        iterator.remove();
      }
    }
  }
}
