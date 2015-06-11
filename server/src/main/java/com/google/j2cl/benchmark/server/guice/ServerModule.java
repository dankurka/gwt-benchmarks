/*
 * Copyright 2015 Google Inc.
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
package com.google.j2cl.benchmark.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.google.j2cl.benchmark.common.runner.Runner;
import com.google.j2cl.benchmark.server.WebDriverRunner;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The compile server guice module.
 */
public class ServerModule extends AbstractModule {

  private final Settings settings;

  public ServerModule(Settings settings) {
    this.settings = settings;
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().implement(Runner.class, WebDriverRunner.class).build(
        Runner.Factory.class));
    bind(String.class).annotatedWith(Names.named("randomStringProvider")).toProvider(
        RandomStringProvider.class);
    bind(Integer.class).annotatedWith(Names.named("poolSize")).toInstance(
        settings.getThreadPoolSize());
    bind(Integer.class).annotatedWith(Names.named("port")).toInstance(
        settings.getServletContainerPort());
    bind(Long.class).annotatedWith(Names.named("timeProvider")).toProvider(TimeProvider.class);
    bind(String.class).annotatedWith(Names.named("ip")).toInstance(settings.getIpAddress());
    bind(URL.class).annotatedWith(Names.named("hubUrl")).toInstance(settings.getHubUrl());
    bind(File.class).annotatedWith(Names.named("extractDir")).toInstance(settings.getExtractDir());
    bind(ExecutorService.class).toInstance(
        Executors.newFixedThreadPool(settings.getThreadPoolSize()));
  }

  private static class RandomStringProvider implements Provider<String> {
    @Override
    public String get() {
      return UUID.randomUUID().toString();
    }
  }

  private static class TimeProvider implements Provider<Long> {
    @Override
    public Long get() {
      return System.currentTimeMillis();
    }
  }
}
