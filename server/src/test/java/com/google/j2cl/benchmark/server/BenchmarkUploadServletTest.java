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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test for {@link BenchmarkUploadServlet}.
 */
public class BenchmarkUploadServletTest {
  private BenchmarkUploadServlet servlet;
  private ServerManager runServerManager;
  private List<FileItem> fileItems;

  @Before
  public void before() {
    fileItems = Lists.newArrayList();
    runServerManager = mock(ServerManager.class);

    servlet = new BenchmarkUploadServlet(runServerManager) {
      @Override
      List<FileItem> parseRequest(HttpServletRequest request) throws ServletException {
        return fileItems;
      }
    };
  }

  @Test
  public void testSimpleUpload() throws ServletException, IOException {
    FileItem fileItem = mock(FileItem.class);
    when(fileItem.getFieldName()).thenReturn("file");
    fileItems.add(fileItem);
    InputStream inputStream = mock(InputStream.class);
    when(fileItem.getInputStream()).thenReturn(inputStream);
    when(fileItem.isFormField()).thenReturn(false);

    FileItem runners = mock(FileItem.class);
    when(runners.getFieldName()).thenReturn("runnerIds");
    when(runners.getString())
        .thenReturn(Joiner.on(",").join(Lists.newArrayList(
            RunnerConfigs.CHROME_LINUX.toString(), RunnerConfigs.FIREFOX_LINUX.toString())));
    when(runners.isFormField()).thenReturn(true);
    fileItems.add(runners);

    when(
        runServerManager.submitJob(
            inputStream, Arrays.asList(RunnerConfigs.CHROME_LINUX, RunnerConfigs.FIREFOX_LINUX)))
        .thenReturn(new JobId("foo"));
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    servlet.doPost(request, response);
    verify(writer).write("{\"jobId\":{\"id\":\"foo\"}}");
  }

  @Test
  public void testSimpleJobRetrieval() throws JobNotFoundException, IOException, ServletException {
    JobId jobId = new JobId("jobId1");
    Job job = new Job(jobId, RunnerConfigs.getAllRunners(), 1);
    when(runServerManager.getStatus(jobId)).thenReturn(job);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("jobId")).thenReturn(jobId.getId());
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    servlet.doGet(request, response);
    verify(writer).write("{\"job\":{\"jobId\":{\"id\":\"jobId1\"},\"status\":\"CREATED\","
        + "\"counter\":0,\"failedCounter\":0,\"expectedResults\":4,\"jobResultsByRunnerId\":"
        + "{\"windows ie IE11\":{\"succeded\":false,\"result\":0.0,\"ran\":false,\"runnerConfig\":"
        + "\"windows ie IE11\"},\"windows ie IE10\":{\"succeded\":false,\"result\":0.0,\"ran\":"
        + "false,\"runnerConfig\":\"windows ie IE10\"},\"linux chrome\":{\"succeded\":false,"
        + "\"result\":0.0,\"ran\":false,\"runnerConfig\":\"linux chrome\"},\"linux firefox\""
        + ":{\"succeded\":false,\"result\":0.0,\"ran\":false,\"runnerConfig\":\"linux firefox\"}}"
        + ",\"runnerConfigs\":[\"linux firefox\",\"linux chrome\",\"windows ie IE10\""
        + ",\"windows ie IE11\"],\"creationTimeInMsEpoch\":1}}");
  }

  @Test
  public void testJobNotFound() throws JobNotFoundException, IOException {
    JobId jobId = new JobId("jobId1");
    when(runServerManager.getStatus(jobId)).thenThrow(new JobNotFoundException());

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("jobId")).thenReturn(jobId.getId());
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    try {
      servlet.doGet(request, response);
      fail("expected exception not thrown");
    } catch (ServletException e) {
      assertThat(e.getCause()).isInstanceOf(JobNotFoundException.class);
    }
  }
}
