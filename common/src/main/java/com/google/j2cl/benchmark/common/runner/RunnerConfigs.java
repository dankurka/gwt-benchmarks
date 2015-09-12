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
package com.google.j2cl.benchmark.common.runner;

import com.google.common.base.Objects;
import com.google.j2cl.benchmark.common.runner.RunnerConfig.Browser;
import com.google.j2cl.benchmark.common.runner.RunnerConfig.OS;

import java.util.Arrays;
import java.util.List;

/**
 * Global Object containing all available {@link RunnerConfig RunnerConfigs}.
 */
public final class RunnerConfigs {

  private static class RunnerConfigImpl implements RunnerConfig {

    private Browser browser;
    private OS os;
    private String version;

    public RunnerConfigImpl(Browser browser, OS os, String version) {
      this.browser = browser;
      this.os = os;
      this.version = version;
    }

    @Override
    public Browser getBrowser() {
      return browser;
    }

    @Override
    public OS getOS() {
      return os;
    }

    @Override
    public String getBrowserVersion() {
      return version;
    }

    @Override
    public String toString() {
      String id =  os + " " + browser;
      if (!"".equals(version)) {
        id += " " + version;
      }
      return id;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(browser, os, version);
    }

    @Override
    public boolean equals(Object other) {
      if (other == null) {
        return false;
      }

      if (!(other instanceof RunnerConfig)) {
        return false;
      }
      RunnerConfig otherConfig = (RunnerConfig) other;

      if (!Objects.equal(os, otherConfig.getOS())) {
        return false;
      }

      if (!Objects.equal(browser, otherConfig.getBrowser())) {
        return false;
      }

      if (!Objects.equal(version, otherConfig.getBrowserVersion())) {
        return false;
      }
      return true;
    }
  }

  /** Chrome on linux */
  public static final RunnerConfig CHROME_LINUX =
      new RunnerConfigImpl(Browser.CHROME, OS.LINUX, "");
  /** Firefox on linux */
  public static final RunnerConfig FIREFOX_LINUX =
      new RunnerConfigImpl(Browser.FIREFOX, OS.LINUX, "");
  /** IE11 on windows */
  public static final RunnerConfig IE11_WIN =
      new RunnerConfigImpl(Browser.INTERNET_EXPLORER, OS.WINDOWS, RunnerConfig.IE_11_VERSION);
  /** IE10 on windows */
  public static final RunnerConfig IE10_WIN =
      new RunnerConfigImpl(Browser.INTERNET_EXPLORER, OS.WINDOWS, RunnerConfig.IE_10_VERSION);


  public static List<RunnerConfig> getAllRunners() {
    return Arrays.asList(
        RunnerConfigs.FIREFOX_LINUX,
        RunnerConfigs.CHROME_LINUX,
        /*RunnerConfigs.IE10_WIN,*/
        RunnerConfigs.IE11_WIN);
  }

  public static RunnerConfig fromString(String s) {
    switch(s) {
      case "linux chrome":
        return CHROME_LINUX;
      case "linux firefox":
        return FIREFOX_LINUX;
      case "windows ie IE10":
        return IE10_WIN;
      case "windows ie IE11":
        return IE11_WIN;
      default:
        throw new IllegalArgumentException(s);
    }
  }

  private RunnerConfigs() {}
}
