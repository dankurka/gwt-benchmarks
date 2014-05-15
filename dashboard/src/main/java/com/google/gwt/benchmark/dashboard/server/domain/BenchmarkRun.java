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

import java.util.Collections;
import java.util.List;

/**
 * A BenchmarkRun contains all information for one execution of all benchmarks.
 */
public class BenchmarkRun {

  public static final String NAME = "BenchmarkRun";

  public static Key createKey(String commitId) {
    return KeyFactory.createKey(NAME, commitId);
  }

  private Entity entity;

  public BenchmarkRun(String commitId, long commitTimeMsEpoch) {
    entity = new Entity(createKey(commitId));
    entity.setProperty("commitId", commitId);
    entity.setProperty("commitTimeMsEpoch", commitTimeMsEpoch);
  }

  public BenchmarkRun(Entity entity) {
    this.entity = entity;
  }

  public void setRunnerIds(List<String> runnerIds) {
    entity.setProperty("runnerIds", runnerIds);
  }

  @SuppressWarnings("unchecked")
  public List<String> getRunnerIds() {
    return Collections.unmodifiableList((List<String>) entity.getProperty("runnerIds"));
  }

  @SuppressWarnings("unchecked")
  public List<Key> getResults() {
    return Collections.unmodifiableList((List<Key>) entity.getProperty("results"));
  }

  public void setResults(List<Key> results) {
    entity.setProperty("results", results);
  }

  public String getCommitId() {
    return (String) entity.getProperty("commitId");
  }

  public long getCommitTimeMsEpoch() {
    return (long) entity.getProperty("commitTimeMsEpoch");
  }

  public Key getKey() {
    return entity.getKey();
  }

  public Entity getEntity() {
    return entity;
  }
}
