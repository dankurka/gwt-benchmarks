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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet context listener, will be invoked by the servlet container.
 */
public class GuiceServletConfig extends GuiceServletContextListener {

  private static final Logger logger = Logger.getLogger(ServerServletModule.class.getName());

  private static Settings createSettings() {
    try {
      String configFile = System.getProperty("configFile");
      if (configFile == null) {
        logger.severe("Can not read property file. Start server with -DconfigFile=...");
        throw new RuntimeException(
            "Can not read property file. Start server with -DconfigFile=...");
      }
      return Settings.parseSettings(new File(configFile));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Can not parse settings from file", e);
      throw new RuntimeException("Can not parse settings from file", e);
    }
  }

  @Override
  protected Injector getInjector() {
    Settings settings = createSettings();
    return Guice.createInjector(
        new ServerModule(settings),
        new ServerServletModule(settings.getExtractDir()));
  }
}
