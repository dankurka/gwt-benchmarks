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
import com.google.gwt.benchmark.dashboard.shared.service.dto.BenchmarkResultsTable;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test for {@link GraphComposite}.
 */
@RunWith(GwtMockitoTestRunner.class)
public class GraphCompositeTest {

  private static class CheckboxHolder {
    CheckBox checkBox;
    HandlerRegistration handlerRegistration;

    public CheckboxHolder(CheckBox checkBox, HandlerRegistration handlerRegistration) {
      this.checkBox = checkBox;
      this.handlerRegistration = handlerRegistration;
    }
  }

  private GraphComposite composite;

  @Mock private DashboardServiceAsync service;
  @Mock private NumberFormat numberFormat;
  @Mock private Provider<CheckBox> checkBoxProvider;
  @Mock private HistoryAccessor historyAccessor;
  @Mock DataTable data;
  @Mock Options options;
  @Mock ValueChangeEvent<Boolean> vce;

  @Captor private ArgumentCaptor<AsyncCallback<BenchmarkResultsTable>> asyncCaptor;
  @Captor private ArgumentCaptor<Runnable> runableCaptor;
  @Captor private ArgumentCaptor<ValueChangeHandler<Boolean>> valueChangeHandlerCaptor;

  @Before
  public void setup() {
    composite = new GraphComposite(service, checkBoxProvider, historyAccessor);
  }

  @Test
  public void testStartWithNowAndNoRunners() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(Mockito.<Runnable> anyObject());

