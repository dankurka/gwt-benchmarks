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
package com.google.j2cl.benchmark.cli;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.ParserProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * The command line interface for the benchmarking system.
 */
public class Cli {

  @Option(name = "-benchmarksLocation", usage = "location of the benchmark repository.")
  File benchmarksLocation = new File("../benchmarks/");

  @Option(name = "-run_server", usage = "url of the run server")
  String runServer = "http://localhost:8080";

  @Option(name = "-module_template", usage = "The template html file to use for benchmarks")
  File moduleTemplate = new File("runner_template.html");

  @Option(name = "-compiler", usage = "which compiler to use, either gwt or j2cl")
  String compiler = "gwt";

  @Option(name = "-deamon", usage = "Run in deamon mode (Pull changes and benchmark new commits)")
  boolean deamon;

  @Option(name = "-persistenceDir", usage = "The directory to store information in")
  File persistenceDir;

  @Option(name = "-gwtSourceLocation", usage = "GWT source code location.")
  File gwtSourceLocation = new File("../../gwt");

  @Option(name = "-gwtDevJar", usage = "location of a prebuilt gwt dev jar."
      + " Use together with -skipBuild.")
  File gwtDevJar;
  @Option(name = "-gwtUserJar", usage = "location of a prebuilt gwt user jar."
      + " Use together with -skipBuild.")
  File gwtUserJar;

  @Option(name = "-threadPoolSize", usage = "How many threads should be used to compile benchmarks")
  int threadPoolSize = 5;

  @Option(name = "-reportResults", usage = "report results to a spreadsheet")
  boolean reportResults = false;

  @Option(name = "-reportErrors", usage = "report errors by mail")
  boolean reportErrors = false;

  @Option(name = "-skipBuild", usage = "skip the build of the SDK")
  boolean skipBuild = false;

  @Option(name = "-mail_to", usage = "email address to notify if deamon fails")
  String mailTo;

  @Option(name = "-mail_from", usage = "email address to send from")
  String mailFrom;

  @Option(name = "-mail_host", usage = "smtp host for sending emails")
  String mailHost;

  @Option(name = "-mail_username", usage = "email username")
  String mailUsername;

  @Option(name = "-mail_password", usage = "email password")
  String mailPassword;

  @Option(name = "-oauth_secret", usage = "oatuh secret file")
  File oauthSecretFile;

  @Option(name = "-spreadsheetId", usage = "The spreasheet's id that should be used for storing data")
  String spreadsheetId;

  @Option(name="-compilerArgs", usage="The compiler args to use to compile a benchark")
  String compilerArgs = "";

  // receives other command line parameters than options
  @Argument
  private List<String> benchmarksToRun = Lists.newArrayList();

  String moduleTemplateContent;

  String oauthSecretContent;

  Predicate<String> benchmarkFilter = Predicates.alwaysTrue();

  public static void main(String[] args) throws InterruptedException {
    new Cli().doMain(args);
  }

  public void doMain(String[] args) throws InterruptedException {
    parseOrDie(args);
    Injector injector = Guice.createInjector(new BenchmarkModule(this));
    Manager manager = injector.getInstance(Manager.class);
    manager.execute();
  }

  private void parseOrDie(String[] args) {
    ParserProperties parserProperties = ParserProperties.defaults()
        // Console width of 80
        .withUsageWidth(80);

    CmdLineParser parser = new CmdLineParser(this, parserProperties);

    try {
      parser.parseArgument(args);

      try {
        moduleTemplateContent = loadContentAsString(moduleTemplate);
      } catch (IOException e) {
        throw new CmdLineException(parser, "Can not load module template", e);
      }

      if (deamon) {
        if (persistenceDir == null) {
          throw new CmdLineException(parser, "Deamon mode but no persistenDir given", null);
        }
        if (!persistenceDir.isDirectory()) {
          throw new CmdLineException(parser, "persistenceDir needs to be an existing directory", null);
        }
      }

      if (reportErrors) {
        if (mailFrom == null) {
          throw new CmdLineException(parser, "if -reportErrors is used -mail_from needs to be set",
              null);
        }

        if (mailTo == null) {
          throw new CmdLineException(parser, "if -reportErrors is used -mail_to needs to be set",
              null);
        }

        if (mailHost == null) {
          throw new CmdLineException(parser, "if -reportErrors is used -mail_host needs to be set",
              null);
        }

        if (mailUsername == null) {
          throw new CmdLineException(parser,
              "if -reportErrors is used -mail_username needs to be set", null);
        }

        if (mailPassword == null) {
          throw new CmdLineException(parser,
              "if -reportErrors is used -mail_password needs to be set", null);
        }
      }

      if (reportResults) {
        if (persistenceDir == null) {
          throw new CmdLineException(parser, "reportResults mode but no persistenDir given", null);
        }
        if (!persistenceDir.isDirectory()) {
          throw new CmdLineException(parser, "persistenceDir needs to be an existing directory", null);
        }

        if (spreadsheetId == null) {
          throw new CmdLineException(parser, "reportResults mode but no spreadsheetId given", null);
        }
        if (oauthSecretFile == null) {
          throw new CmdLineException(parser, "reportResults mode but no oauth_secret given", null);
        }

        try {
          oauthSecretContent = loadContentAsString(oauthSecretFile);
        } catch (IOException e) {
          throw new CmdLineException(parser, "Can no load content of oauth secret file", e);
        }
      }

      if (gwtUserJar == null || gwtDevJar == null) {
        gwtDevJar = new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-dev.jar");
        gwtUserJar = new File(gwtSourceLocation, "build/staging/gwt-0.0.0/gwt-user.jar");
      }

      if (!deamon) {
        if (!benchmarksToRun.isEmpty()) {
          final Set<String> normlizedNames = Sets.newHashSet();
          for (String benchmarkName : benchmarksToRun) {
            if (!benchmarkName.startsWith("com.google.gwt.benchmark.benchmarks.")) {
              benchmarkName = "com.google.gwt.benchmark.benchmarks." + benchmarkName;
            }
            normlizedNames.add(benchmarkName);
          }

          benchmarkFilter = new Predicate<String>() {

            @Override
            public boolean apply(@Nullable String input) {
              return normlizedNames.contains(input);
            }
          };

        }

        // setting values since we do not need these params
        if (persistenceDir == null) {
          persistenceDir = new File(".");
        }

        if (spreadsheetId == null) {
          spreadsheetId = "no spreadsheet id";
        }

        if (oauthSecretContent == null) {
          oauthSecretContent = "no oauth secret";
        }
      }

    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      System.err.println("run_system.sh [options...] arguments...");
      // print the list of available options
      parser.printUsage(System.err);
      System.err.println();

      // print option sample. This is useful some time
      System.err.println("  Example: run_system.sh " + parser.printExample(OptionHandlerFilter.ALL));
      System.exit(-1);
    }
  }

  private static String loadContentAsString(File fileName) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(fileName)) {
      return IOUtils.toString(inputStream, "UTF-8");
    }
  }
}
