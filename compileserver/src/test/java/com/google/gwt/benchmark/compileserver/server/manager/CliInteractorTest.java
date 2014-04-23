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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Test for {@link CliInteractor}.
 */
public class CliInteractorTest {

  private static String getTestOutput() throws IOException {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File("./target/test-out"));
      String out = IOUtils.toString(inputStream);
      // Cut off new line char
      return out.substring(0, out.length() - 1);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  private File scriptDirectory;
  private File persistenceDir;

  private File gwtSourceLocation;
  private File benchmarkSourceLocation;
  private File compilerOutputDir;
  private CliInteractor scriptInteractor;
  private File scriptDirectoryFail;

  @Before
  public void setup() {

    scriptDirectory = new File("./src/test/resources/scripts-working/");
    scriptDirectoryFail = new File("./src/test/resources/scripts-fail/");
    persistenceDir = new File("./target/");
    gwtSourceLocation = new File("./target/fakesource/");
    benchmarkSourceLocation = new File("./target/fakebenchmark/");
    compilerOutputDir = new File("./target/compilerout/");

    scriptInteractor = new CliInteractor(scriptDirectory, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);

  }

  @Test
  public void testCompileModule() throws BenchmarkCompilerException, IOException {
    scriptInteractor.compile("myModule1", compilerOutputDir);

    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(new File("./target/test-out"));
      String out = IOUtils.toString(inputStream);
      // Cut off new line char
      out = out.substring(0, out.length() - 1);

      String[] split = out.split(";");
      Assert.assertEquals(5, split.length);

      Assert.assertEquals("myModule1", split[0]);
      Assert.assertEquals(
          new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-dev.jar").getAbsolutePath(),
          new File(split[1]).getAbsolutePath());

      Assert.assertEquals(
          new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-user.jar").getAbsolutePath(),
          new File(split[2]).getAbsolutePath());

      Assert.assertEquals(benchmarkSourceLocation.getAbsolutePath(),
          new File(split[3]).getAbsolutePath());
      Assert.assertEquals(compilerOutputDir.getAbsolutePath(),
          new File(split[4]).getAbsolutePath());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  @Test
  public void testFailingDueToGWTCompilerFail() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.compile("myModule1", compilerOutputDir);
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkCompilerException e) {
      Assert.assertEquals("Command returned with 1 This is my errormessage!\n",
          e.getCause().getMessage());
    }
  }

  @Test
  public void testGetCurrentCommitId() throws BenchmarkManagerException {
    String commitId = scriptInteractor.getCurrentCommitId();
    // Cut off new line char
    commitId = commitId.substring(0, commitId.length() - 1);

    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(), new File(commitId).getAbsolutePath());
  }

  @Test
  public void testGetCurrentCommitIdFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.getCurrentCommitId();
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkManagerException e) {
      Assert.assertEquals("Command returned with 1 commitId: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testGetDateForCommit() throws BenchmarkManagerException {
    String date = scriptInteractor.getDateForCommit("asdf1");
    // Cut off new line char
    date = date.substring(0, date.length() - 1);

    String[] split = date.split(";");
    Assert.assertEquals(2, split.length);
    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(), split[0]);
    Assert.assertEquals("asdf1", split[1]);
  }

  @Test
  public void testGetDateForCommitFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.getDateForCommit("commitId1");
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkManagerException e) {
      Assert.assertEquals("Command returned with 1 commitDate: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testBuildSDK() throws BenchmarkManagerException, IOException {
    scriptInteractor.buildSDK();
    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(),
        new File(getTestOutput()).getAbsolutePath());
  }

  @Test
  public void testBuildSDKFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.buildSDK();
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkManagerException e) {
      Assert.assertEquals("Command returned with 1 buildSDK: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testCheckout() throws BenchmarkManagerException, IOException {
    scriptInteractor.checkout("commit12");
    String[] split = getTestOutput().split(";");
    Assert.assertEquals(2, split.length);
    Assert.assertEquals("commit12", split[0]);
    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(), new File(split[1]).getAbsolutePath());
  }

  @Test
  public void testCheckoutFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.buildSDK();
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkManagerException e) {
      Assert.assertEquals("Command returned with 1 buildSDK: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testCheckoutNextCommit() throws BenchmarkManagerException, IOException {
    scriptInteractor.maybeCheckoutNextCommit("baseCommit1");

    String[] split = getTestOutput().split(";");
    Assert.assertEquals(2, split.length);

    Assert.assertEquals("baseCommit1", split[0]);
    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(), new File(split[1]).getAbsolutePath());
  }

  @Test
  public void testCheckoutNextCommitFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.maybeCheckoutNextCommit("doesntmatter");
      Assert.fail("Expected exception did not occur");
    } catch (BenchmarkManagerException e) {
      Assert.assertEquals("Command returned with 1 maybe: This is my errormessage!\n",
          e.getMessage());
    }
  }
}
