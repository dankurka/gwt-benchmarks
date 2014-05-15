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
package com.google.gwt.benchmark.dashboard.server.servlets;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.benchmark.common.shared.json.BenchmarkResultJson;
import com.google.gwt.benchmark.common.shared.json.BenchmarkRunJson;
import com.google.gwt.benchmark.common.shared.json.JsonFactory;
import com.google.gwt.benchmark.dashboard.server.controller.AuthController;
import com.google.gwt.benchmark.dashboard.server.controller.BenchmarkController;
import com.google.gwt.benchmark.dashboard.server.controller.ControllerException;
import com.google.gwt.benchmark.dashboard.server.servlets.AddBenchmarkResultServlet;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test for {@link AddBenchmarkResultServlet}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddBenchmarkResultServletTest {

  public static BenchmarkRunJson buildBenchmarkRunJSON() {
    JsonFactory.Factory factory = JsonFactory.get();

    BenchmarkRunJson runJSON = factory.run().as();
    runJSON.setCommitId("commit1");
    runJSON.setCommitTimeMsEpoch(1);

    Map<String, List<BenchmarkResultJson>> results = new LinkedHashMap<>();
    String moduleName = "module1";
    List<BenchmarkResultJson> list = new ArrayList<>();
    results.put(moduleName, list);

    BenchmarkResultJson resultJSON = factory.result().as();
    resultJSON.setBenchmarkName(moduleName);
    resultJSON.setRunnerId("firefox_linux");
    resultJSON.setRunsPerSecond(3);
    list.add(resultJSON);

    BenchmarkResultJson resultJSON1 = factory.result().as();
    resultJSON1.setBenchmarkName(moduleName);
    resultJSON1.setRunnerId("chrome_linux");
    resultJSON1.setRunsPerSecond(4);
    list.add(resultJSON1);

    String moduleName1 = "module2";
    List<BenchmarkResultJson> list1 = new ArrayList<>();
    results.put(moduleName1, list1);

    BenchmarkResultJson resultJSON2 = factory.result().as();
    resultJSON2.setBenchmarkName(moduleName1);
    resultJSON2.setRunnerId("firefox_linux");
    resultJSON2.setRunsPerSecond(3);
    list1.add(resultJSON2);
    BenchmarkResultJson resultJSON3 = factory.result().as();
    resultJSON3.setBenchmarkName(moduleName1);
    resultJSON3.setRunnerId("chrome_linux");
    resultJSON3.setRunsPerSecond(4);
    list1.add(resultJSON3);

    runJSON.setResultByBenchmarkName(results);
    return runJSON;
  }

  private AddBenchmarkResultServlet servlet;

  private AutoBean<BenchmarkRunJson> bean;
  @Mock
  private BenchmarkController benchmarkController;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpServletRequest request;

  @Mock
  private AuthController authController;

  @Before
  public void setup() throws IOException {

    bean = AutoBeanUtils.getAutoBean(buildBenchmarkRunJSON());

    final ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(AutoBeanCodex.encode(bean).getPayload().getBytes("UTF-8"));
    ServletInputStream servletInputStream = new ServletInputStream() {

      @Override
      public int read() throws IOException {
        return byteArrayInputStream.read();
      }
    };
    when(request.getInputStream()).thenReturn(servletInputStream);

    servlet = new AddBenchmarkResultServlet(authController, benchmarkController);
  }

  @Test
  public void testSuccessfulResultAdd() throws ServletException, IOException, ControllerException {
    when(request.getHeader("auth")).thenReturn("authtest");
    when(authController.validateAuth("authtest")).thenReturn(true);

    servlet.doPut(request, response);
    ArgumentCaptor<BenchmarkRunJson> captor = ArgumentCaptor.forClass(BenchmarkRunJson.class);
    verify(benchmarkController).addBenchmarkResult(captor.capture());
    BenchmarkRunJson value = captor.getValue();
    AutoBean<BenchmarkRunJson> autoBean = AutoBeanUtils.getAutoBean(value);
    Assert.assertTrue(AutoBeanUtils.deepEquals(bean, autoBean));
    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  public void testBadAuth() throws ServletException, IOException {
    when(request.getHeader("auth")).thenReturn("wrongauth");
    when(authController.validateAuth("wrongauth")).thenReturn(false);
    servlet.doPut(request, response);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void testBadController() throws ServletException, IOException, ControllerException {
    when(request.getHeader("auth")).thenReturn("authtest");
    when(authController.validateAuth("authtest")).thenReturn(true);
    doThrow(new ControllerException("test", null)).when(benchmarkController).addBenchmarkResult(
        Mockito.<BenchmarkRunJson> anyObject());
    servlet.doPut(request, response);
    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }
}
