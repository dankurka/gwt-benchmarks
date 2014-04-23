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
package com.google.gwt.benchmark.compileserver.client.status;

import com.google.gwt.benchmark.compileserver.shared.ServiceAsync;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewEntryDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewResponseDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkRunDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkRunDTO.State;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A view that displays the status of the compile server.
 */
public class BenchmarkStatusComposite extends Composite {

  interface Bundle extends ClientBundle {

    @Source("benchmarkstatus.css")
    Css css();
  }

  interface Css extends CssResource {
    String clickable();

    String statusFatalError();

    String statusOneFailed();

    String statusDone();

    String statusRunning();
  }

  interface Binder extends UiBinder<Widget, BenchmarkStatusComposite> {
  }

  private static final Binder uiBinder = GWT.create(Binder.class);

  // Visible for testing
  static final Bundle bundle = GWT.create(Bundle.class);

  private final NumberFormat format;
  private final List<HandlerRegistration> handlers = new ArrayList<>();
  private final Provider<Label> labelProvider;
  private BenchmarkOverviewResponseDTO result;
  private boolean running;
  private final ServiceAsync service;

  @UiField
  Widget contentContainer;

  @UiField
  Label errorLabel;

  @UiField
  Grid grid;

  @UiField
  Widget loadingLabel;

  @UiField
  Label statusText;

  @UiField
  Button startStopButton;


  @Inject
  public BenchmarkStatusComposite(ServiceAsync service, NumberFormat format,
      Provider<Label> labelProvider) {
    this.service = service;
    this.labelProvider = labelProvider;
    this.format = format;
    initWidget(uiBinder.createAndBindUi(this));
    bundle.css().ensureInjected();
  }

  public void start() {
    loadBenchmarks();
  }

  @UiHandler("startStopButton")
  protected void onStartButtonPressed(@SuppressWarnings("unused") ClickEvent e) {

    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        alert("Error while starting/stopping service");
      }

      @Override
      public void onSuccess(Void result) {
        loadBenchmarks();
      }
    };

    if (running) {
      service.stopServer(callback);
    } else {
      service.startServer(callback);
    }
  }

  private void addClickHandler(Label label, final int row, final int column) {
    handlers.add(label.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        onGridEntryClicked(row, column);
      }
    }));
    label.addStyleName(bundle.css().clickable());
  }

  private void loadBenchmarks() {
    loadingLabel.setVisible(true);
    errorLabel.setVisible(false);
    contentContainer.setVisible(false);
    statusText.setText("");
    startStopButton.setVisible(false);

    service.loadBenchmarkOverview(new AsyncCallback<BenchmarkOverviewResponseDTO>() {

        @Override
      public void onFailure(Throwable caught) {
        errorLabel.setVisible(true);
        errorLabel.setText("Can not load benchmarks");
        loadingLabel.setVisible(false);
      }

        @Override
      public void onSuccess(BenchmarkOverviewResponseDTO result) {
        renderResult(result);
      }
    });
  }

  private void renderResult(BenchmarkOverviewResponseDTO result) {
    this.result = result;
    this.running = result.isExecutingBenchmarks();

    removeHandlers();

    loadingLabel.setVisible(false);
    contentContainer.setVisible(true);
    startStopButton.setVisible(true);
    startStopButton.setText(result.isExecutingBenchmarks() ? "Stop executing" : "Start executing");
    statusText.setText(result.isExecutingBenchmarks() ? "System is running" : "System is idle");
    grid.clear();

    if (result.isHasLatestRun()) {
      int columns = result.getRunnerNames().size() + 1;
      int rows = result.getBenchmarks().size() + 1;
      grid.resize(rows, columns);
      renderHeader(result.getRunnerNames());
      renderEntries();
    }
  }

  private void removeHandlers() {
    for (HandlerRegistration hr : handlers) {
      hr.removeHandler();
    }
    handlers.clear();
  }

  private void onGridEntryClicked(int row, int column) {
    if (row == 0) {
      return;
    }

    BenchmarkOverviewEntryDTO entry = result.getBenchmarks().get(row - 1);

    String errorMessage = entry.getErrorMessage();
    if (errorMessage != null) {
      alert(errorMessage);
    } else {
      errorMessage = entry.getBenchmarkRuns().get(column - 1).getErrorMessage();
      if (errorMessage != null) {
        alert(errorMessage);
      }
    }
  }

  private void renderEntries() {
    for (int row = 0; row < result.getBenchmarks().size(); row++) {
      BenchmarkOverviewEntryDTO entry = result.getBenchmarks().get(row);
      Label gridEntry = labelProvider.get();
      gridEntry.setText(entry.getBenchmarkName());
      grid.setWidget(row + 1, 0, gridEntry);
      switch (entry.getState()) {
        case AT_LEAST_ONE_FAILED:
          gridEntry.addStyleName(bundle.css().statusOneFailed());
          for (int column = 0; column < entry.getBenchmarkRuns().size(); column++) {
            BenchmarkRunDTO benchmarkRun = entry.getBenchmarkRuns().get(column);
            Label gridNumberEntry = labelProvider.get();
            if (benchmarkRun.getState() == State.DONE) {
              gridNumberEntry.setText(format.format(benchmarkRun.getRunsPerMinute()));
            } else {
              gridNumberEntry.setText("Error");
              addClickHandler(gridNumberEntry, row + 1, column + 1);
            }
            grid.setWidget(row + 1, column + 1, gridNumberEntry);
          }
          break;
        case DONE:
          gridEntry.addStyleName(bundle.css().statusDone());
          for (int column = 0; column < entry.getBenchmarkRuns().size(); column++) {
            BenchmarkRunDTO benchmarkRun = entry.getBenchmarkRuns().get(column);
            Label gridNumberEntry = labelProvider.get();
            gridNumberEntry.setText(format.format(benchmarkRun.getRunsPerMinute()));
            grid.setWidget(row + 1, column + 1, gridNumberEntry);
          }
          break;
        case FATAL_ERROR:
          gridEntry.addStyleName(bundle.css().statusFatalError());
          addClickHandler(gridEntry, row + 1, 0);
          break;
        case RUNNING:
          gridEntry.addStyleName(bundle.css().statusRunning());
          break;
      }
    }
  }

  private void renderHeader(ArrayList<String> runnerNames) {
    Label nameEntry = labelProvider.get();
    nameEntry.setText("Benchmark Name");
    grid.setWidget(0, 0, nameEntry);

    for (int i = 0; i < runnerNames.size(); i++) {
      Label entry = labelProvider.get();
      entry.setText(runnerNames.get(i));
      grid.setWidget(0, i + 1, entry);
    }
  }

  // Visible for testing
  void alert(String message) {
    Window.alert(message);
  }
}
