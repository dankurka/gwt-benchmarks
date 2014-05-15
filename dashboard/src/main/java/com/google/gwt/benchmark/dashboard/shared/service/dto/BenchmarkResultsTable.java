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
package com.google.gwt.benchmark.dashboard.shared.service.dto;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A table of results for one benchmark and one week.
 * There is a row for each commit at which the benchmark was run, and a column for each variation
 * of the benchmark.
 * <p>
 * The columns are in chronological order; the most recent is first. The rows are ordered by their
 * corresponding runner ids.
 */
public class BenchmarkResultsTable implements Serializable {

  public static BenchmarkResultsTable create(String benchmarkName, String weekName, int year,
      int week, Collection<String> commitIds, Collection<String> runnerIds,
      Collection<double[]> cellData) {
    checkArgument(runnerIds.size() == cellData.size());
    for (double[] row : cellData) {
      checkArgument(row.length == commitIds.size());
    }

    BenchmarkResultsTable table = new BenchmarkResultsTable();
    table.allRunnerIds = Lists.newArrayList(runnerIds);
    table.benchmarkName = benchmarkName;
    table.commitIds = Lists.newArrayList(commitIds);
    table.runnerResultList = Lists.newArrayList(cellData);
    table.weekName = weekName;
    table.year = year;
    table.week = week;
    return table;
  }

  private ArrayList<double[]> runnerResultList;
  private ArrayList<String> commitIds;

  private ArrayList<String> allRunnerIds;
  private String benchmarkName;

  private String weekName;
  private int year;

  private int week;

  protected BenchmarkResultsTable() {
  }

  public ArrayList<String> getCommitIds() {
    return commitIds;
  }

  /** The name of the benchmark shown in this table */
  public String getBenchmarkName() {
    return benchmarkName;
  }

  /**
   * The commit id for a column.
   */
  public String getCommitId(int columnIndex) {
    return commitIds.get(columnIndex);
  }

  /**
   * The runner id for a row.
   */
  public String getRunnerId(int rowIndex) {
    return allRunnerIds.get(rowIndex);
  }

  /**
   * The data for one cell in the table.
   */
  public double getRunsPerSecond(int rowIndex, int columnIndex) {
    return runnerResultList.get(rowIndex)[columnIndex];
  }

  /**
   * All runner ids for this benchmark in this week.
   */
  public ArrayList<String> getAllRunnerIds() {
    return allRunnerIds;
  }

  public int getRowCount() {
    return commitIds.size();
  }

  public int getColumnCount() {
    return allRunnerIds.size();
  }

  /** The ISO-8601 week number. */
  public int getWeek() {
    return week;
  }

  /** The ISO-8601 week number. */
  public int getYear() {
    return year;
  }

  /**
   * A user-friendly description of the week shown in this table.
   */
  public String getWeekName() {
    return weekName;
  }
}
