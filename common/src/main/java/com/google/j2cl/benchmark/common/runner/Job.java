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
package com.google.j2cl.benchmark.common.runner;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A job represents a benchmark being run through different kind of browsers.
 */
public class Job {
  private final JobId jobId;
  private Status status = Status.CREATED;
  private int counter;
  private int failedCounter;
  private final int expectedResults;

  private Map<String, JobResult> jobResultsByRunnerId = new HashMap<>();
  private List<RunnerConfig> runnerConfigs;
  private File folder;
  private FailReason failReason;
  private final long creationTimeInMsEpoch;

  public enum FailReason {
    CAN_NOT_EXTRACT_ZIP, AT_LEAST_ONE_BENCHMARK_FAILED_TO_RUN
  }

  public enum Status {
    CREATED, SUBMITTED, RUNNING, FINISHED, FAILED
  }

  public Job(JobId id, List<RunnerConfig> runnerConfigs, long creationTimeInMsEpoch) {
    this.jobId = id;
    this.creationTimeInMsEpoch = creationTimeInMsEpoch;
    this.runnerConfigs = runnerConfigs;
    for (RunnerConfig runnerConfig : runnerConfigs) {
      jobResultsByRunnerId.put(runnerConfig.toString(), new JobResult(runnerConfig));
    }
    this.expectedResults = runnerConfigs.size();
  }

  public JobId getJobId() {
    return jobId;
  }

  public Status getStatus() {
    return status;
  }

  public void addResult(RunnerConfig config, double result) {
    JobResult jobResult = getJobResult(config);
    if (jobResult.isRan()) {
      throw new IllegalStateException();
    }
    jobResult.setSucceded(true);
    jobResult.setResult(result);
    jobResult.setRan(true);
    counter++;

    maybeChangeStatus();
  }

  public void setRunFailed(RunnerConfig config, String reason) {
    JobResult jobResult = getJobResult(config);
    if (jobResult.isRan()) {
      throw new IllegalStateException();
    }

    jobResult.setSucceded(false);
    jobResult.setErrorMessage(reason);
    jobResult.setRan(true);
    this.failReason = FailReason.AT_LEAST_ONE_BENCHMARK_FAILED_TO_RUN;

    failedCounter++;
    maybeChangeStatus();
  }

  private JobResult getJobResult(RunnerConfig config) {
    JobResult jobResult = jobResultsByRunnerId.get(config.toString());

    if (jobResult == null) {
      throw new IllegalStateException();
    }
    return jobResult;
  }

  private void maybeChangeStatus() {
    if (counter == expectedResults) {
      status = Status.FINISHED;
    } else if (counter + failedCounter == expectedResults) {
      status = Status.FAILED;
    } else {
      if (status == Status.CREATED) {
        status = Status.RUNNING;
      }
    }
  }

  @Override
  public Job clone() {
    Job other = new Job(jobId, new ArrayList<>(runnerConfigs), creationTimeInMsEpoch);
     other.counter = counter;
     other.failedCounter = failedCounter;
     other.status = status;
     other.folder = folder;
     other.failReason = failReason;

     for (Entry<String, JobResult> entry : jobResultsByRunnerId.entrySet()) {
       other.jobResultsByRunnerId.put(entry.getKey(), entry.getValue().clone());
     }

     return other;
  }

  public boolean isOld(long currentTimeInMsEpoch) {
    long oneHour = 1000l * 60 * 60;
    if (currentTimeInMsEpoch - oneHour > creationTimeInMsEpoch) {
      return true;
    }
    return false;
  }

  public JobResult getResult(RunnerConfig config) {
    return getJobResult(config);
  }

  public void setFolder(File folder) {
    this.folder = folder;
  }

  public File getFolder() {
    return folder;
  }

  public FailReason getFailReason() {
    return failReason;
  }

  public boolean isDone() {
    for (JobResult r : jobResultsByRunnerId.values()) {
      if (!r.isRan()) {
        return false;
      }
    }
    return true;
  }

  public List<JobResult> getJobResults() {
    return Lists.newArrayList(jobResultsByRunnerId.values());
  }

  public boolean isSucceeded() {
    return status == Status.FINISHED;
  }

  public void setFailed(FailReason failReason) {
    this.failReason = failReason;
    status = Status.FAILED;
  }
}
