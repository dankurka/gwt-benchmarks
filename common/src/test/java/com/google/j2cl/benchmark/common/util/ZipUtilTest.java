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
package com.google.j2cl.benchmark.common.util;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test for {@link ZipUtil}.
 */
public class ZipUtilTest {

  private File tempDir;
  private File barDir;
  private File bazFile;
  private File fooFile;
  private File zipFile;
  private File unzipDir;

  @Before
  public void before() throws IOException {
    tempDir = Files.createTempDir();
    fooFile = new File(tempDir, "foo");
    Files.asCharSink(fooFile, Charsets.UTF_8, FileWriteMode.APPEND).write("foo");
    barDir = new File(tempDir, "bar/");
    barDir.mkdir();
    bazFile = new File(barDir, "baz");
    Files.asCharSink(bazFile, Charsets.UTF_8, FileWriteMode.APPEND).write("baz");
  }

  @After
  public void after() {
    FileUtils.deleteQuietly(tempDir);
    FileUtils.deleteQuietly(zipFile);
    FileUtils.deleteQuietly(unzipDir);
  }

  @Test
  public void zipAndUnzipFolder() throws IOException {
    zipFile = ZipUtil.zipFolder(tempDir, "my_zipped_content");
    assertThat(zipFile.exists()).isTrue();

    unzipDir = Files.createTempDir();

    assertThat(unzipDir.listFiles().length).isEqualTo(0);

    ZipUtil.unzip(zipFile, unzipDir);

    assertThat(unzipDir.listFiles()).hasLength(2);

    File newBarDir = new File(unzipDir, "bar/");
    File newFooFile = new File(unzipDir, "foo");
    File newBazFile = new File(newBarDir, "baz");
    assertThat(unzipDir.listFiles())
        .asList()
        .containsExactly(newFooFile, newBarDir);

    assertThat(newBarDir.listFiles())
    .asList()
    .containsExactly(newBazFile);

    assertThat(Files.asCharSource(newFooFile, Charsets.UTF_8).read()).isEqualTo("foo");
    assertThat(Files.asCharSource(newBazFile, Charsets.UTF_8).read()).isEqualTo("baz");
  }

}

