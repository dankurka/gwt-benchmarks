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

import com.google.j2cl.benchmark.common.util.Util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Setting class containing all the configuration for the benchmarking system.
 */
public class Settings {

  public static Settings parseSettings(File settingsFile) throws Exception {
    Settings settings = new Settings();
    Properties prop = new Properties();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(settingsFile);
      // load a properties file from class path, inside static method
      prop.load(stream);
      settings.hubUrl = new URL(prop.getProperty("seleniumHubUrl"));
      settings.threadPoolSize = Integer.parseInt(prop.getProperty("threadPoolSize"));
      settings.servletContainerPort = Integer.parseInt(prop.getProperty("servletContainerPort"));
      settings.ipAddress = Util.getFirstNonLoopbackAddress().getHostAddress();
      settings.extractDir = new File(prop.getProperty("extractDir"));
      if (!settings.extractDir.exists() && !settings.extractDir.mkdirs()) {
        throw new RuntimeException("Can not create dir");
      }
    } finally {
      IOUtils.closeQuietly(stream);
    }
    return settings;
  }

  private String ipAddress;
  private URL hubUrl;
  private int threadPoolSize;
  private int servletContainerPort;
  private File extractDir;

  public String getIpAddress() {
    return ipAddress;
  }

  public URL getHubUrl() {
    return hubUrl;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public int getServletContainerPort() {
    return servletContainerPort;
  }

  public File getExtractDir() {
    return extractDir;
  }

  private Settings() {}
}
