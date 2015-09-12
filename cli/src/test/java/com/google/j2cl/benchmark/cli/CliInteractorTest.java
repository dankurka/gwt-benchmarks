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
package com.google.j2cl.benchmark.cli;

import static com.google.common.truth.Truth.assertThat;

import com.google.j2cl.benchmark.cli.CliException;
import com.google.j2cl.benchmark.cli.CliInteractor;

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
  private File devJar;
  private File userJar;

  @Before
  public void setup() {

    scriptDirectory = new File("./src/test/resources/scripts-working/");
    scriptDirectoryFail = new File("./src/test/resources/scripts-fail/");
    persistenceDir = new File("./target/");
    gwtSourceLocation = new File("./target/fakesource/");
    benchmarkSourceLocation = new File("./target/fakebenchmark/");
    compilerOutputDir = new File("./target/compilerout/");
    devJar = new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-dev.jar");
    userJar = new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-user.jar");

    scriptInteractor = new CliInteractor(scriptDirectory, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);

  }

  @Test
  public void testCompileModule() throws CliException, IOException {
    scriptInteractor.compile("myModule1", compilerOutputDir, devJar, userJar, "-style PRETTY -foo");

    try (FileInputStream inputStream = new FileInputStream(new File("./target/test-out"));) {

      String out = IOUtils.toString(inputStream);
      // Cut off new line char
      out = out.substring(0, out.length() - 1);

      String[] split = out.split(";");
      assertThat(split.length).isEqualTo(6);
      assertThat(split[0]).isEqualTo("myModule1");
      assertThat(new File(split[1]).getAbsolutePath()).isEqualTo(devJar.getAbsolutePath());
      assertThat(new File(split[2]).getAbsolutePath()).isEqualTo(userJar.getAbsolutePath());
      assertThat(new File(split[3]).getAbsolutePath()).isEqualTo(
          benchmarkSourceLocation.getAbsolutePath());

      assertThat(new File(split[4]).getAbsolutePath()).isEqualTo(
          compilerOutputDir.getAbsolutePath());
      assertThat(split[5]).isEqualTo("-style PRETTY -foo");
    }
  }

  @Test
  public void testFailingDueToGWTCompilerFail() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.compile("myModule1", compilerOutputDir, devJar, userJar, "-style PRETTY");
      Assert.fail("Expected exception did not occur");
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 This is my errormessage!\n",
          e.getCause().getMessage());
    }
  }

  @Test
  public void testGetCurrentCommitId() throws CliException {
    String commitId = scriptInteractor.getCurrentCommitId();

    Assert.assertEquals(gwtSourceLocation.getAbsolutePath(), new File(commitId).getAbsolutePath());
  }

  @Test
  public void testGetCurrentCommitIdFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.getCurrentCommitId();
      Assert.fail("Expected exception did not occur");
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 commitId: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testGetDateForCommit() throws CliException {
    long date = scriptInteractor.getDateForCommitInMsEpoch("asdf1");
    Assert.assertEquals(1234987000L, date);
  }

  @Test
  public void testGetDateForCommitFailing() {
    scriptInteractor = new CliInteractor(scriptDirectoryFail, persistenceDir, gwtSourceLocation,
        benchmarkSourceLocation);
    try {
      scriptInteractor.getDateForCommitInMsEpoch("commitId1");
      Assert.fail("Expected exception did not occur");
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 commitDate: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testBuildSDK() throws CliException, IOException {
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
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 buildSDK: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testCheckout() throws CliException, IOException {
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
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 buildSDK: This is my errormessage!\n",
          e.getMessage());
    }
  }

  @Test
  public void testCheckoutNextCommit() throws CliException, IOException {
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
    } catch (CliException e) {
      Assert.assertEquals("Command returned with 1 maybe: This is my errormessage!\n",
          e.getMessage());
    }
  }
}
