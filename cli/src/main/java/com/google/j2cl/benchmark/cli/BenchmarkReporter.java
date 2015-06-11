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

import com.google.api.client.auth.oauth2.Credential;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.google.j2cl.benchmark.cli.BenchmarkRun.Result;
import com.google.j2cl.benchmark.common.runner.RunnerConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BenchmarkReporter reports benchmark results to a google spreadsheet.
 * <p>
 * It creates a worksheet for each benchmark, adds headers (commit id + runners). It also searches
 * for the current commit id in the spreadsheet and will update a row, this means that even if the
 * reporter failed one update the data will be consitent after the next upload.
 */
public class BenchmarkReporter {

  private static final String COMMIT_HEADER = "commit";

  public interface Factory {
    BenchmarkReporter create(List<BenchmarkRun> results,
        @Assisted("commitId") String commitId);
  }

  private static class ValueToAdd {
    int row;
    int col;
    String value;
  }

  public static final List<Integer> WAITING_TIME_SECONDS =
      Lists.newArrayList(1, 10, 100, 1000, 1000);

  private static Logger logger = Logger.getLogger(BenchmarkReporter.class.getName());

  private final List<BenchmarkRun> benchmarkRuns;
  private final String commitId;

  private final File oAuthDir;

  private final String clientJsonSecret;

  private final Provider<SpreadsheetService> spreadSheetServiveProvider;

  private final String spreadSheetId;

  @Inject
  public BenchmarkReporter(@Assisted List<BenchmarkRun> benchmarkRunsByBenchmarkName,
      @Assisted("commitId") String commitId,
      @Named("persistenceDir") File oAuthDir, @Named("client_json_secret") String clientJsonSecret,
      Provider<SpreadsheetService> spreadSheetServiveProvider,
      @Named("spreadSheetId") String spreadSheetId) {
    this.benchmarkRuns = benchmarkRunsByBenchmarkName;
    this.commitId = commitId;
    this.oAuthDir = oAuthDir;
    this.clientJsonSecret = clientJsonSecret;
    this.spreadSheetServiveProvider = spreadSheetServiveProvider;
    this.spreadSheetId = spreadSheetId;
  }

  public boolean report() {
    for (int delay : WAITING_TIME_SECONDS) {
      if (postResultToServer()) {
        return true;
      }
      logger.warning(String.format("Could not post results to dashboard retrying in %d seconds.",
          delay));
      if (!sleep(delay)) {
        break;
      }
    }
    return false;
  }

  private Map<String, WorksheetEntry> sheetsByName(SpreadsheetService service) throws IOException,
      ServiceException {
    Map<String, WorksheetEntry> map = Maps.newHashMap();

    URL worksheetFeedUrl =
        FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadSheetId, "private", "values");
    WorksheetFeed feed = service.getFeed(worksheetFeedUrl, WorksheetFeed.class);
    List<WorksheetEntry> worksheetList = feed.getEntries();

