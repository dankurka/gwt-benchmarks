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
package com.google.gwt.benchmark.dashboard.server.domain;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A BenchmarkResult contains the runs per minute for one module on one runner.
 */
public class BenchmarkResult {

  public static Key createKey(Key run, String benchmarkName, String runnerId) {
    String name = createName(benchmarkName, runnerId);
    return KeyFactory.createKey(run, NAME, name);
  }

  private static String createName(String benchmarkName, String runnerId) {
    return benchmarkName + "&" + runnerId;
  }

  public static final String NAME = "BenchmarkResult";

  private Entity entity;

  public BenchmarkResult(Key run, String benchmarkName, String runnerId) {
    entity = new Entity(createKey(run, benchmarkName, runnerId));
    entity.setProperty("runnerId", runnerId);
    entity.setProperty("benchmarkName", benchmarkName);
    setRunsPerMinute(0);
  }

  public BenchmarkResult(Entity entity) {
    this.entity = entity;
  }

  public String getBenchmarkName() {
    return (String) entity.getProperty("benchmarkName");
  }

  public String getRunnerId() {
    return (String) entity.getProperty("runnerId");
  }

  public void setRunsPerMinute(double runsPerMinute) {
    entity.setProperty("runsPerMinute", runsPerMinute);
  }

  public double getRunsPerMinute() {
    return (double) entity.getProperty("runsPerMinute");
  }

  public Key getKey() {
    return entity.getKey();
  }

  public Entity getEntity() {
    return entity;
  }
}
