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

import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * WebDriverRunner uses Webdriver to remotely control a browser to run a benchmark.
 */
public class WebDriverRunner implements Runner {
  private static final String IS_READY_JS = "return !!window.__gwt__benchmarking__ran";
  private static final String IS_FAILED_JS = "return !!window.__gwt__benchmarking__failed";
  private static final String GET_RESULT_JS = "return window.__gwt__benchmarking__result";
  private static final int TIMEOUT_MS = 60000;
  private static final Logger logger = Logger.getLogger(WebDriverRunner.class.getName());

  private static DesiredCapabilities createCapabilities(RunnerConfig config) {
    switch(config.getBrowser()) {
      case CHROME:
        return DesiredCapabilities.chrome();
      case FIREFOX:
        return DesiredCapabilities.firefox();
      case INTERNET_EXPLORER:
        DesiredCapabilities internetExplorer = DesiredCapabilities.internetExplorer();
        if(RunnerConfig.IE_11_VERSION.equals(config.getBrowserVersion())) {
          internetExplorer.setVersion("11");
          return internetExplorer;
        }
        if(RunnerConfig.IE_10_VERSION.equals(config.getBrowserVersion())) {
          internetExplorer.setVersion("10");
          return internetExplorer;
        }
        throw new RuntimeException("No IE version for " + config.getBrowserVersion());
      default:
        throw new RuntimeException("No binding for " + config.getBrowser());
    }
  }

  private boolean done;
  private double result;
  private String errorMessage;
  private String url;
  private RunnerConfig config;
  private URL hubURL;
  private boolean failed = false;

  @Inject
  public WebDriverRunner(@Assisted RunnerConfig config, @Assisted String url,
      @Named("hubUrl") URL hubUrl) {
    this.config = config;
    this.url = url;
    this.hubURL = hubUrl;
  }

  @Override
  public void run() {
    logger.info("Starting webdriver for " + url);

    DesiredCapabilities capabilities = createCapabilities(config);
    RemoteWebDriver driver = null;
    try {
      driver = new RemoteWebDriver(hubURL, capabilities);
      driver.navigate().to(url);

      long startMs = System.currentTimeMillis();

      // Initial wait since IE11 has issues running JS before the page has loaded
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
      }

      // Wait till the benchmark has finished running.
      while (true) {
        boolean isReady = (Boolean) driver.executeScript(IS_READY_JS, new Object[] {});
        if (isReady) {
          break;
        }
        if (System.currentTimeMillis() - startMs > TIMEOUT_MS) {
          this.failed = true;
          logger.info("Timeout webdriver for " + url);

          failed = true;
          errorMessage = "Timeout";
          return;
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
      }

      // Read and report status.
      boolean failed = (Boolean) driver.executeScript(IS_FAILED_JS, new Object[] {});
      if (failed) {
        this.failed = true;
        this.errorMessage =
            "Benchmark failed to run in browser - Benchmarkframework reported a failure";
        logger.info("Benchmark failed to run for " + url);
      } else {

        result = ((Number) driver.executeScript(GET_RESULT_JS, new Object[] {})).doubleValue();
        done = true;
      }
    } catch (Exception e) {
      logger.log(Level.INFO, "Error while running webdriver for " + url, e);
      failed = true;
      errorMessage = "Unexpected excpetion during webdriver run: " + e.getMessage();
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

  @Override
  public double getResult() {
    return result;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public RunnerConfig getConfig() {
    return config;
  }

  @Override
  public boolean isFailed() {
    return failed;
  }
}
