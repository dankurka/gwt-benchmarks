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
package com.google.gwt.benchmark.dashboard.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.LineChart.Options;

/**
 * This widget provides access to the visualization API.
 */
public class GraphWidget extends Composite {

  private SimplePanel chartContainer = new SimplePanel();

  public GraphWidget() {
    initWidget(chartContainer);
  }

  public DataTable createData() {
    return DataTable.create();
  }

  public Options createOptions() {
    return Options.create();
  }

  public void clear() {
    chartContainer.clear();
  }

  public void displayChart(Options options, DataTable data) {
    LineChart lineChart = new LineChart(data, options);
    chartContainer.setWidget(lineChart);
  }

  public void init(Runnable r) {
    VisualizationUtils.loadVisualizationApi(r, LineChart.PACKAGE);
  }
}
