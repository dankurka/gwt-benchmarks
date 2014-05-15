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

import com.google.gwt.benchmark.dashboard.server.controller.BenchmarkController;
import com.google.gwt.benchmark.dashboard.server.controller.ControllerException;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles request for the graph update queue.
 */
public class GraphUpdateWorkerServlet extends HttpServlet {

  private static final Logger logger = Logger.getLogger(GraphUpdateWorkerServlet.class.getName());

  private BenchmarkController controller;


  @Inject
  public GraphUpdateWorkerServlet(BenchmarkController controller) {
    this.controller = controller;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {

    long commitTimeMsEpoch = Long.parseLong(request.getParameter("commitTimeMsEpoch"));
    String benchmarkName = request.getParameter("benchmarkName");
    String runnerId = request.getParameter("runnerId");

    logger.info(String.format("Received update request for graph (%s %s %d", benchmarkName, runnerId, commitTimeMsEpoch));

    try {
      controller.updateGraph(commitTimeMsEpoch, benchmarkName, runnerId);
    } catch (ControllerException e) {
      logger.log(Level.WARNING, "Can not update graph", e);
      throw new ServletException("Can not update graph", e);
    }
  }
}
