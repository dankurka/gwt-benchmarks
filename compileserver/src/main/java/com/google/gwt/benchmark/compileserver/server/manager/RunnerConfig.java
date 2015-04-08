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
package com.google.gwt.benchmark.compileserver.server.manager;

/**
 * A RunnerConfig describes all attributes of a runner like browser, OS and version.
 */
public interface RunnerConfig {

  public static final String IE_10_VERSION = "IE10";
  public static final String IE_11_VERSION = "IE11";

  /**
   * Enum for all supported browsers.
   */
  public enum Browser {
    CHROME("chrome"), FIREFOX("firefox"), INTERNET_EXPLORER("ie"), SAFARI("safari");
    private String value;

    Browser(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /**
   * Enum for all supported operating systems.
   */
  public enum OS {
    ANDROID("android"), IOS("ios"), LINUX("linux"), OSX("osx"), WINDOWS("windows");
    private String value;

    OS(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /**
   * Get the browser for this config.
   *
   * @return the {@link Browser} for this config.
   */
  Browser getBrowser();

  /**
   * Get the operating system for this config.
   *
   * @return the operating system for this config.
   */
  OS getOS();

  /**
   * Get the version of the browser for this config.
   *
   * @return the version of the browser for this config.
   */
  String getBrowserVersion();


  /**
   * Returns a human readable representation of this runner.
   */
  @Override
  public String toString();
}
