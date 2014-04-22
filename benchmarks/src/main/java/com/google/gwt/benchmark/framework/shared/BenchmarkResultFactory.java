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
package com.google.gwt.benchmark.framework.shared;

import com.google.gwt.benchmark.framework.client.BenchmarkResultJsImpl;
import com.google.gwt.benchmark.framework.shared.ui.BenchmarkResultJavaImpl;
import com.google.gwt.core.client.GWT;

/**
 * Factory to create {@link BenchmarkResult}.
 */
public class BenchmarkResultFactory {
  /**
   * Create a {@link BenchmarkResult}.
   *
   * @return the created {@link BenchmarkResult}
   */
  public static BenchmarkResult create() {
    if (GWT.isScript()) {
      return BenchmarkResultJsImpl.create();
    } else {
      return new BenchmarkResultJavaImpl();
    }
  }
}
