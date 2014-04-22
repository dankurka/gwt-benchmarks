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
package com.google.gwt.benchmark.benchmarks.octane.client.richards.gwt;

import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

public class RichardsGWTBenchmark extends AbstractBenchmark {

  /**
   * EntryPoint or RichardGWT benchmark.
   */
  public static class EntryPoint extends AbstractBenchmarkEntryPoint {

    @Override
    protected AbstractBenchmark getBenchmark() {
      return new RichardsGWTBenchmark();
    }
  }

  private Richards richards;

  public RichardsGWTBenchmark() {
    super("RichardsGWTBenchmark");
  }

  @Override
  public void setupOneTime() {
    richards = new Richards();
  }

  @Override
  public Object run() {
    richards.runRichards();
    return null;
  }

  @Override
  public void tearDownOneTime() {
    richards = null;
  }
}
