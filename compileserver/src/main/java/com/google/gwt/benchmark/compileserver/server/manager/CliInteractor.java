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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All low level interactions with scripts are done by this class.
 */
@Singleton
public class CliInteractor implements BenchmarkCompiler {

  private static final Logger logger = Logger.getLogger(CliInteractor.class.getName());

  private File scriptDirectory;

  private File persistenceDir;

  private File gwtSourceLocation;

  private File benchmarkSourceLocation;

  @Inject
  public CliInteractor(@Named("scriptDirectory") File scriptDirectory,
      @Named("persistenceDir") File persistenceDir,
      @Named("gwtSourceLocation") File gwtSourceLocation,
      @Named("benchmarkSourceLocation") File benchmarkSourceLocation) {
    this.scriptDirectory = scriptDirectory;
    this.persistenceDir = persistenceDir;
    this.gwtSourceLocation = gwtSourceLocation;
    this.benchmarkSourceLocation = benchmarkSourceLocation;
  }

  public void buildSDK() throws BenchmarkManagerException {
    File pullChangesScript = new File(scriptDirectory, "buildSDK");
    runCommand(pullChangesScript.getAbsolutePath() + " " + gwtSourceLocation.getAbsolutePath());
  }

  public void checkout(String commitId) throws BenchmarkManagerException {
    File pullChangesScript = new File(scriptDirectory, "checkout");
    runCommand(pullChangesScript.getAbsolutePath() + " " + commitId + " "
        + gwtSourceLocation.getAbsolutePath());
  }

  @Override
  public void compile(String moduleName, File compilerOutputDir, File devJar, File userJar)
      throws BenchmarkCompilerException {
    logger.info("compiling: " + moduleName);
    File compileScript = new File(scriptDirectory, "compileModule");

    String bsl = benchmarkSourceLocation.getAbsolutePath();
    if (!bsl.endsWith("/")) {
      bsl += "/";
    }

    String outputDir = compilerOutputDir.getAbsolutePath();
    try {
      runCommand(compileScript.getAbsolutePath() + " " + moduleName + " " + devJar.getAbsolutePath()
          + " " + userJar.getAbsolutePath() + " " + bsl + " " + outputDir, false);
    } catch (BenchmarkManagerException e) {
      throw new BenchmarkCompilerException("failed compile", e);
    }
  }

  public String getCurrentCommitId() throws BenchmarkManagerException {
    File gitCommitScript = new File(scriptDirectory, "commitId");
    return runCommand(
        gitCommitScript.getAbsolutePath() + " " + gwtSourceLocation.getAbsolutePath());
  }

  public long getDateForCommitInMsEpoch(String currentCommitId) throws BenchmarkManagerException {
    File commitDateScript = new File(scriptDirectory, "commitDate");
    String dateForCommitString = runCommand(commitDateScript.getAbsolutePath() + " "
        + gwtSourceLocation.getAbsolutePath() + " " + currentCommitId);
    // cut off new line
    dateForCommitString = dateForCommitString.substring(0, dateForCommitString.length() - 1);
    return Long.valueOf(dateForCommitString) * 1000;
  }

  public String getLastCommitId() throws BenchmarkManagerException {
    Properties prop = new Properties();
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(new File(persistenceDir, "store"));
      prop.load(stream);
      String commitId = prop.getProperty("commitId");
      if (commitId == null) {
        logger.severe("can not load last commitId from store");
        throw new BenchmarkManagerException("can not load last commitId from store");
      }
      return commitId;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Can not read commit from store file", e);
      throw new BenchmarkManagerException("Can not read commit from store file", e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  public void maybeCheckoutNextCommit(String baseCommitId) throws BenchmarkManagerException {
    File pullChangesScript = new File(scriptDirectory, "maybe_checkout_next_commit");
    runCommand(pullChangesScript.getAbsolutePath() + " " + baseCommitId + " "
        + gwtSourceLocation.getAbsolutePath());
  }

  public void storeCommitId(String commitId) throws BenchmarkManagerException {

    Properties prop = new Properties();
    prop.setProperty("commitId", commitId);

    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(new File(persistenceDir, "store"));
      prop.store(stream, null);

    } catch (IOException e) {
      logger.log(Level.WARNING, "Can not read commit from store file", e);
      throw new BenchmarkManagerException("Can not read commit from store file", e);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private String runCommand(String command) throws BenchmarkManagerException {
    return runCommand(command, true);
  }

  private String runCommand(String command, boolean useErrorSteam)
      throws BenchmarkManagerException {
    InputStream stream = null;
    try {
      Process process = Runtime.getRuntime().exec(command);
      int exitValue = process.waitFor();

      if (exitValue != 0) {
        stream = useErrorSteam ? process.getErrorStream() : process.getInputStream();
        String error =
            "Command returned with " + exitValue + " " + IOUtils.toString(stream, "UTF-8");
        logger.warning(error);
        throw new BenchmarkManagerException(error);
      }

      stream = process.getInputStream();
      return IOUtils.toString(stream, "UTF-8");

    } catch (IOException | InterruptedException e) {
      logger.log(Level.WARNING, "Can not run command", e);
      throw new BenchmarkManagerException("Can not run command");
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
}
