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
import com.google.j2cl.benchmark.common.runner.Job;
import com.google.j2cl.benchmark.common.runner.JobId;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;
import com.google.j2cl.benchmark.common.runner.RunnerConfigs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Test for {@link RunZip}.
 */
public class RunZipTest {
  private BenchmarkUploader benchmarkUploader;
  private RunZip runZip;

  private PrintStream sysout;
  private ByteArrayOutputStream outputBytes;

  @Before
  public void before() {
    sysout = System.out;
    benchmarkUploader = mock(BenchmarkUploader.class);

    runZip = new RunZip() {
      @Override
      BenchmarkUploader createBenchmarkUploader(String serverUrl, File zip,
          List<RunnerConfig> runnerConfigs) {
        return benchmarkUploader;
      }
    };

    outputBytes = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputBytes));
  }

  @After
  public void after() {
    System.setOut(sysout);
  }

  @Test
  public void testSimpleUpload() throws IOException, InterruptedException {
    String[] args = new String[] {
        "-benchmark",
        "foo.zip",
        "-runnerServerUrl",
        "http://foo.bar/"
    };

    Job job = new Job(new JobId("id1"), Lists.newArrayList(RunnerConfigs.CHROME_LINUX), 1);
    job.addResult(RunnerConfigs.CHROME_LINUX, 2.0);
    when(benchmarkUploader.run(true)).thenReturn(job);

    runZip.doMain(args);
    String output = outputBytes.toString("UTF8");
    assertThat(output).isEqualTo("Results:\n" + "linux chrome: 2.0\n");
  }
}
