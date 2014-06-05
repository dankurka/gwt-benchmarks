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
package com.google.gwt.benchmark.compileserver.server.guice;

import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkCompiler;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkWorker;
import com.google.gwt.benchmark.compileserver.server.manager.CliInteractor;
import com.google.gwt.benchmark.compileserver.server.manager.MailReporter.MailHelper;
import com.google.gwt.benchmark.compileserver.server.manager.MailReporter.MailHelperProdImpl;
import com.google.gwt.benchmark.compileserver.server.manager.Runner;
import com.google.gwt.benchmark.compileserver.server.manager.WebDriverRunner;
import com.google.gwt.benchmark.compileserver.server.runners.settings.MailSettings;
import com.google.gwt.benchmark.compileserver.server.runners.settings.Settings;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The compile server guice module.
 */
public class BenchmarkModule extends AbstractModule {

  private final Settings settings;

  public BenchmarkModule(Settings settings) {
    this.settings = settings;
  }

  @Override
  protected void configure() {
    bind(BenchmarkCompiler.class).to(CliInteractor.class);
    install(new FactoryModuleBuilder().implement(Runner.class, WebDriverRunner.class).build(
        Runner.Factory.class));
    install(new FactoryModuleBuilder().build(BenchmarkWorker.Factory.class));
    install(new FactoryModuleBuilder().build(BenchmarkReporter.Factory.class));
    bind(BenchmarkReporter.HttpURLConnectionFactory.class).to(HttpUrlConnectionProvider.class);
    bind(MailHelper.class).to(MailHelperProdImpl.class);
    bind(String.class).annotatedWith(Names.named("randomStringProvider"))
        .toProvider(RandomStringProvider.class);

    bind(File.class).annotatedWith(Names.named("compilerOutputDir"))
        .toInstance(settings.getBenchmarkCompileOutputDir());
    bind(Integer.class).annotatedWith(Names.named("poolSize"))
        .toInstance(settings.getThreadPoolSize());
    bind(Integer.class).annotatedWith(Names.named("port"))
        .toInstance(settings.getServletContainerPort());
    bind(File.class).annotatedWith(Names.named("benchmarkSourceLocation"))
        .toInstance(new File(settings.getBenchmarkRootDirectory(), "src/main/java/"));
    bind(ExecutorService.class).annotatedWith(Names.named("managerPoolSize"))
        .toProvider(PoolProvider.class);
    bind(String.class).annotatedWith(Names.named("ip")).toInstance(settings.getIpAddress());
    bind(String.class).annotatedWith(Names.named("moduleTemplate"))
        .toInstance(settings.getModuleTemplate());
    bind(URL.class).annotatedWith(Names.named("hubUrl")).toInstance(settings.getHubUrl());
    bind(File.class).annotatedWith(Names.named("scriptDirectory"))
        .toInstance(settings.getScriptsDirectory());
    bind(Boolean.class).annotatedWith(Names.named("useReporter"))
        .toInstance(settings.reportResults());
    bind(String.class).annotatedWith(Names.named("reporterSecret")).toInstance(
        settings.getReporterSecret());
    bind(String.class).annotatedWith(Names.named("benchmarkDashboardUrl"))
        .toInstance(settings.getReporterUrl());
    bind(File.class).annotatedWith(Names.named("persistenceDir"))
        .toInstance(settings.getPersistenceDir());
    bind(File.class).annotatedWith(Names.named("gwtSourceLocation"))
        .toInstance(settings.getGwtSourceLocation());
    bind(MailSettings.class).toInstance(settings.getMailSettings());

  }

  private static class PoolProvider implements Provider<ExecutorService> {

    private int poolSize;

    @Inject
    public PoolProvider(@Named("poolSize") int poolSize) {
      this.poolSize = poolSize;
    }

    @Override
    public ExecutorService get() {
      return Executors.newFixedThreadPool(poolSize);
    }
  }

  private static class HttpUrlConnectionProvider implements
      BenchmarkReporter.HttpURLConnectionFactory {

    private String url;

    @Inject
    public HttpUrlConnectionProvider(@Named("benchmarkDashboardUrl") String url) {
      this.url = url;
    }

    @Override
    public HttpURLConnection create() throws IOException {
      return (HttpURLConnection) new URL(url).openConnection();
    }
  }

  private static class RandomStringProvider implements Provider<String> {
    @Override
    public String get() {
      return UUID.randomUUID().toString();
    }
  }
}
