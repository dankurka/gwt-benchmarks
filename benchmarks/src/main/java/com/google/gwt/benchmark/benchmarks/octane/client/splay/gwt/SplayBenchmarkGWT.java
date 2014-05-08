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
package com.google.gwt.benchmark.benchmarks.octane.client.splay.gwt;

import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

public class SplayBenchmarkGWT extends AbstractBenchmark {

  /**
   * EntryPoint for SplayGWT benchmark.
   */
  public static class EntryPoint extends AbstractBenchmarkEntryPoint {

    @Override
    protected AbstractBenchmark getBenchmark() {
      return new SplayBenchmarkGWT();
    }
  }

  private Splay splay;

  public SplayBenchmarkGWT() {
    super("SplayBenchmarkGWT");
  }

  @Override
  public Object run() {
    splay.SplayRun();
    return null;
  }

  @Override
  public void setupOneTime() {
    splay = new Splay();
    splay.SplaySetup();
  }

  @Override
  public void tearDownOneTime() {
    splay.SplayTearDown();
    splay = null;
  }
}
