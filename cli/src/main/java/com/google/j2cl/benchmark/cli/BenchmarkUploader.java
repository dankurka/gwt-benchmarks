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

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigJson;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * BenchmarkUploader uploads a zip file to the lab server and gathers results.
 */
public class BenchmarkUploader {

  public interface Factory {
    BenchmarkUploader create(File zip, List<RunnerConfig> runnerConfigs);
  }

  private final File zip;
  private final String serverUrl;
  private final List<RunnerConfig> runnerConfigs;

  @Inject
  public BenchmarkUploader(@Named("runServerUrl") String serverUrl, @Assisted File zip,
      @Assisted List<RunnerConfig> runnerConfigs) {
    this.zip = zip;
    this.runnerConfigs = runnerConfigs;
    if (!serverUrl.endsWith("/")) {
      serverUrl += "/";
    }
    serverUrl += "upload";
    this.serverUrl = serverUrl;
  }

  public Job run(boolean verbose) throws IOException, InterruptedException {
    JobId jobId = sendToWebDriver(zip, runnerConfigs);
    Job job = getStatus(jobId);
    while (!job.isDone()) {
      Thread.sleep(1000l);
      if (verbose) {
        System.out.print(".");
      }
      job = getStatus(jobId);
    }
    if (verbose) {
      System.out.print("\n");
    }
    return job;
  }

  private Job getStatus(JobId jobId) throws IOException {
    HttpGet httpGet = new HttpGet(serverUrl + "?jobId=" + jobId.getId());
    String responseJson = execute(httpGet);
    JobResponse jobResponse = new GsonBuilder()
        .registerTypeAdapter(RunnerConfig.class, new RunnerConfigJson())
        .create()
        .fromJson(responseJson, JobResponse.class);
    return jobResponse.job;
  }

  private JobId sendToWebDriver(File zipFile, List<RunnerConfig> runnerConfigs) throws IOException {
    String runnerIdsByComma = Joiner.on(",").join(runnerConfigs);

    HttpEntity entity = MultipartEntityBuilder.create()
        .addTextBody("runnerIds", runnerIdsByComma)
        .addBinaryBody("file", zipFile, ContentType.create("application/zip"), zipFile.getName())
        .build();

    HttpPost post = new HttpPost(serverUrl);
    post.setEntity(entity);

    String responseJson = execute(post);
    JobIdResponse jobIdResponse = new Gson().fromJson(responseJson, JobIdResponse.class);
    return jobIdResponse.jobId;
  }

  private static class JobIdResponse {
    JobId jobId;
  }

  private static class JobResponse {
    Job job;
  }

  private String execute(HttpUriRequest request) throws IOException {
    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        @Override
      public String handleResponse(final HttpResponse response) throws ClientProtocolException,
          IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          throw new ClientProtocolException("Unexpected response status: " + status + " message:"
              + IOUtils.toString(response.getEntity().getContent()));
        }
      }
    };

    try(CloseableHttpClient httpclient = createHttpClient();) {
      return httpclient.execute(request, responseHandler);
    }
  }

  @VisibleForTesting
  CloseableHttpClient createHttpClient() {
    return HttpClients.createDefault();
  }
}
