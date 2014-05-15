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

import com.google.gwt.benchmark.common.shared.json.BenchmarkRunJson;
import com.google.gwt.benchmark.common.shared.json.JsonFactory;
import com.google.gwt.benchmark.dashboard.server.controller.AuthController;
import com.google.gwt.benchmark.dashboard.server.controller.BenchmarkController;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A put request on this servlet will add a new benchmark to the dashboard.
 */
public class AddBenchmarkResultServlet extends HttpServlet {

  private static final Logger logger = Logger.getLogger(AddBenchmarkResultServlet.class.getName());

  private final BenchmarkController benchmarkController;
  private final AuthController authController;

  @Inject
  public AddBenchmarkResultServlet(AuthController authController, BenchmarkController benchmarkController) {
    this.authController = authController;
    this.benchmarkController = benchmarkController;
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    String auth = req.getHeader("auth");
    if (!authController.validateAuth(auth)) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    BenchmarkRunJson benchmarkRunJSON = null;
    String json = null;
    try {
      json = IOUtils.toString(req.getInputStream(), "UTF-8");
      AutoBean<BenchmarkRunJson> bean =
          AutoBeanCodex.decode(JsonFactory.get(), BenchmarkRunJson.class, json);
      benchmarkRunJSON = bean.as();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Can not deserialize JSON", e);
      if (json != null) {
        logger.warning(json);
      }
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("Can't parse JSON, see App Engine log for details.");
      return;
    }

    try {

      benchmarkController.addBenchmarkResult(benchmarkRunJSON);
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Can not add benchmark results", e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
