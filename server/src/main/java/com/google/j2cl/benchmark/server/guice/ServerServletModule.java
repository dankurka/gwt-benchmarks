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
package com.google.j2cl.benchmark.server.guice;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.j2cl.benchmark.server.BenchmarkUploadServlet;

import org.eclipse.jetty.servlet.DefaultServlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The guice module for all our servlets.
 */
public class ServerServletModule extends ServletModule {

  private static Map<String, String> createServletParams(String resourceBaseAbsolutePath) {
    Map<String, String> initParams = new HashMap<String, String>();
    initParams.put("dirAllowed", "true"); // Allow dir listing
    initParams.put("pathInfoOnly", "true");
    initParams.put("resourceBase", resourceBaseAbsolutePath);
    return initParams;
  }

  private final File extractDir;

  public ServerServletModule(File extractDir) {
    this.extractDir = extractDir;
  }

  @Override
  protected void configureServlets() {
    bind(DefaultServlet.class).in(Singleton.class);
    serve("/__bench/*").with(DefaultServlet.class,
        createServletParams(extractDir.getAbsolutePath()));

    bind(BenchmarkUploadServlet.class).in(Singleton.class);
    serve("/upload").with(BenchmarkUploadServlet.class);
  }
}
