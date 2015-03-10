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

import java.io.File;

/**
 * Implementing classes can compile a given GWT module.
 */
public interface BenchmarkCompiler {
  /**
   * Invokes the GWT compiler for the specified module.
   */
  public void compile(String moduleName, File outputDir, File devJar, File userJar) throws BenchmarkCompilerException;
}
