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
package com.google.gwt.benchmark.compileserver.server.runners.settings;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

      // get the property value and print it out
      settings.benchmarkRootDirectory = new File(prop.getProperty("benchmarksDirectory"));

      settings.moduleTemplate =
          loadModuleTemplate(prop.getProperty("moduleTemplate"));
      settings.hubUrl = new URL(prop.getProperty("seleniumHubUrl"));
      settings.benchmarkCompileOutputDir = new File(prop.getProperty("compileOutputDir"));
      settings.threadPoolSize = Integer.parseInt(prop.getProperty("threadPoolSize"));
      settings.servletContainerPort = Integer.parseInt(prop.getProperty("servletContainerPort"));
      settings.ipAddress = Util.getFirstNonLoopbackAddress().getHostAddress();
      settings.reportResults = prop.getProperty("reportResuts").equals("true");
      settings.reporterUrl = prop.getProperty("reporterUrl");
      settings.mode =
          prop.getProperty("mode").equals("server") ? ManagerMode.SERVER : ManagerMode.LOCAL;
      settings.persistenceDir = new File(prop.getProperty("persistenceDir"));
      settings.gwtSourceLocation = new File(prop.getProperty("gwtSourceLocation"));

      String mailTo = prop.getProperty("mail.to");
      String mailFrom = prop.getProperty("mail.from");
      String mailHost = prop.getProperty("mail.host");
      String mailUsername = prop.getProperty("mail.username");
      String mailPassword = prop.getProperty("mail.password");

      settings.mailSettings =
          new MailSettings(mailFrom, mailTo, mailHost, mailUsername, mailPassword);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    return settings;
  }

  private static String loadModuleTemplate(String fileName) throws IOException {
    FileInputStream inputStream = null;

    try {
      inputStream = new FileInputStream(new File(fileName));
      return IOUtils.toString(inputStream, "UTF-8");
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private File benchmarkRootDirectory;
  private String moduleTemplate;
  private String ipAddress;
  private URL hubUrl;
  private File benchmarkCompileOutputDir;
  private int threadPoolSize;
  private boolean reportResults;
  private String reporterUrl;
  private File persistenceDir;
  private ManagerMode mode;
  private File gwtSourceLocation;
  private MailSettings mailSettings;
  private int servletContainerPort;

  public File getBenchmarkRootDirectory() {
    return benchmarkRootDirectory;
  }

  public String getModuleTemplate() {
    return moduleTemplate;
  }

  public File getScriptsDirectory() {
    return new File(getBenchmarkRootDirectory(), "src/main/scripts/");
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public URL getHubUrl() {
    return hubUrl;
  }

  public File getBenchmarkCompileOutputDir() {
    return benchmarkCompileOutputDir;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public boolean reportResults() {
    return reportResults;
  }

  public String getReporterUrl() {
    return reporterUrl;
  }

  public ManagerMode getMode() {
    return mode;
  }

  public File getPersistenceDir() {
    return persistenceDir;
  }

  public File getGwtSourceLocation() {
    return gwtSourceLocation;
  }

  public MailSettings getMailSettings() {
    return mailSettings;
  }

  public int getServletContainerPort() {
    return servletContainerPort;
  }

  private Settings() {}
}
