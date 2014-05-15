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

import com.google.gwt.benchmark.dashboard.server.controller.AuthController;
import com.google.gwt.benchmark.dashboard.server.controller.ControllerException;
import com.google.inject.Inject;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet can update the auth phrase required to post benchmarks.
 */
public class AuthServlet extends HttpServlet {

  private final AuthController authController;

  @Inject
  public AuthServlet(AuthController authController) {
    this.authController = authController;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("/admin/change_pwd_form.html").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String newPassword = req.getParameter("password");
    try {
      authController.updateAuth(newPassword);
    } catch (ControllerException e) {
      resp.getWriter().print(e.getMessage());
    }
  }
}
