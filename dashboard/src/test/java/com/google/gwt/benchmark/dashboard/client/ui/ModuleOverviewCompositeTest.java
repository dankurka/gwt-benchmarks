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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test for {@link ModuleOverviewComposite}.
 */
@RunWith(GwtMockitoTestRunner.class)
public class ModuleOverviewCompositeTest {

  private ModuleOverviewComposite composite;

  @Mock private DashboardServiceAsync service;
  @Mock private NumberFormat numberFormat;
  @Mock private Provider<Label> labelProvider;
  @Mock HistoryAccessor historyAccessor;

  @Captor private ArgumentCaptor<AsyncCallback<ArrayList<String>>> asyncCaptor;
  @Captor private ArgumentCaptor<ClickHandler> clickHandlerCaptor1;
  @Captor private ArgumentCaptor<ClickHandler> clickHandlerCaptor2;

  @Before
  public void setup() {
    composite = new ModuleOverviewComposite(service, labelProvider, historyAccessor);
  }

  @Test
  public void testPresenterAddsViewAndInitializedView() {
    composite.start();
    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(false);
  }

  @Test
  public void testRenderDataWhileServerIsNotRunning() {
    composite.start();

    verify(composite.loadingLabel).setVisible(true);
    verify(composite.errorLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(false);

    Label label1 = mock(Label.class);
    Label label2 = mock(Label.class);
    when(labelProvider.get()).thenReturn(label1, label2);

    verify(service).getLatestBenchmarkNames(asyncCaptor.capture());

    AsyncCallback<ArrayList<String>> asyncCallback = asyncCaptor.getValue();
    asyncCallback.onSuccess(new ArrayList<>(Arrays.asList("module1", "module2")));

    verify(composite.loadingLabel).setVisible(false);
    verify(composite.contentContainer).setVisible(true);
    verify(composite.contentContainer).clear();
    verify(composite.contentContainer).add(label1);
    verify(composite.contentContainer).add(label2);

    verify(label1).setText("module1");
    verify(label1).addClickHandler(clickHandlerCaptor1.capture());
    verify(label2).setText("module2");
    verify(label2).addClickHandler(clickHandlerCaptor2.capture());

    verifyNoMoreInteractions(composite.contentContainer, composite.errorLabel,
     composite.loadingLabel, label1, label2);

    clickHandlerCaptor1.getValue().onClick(null);
    verify(historyAccessor).newItem("!graph?benchmark=module1", true);

    clickHandlerCaptor2.getValue().onClick(null);
    verify(historyAccessor).newItem("!graph?benchmark=module2", true);
  }
}
