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
package com.google.gwt.benchmark.framework.client;

import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;
import com.google.gwt.benchmark.framework.shared.BenchmarkResult;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Implementation of {@link BenchmarkResult} for JavaScript code.
 * <p>
 * Note: This is a pure model object and does not contain any logic.
 */
public final class BenchmarkResultJsImpl extends JavaScriptObject implements BenchmarkResult {

  public native static BenchmarkResultJsImpl create() /*-{
    return {name: "", numberOfRuns: 0, totalTimePassedMs: 0, timesForIndividualRunsMs: []};
  }-*/;

  @Override
  public native String getName() /*-{
    return this.name;
  }-*/;

  @Override
  public native int getNumberOfRuns() /*-{
    return this.numberOfRuns;
  }-*/;

  @Override
  public native JavaScriptArrayNumber getTimesForIndividualRunsMs() /*-{
    return this.timesForIndividualRunsMs;
  }-*/;

  @Override
  public native double getTotalTimePassedMs() /*-{
    return this.totalTimePassedMs;
  }-*/;

  @Override
  public native void setName(String name) /*-{
    this.name = name;
  }-*/;

  @Override
  public native void setNumberOfRuns(int runs) /*-{
    this.numberOfRuns = runs;
  }-*/;

  @Override
  public native void setTimesForIndividualRunsMs(
      JavaScriptArrayNumber timesForIndividualRunsMs) /*-{
    this.timesForIndividualRunsMs = timesForIndividualRunsMs;
  }-*/;

  @Override
  public native void setTotalTimePassed(double time) /*-{
    this.totalTimePassedMs = time;
  }-*/;

  protected BenchmarkResultJsImpl() {
  }
}
