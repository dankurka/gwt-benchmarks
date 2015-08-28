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



import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Tests for {@link BenchmarkFinder}.
 */
public class BenchmarkFinderTest {

  @Test
  public void testCollectionFromFileSystem() {
    BenchmarkFinder finder = new BenchmarkFinder(new File("./src/test/resources/collector-test/"));
    List<String> list = finder.get();

    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.contains("com.google.gwt.benchmark.benchmarks.TestBenchmark"));
    Assert.assertTrue(list.contains("com.google.gwt.benchmark.benchmarks.package.TestBenchmark2"));
  }
}