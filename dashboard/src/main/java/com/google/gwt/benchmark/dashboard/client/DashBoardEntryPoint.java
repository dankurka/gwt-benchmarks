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
package com.google.gwt.benchmark.dashboard.client;

import com.google.gwt.benchmark.dashboard.client.ui.GraphComposite;
import com.google.gwt.benchmark.dashboard.client.ui.ModuleOverviewComposite;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Provides;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import javax.inject.Singleton;

/**
 * EntryPoint for dashboard UI.
 */
public class DashBoardEntryPoint implements EntryPoint {

  private Injector injector;
  private SimplePanel simplePanel;


  @GinModules(Module.class)
  public interface Injector extends Ginjector {

    EventBus getEventBus();

    ModuleOverviewComposite getModuleOverviewComposite();

    GraphComposite getGraphComposite();
  }

  public static class Module extends AbstractGinModule {

    @Override
    protected void configure() {
      bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    }

    @Provides
    protected Label createLabel() {
      return new Label();
    }

    @Provides
    protected CheckBox createCheckBox() {
      return new CheckBox();
    }
  }

  @Override
  public void onModuleLoad() {
    injector = GWT.create(Injector.class);
    simplePanel = new SimplePanel();
    RootPanel.get().add(simplePanel);


    String token = History.getToken();
    handleHistory(token);

    History.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
          handleHistory(event.getValue());
      }
    });
  }

  private void handleHistory(String token) {
    if(token.startsWith("!graph?")) {
      GraphComposite graphComposite = injector.getGraphComposite();
      simplePanel.setWidget(graphComposite);
      graphComposite.start();
    } else {
      ModuleOverviewComposite moduleOverviewComposite = injector.getModuleOverviewComposite();
      simplePanel.setWidget(moduleOverviewComposite);
      moduleOverviewComposite.start();
    }
  }
}
