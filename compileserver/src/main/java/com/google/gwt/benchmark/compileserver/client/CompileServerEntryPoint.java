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
package com.google.gwt.benchmark.compileserver.client;

import com.google.gwt.benchmark.compileserver.client.status.BenchmarkStatusComposite;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Provides;


/**
 * EntryPoint for compile server UI.
 */
public class CompileServerEntryPoint implements EntryPoint {

  public static class CompileServerClientModule extends AbstractGinModule {

    @Override
    protected void configure() {
    }

    @Provides
    protected NumberFormat provideNumberFormat() {
      return NumberFormat.getFormat("#,##0.##");
    }

    @Provides
    protected Label providesLabel() {
      return new Label();
    }
  }

  @GinModules(CompileServerClientModule.class)
  public interface CompileServerInjector extends Ginjector {
    BenchmarkStatusComposite getBenchmarkListComposite();
  }

  @Override
  public void onModuleLoad() {
    CompileServerInjector injector = GWT.create(CompileServerInjector.class);
    BenchmarkStatusComposite benchmarkStatusComposite = injector.getBenchmarkListComposite();
    RootPanel.get().add(benchmarkStatusComposite);
    benchmarkStatusComposite.start();
  }
}
