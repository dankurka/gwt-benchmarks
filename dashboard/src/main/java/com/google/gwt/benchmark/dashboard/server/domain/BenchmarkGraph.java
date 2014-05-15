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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A BenchmarkGraph contains all results for a certain module and runner for one week.
 */
public class BenchmarkGraph {

  public static final String NAME = "Graph";

  public static Key createKey(String benchmarkName, String runnerId, int week, int year) {
    return KeyFactory.createKey(NAME,
        BenchmarkGraph.createName(benchmarkName, runnerId, week, year));
  }

  private static String createName(String module, String runnerId, int week, int year) {
    return module + "_" + runnerId + "_" + week + "_" + year;
  }


  public  static Query createQuery(String benchmarkName, int week, int year) {
    Query query = new Query(BenchmarkGraph.NAME);
    Filter moduleFilter = new Query.FilterPredicate("module", FilterOperator.EQUAL, benchmarkName);
    Filter weekFilter = new Query.FilterPredicate("week", FilterOperator.EQUAL, week);
    Filter yearFilter = new Query.FilterPredicate("year", FilterOperator.EQUAL, year);

    Filter compositeFilter =
        Query.CompositeFilterOperator.and(moduleFilter, weekFilter, yearFilter);

    query.setFilter(compositeFilter);
    return query;
  }

  private Entity entity;

  /**
   * Creates a new, empty graph.
   */
  public BenchmarkGraph(String module, String runnerId, int week, int year) {
    entity = new Entity(createKey(module, runnerId, week, year));
    setModule(module);
    setYear(year);
    setWeek(week);
    setCommitIds(new ArrayList<String>());
    setRunsPerSecond(new ArrayList<Double>());
    setRunnerId(runnerId);
  }

  /**
   * Wraps an existing graph loaded from the datastore.
   */
  public BenchmarkGraph(Entity entity) {
    this.entity = entity;
  }

  private void setWeek(int week) {
    entity.setProperty("week", week);
  }

  public int getWeek() {
    return ((Long) entity.getProperty("week")).intValue();
  }

  private void setYear(int year) {
    entity.setProperty("year", year);
  }

  public int getYear() {
    return ((Long) entity.getProperty("year")).intValue();
  }

  @SuppressWarnings("unchecked")
  public List<String> getCommitIds() {
    return Collections.unmodifiableList((List<String>) entity.getProperty("commitIds"));
  }

  public void setCommitIds(List<String> commitIds) {
    entity.setProperty("commitIds", commitIds);
  }

  private void setModule(String module) {
    entity.setProperty("module", module);
  }

  public String getModule() {
    return (String) entity.getProperty("module");
  }

  public String getRunnerId() {
    return (String) entity.getProperty("runnerId");
  }

  private void setRunnerId(String runnerId) {
    entity.setProperty("runnerId", runnerId);
  }

  public void setRunsPerSecond(List<Double> runsPerSecond) {
    entity.setProperty("runsPerSecond", runsPerSecond);
  }

  @SuppressWarnings("unchecked")
  public List<Double> getRunsPerSecond() {
    return Collections.unmodifiableList((List<Double>) entity.getProperty("runsPerSecond"));
  }

  public Entity getEntity() {
    return entity;
  }
}