    for (WorksheetEntry worksheetEntry : worksheetList) {
      map.put(worksheetEntry.getTitle().getPlainText(), worksheetEntry);
    }
    return map;
  }

  private boolean postResultToServer() {
    try {
      doPostResult();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to post results", e);
      return false;
    }
    return true;
  }

  @VisibleForTesting
  void doPostResult() throws Exception {
    Credential credential = authorize();
    SpreadsheetService service = spreadSheetServiveProvider.get();
    service.setOAuth2Credentials(credential);
    Map<String, WorksheetEntry> sheetsByName = sheetsByName(service);

    // calculate all sheets that need to be created
    Set<String> sheetsToCreate = new HashSet<>(FluentIterable.from(benchmarkRuns).transform(
        new Function<BenchmarkRun, String>() {
            @Override
            public String apply(BenchmarkRun input) {
              return getNameFromBenchmarkRun(input);
            }
        }).toSet());
    Set<String> allSpreadSheetNames = sheetsByName.keySet();
    sheetsToCreate.removeAll(allSpreadSheetNames);

    Map<String, WorksheetEntry> createdWorkSheetsByName = createSheets(service, sheetsToCreate);
    sheetsByName.putAll(createdWorkSheetsByName);


    List<BenchmarkRun> benchmarkNamesList = Lists.newArrayList(benchmarkRuns);
    // make sure our order of adding the spreadsheets is deterministic (makes testing easier)
    Collections.sort(benchmarkNamesList, new Comparator<BenchmarkRun>() {
      @Override
      public int compare(BenchmarkRun o1, BenchmarkRun o2) {
        return o1.getModuleName().compareTo(o2.getModuleName());
      }
    });

    for (BenchmarkRun benchmarkRun  : benchmarkNamesList) {
      WorksheetEntry worksheetEntry = sheetsByName.get(getNameFromBenchmarkRun(benchmarkRun));
      if (worksheetEntry == null) {
        // This should never happen since we just created the damn thing
        throw new IllegalStateException();
      }
      postBenchmarkRunToSpreadSheet(service, worksheetEntry, benchmarkRun);
    }
  }

  private String getNameFromBenchmarkRun(BenchmarkRun run) {
    if (!run.getModuleName().startsWith("com.google.gwt.benchmark.benchmarks.")) {
      throw new IllegalStateException(
          "Uploader is assuming all benchmarks reside in the same package");
    }
    return run.getModuleName().substring("com.google.gwt.benchmark.benchmarks.".length()) + "_"
        + run.getReportingName();
  }

  private void postBenchmarkRunToSpreadSheet(SpreadsheetService service,
      WorksheetEntry worksheetEntry, BenchmarkRun benchmarkRun) throws Exception {

    logger.info(String.format("Updating spreadsheet with commit %s for benchmark %s", commitId,
        benchmarkRun.getModuleName()));
    ensureAllHeadersPresent(service, worksheetEntry, benchmarkRun);

    // Load headers from server so we get the actual order after we added some
    List<String> headers = getHeaders(service, worksheetEntry);

    // remove the first entry since its only the "commit header"
    headers.remove(0);

    // Maybe the commit id already exists in the spreadsheet
    // Reusing rows for commit ids means that even if our update fails half way it will be
    // eventually consistent
    int rowIndex = getRowIndexForCommit(service, worksheetEntry, commitId);

    if (rowIndex == -1) {
      // No entry for this commit id which is fine
      // This means that we will just create a new row
      rowIndex = getFirstEmptyRow(service, worksheetEntry);
    }
    List<ValueToAdd> valuesToAdd = calculateValuesToAdd(headers, benchmarkRun, rowIndex);
    addOrUpdateCells(service, worksheetEntry, valuesToAdd);

    sleepBecauseOfBug();
  }

  @VisibleForTesting
  void sleepBecauseOfBug() throws InterruptedException {
    // TODO(dankurka): follow up with docs teams
    //see http://b/21856597
    Thread.sleep(5000);
  }

  private void ensureAllHeadersPresent(SpreadsheetService service, WorksheetEntry worksheetEntry,
      BenchmarkRun benchmarkRun) throws IOException, ServiceException, URISyntaxException {
    List<String> headers = getHeaders(service, worksheetEntry);
    List<String> runnerNames = getNames(benchmarkRun.getRunConfigs());
    List<String> missingNames = Lists.newArrayList(runnerNames);
    missingNames.removeAll(headers);

    if (!headers.contains(COMMIT_HEADER)) {
      List<String> tmp = Lists.newArrayList(COMMIT_HEADER);
      tmp.addAll(missingNames);
      missingNames = tmp;
    }
    maybeAddMissingHeaders(service, worksheetEntry, missingNames);
  }

  private void addOrUpdateCells(SpreadsheetService service, WorksheetEntry worksheet,
      List<ValueToAdd> valuesToAdd) throws IOException, ServiceException, URISyntaxException {
    URL cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString()).toURL();
    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
    for (ValueToAdd valueToAdd : valuesToAdd) {
      CellEntry cellEntry= new CellEntry (valueToAdd.row, valueToAdd.col, valueToAdd.value);
      cellFeed.insert(cellEntry);
    }
  }

  private List<ValueToAdd> calculateValuesToAdd(List<String> headers, BenchmarkRun benchmarkRun,
      int rowIndex) {
    List<ValueToAdd> valuesToAdd = Lists.newArrayList();

    ValueToAdd v = new ValueToAdd();
    v.row = rowIndex;
    v.col = 1;
    v.value = commitId;
    valuesToAdd.add(v);

    int index = 2;
    for (String header : headers) {
      RunnerConfig runnerConfigByName = getRunnerConfigByName(benchmarkRun.getRunConfigs(), header);
      Result result = benchmarkRun.getResults().get(runnerConfigByName);
      double runsPerSecond = result.getRunsPerSecond();

      ValueToAdd valueToAdd = new ValueToAdd();
      valueToAdd.row = rowIndex;
      valueToAdd.col = index;
      valueToAdd.value = Double.toString(runsPerSecond);
      valuesToAdd.add(valueToAdd);
      index++;
    }
    return valuesToAdd;
  }



  private RunnerConfig getRunnerConfigByName(List<RunnerConfig> runConfigs, String name) {
    for (RunnerConfig runnerConfig : runConfigs) {
     if(name.equals(runnerConfig.toString())) {
       return runnerConfig;
     }
   }
    throw new IllegalStateException("should never happen");
  }


  private int getFirstEmptyRow(SpreadsheetService service, WorksheetEntry worksheet)
      throws URISyntaxException, IOException, ServiceException {
    URL cellFeedUrl =
        new URI(worksheet.getCellFeedUrl().toString() + "?min-col=1&max-col=1").toURL();
    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

    int size = cellFeed.getEntries().size();

    if (size + 1 >= worksheet.getRowCount()) {
      worksheet.setRowCount(size + 1);
    }

    return size + 1;
  }

  private void maybeAddMissingHeaders(SpreadsheetService service, WorksheetEntry worksheet,
      List<String> missingNames) throws URISyntaxException, IOException, ServiceException {
    Collections.sort(missingNames);

    URL cellFeedUrl =
        new URI(worksheet.getCellFeedUrl().toString() + "?min-row=1&max-row=1").toURL();
    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

    int size = cellFeed.getEntries().size();

    if (size + missingNames.size() >= worksheet.getColCount()) {
      worksheet.setColCount(size + missingNames.size());
    }

    for (String name : missingNames) {
      CellEntry cellEntry = new CellEntry(1, 1 + size, name);
      cellFeed.insert(cellEntry);
      size++;
    }
  }

  private List<String> getNames(List<RunnerConfig> runnerConfigs) {
    List<String> list = Lists.newArrayList();
    for (RunnerConfig config : runnerConfigs) {
      list.add(config.toString());
    }
    return list;
  }

  private List<String> getHeaders(SpreadsheetService service, WorksheetEntry worksheet)
      throws IOException, ServiceException, URISyntaxException {
    URL cellFeedUrl =
        new URI(worksheet.getCellFeedUrl().toString() + "?min-row=1&max-row=1").toURL();
    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

    List<String> headers = Lists.newArrayList();

    for (CellEntry cell : cellFeed.getEntries()) {
      String value = cell.getCell().getValue();
      if ("".equals(value)) {
        continue;
      }
      headers.add(value);
    }
    return headers;
  }

  private int getRowIndexForCommit(SpreadsheetService service, WorksheetEntry worksheet,
      String commitId) throws URISyntaxException, IOException, ServiceException {
    URL cellFeedUrl =
        new URI(worksheet.getCellFeedUrl().toString() + "?min-col=1&max-col=1").toURL();
    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

    for (CellEntry cell : cellFeed.getEntries()) {
      String value = cell.getCell().getValue();
      if (commitId.equals(value)) {
        return cell.getCell().getRow();
      }
    }
    return -1;
  }

  private Map<String, WorksheetEntry> createSheets(SpreadsheetService service,
      Set<String> sheetsToCreate) throws IOException, ServiceException {
    Map<String, WorksheetEntry> map = Maps.newHashMap();
    if (sheetsToCreate.isEmpty()) {
      return map;
    }

    URL worksheetFeedUrl =
        FeedURLFactory.getDefault().getWorksheetFeedUrl(spreadSheetId, "private", "values");
    WorksheetFeed feed = service.getFeed(worksheetFeedUrl, WorksheetFeed.class);

    List<String> sortedSheetsToCreate = Lists.newArrayList(sheetsToCreate);
    // make sure our order of adding the spreadsheets is deterministic (makes testing easier)
    Collections.sort(sortedSheetsToCreate);

    for (String name : sortedSheetsToCreate) {
      WorksheetEntry entry = new WorksheetEntry();
      entry.setTitle(new PlainTextConstruct(name));
      entry.setRowCount(1000);
      entry.setColCount(10);
      map.put(name, feed.insert(entry));
    }

    return map;
  }

  @VisibleForTesting
  boolean sleep(int seconds) {
    try {
      Thread.sleep(1000L * seconds);
      return true;
    } catch (InterruptedException e) {
      // Our framework does not make use of thread.interrupt() so this must mean the JVM is trying
      // to gracefully shut down in response to an external signal. Let it happen.
      return false;
    }
  }

  @VisibleForTesting
  Credential authorize() throws IOException, GeneralSecurityException {
    return OAuthHelper.authorize(oAuthDir, clientJsonSecret);
  }
}
