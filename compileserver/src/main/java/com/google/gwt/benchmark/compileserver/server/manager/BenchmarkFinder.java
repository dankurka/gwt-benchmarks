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

import com.google.inject.name.Named;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * BenchmarkFinder will traverse a directory structure and find all benchmarks in it.
 * <p>
 * Currently benchmarks need to be located under com.google.gwt.benchmark.benchmarks in order to get
 * picked up by the collector and have the word 'Benchmark' in them, e.g. 'MyCoolBenchmark.gwt.xml'.
 */
public class BenchmarkFinder {

  private File benchmarkSourceLocation;

  @Inject
  public BenchmarkFinder(@Named("benchmarkSourceLocation") File benchmarkSourceLocation) {
    this.benchmarkSourceLocation = benchmarkSourceLocation;
  }

  /**
   * Returns a list of names of available benchmark modules.
   */
  public List<String> get() {
    return traverse(benchmarkSourceLocation);
  }

  private List<String> traverse(File file) {
    List<String> moduleNames = new ArrayList<>();
    traverse(file, file, moduleNames);
    Collections.sort(moduleNames);
    return moduleNames;
  }

  private void traverse(File root, File file, List<String> moduleNames) {
    if (file.isDirectory()) {
      File[] listFiles = file.listFiles();
      for (File newFile : listFiles) {
        traverse(root, newFile, moduleNames);
      }
    } else if (file.isFile()) {
      if (file.getName().endsWith("gwt.xml") && file.getName().contains("Benchmark")) {
        String path = file.getAbsolutePath();
        String relative = root.toURI().relativize(new File(path).toURI()).getPath();
        String moduleName = relative.replaceAll("\\/", "\\.");
        moduleName = moduleName.substring(0, moduleName.length() - ".gwt.xml".length());

        if (moduleName.startsWith("com.google.gwt.benchmark.benchmarks.")) {
          moduleNames.add(moduleName);
        }
      }
    }
  }
}
