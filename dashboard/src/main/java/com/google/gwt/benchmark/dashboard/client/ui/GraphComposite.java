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

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.benchmark.dashboard.shared.service.DashboardServiceAsync;
import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.LineChart.Options;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This widget fetches benchmark runs from the server and renders a graph.
 */
public class GraphComposite extends Composite {

  interface Binder extends UiBinder<Widget, GraphComposite> {
  }

  private static Binder uiBinder = GWT.create(Binder.class);

  private boolean graphWidgetLoaded;
  private boolean dataReady;
  private BenchmarkResultsTable result;
  private Set<String> runnerIds = new TreeSet<>();

  private final List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

  private String benchmarkName;
  private int week;
  private int year;

  private final HistoryAccessor history;
  private final DashboardServiceAsync service;
  private final Provider<CheckBox> checkboxProvider;

  @UiField
  Button backButton;

  @UiField
  Button forwardButton;

  @UiField
  Panel checkBoxContainer;

  @UiField
  GraphWidget graphWidget;

  @UiField
  Label errorLabel;

  @UiField
  Label loadingLabel;

  @UiField
  Widget container;

  @UiField
  Label weekLabel;

  @Inject
  public GraphComposite(DashboardServiceAsync service, Provider<CheckBox> checkboxProvider,
      HistoryAccessor history) {
    this.service = service;
    this.checkboxProvider = checkboxProvider;
    this.history = history;
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void start() {
    graphWidgetLoaded = false;
    dataReady = false;
    if (parseHistory(history.getToken())) {
      loadData();

      graphWidget.init(new Runnable() {

          @Override
        public void run() {
          graphWidgetLoaded = true;
          maybeRender();
        }
      });
    }
  }

  private void maybeRender() {
    if (!graphWidgetLoaded || !dataReady) {
      return;
    }
    resetView();
    renderCheckBoxes();
    renderGraph();
  }

  private void renderGraph() {
    if (result.getColumnCount() == 0) {
      graphWidget.clear();
      return;
    }

    DataTable data = graphWidget.createData();
    data.addColumn(ColumnType.STRING, "Commits");

    for (String runnerId : result.getAllRunnerIds()) {
      if (!runnerIds.contains(runnerId)) {
        continue;
      }
      data.addColumn(ColumnType.NUMBER, runnerId);
    }

    data.addRows(result.getRowCount());
    for (int i = 0; i < result.getRowCount(); i++) {
      data.setValue(i, 0, result.getCommitIds().get(i));
    }

    int skippedColumn = 0;
    for (int columnIndex = 0; columnIndex < result.getColumnCount(); columnIndex++) {
      if (!runnerIds.contains(result.getRunnerId(columnIndex))) {
        skippedColumn++;
        continue;
      }
      for (int rowIndex = 0; rowIndex < result.getRowCount(); rowIndex++) {
        data.setValue(rowIndex, columnIndex + 1 - skippedColumn,
            result.getRunsPerSecond(columnIndex, rowIndex));
      }
    }

    Options options = graphWidget.createOptions();
    options.setWidth(800);
    options.setHeight(600);
    options.setTitle(result.getBenchmarkName());
    graphWidget.displayChart(options, data);
  }

  private void renderCheckBoxes() {
    for (final String runner : result.getAllRunnerIds()) {
      CheckBox checkBox = checkboxProvider.get();
      checkBox.setValue(runnerIds.contains(runner));
      checkBox.setText(runner);
      handlers.add(checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          onCheckboxClicked(runner, event.getValue());
        }
      }));
      checkBoxContainer.add(checkBox);
    }
  }

  private void resetView() {
    for (HandlerRegistration hr : handlers) {
      hr.removeHandler();
    }
    handlers.clear();
    checkBoxContainer.clear();
    container.setVisible(true);
    weekLabel.setText(result.getWeekName());
  }

  private void onCheckboxClicked(String runners, boolean value) {
    if (value) {
      runnerIds.add(runners);
    } else {
      runnerIds.remove(runners);
    }

    if (runnerIds.isEmpty()) {
      runnerIds = new TreeSet<>(result.getAllRunnerIds());
    }
    history.newItem(createHistoryToken(), false);
    maybeRender();
  }

  private void loadData() {
    loadingLabel.setVisible(true);
    errorLabel.setVisible(false);
    container.setVisible(false);

    AsyncCallback<BenchmarkResultsTable> callback = new AsyncCallback<BenchmarkResultsTable>() {

      @Override
      public void onFailure(Throwable caught) {
        errorLabel.setText("Error while fetching graphs: " + caught.getMessage());
        errorLabel.setVisible(true);
        loadingLabel.setVisible(false);
      }

      @Override
      public void onSuccess(BenchmarkResultsTable result) {
        loadingLabel.setVisible(false);
        GraphComposite.this.result = result;
        if (GraphComposite.this.runnerIds.isEmpty()) {
          GraphComposite.this.runnerIds =  new TreeSet<String>(result.getAllRunnerIds());
        }
        GraphComposite.this.week = result.getWeek();
        GraphComposite.this.year = result.getYear();
        dataReady = true;
        maybeRender();
      }
    };

    if (week == -1 || year == -1) {
      service.getLatestGraphs(benchmarkName, callback);
    } else {
      service.getGraphs(benchmarkName, week, year, callback);
    }
  }

  //Visible for testing
  @UiHandler("backButton")
  void onBackButtonClicked(@SuppressWarnings("unused") ClickEvent e) {
    // until the next time we have 54 weeks in a year (2028),
    // this system will not be running anymore
    // and even then we won't be working over new year either.
    if (week - 1 < 1) {
      year -= 1;
      week = 53;
    } else {
      week -= 1;
    }

    history.newItem(createHistoryToken(), false);
    loadData();
  }

  // Visible for testing
  @UiHandler("forwardButton")
  void onForwardButtonClicked(@SuppressWarnings("unused") ClickEvent e) {
    // until the next time we have 54 weeks in a year (2028),
    // this system will not be running anymore
    // and even then we won't be working over new year either.
    if (week + 1 > 53) {
      year += 1;
      week = 1;
    } else {
      week += 1;
    }

    history.newItem(createHistoryToken(), false);
    loadData();
  }

  private String createHistoryToken() {
    ImmutableMap<String, Object> params = ImmutableMap.<String, Object> of(
        "benchmark", benchmarkName, "w", week, "y", year, "rids", runnerIds);
    return "!graph?" + Joiner.on("&").withKeyValueSeparator("=").join(params);
  }

  private boolean parseHistory(String token) {
    if (!token.startsWith("!graph?")) {
      history.newItem("", true);
      return false;
    }

    token = token.substring("!graph?".length());
    Map<String, String> split = null;
    try {
      split = Splitter.on("&").withKeyValueSeparator("=").split(token);
    } catch (IllegalArgumentException e) {
      history.newItem("", true);
      return false;
    }

    benchmarkName = split.get("benchmark");
    if (benchmarkName == null) {
      history.newItem("", true);
      return false;
    }

    String weekString = split.get("w");
    String yearString = split.get("y");

    if (weekString == null || yearString == null) {
      year = -1;
      week = -1;
      runnerIds = new TreeSet<>();
      return true;
    }

    week = Integer.parseInt(weekString);
    year = Integer.parseInt(yearString);

    Splitter splitter = Splitter.on(",").trimResults(
        CharMatcher.anyOf("[]").or(CharMatcher.WHITESPACE));

    runnerIds = new TreeSet<String>(splitter.splitToList(split.get("rids")));
    return true;
  }
}
