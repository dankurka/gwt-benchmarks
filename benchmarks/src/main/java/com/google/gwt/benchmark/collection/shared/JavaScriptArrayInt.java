/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.collection.shared;

/**
 * A simple interface for JavaScript arrays containing only numbers.
 * <p>
 * Using these interfaces allows benchmarks to run on a JVM and be compiled to JavaScript.
 */
public interface JavaScriptArrayInt {
  int get(int i);

  int length();

  void push(int t);

  void set(int i, int d);
}
