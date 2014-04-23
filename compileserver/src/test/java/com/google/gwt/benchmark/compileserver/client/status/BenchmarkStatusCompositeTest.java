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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gwt.benchmark.compileserver.shared.ServiceAsync;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewEntryDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewEntryDTO.BenchmarState;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkOverviewResponseDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkRunDTO;
import com.google.gwt.benchmark.compileserver.shared.dto.BenchmarkRunDTO.State;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test for {@link BenchmarkStatusComposite}.
 */
@RunWith(GwtMockitoTestRunner.class)
public class BenchmarkStatusCompositeTest {

  private BenchmarkStatusComposite composite;
  private List<String> messages = new ArrayList<>();

  @Mock private ServiceAsync service;
  @Mock private NumberFormat numberFormat;
  @Mock private Provider<Label> labelProvider;

  @Captor private ArgumentCaptor<AsyncCallback<BenchmarkOverviewResponseDTO>> asyncCaptor;
  @Captor private ArgumentCaptor<ClickHandler> clickHandler1;
  @Captor private ArgumentCaptor<ClickHandler> clickHandler2;

  @Before
  public void setup() {
    messages.clear();
    composite = new BenchmarkStatusComposite(service, numberFormat, labelProvider) {
      @Override
      void alert(String message) {
        messages.add(message);
      }
    };

    when(numberFormat.format(22)).thenReturn("22");
    when(numberFormat.format(33)).thenReturn("33");
  }

  @Test
  public void testPresenterAddsViewAndInitializedView() {
    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(false);
    verify(composite.statusText).setText("");
    verify(composite.errorLabel).setVisible(false);
  }