    verify(service).getLatestGraphs(eq("module1"), asyncCaptor.capture());
  }

  @Test
  public void testStartWithOneRunners() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1&w=4&y=2014&rids=[linux_ff]");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(Mockito.<Runnable> anyObject());

    verify(service).getGraphs(eq("module1"), eq(4), eq(2014), asyncCaptor.capture());
  }

  @Test
  public void testStartWithNowAndTwoRunners() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1&w=5&y=2014&rids=[linux_ff, linux_chrome]");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(Mockito.<Runnable> anyObject());

    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());
  }

  @Test
  public void testRenderData() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1&w=5&y=2014&rids=[linux_ff, linux_chrome]");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(runableCaptor.capture());
    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());

    verifyNoMoreInteractions(composite.container);

    BenchmarkResultsTable result = BenchmarkResultsTable.create("module1", "someWeekName1", 2014, 5,
        Arrays.asList("commit1", "commit2", "commit3"),
        Arrays.asList("linux_ff", "linux_chrome", "win_ie11"),
        Arrays.asList(new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}));

    CheckboxHolder holder1 = createMockedCheckBox();
    CheckboxHolder holder2 = createMockedCheckBox();
    CheckboxHolder holder3 = createMockedCheckBox();

    when(checkBoxProvider.get()).thenReturn(holder1.checkBox, holder2.checkBox, holder3.checkBox);

    when(composite.graphWidget.createData()).thenReturn(data);
    when(composite.graphWidget.createOptions()).thenReturn(options);

    // Test that we do not render before the visualization api is loaded
    asyncCaptor.getValue().onSuccess(result);
    verifyNoMoreInteractions(composite.container);

    runableCaptor.getValue().run();

    verify(composite.loadingLabel).setVisible(false);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(true);

    verify(holder1.checkBox).setText("linux_ff");
    verify(holder2.checkBox).setText("linux_chrome");
    verify(holder3.checkBox).setText("win_ie11");

    verify(holder1.checkBox).setValue(true);
    verify(holder2.checkBox).setValue(true);
    verify(holder3.checkBox).setValue(false);

    verify(holder1.checkBox).addValueChangeHandler(valueChangeHandlerCaptor.capture());

    verify(composite.checkBoxContainer).add(holder1.checkBox);
    verify(composite.checkBoxContainer).add(holder2.checkBox);
    verify(composite.checkBoxContainer).add(holder3.checkBox);

    verify(data).addColumn(ColumnType.STRING, "Commits");
    verify(data).addColumn(ColumnType.NUMBER, "linux_ff");
    verify(data).addColumn(ColumnType.NUMBER, "linux_chrome");

    verify(data).addRows(3);

    verify(data).setValue(0, 0, "commit1");
    verify(data).setValue(1, 0, "commit2");
    verify(data).setValue(2, 0, "commit3");

    verify(data).setValue(0, 1, 1.0);
    verify(data).setValue(1, 1, 2.0);
    verify(data).setValue(2, 1, 3.0);

    verify(data).setValue(0, 2, 4.0);
    verify(data).setValue(1, 2, 5.0);
    verify(data).setValue(2, 2, 6.0);

    verify(options).setWidth(800);
    verify(options).setHeight(600);
    verify(options).setTitle("module1");

    verify(composite.graphWidget).createData();
    verify(composite.graphWidget).createOptions();
    verify(composite.graphWidget, times(2)).createAxisOptions();
    verify(composite.graphWidget).displayChart(options, data);
    verify(options).setHAxisOptions(Mockito.<AxisOptions> anyObject());
    verify(options).setVAxisOptions(Mockito.<AxisOptions> anyObject());

    verifyNoMoreInteractions(composite.graphWidget, data, options);

    // change a CheckBox
    when(vce.getValue()).thenReturn(false);
    valueChangeHandlerCaptor.getValue().onValueChange(vce);

    verify(holder1.handlerRegistration).removeHandler();
    verify(holder2.handlerRegistration).removeHandler();
    verify(holder3.handlerRegistration).removeHandler();

    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());
    verify(historyAccessor).newItem("!graph?benchmark=module1&w=5&y=2014&rids=[linux_chrome]", false);
  }

  @Test
  public void testRenderDataEmptyDataSet() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1&w=5&y=2014&rids=[linux_ff, linux_chrome]");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(runableCaptor.capture());
    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());

    verifyNoMoreInteractions(composite.container);

    BenchmarkResultsTable result = BenchmarkResultsTable.create("module1", "someWeekName1", 2014, 5,
        Collections.<String>emptyList(),
        Collections.<String>emptyList(),
        Collections.<double[]>emptyList());

    runableCaptor.getValue().run();
    asyncCaptor.getValue().onSuccess(result);

    verify(composite.checkBoxContainer).clear();
    verify(composite.container).setVisible(true);
    verify(composite.weekLabel).setText("someWeekName1");

    verifyNoMoreInteractions(checkBoxProvider);

    verify(composite.graphWidget).clear();
    verifyNoMoreInteractions(composite.graphWidget);
  }

  @Test
  public void testForwardButton() {

    when(historyAccessor.getToken()).thenReturn("!graph?benchmark=module1&w=5&y=2014&rids=[linux_ff, linux_chrome]");

    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(composite.graphWidget).init(runableCaptor.capture());
    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());

    verifyNoMoreInteractions(composite.container);

    BenchmarkResultsTable result = BenchmarkResultsTable.create("module1", "someWeek1", 2014, 5,
        Arrays.asList("commit1", "commit2", "commit3"),
        Arrays.asList("linux_ff", "linux_chrome", "win_ie11"),
        Arrays.asList(new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}));

    CheckboxHolder holder1 = createMockedCheckBox();
    CheckboxHolder holder2 = createMockedCheckBox();
    CheckboxHolder holder3 = createMockedCheckBox();
    CheckboxHolder holder4 = createMockedCheckBox();
    CheckboxHolder holder5 = createMockedCheckBox();
    CheckboxHolder holder6 = createMockedCheckBox();

    when(checkBoxProvider.get()).thenReturn(holder1.checkBox, holder2.checkBox, holder3.checkBox,
        holder4.checkBox, holder5.checkBox, holder6.checkBox);

    when(composite.graphWidget.createData()).thenReturn(data);
    when(composite.graphWidget.createOptions()).thenReturn(options);

    // Test that we do not render before the visualization api is loaded
    asyncCaptor.getValue().onSuccess(result);
    verifyNoMoreInteractions(composite.container);

    runableCaptor.getValue().run();
    verify(composite.loadingLabel).setVisible(false);


    reset(composite.loadingLabel, composite.errorLabel, composite.container);

    // press the forward button
    composite.onForwardButtonClicked(null);
    verify(historyAccessor).newItem(
        "!graph?benchmark=module1&w=6&y=2014&rids=[linux_chrome, linux_ff]", false);

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(service).getGraphs(eq("module1"), eq(6), eq(2014), asyncCaptor.capture());


    result = BenchmarkResultsTable.create("module1", "someWeek1", 2014, 6,
        Arrays.asList("commit1", "commit2", "commit3"),
        Arrays.asList("linux_ff", "linux_chrome", "win_ie11"),
        Arrays.asList(new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}));

    asyncCaptor.getValue().onSuccess(result);

    verify(holder1.handlerRegistration).removeHandler();
    verify(holder2.handlerRegistration).removeHandler();
    verify(holder3.handlerRegistration).removeHandler();
    reset(composite.loadingLabel, composite.errorLabel, composite.container, service);

    // press the back button
    composite.onBackButtonClicked(null);

    verify(historyAccessor).newItem(
        "!graph?benchmark=module1&w=5&y=2014&rids=[linux_chrome, linux_ff]", false);

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.container).setVisible(false);
    verify(service).getGraphs(eq("module1"), eq(5), eq(2014), asyncCaptor.capture());


    result = BenchmarkResultsTable.create("module1", "someWeek1", 2014, 6,
        Arrays.asList("commit1", "commit2", "commit3"),
        Arrays.asList("linux_ff", "linux_chrome", "win_ie11"),
        Arrays.asList(new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}));

    asyncCaptor.getValue().onSuccess(result);

    verify(holder1.handlerRegistration).removeHandler();
    verify(holder2.handlerRegistration).removeHandler();
    verify(holder3.handlerRegistration).removeHandler();
  }

  private CheckboxHolder createMockedCheckBox() {
    CheckBox checkBox = mock(CheckBox.class);
    HandlerRegistration hr = mock(HandlerRegistration.class);
    when(checkBox.addValueChangeHandler(Mockito.<ValueChangeHandler<Boolean>> anyObject()))
    .thenReturn(hr);
    when(checkBoxProvider.get()).thenReturn(checkBox);
    return new CheckboxHolder(checkBox, hr);
  }
}
