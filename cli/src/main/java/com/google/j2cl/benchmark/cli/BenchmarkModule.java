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
package com.google.j2cl.benchmark.cli;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.j2cl.benchmark.cli.MailReporter.MailHelper;
import com.google.j2cl.benchmark.cli.MailReporter.MailHelperProdImpl;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The compile server guice module.
 */
public class BenchmarkModule extends AbstractModule {

  private final Cli cli;

  public BenchmarkModule(Cli cli) {
    this.cli = cli;
  }

  @Override
  protected void configure() {
    bind(BenchmarkCompiler.class).to(CliInteractor.class);
    install(new FactoryModuleBuilder().build(BenchmarkWorker.Factory.class));
    install(new FactoryModuleBuilder().build(BenchmarkReporter.Factory.class));
    install(new FactoryModuleBuilder().build(BenchmarkUploader.Factory.class));
    bind(MailHelper.class).to(MailHelperProdImpl.class);
    bind(String.class).annotatedWith(Names.named("compilerArgs")).toInstance(cli.compilerArgs);
    bind(String.class).annotatedWith(Names.named("randomStringProvider"))
        .toProvider(RandomStringProvider.class);


    final File tempDir = Files.createTempDir();
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        try {
          FileUtils.deleteDirectory(tempDir);
        } catch (IOException ignored) {
        }
      }
    });

    bind(File.class).annotatedWith(Names.named("compilerOutputDir"))
        .toInstance(tempDir);

    bind(Integer.class).annotatedWith(Names.named("poolSize"))
        .toInstance(cli.threadPoolSize);
    bind(File.class).annotatedWith(Names.named("benchmarkSourceLocation"))
        .toInstance(new File(cli.benchmarksLocation, "src/main/java/"));
    bind(ExecutorService.class).annotatedWith(Names.named("managerPoolSize"))
        .toProvider(PoolProvider.class);
    bind(String.class).annotatedWith(Names.named("moduleTemplate"))
        .toInstance(cli.moduleTemplateContent);
    bind(String.class).annotatedWith(Names.named("runServerUrl"))
    .toInstance(cli.runServer);
    bind(new TypeLiteral<Predicate<String>>(){})
        .annotatedWith(Names.named("benchmarkFilter"))
        .toInstance(cli.benchmarkFilter);
    bind(Boolean.class).annotatedWith(Names.named("deamonMode")).toInstance(cli.deamon);
    bind(Boolean.class).annotatedWith(Names.named("skipSDKBuild")).toInstance(cli.skipBuild);
    bind(Boolean.class).annotatedWith(Names.named("skipFailures")).toInstance(cli.skipFailures);

    bind(File.class).annotatedWith(Names.named("scriptDirectory"))
        .toInstance(new File(cli.benchmarksLocation, "src/main/scripts/"));
    bind(Boolean.class).annotatedWith(Names.named("reportResults"))
        .toInstance(cli.reportResults);
    bind(Boolean.class).annotatedWith(Names.named("reportErrors"))
    .toInstance(cli.reportErrors);
    bind(File.class).annotatedWith(Names.named("persistenceDir"))
        .toInstance(cli.persistenceDir);
    bind(File.class).annotatedWith(Names.named("gwtSourceLocation"))
        .toInstance(cli.gwtSourceLocation);
    bind(File.class).annotatedWith(Names.named("gwtDevJar"))
    .toInstance(cli.gwtDevJar);
    bind(File.class).annotatedWith(Names.named("gwtUserJar"))
    .toInstance(cli.gwtUserJar);

    bind(MailSettings.class).toInstance(new MailSettings(cli.mailFrom, cli.mailTo, cli.mailHost, cli.mailUsername, cli.mailPassword));
    bind(String.class).annotatedWith(Names.named("spreadSheetId")).toInstance(
        cli.spreadsheetId);
    bind(String.class).annotatedWith(Names.named("client_json_secret")).toInstance(
        cli.oauthSecretContent);
  }

  @Provides
  public SpreadsheetService spreadSheetService() {
    return new SpreadsheetService("benchmark");
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

  private static class RandomStringProvider implements Provider<String> {
    @Override
    public String get() {
      return UUID.randomUUID().toString();
    }
  }
}