  @Test
  public void testRenderDataWhileServerIsNotRunning() {
    composite.start();

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(false);
    verify(composite.statusText).setText("");
    verify(composite.startStopButton).setVisible(false);

    verify(service).loadBenchmarkOverview(asyncCaptor.capture());

    AsyncCallback<BenchmarkOverviewResponseDTO> asyncCallback = asyncCaptor.getValue();

    BenchmarkOverviewResponseDTO response = new BenchmarkOverviewResponseDTO();
    response.setHasLatestRun(false);

    asyncCallback.onSuccess(response);

    verify(composite.loadingLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(true);
    verify(composite.startStopButton).setVisible(true);
    verify(composite.startStopButton).setText("Start executing");
    verify(composite.statusText).setText("System is idle");
    verify(composite.grid).clear();

    verifyNoMoreInteractions(composite.contentContainer, composite.errorLabel,
        composite.grid, composite.loadingLabel, composite.startStopButton, composite.statusText);

  }

  @Test
  public void testRenderDataWhileBenchmarksRunning() {

    composite.start();

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(false);
    verify(composite.statusText).setText("");
    verify(composite.startStopButton).setVisible(false);

    verify(service).loadBenchmarkOverview(asyncCaptor.capture());

    AsyncCallback<BenchmarkOverviewResponseDTO> asyncCallback = asyncCaptor.getValue();

    BenchmarkOverviewResponseDTO response = new BenchmarkOverviewResponseDTO();
    response.setHasLatestRun(true);

    // first entry
    BenchmarkOverviewEntryDTO benchmarkOverviewEntry = new BenchmarkOverviewEntryDTO();
    benchmarkOverviewEntry.setBenchmarkName("benchmark1");
    benchmarkOverviewEntry.setStatus(BenchmarState.DONE);
    benchmarkOverviewEntry.setErrorMessage(null);
    BenchmarkRunDTO benchmarkRunDTO = new BenchmarkRunDTO();
    benchmarkRunDTO.setErrorMessage(null);
    benchmarkRunDTO.setRunsPerMinute(22);
    benchmarkRunDTO.setState(State.DONE);
    BenchmarkRunDTO benchmarkRunDTO1 = new BenchmarkRunDTO();
    benchmarkRunDTO1.setErrorMessage(null);
    benchmarkRunDTO1.setRunsPerMinute(33);
    benchmarkRunDTO1.setState(State.DONE);
    benchmarkOverviewEntry.setBenchmarkRuns(
        new ArrayList<>(Arrays.asList(benchmarkRunDTO, benchmarkRunDTO1)));

    // second entry
    BenchmarkOverviewEntryDTO benchmarkOverviewEntry1 = new BenchmarkOverviewEntryDTO();
    benchmarkOverviewEntry1.setBenchmarkName("benchmark2");
    benchmarkOverviewEntry1.setStatus(BenchmarState.FATAL_ERROR);
    benchmarkOverviewEntry1.setErrorMessage("benchmark2 error message");
    benchmarkRunDTO = new BenchmarkRunDTO();
    benchmarkRunDTO1 = new BenchmarkRunDTO();
    benchmarkOverviewEntry1.setBenchmarkRuns(
        new ArrayList<>(Arrays.asList(benchmarkRunDTO, benchmarkRunDTO1)));

    // third entry
    BenchmarkOverviewEntryDTO benchmarkOverviewEntry2 = new BenchmarkOverviewEntryDTO();
    benchmarkOverviewEntry2.setBenchmarkName("benchmark3");
    benchmarkOverviewEntry2.setStatus(BenchmarState.RUNNING);
    benchmarkOverviewEntry2.setErrorMessage(null);
    benchmarkRunDTO = new BenchmarkRunDTO();
    benchmarkRunDTO1 = new BenchmarkRunDTO();
    benchmarkOverviewEntry1.setBenchmarkRuns(
        new ArrayList<>(Arrays.asList(benchmarkRunDTO, benchmarkRunDTO1)));

    // fourth entry
    BenchmarkOverviewEntryDTO benchmarkOverviewEntry3 = new BenchmarkOverviewEntryDTO();
    benchmarkOverviewEntry3.setBenchmarkName("benchmark4");
    benchmarkOverviewEntry3.setStatus(BenchmarState.AT_LEAST_ONE_FAILED);
    benchmarkOverviewEntry3.setErrorMessage(null);
    benchmarkRunDTO = new BenchmarkRunDTO();
    benchmarkRunDTO.setRunsPerMinute(22);
    benchmarkRunDTO.setState(State.DONE);
    benchmarkRunDTO1 = new BenchmarkRunDTO();
    benchmarkRunDTO1.setErrorMessage("error message b4 r2");
    benchmarkRunDTO1.setState(State.FAILED_RUN);
    benchmarkOverviewEntry3.setBenchmarkRuns(
        new ArrayList<>(Arrays.asList(benchmarkRunDTO, benchmarkRunDTO1)));

    response.setBenchmarks(new ArrayList<>(Arrays.asList(benchmarkOverviewEntry,
        benchmarkOverviewEntry1, benchmarkOverviewEntry2, benchmarkOverviewEntry3)));
    response.setRunnerNames(new ArrayList<>(Arrays.asList("chrome_linux", "firefox_linux")));

    // header
    Label gridEntry_0_0 = mock(Label.class);
    Label gridEntry_0_1 = mock(Label.class);
    Label gridEntry_0_2 = mock(Label.class);

    // first entry
    Label gridEntry_1_0 = mock(Label.class);
    Label gridEntry_1_1 = mock(Label.class);
    Label gridEntry_1_2 = mock(Label.class);

    // second entry
    Label gridEntry_2_0 = mock(Label.class);

    // third entry
    Label gridEntry_3_0 = mock(Label.class);

    // fourth entry
    Label gridEntry_4_0 = mock(Label.class);
    Label gridEntry_4_1 = mock(Label.class);
    Label gridEntry_4_2 = mock(Label.class);

    when(labelProvider.get()).thenReturn(gridEntry_0_0, gridEntry_0_1, gridEntry_0_2,
        gridEntry_1_0, gridEntry_1_1, gridEntry_1_2, gridEntry_2_0, gridEntry_3_0, gridEntry_4_0,
        gridEntry_4_1, gridEntry_4_2);

    asyncCallback.onSuccess(response);

    verify(composite.grid).clear();
    verify(composite.grid).resize(5, 3);

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.contentContainer).setVisible(false);
    verify(composite.statusText).setText("System is idle");
    verify(composite.startStopButton).setVisible(true);
    verify(composite.startStopButton).setText("Start executing");

    // header
    verify(gridEntry_0_0).setText("Benchmark Name");
    verify(composite.grid).setWidget(0, 0, gridEntry_0_0);
    verify(gridEntry_0_1).setText("chrome_linux");
    verify(composite.grid).setWidget(0, 1, gridEntry_0_1);
    verify(gridEntry_0_2).setText("firefox_linux");
    verify(composite.grid).setWidget(0, 2, gridEntry_0_2);
    verifyNoMoreInteractions(gridEntry_0_0, gridEntry_0_1, gridEntry_0_2);

    // first entry
    verify(gridEntry_1_0).setText("benchmark1");
    verify(gridEntry_1_0).addStyleName(BenchmarkStatusComposite.bundle.css().statusDone());
    verify(composite.grid).setWidget(1, 0, gridEntry_1_0);
    verify(gridEntry_1_1).setText("22");
    verify(composite.grid).setWidget(1, 1, gridEntry_1_1);
    verify(gridEntry_1_2).setText("33");
    verify(composite.grid).setWidget(1, 2, gridEntry_1_2);
    verifyNoMoreInteractions(gridEntry_1_0, gridEntry_1_1, gridEntry_1_2);

    // second entry
    verify(gridEntry_2_0).setText("benchmark2");
    verify(gridEntry_2_0).addStyleName(
        BenchmarkStatusComposite.bundle.css().statusFatalError());
    verify(gridEntry_2_0).addStyleName(BenchmarkStatusComposite.bundle.css().clickable());
    verify(gridEntry_2_0).addClickHandler(clickHandler1.capture());
    verify(composite.grid).setWidget(2, 0, gridEntry_2_0);
    verifyNoMoreInteractions(gridEntry_2_0);

    // third entry
    verify(gridEntry_3_0).setText("benchmark3");
    verify(gridEntry_3_0).addStyleName(
        BenchmarkStatusComposite.bundle.css().statusRunning());
    verify(composite.grid).setWidget(3, 0, gridEntry_3_0);
    verifyNoMoreInteractions(gridEntry_3_0);

    // fourth entry
    verify(gridEntry_4_0).setText("benchmark4");
    verify(gridEntry_4_0).addStyleName(
        BenchmarkStatusComposite.bundle.css().statusOneFailed());
    verify(composite.grid).setWidget(4, 0, gridEntry_4_0);
    verify(gridEntry_4_1).setText("22");
    verify(composite.grid).setWidget(4, 1, gridEntry_4_1);
    verify(gridEntry_4_2).setText("Error");
    verify(gridEntry_4_2).addStyleName(BenchmarkStatusComposite.bundle.css().clickable());
    verify(gridEntry_4_2).addClickHandler(clickHandler2.capture());
    verify(composite.grid).setWidget(4, 2, gridEntry_4_2);

    verifyNoMoreInteractions(gridEntry_4_0, gridEntry_4_1, gridEntry_4_2);


    // Click the first error
    clickHandler1.getValue().onClick(null);
    // Click the second error
    clickHandler2.getValue().onClick(null);

    // we should have alerted two messages
    Assert.assertEquals(2, messages.size());
    Assert.assertEquals("benchmark2 error message", messages.get(0));
    Assert.assertEquals("error message b4 r2", messages.get(1));
  }
}
