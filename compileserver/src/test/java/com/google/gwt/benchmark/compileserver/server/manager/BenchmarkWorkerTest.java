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

import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkWorker.ProgressHandler;
import com.google.gwt.benchmark.compileserver.server.manager.Runner.Factory;
import com.google.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Test for {@link BenchmarkWorker}.
 */
public class BenchmarkWorkerTest {

  private String ip;
  private int port;
  private RunnerConfig runnerConfig;
  private BenchmarkCompiler compiler;
  private Factory runnerProvider;
  private Runner runner;
  private String moduleName;
  private BenchmarkWorker worker;
  private File benchmarkCompileOutputDir;
  private ProgressHandler progressHandler;
  private Provider<String> randomStringProvider;
  private String moduleTemplate;
  private BenchmarkWorkerConfig benchmarkData;

  @Before
  public void setup() {
    moduleName = "moduleName1";

    benchmarkCompileOutputDir = new File("./target/test/TestBenchmarkWorker/");
    if (!benchmarkCompileOutputDir.exists()) {
      if (!benchmarkCompileOutputDir.mkdirs()) {
        Assert.fail("failed to create dirs");
      }
    }

    ip = "127.0.0.1";
    port = 8080;

    runnerConfig = RunnerConfigs.CHROME_LINUX;

    compiler = Mockito.mock(BenchmarkCompiler.class);
    runnerProvider = Mockito.mock(Runner.Factory.class);

    runner = Mockito.mock(Runner.class);

    Mockito.when(runnerProvider.create(RunnerConfigs.CHROME_LINUX,
        "http://" + ip + ":" + port + "/__bench/" + moduleName + ".html")).thenReturn(runner);

    moduleTemplate = "{module_nocache}";

    benchmarkData = new BenchmarkWorkerConfig(moduleName, Arrays.asList(runnerConfig));

    progressHandler = Mockito.mock(ProgressHandler.class);

    randomStringProvider = BenchmarkManagerTest.cast(Mockito.mock(Provider.class));

    worker = new BenchmarkWorker(compiler, runnerProvider, moduleTemplate, benchmarkData,
        progressHandler, ip, port, benchmarkCompileOutputDir, randomStringProvider);
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(benchmarkCompileOutputDir);
  }

  @Test
  public void testCompilerError() throws BenchmarkCompilerException {

    BenchmarkCompiler compiler = Mockito.mock(BenchmarkCompiler.class);
    Factory runnerProvider = Mockito.mock(Runner.Factory.class);

    Mockito.when(randomStringProvider.get()).thenReturn("randomDir1");

    String moduleTemplate = "mytemplate [{module_nocache}]";
    String moduleName = "moduleName1";
    BenchmarkWorkerConfig benchmarkData =
        new BenchmarkWorkerConfig(moduleName, Arrays.asList(RunnerConfigs.CHROME_LINUX));

    ProgressHandler progressHandler = Mockito.mock(ProgressHandler.class);
    String ip = "127.0.0.1";
    File benchmarkCompileOutputDir = new File("./target/test/TestBenchmarkWorker");
    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");

    BenchmarkWorker worker = new BenchmarkWorker(compiler, runnerProvider, moduleTemplate,
        benchmarkData, progressHandler, ip, 8080, benchmarkCompileOutputDir, randomStringProvider);

    Mockito.doThrow(new BenchmarkCompilerException("test")).when(compiler)
        .compile(moduleName, workDir);

    worker.run();

    Mockito.verify(compiler).compile(Mockito.eq(moduleName), Mockito.<File>anyObject());
    Mockito.verify(progressHandler).onCompilationFailed(Mockito.anyString());
    Mockito.verifyZeroInteractions(runnerProvider);
    Assert.assertFalse(workDir.exists());
  }

  @Test
  public void testBenchmarkWorker() throws BenchmarkCompilerException {

    Mockito.when(runner.isFailed()).thenReturn(false);
    Mockito.when(runner.getResult()).thenReturn(Double.valueOf(1337));
    Mockito.when(runner.getConfig()).thenReturn(runnerConfig);

    Mockito.when(randomStringProvider.get()).thenReturn("randomDir1");

    Mockito.when(runnerProvider.create(RunnerConfigs.CHROME_LINUX,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html")).thenReturn(
        runner);

    worker.run();

    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");

    Mockito.verify(compiler).compile(moduleName, workDir);

    Mockito.verify(runnerProvider).create(runnerConfig,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html");

    Mockito.verify(progressHandler).onResult(runnerConfig, 1337);
    Mockito.verify(progressHandler).onRunEnded();

    Assert.assertFalse(workDir.exists());
  }

  @Test
  public void testBenchmarkWorkerWithFailingRuns() throws BenchmarkCompilerException {

    Mockito.when(runner.isFailed()).thenReturn(true);
    Mockito.when(runner.getResult()).thenReturn(Double.valueOf(1337));
    Mockito.when(runner.getConfig()).thenReturn(runnerConfig);
    Mockito.when(randomStringProvider.get()).thenReturn("randomDir1");

    Mockito.when(runnerProvider.create(RunnerConfigs.CHROME_LINUX,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html")).thenReturn(
        runner);

    worker.run();
    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");

    Mockito.verify(compiler).compile(moduleName, workDir);

    Mockito.verify(runnerProvider).create(runnerConfig,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html");

    Mockito.verify(progressHandler).failedToRunBenchmark(Mockito.eq(runnerConfig),
        Mockito.anyString());
    Mockito.verify(progressHandler).onRunEnded();

    Assert.assertFalse(workDir.exists());
  }

  @Test
  public void testModuleFileIsBeingWritten() throws BenchmarkCompilerException,
      FileNotFoundException, IOException {

    worker = new BenchmarkWorker(compiler, runnerProvider, moduleTemplate, benchmarkData,
        progressHandler, ip, port, benchmarkCompileOutputDir, randomStringProvider) {
      @Override
      void cleanupDirectory(File outputDir) {
        // do nothing so we can see if the directory has the right content
      }
    };

    Mockito.when(runner.isFailed()).thenReturn(false);
    Mockito.when(runner.getResult()).thenReturn(Double.valueOf(1337));
    Mockito.when(runner.getConfig()).thenReturn(runnerConfig);

    Mockito.when(randomStringProvider.get()).thenReturn("randomDir1");

    Mockito.when(runnerProvider.create(RunnerConfigs.CHROME_LINUX,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html")).thenReturn(
        runner);

    worker.run();

    File workDir = new File(benchmarkCompileOutputDir, "randomDir1");

    Mockito.verify(compiler).compile(moduleName, workDir);

    Mockito.verify(runnerProvider).create(runnerConfig,
        "http://" + ip + ":" + port + "/__bench/randomDir1/" + moduleName + ".html");

    Mockito.verify(progressHandler).onResult(runnerConfig, 1337);
    Mockito.verify(progressHandler).onRunEnded();

    Assert.assertTrue(workDir.exists());

    String hostPageContent =
        IOUtils.toString(new FileInputStream(new File(workDir, moduleName + ".html")), "UTF-8");

    Assert.assertEquals(moduleName + "/" + moduleName + ".nocache.js", hostPageContent);
  }
}
