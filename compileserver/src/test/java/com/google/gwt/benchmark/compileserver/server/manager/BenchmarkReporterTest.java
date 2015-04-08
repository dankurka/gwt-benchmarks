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
package com.google.gwt.benchmark.compileserver.server.manager;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.auth.oauth2.Credential;
import com.google.common.collect.Lists;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import com.google.gwt.benchmark.compileserver.server.manager.BenchmarkReporter.ReportProgressHandler;
import com.google.inject.Provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Test for {@link BenchmarkReporter}.
 */
public class BenchmarkReporterTest {

  private BenchmarkReporter reporter;
  private HashMap<String, BenchmarkRun> results;
  private String commitId;
  private ReportProgressHandler reportProgressHandler;
  private SpreadsheetService spreadsheetService;
  private Provider<SpreadsheetService> spreadsheetServiceProvider;
  private File oauthDir;
  private Credential credential;

  private URL SPREAD_SHEET_URL;
  private URL HEADER_URL1;
  private URL HEADER_URL2;
  private URL COMMIT_URL1;
  private URL COMMIT_URL2;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws MalformedURLException, URISyntaxException {

    SPREAD_SHEET_URL = new URI(
        "https://spreadsheets.google.com/feeds/worksheets/spreadSheetId/private/values").toURL();

    HEADER_URL1 = new URI("https://foo/module1?min-row=1&max-row=1").toURL();
    HEADER_URL2 = new URI("https://foo/module2?min-row=1&max-row=1").toURL();

    COMMIT_URL1 = new URI("https://foo/module1?min-col=1&max-col=1").toURL();
    COMMIT_URL2 = new URI("https://foo/module2?min-col=1&max-col=1").toURL();

    commitId = "commitId1";
    int commitDate = 77;

    results = new HashMap<>();
    BenchmarkRun benchmarkRun = new BenchmarkRun("module1", commitId, commitDate);
    benchmarkRun.addRunner(RunnerConfigs.CHROME_LINUX);
    benchmarkRun.addRunner(RunnerConfigs.FIREFOX_LINUX);
    benchmarkRun.addResult(RunnerConfigs.CHROME_LINUX, 2);
    benchmarkRun.addResult(RunnerConfigs.FIREFOX_LINUX, 3);
    results.put("module1", benchmarkRun);
    BenchmarkRun benchmarkRun1 = new BenchmarkRun("module2", commitId, commitDate);
    benchmarkRun1.addRunner(RunnerConfigs.CHROME_LINUX);
    benchmarkRun1.addRunner(RunnerConfigs.FIREFOX_LINUX);
    benchmarkRun1.addResult(RunnerConfigs.CHROME_LINUX, 4);
    benchmarkRun1.addResult(RunnerConfigs.FIREFOX_LINUX, 5);
    results.put("module2", benchmarkRun1);

    reportProgressHandler = Mockito.mock(ReportProgressHandler.class);

    spreadsheetService = mock(SpreadsheetService.class);

    spreadsheetServiceProvider = mock(Provider.class);
    when(spreadsheetServiceProvider.get()).thenReturn(spreadsheetService);

    oauthDir = mock(File.class);

    credential = mock(Credential.class);

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAddToExistingSpreadSheets() throws IOException, URISyntaxException,
      ServiceException {

    reporter = new BenchmarkReporter(results, commitId, reportProgressHandler, oauthDir, "secret",
        spreadsheetServiceProvider, "spreadSheetId") {

        @Override
      Credential authorize() throws IOException, GeneralSecurityException {
        return credential;
      }

        @Override
      boolean sleep(int seconds) {
        return true;
      }
    };

    List<CellEntry> headerEntriesList = Lists.<CellEntry> newArrayList(
        mockCellEntry(1, 1, "commit"), mockCellEntry(1, 2, "linux chrome"),
        mockCellEntry(1, 3, "linux firefox"));

    WorksheetFeed worksheetFeed = mock(WorksheetFeed.class);
    when(spreadsheetService.getFeed(SPREAD_SHEET_URL, WorksheetFeed.class)).thenReturn(
        worksheetFeed);

    // mock two worksheets
    WorksheetEntry module1Worksheet = mockWorkSheetEntry("module1", "https://foo/module1");
    WorksheetEntry module2Worksheet = mockWorkSheetEntry("module2", "https://foo/module2");
    when(worksheetFeed.getEntries()).thenReturn(Arrays.asList(module1Worksheet, module2Worksheet));

    // mock cell feeds of the two work sheets
    CellFeed cellFeedSheet1 = mock(CellFeed.class);
    when(cellFeedSheet1.getEntries()).thenReturn(Lists.<CellEntry> newArrayList(),
        Lists.<CellEntry> newArrayList(), headerEntriesList);
    when(spreadsheetService.getFeed(HEADER_URL1, CellFeed.class)).thenReturn(cellFeedSheet1);
    CellFeed cellFeedSheet2 = mock(CellFeed.class);
    when(cellFeedSheet2.getEntries()).thenReturn(Lists.<CellEntry> newArrayList(),
        Lists.<CellEntry> newArrayList(), headerEntriesList);
    when(spreadsheetService.getFeed(HEADER_URL2, CellFeed.class)).thenReturn(cellFeedSheet2);

    // mock queries for commit row
    List<CellEntry> commitsCol = Lists.<CellEntry> newArrayList(mockCellEntry(1, 1, "commit"));
    CellFeed commitCol1 = mock(CellFeed.class);
    when(commitCol1.getEntries()).thenReturn(commitsCol);
    when(spreadsheetService.getFeed(COMMIT_URL1, CellFeed.class)).thenReturn(commitCol1);
    CellFeed commitCol2 = mock(CellFeed.class);
    when(commitCol2.getEntries()).thenReturn(commitsCol);
    when(spreadsheetService.getFeed(COMMIT_URL2, CellFeed.class)).thenReturn(commitCol2);

    // return mock for the work
    CellFeed module1CellFeed = mock(CellFeed.class);
    when(spreadsheetService.getFeed(new URI("https://foo/module1").toURL(), CellFeed.class))
        .thenReturn(module1CellFeed);
    CellFeed module2CellFeed = mock(CellFeed.class);
    when(spreadsheetService.getFeed(new URI("https://foo/module2").toURL(), CellFeed.class))
        .thenReturn(module2CellFeed);

    reporter.run();

    verify(spreadsheetService).setOAuth2Credentials(credential);

    ArgumentCaptor<CellEntry> cellEntryModule1Captor = ArgumentCaptor.forClass(CellEntry.class);
    verify(module1CellFeed, times(3)).insert(cellEntryModule1Captor.capture());

    List<CellEntry> cellsModule1 = cellEntryModule1Captor.getAllValues();
    assertThat(cellsModule1.size()).isEqualTo(3);

    assertThat(cellsModule1.get(0).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule1.get(0).getCell().getCol()).isEqualTo(1);
    assertThat(cellsModule1.get(0).getCell().getInputValue()).isEqualTo("commitId1");

    assertThat(cellsModule1.get(1).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule1.get(1).getCell().getCol()).isEqualTo(2);
    assertThat(cellsModule1.get(1).getCell().getInputValue()).isEqualTo("2.0");

    assertThat(cellsModule1.get(2).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule1.get(2).getCell().getCol()).isEqualTo(3);
    assertThat(cellsModule1.get(2).getCell().getInputValue()).isEqualTo("3.0");

    ArgumentCaptor<CellEntry> cellEntryModule2Captor = ArgumentCaptor.forClass(CellEntry.class);
    verify(module2CellFeed, times(3)).insert(cellEntryModule2Captor.capture());

    List<CellEntry> cellsModule2 = cellEntryModule2Captor.getAllValues();
    assertThat(cellsModule2.size()).isEqualTo(3);

    assertThat(cellsModule2.get(0).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule2.get(0).getCell().getCol()).isEqualTo(1);
    assertThat(cellsModule2.get(0).getCell().getInputValue()).isEqualTo("commitId1");

    assertThat(cellsModule2.get(1).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule2.get(1).getCell().getCol()).isEqualTo(2);
    assertThat(cellsModule2.get(1).getCell().getInputValue()).isEqualTo("4.0");

    assertThat(cellsModule2.get(2).getCell().getRow()).isEqualTo(2);
    assertThat(cellsModule2.get(2).getCell().getCol()).isEqualTo(3);
    assertThat(cellsModule2.get(2).getCell().getInputValue()).isEqualTo("5.0");
  }

  @Test
  public void createWorkSheets() throws IOException, ServiceException, URISyntaxException {
    reporter = new BenchmarkReporter(results, commitId, reportProgressHandler, oauthDir, "secret",
        spreadsheetServiceProvider, "spreadSheetId") {

        @Override
      Credential authorize() throws IOException, GeneralSecurityException {
        return credential;
      }

        @Override
      boolean sleep(int seconds) {
        return true;
      }
    };

    WorksheetFeed worksheetFeed = mock(WorksheetFeed.class);
    when(spreadsheetService.getFeed(SPREAD_SHEET_URL, WorksheetFeed.class)).thenReturn(
        worksheetFeed);
    // no worksheets on server
    when(worksheetFeed.getEntries()).thenReturn(Lists.<WorksheetEntry> newArrayList());

    // setup mocking for adding worksheets
    ArgumentCaptor<WorksheetEntry> worksheetEntryCaptor =
        ArgumentCaptor.forClass(WorksheetEntry.class);
    WorksheetEntry module1Worksheet = mockWorkSheetEntry("module1", "https://foo/module1");
    WorksheetEntry module2Worksheet = mockWorkSheetEntry("module2", "https://foo/module2");
    when(worksheetFeed.insert(worksheetEntryCaptor.capture())).thenReturn(module1Worksheet,
        module2Worksheet);

    // no headers in worksheets
    CellFeed emptyHeaderFeed = mockEmptyCellFeed();

    CellFeed feedThatGetsHeadersInsertedModule1 = mockEmptyCellFeed();
    ArgumentCaptor<CellEntry> cellEntryCaptor1 = ArgumentCaptor.forClass(CellEntry.class);
    when(feedThatGetsHeadersInsertedModule1.insert(cellEntryCaptor1.capture())).thenReturn(null);

    CellFeed feedThatGetsHeadersInsertedModule2 = mockEmptyCellFeed();
    ArgumentCaptor<CellEntry> cellEntryCaptor2 = ArgumentCaptor.forClass(CellEntry.class);
    when(feedThatGetsHeadersInsertedModule2.insert(cellEntryCaptor2.capture())).thenReturn(null);

    // headers
    List<CellEntry> headerEntriesList = Lists.<CellEntry> newArrayList(
        mockCellEntry(1, 1, "commit"), mockCellEntry(1, 2, "linux chrome"),
        mockCellEntry(1, 3, "linux firefox"));
    CellFeed addedHeaderFeed = mock(CellFeed.class);
    when(addedHeaderFeed.getEntries()).thenReturn(headerEntriesList);

    // mock queries for commit row
    List<CellEntry> commitsCol = Lists.<CellEntry> newArrayList(mockCellEntry(1, 1, "commit"));
    CellFeed commitCol = mock(CellFeed.class);
    when(commitCol.getEntries()).thenReturn(commitsCol);
    when(spreadsheetService.getFeed(COMMIT_URL1, CellFeed.class)).thenReturn(commitCol);
    when(spreadsheetService.getFeed(COMMIT_URL2, CellFeed.class)).thenReturn(commitCol);

    when(spreadsheetService.getFeed(HEADER_URL1, CellFeed.class)).thenReturn(emptyHeaderFeed,
        feedThatGetsHeadersInsertedModule1, addedHeaderFeed);
    when(spreadsheetService.getFeed(HEADER_URL2, CellFeed.class)).thenReturn(emptyHeaderFeed,
        feedThatGetsHeadersInsertedModule2, addedHeaderFeed);

    CellFeed module1AddCellFeed = mockEmptyCellFeed();
    when(spreadsheetService.getFeed(new URI("https://foo/module1").toURL(), CellFeed.class))
        .thenReturn(module1AddCellFeed);

    CellFeed module2AddCellFeed = mockEmptyCellFeed();
    when(spreadsheetService.getFeed(new URI("https://foo/module2").toURL(), CellFeed.class))
        .thenReturn(module2AddCellFeed);

    ArgumentCaptor<CellEntry> module1AddCellFeedCaptor = ArgumentCaptor.forClass(CellEntry.class);
    when(module1AddCellFeed.insert(module1AddCellFeedCaptor.capture())).thenReturn(null);
    ArgumentCaptor<CellEntry> module2AddCellFeedCaptor = ArgumentCaptor.forClass(CellEntry.class);
    when(module2AddCellFeed.insert(module2AddCellFeedCaptor.capture())).thenReturn(null);

    reporter.run();

    assertHeader(cellEntryCaptor1.getAllValues());
    assertHeader(cellEntryCaptor2.getAllValues());

    List<CellEntry> addedCellsModule1 = module1AddCellFeedCaptor.getAllValues();
    assertThat(addedCellsModule1.size()).isEqualTo(3);
    assertThat(addedCellsModule1.get(0).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule1.get(0).getCell().getCol()).isEqualTo(1);
    assertThat(addedCellsModule1.get(0).getCell().getInputValue()).isEqualTo("commitId1");
    assertThat(addedCellsModule1.get(1).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule1.get(1).getCell().getCol()).isEqualTo(2);
    assertThat(addedCellsModule1.get(1).getCell().getInputValue()).isEqualTo("2.0");
    assertThat(addedCellsModule1.get(2).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule1.get(2).getCell().getCol()).isEqualTo(3);
    assertThat(addedCellsModule1.get(2).getCell().getInputValue()).isEqualTo("3.0");

    List<CellEntry> addedCellsModule2 = module2AddCellFeedCaptor.getAllValues();
    assertThat(addedCellsModule2.size()).isEqualTo(3);
    assertThat(addedCellsModule2.get(0).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule2.get(0).getCell().getCol()).isEqualTo(1);
    assertThat(addedCellsModule2.get(0).getCell().getInputValue()).isEqualTo("commitId1");
    assertThat(addedCellsModule2.get(1).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule2.get(1).getCell().getCol()).isEqualTo(2);
    assertThat(addedCellsModule2.get(1).getCell().getInputValue()).isEqualTo("4.0");
    assertThat(addedCellsModule2.get(2).getCell().getRow()).isEqualTo(2);
    assertThat(addedCellsModule2.get(2).getCell().getCol()).isEqualTo(3);
    assertThat(addedCellsModule2.get(2).getCell().getInputValue()).isEqualTo("5.0");

    List<WorksheetEntry> addedWorkSheets = worksheetEntryCaptor.getAllValues();

    assertThat(addedWorkSheets.size()).isEqualTo(2);
    assertThat(addedWorkSheets.get(0).getTitle().getPlainText()).isEqualTo("module1");
    assertThat(addedWorkSheets.get(1).getTitle().getPlainText()).isEqualTo("module2");

    verify(spreadsheetService).setOAuth2Credentials(credential);
  }

  private void assertHeader(List<CellEntry> headerEntries) {
    assertThat(headerEntries.size()).isEqualTo(3);
    assertThat(headerEntries.get(0).getCell().getRow()).isEqualTo(1);
    assertThat(headerEntries.get(0).getCell().getCol()).isEqualTo(1);
    assertThat(headerEntries.get(0).getCell().getInputValue()).isEqualTo("commit");

    assertThat(headerEntries.get(1).getCell().getRow()).isEqualTo(1);
    assertThat(headerEntries.get(1).getCell().getCol()).isEqualTo(2);
    assertThat(headerEntries.get(1).getCell().getInputValue()).isEqualTo("linux chrome");

    assertThat(headerEntries.get(2).getCell().getRow()).isEqualTo(1);
    assertThat(headerEntries.get(2).getCell().getCol()).isEqualTo(3);
    assertThat(headerEntries.get(2).getCell().getInputValue()).isEqualTo("linux firefox");
  }

  private WorksheetEntry mockWorkSheetEntry(String title, String url) throws MalformedURLException,
      URISyntaxException {
    WorksheetEntry worksheetEntry = mock(WorksheetEntry.class);
    when(worksheetEntry.getTitle()).thenReturn(new PlainTextConstruct(title));
    when(worksheetEntry.getCellFeedUrl()).thenReturn(new URI(url).toURL());
    return worksheetEntry;
  }

  @Test
  public void testFailingRetries() {
    final List<Integer> waitingTimes = new ArrayList<>();
    reporter = new BenchmarkReporter(results, commitId, reportProgressHandler, oauthDir, "secret",
        spreadsheetServiceProvider, "spreadSheetId") {
        @Override
      void doPostResult() throws Exception {
        throw new Exception();
      }

        @Override
      boolean sleep(int seconds) {
        waitingTimes.add(seconds);
        return true;
      }
    };
    reporter.run();
    assertThat(waitingTimes).containsExactlyElementsIn(BenchmarkReporter.WAITING_TIME_SECONDS);
    Mockito.verify(reportProgressHandler).onPermanentFailure();
  }

  private CellEntry mockCellEntry(int row, int col, String value) {
    Cell cell = mock(Cell.class);
    when(cell.getValue()).thenReturn(value);
    when(cell.getCol()).thenReturn(Integer.valueOf(col));
    when(cell.getRow()).thenReturn(Integer.valueOf(row));

    CellEntry cellEntry = mock(CellEntry.class);
    when(cellEntry.getCell()).thenReturn(cell);

    return cellEntry;
  }

  private CellFeed mockEmptyCellFeed() {
    CellFeed cellFeed = mock(CellFeed.class);
    when(cellFeed.getEntries()).thenReturn(Lists.<CellEntry> newArrayList());
    return cellFeed;
  }
}
