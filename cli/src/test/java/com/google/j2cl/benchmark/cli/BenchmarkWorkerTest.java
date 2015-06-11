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

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Provider;
import com.google.j2cl.benchmark.cli.BenchmarkWorker.WorkResult;
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.Runner;
import com.google.j2cl.benchmark.common.runner.Runner.Factory;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;
import com.google.j2cl.benchmark.common.util.ZipUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Test for {@link BenchmarkWorker}.
 */
public class BenchmarkWorkerTest {

  private RunnerConfig runnerConfig;
  private BenchmarkCompiler compiler;
  private String moduleName;
  private BenchmarkWorker worker;
  private File benchmarkCompileOutputDir;
  private Provider<String> randomStringProvider;
  private String moduleTemplate;
  private BenchmarkWorkerConfig benchmarkData;
  private File devJar;
  private File userJar;
  private BenchmarkUploader.Factory benchmarkUploderFactory;
  private File zipFile;

  @Before
  public void setup() {
    moduleName = "moduleName1";

    benchmarkCompileOutputDir = new File("./target/test/TestBenchmarkWorker/");
    if (!benchmarkCompileOutputDir.exists()) {
      if (!benchmarkCompileOutputDir.mkdirs()) {
        Assert.fail("failed to create dirs");
      }
    }

    runnerConfig = RunnerConfigs.CHROME_LINUX;

    compiler = Mockito.mock(BenchmarkCompiler.class);

    moduleTemplate = "{module_nocache}";

    devJar = mock(File.class);
    userJar = mock(File.class);

    benchmarkData =
        new BenchmarkWorkerConfig(moduleName, Arrays.asList(runnerConfig), devJar, userJar, "");

    randomStringProvider = cast(Mockito.mock(Provider.class));
    benchmarkUploderFactory = mock(BenchmarkUploader.Factory.class);

    worker = new BenchmarkWorker(compiler, moduleTemplate, benchmarkData, benchmarkCompileOutputDir,
        randomStringProvider, benchmarkUploderFactory) {
        @Override
      void writeHostPage(File outputDir, String moduleName) throws IOException {
        super.writeHostPage(outputDir, moduleName);

        // lets write some files so we can assert that we zip up everything
        File fooJs = new File(outputDir, "foo.js");
        FileUtils.writeStringToFile(fooJs, "// foo.js");

        File barDir = new File(outputDir, "bar");
        barDir.mkdirs();

        File bazJs = new File(barDir, "baz.js");
        FileUtils.writeStringToFile(bazJs, "// baz.js");
      }
    };
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(benchmarkCompileOutputDir);
  }

  @Test
  public void testCompilerError() throws Exception {

    BenchmarkCompiler compiler = Mockito.mock(BenchmarkCompiler.class);
    Factory runnerProvider = Mockito.mock(Runner.Factory.class);

    when(randomStringProvider.get()).thenReturn("randomDir1", "randomDirFile1");

    String moduleTemplate = "mytemplate [{module_nocache}]";
    String moduleName = "moduleName1";
    BenchmarkWorkerConfig benchmarkData = new BenchmarkWorkerConfig(moduleName,
        Arrays.asList(RunnerConfigs.CHROME_LINUX), devJar, userJar, "");

    File benchmarkCompileOutputDir = new File("./target/test/TestBenchmarkWorker");
    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");

    BenchmarkWorker worker = new BenchmarkWorker(compiler, moduleTemplate, benchmarkData,
        benchmarkCompileOutputDir, randomStringProvider, benchmarkUploderFactory);

    Mockito.doThrow(new CliException("test")).when(compiler).compile(moduleName, workDir, devJar,
        userJar, "");

    WorkResult workResult = worker.call();

    assertThat(workResult.state).isEqualTo(BenchmarkRun.State.FAILED_COMPILE);
    Mockito.verify(compiler).compile(Mockito.eq(moduleName), Mockito.<File> anyObject(),
        Mockito.eq(devJar), Mockito.eq(userJar), Mockito.eq(""));
    Mockito.verifyZeroInteractions(runnerProvider);
    assertThat(workDir.exists()).isFalse();
  }

  @Test
  public void testBenchmarkWorker() throws Exception {
    when(randomStringProvider.get()).thenReturn("randomDir1");
    ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
    final BenchmarkUploader benchmarkUploader = mock(BenchmarkUploader.class);

    when(benchmarkUploderFactory.create(captor.capture(), Mockito.<List<RunnerConfig>>anyObject()))
        .thenAnswer(new Answer<BenchmarkUploader>() {
          @Override
          public BenchmarkUploader answer(InvocationOnMock invocation) throws Throwable {
            zipFile = (File) invocation.getArguments()[0];

            // assert content of zip
            File tempDir = Files.createTempDir();
            ZipUtil.unzip(zipFile, tempDir);

            File indexHtml = new File(tempDir, "index.html");
            assertThat(indexHtml.exists()).isTrue();
            assertThat(IOUtils.toString(new FileInputStream(indexHtml), "UTF-8")).isEqualTo(
                "moduleName1/moduleName1.nocache.js");

            File fooJs = new File(tempDir, "foo.js");
            assertThat(fooJs.exists()).isTrue();
            assertThat(IOUtils.toString(new FileInputStream(fooJs), "UTF-8")).isEqualTo(
                "// foo.js");

            File barDir = new File(tempDir, "bar");
            assertThat(barDir.exists()).isTrue();

            File bazJs = new File(barDir, "baz.js");
            assertThat(bazJs.exists()).isTrue();
            assertThat(IOUtils.toString(new FileInputStream(bazJs), "UTF-8")).isEqualTo(
                "// baz.js");
            return benchmarkUploader;
          }
        });

    Job job = new Job(new JobId("jobId1"), Lists.newArrayList(RunnerConfigs.CHROME_LINUX), 1);
    when(benchmarkUploader.run(false)).thenReturn(job);
    WorkResult workResult = worker.call();
    assertThat(zipFile.exists()).isFalse();
    assertThat(workResult.job).isSameAs(job);
    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");
    Mockito.verify(compiler).compile(moduleName, workDir, devJar, userJar, "");
    Assert.assertFalse(workDir.exists());
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object a) {
    return (T) a;
  }
}
