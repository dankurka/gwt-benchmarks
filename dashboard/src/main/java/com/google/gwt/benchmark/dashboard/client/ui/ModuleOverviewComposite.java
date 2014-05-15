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

import com.google.gwt.benchmark.dashboard.shared.service.DashboardServiceAsync;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * This widget displays the overview of all benchmarks.
 */
public class ModuleOverviewComposite extends Composite {

  interface Binder extends UiBinder<Widget, ModuleOverviewComposite> {
  }

  private static Binder uiBinder = GWT.create(Binder.class);

  private final DashboardServiceAsync service;
  private final Provider<Label> labelProvider;
  private final List<HandlerRegistration> handlers = new ArrayList<>();
  private final HistoryAccessor history;

  @UiField
  Panel contentContainer;

  @UiField
  Label errorLabel;

  @UiField
  Label loadingLabel;

  @Inject
  public ModuleOverviewComposite(DashboardServiceAsync service, Provider<Label> labelProvider, HistoryAccessor history) {
    this.service = service;
    this.labelProvider = labelProvider;
    this.history = history;
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void start() {
    loadBenchmarks();
  }

  private void loadBenchmarks() {
    errorLabel.setVisible(false);
    loadingLabel.setVisible(true);
    contentContainer.setVisible(false);

    service.getLatestBenchmarkNames(new AsyncCallback<ArrayList<String>>() {

      @Override
      public void onFailure(Throwable caught) {
        errorLabel.setVisible(true);
        errorLabel.setText("Can not load benchmarks");
        loadingLabel.setVisible(false);
      }

      @Override
      public void onSuccess(ArrayList<String> result) {
        renderGraphList(result);
      }
    });
  }

  private void renderGraphList(List<String> benchmarkNames) {
    loadingLabel.setVisible(false);
    contentContainer.setVisible(true);
    contentContainer.clear();

    for (final String benchmarkName : benchmarkNames) {
      Label label = labelProvider.get();
      handlers.add(label.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          ModuleOverviewComposite.this.onClick(benchmarkName);
        }
      }));
      label.setText(benchmarkName);
      contentContainer.add(label);
    }
  }

  private void onClick(String module) {
    history.newItem("!graph?benchmark=" + module, true);
  }
}
