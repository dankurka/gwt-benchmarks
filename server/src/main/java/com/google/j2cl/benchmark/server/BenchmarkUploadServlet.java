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
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigJson;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for uploading zip files for benchmarking.
 */
public class BenchmarkUploadServlet extends HttpServlet {

  static class JobIdResponse {
    @SuppressWarnings("unused")
    JobId jobId;
  }

  static class JobResponse {
    @SuppressWarnings("unused")
    Job job;
  }

  private final ServerManager manager;

  @Inject
  public BenchmarkUploadServlet(ServerManager manager) {
    this.manager = manager;
  }

  private static class ParsedRequest {
    InputStream fileContent;
    List<RunnerConfig> runnerIds;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      List<FileItem> items = parseRequest(request);
      ParsedRequest parsedRequest = parseFileItems(items);

      JobId jobId = manager.submitJob(parsedRequest.fileContent, parsedRequest.runnerIds);

      JobIdResponse resp = new JobIdResponse();
      resp.jobId = jobId;

      Gson gson = new Gson();
      String json = gson.toJson(resp);

      response.getWriter().write(json);
  }

  private ParsedRequest parseFileItems(List<FileItem> items) throws IOException, ServletException {
    ParsedRequest parsedRequest = new ParsedRequest();
    List<String> runners = null;
    for (FileItem item : items) {
      if (!item.isFormField()) {
        if ("file".equals(item.getFieldName())) {
          parsedRequest.fileContent = item.getInputStream();
        }
      } else {
        if ("runnerIds".equals(item.getFieldName())) {
          runners = Splitter.on(",").splitToList(item.getString());
        }
      }
    }

    if (parsedRequest.fileContent == null) {
      throw new ServletException("No filed supplied");
    }

    if (runners == null) {
      parsedRequest.runnerIds = RunnerConfigs.getAllRunners();
    } else {
      List<RunnerConfig> configs = Lists.newArrayList();
      for (String runner : runners) {
        configs.add(RunnerConfigs.fromString(runner));
      }
      parsedRequest.runnerIds = configs;
    }
    return parsedRequest;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    String jobIdsAsString = request.getParameter("jobId");

    Job job;
    try {
      job = manager.getStatus(new JobId(jobIdsAsString));
    } catch (JobNotFoundException e) {
      throw new ServletException(e);
    }


    JobResponse jobResponse = new JobResponse();
    jobResponse.job = job;

    String outputJson = new GsonBuilder()
        .registerTypeAdapter(RunnerConfig.class, new RunnerConfigJson())
        .create()
        .toJson(jobResponse);
    response.getWriter().write(outputJson);
  }

  @VisibleForTesting
  List<FileItem> parseRequest(HttpServletRequest request) throws ServletException {
    try {
      return new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
    } catch (FileUploadException e) {
      throw new ServletException("Can not parse upload data", e);
    }
  }
}
