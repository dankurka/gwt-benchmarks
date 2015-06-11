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

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to deal with zip files.
 */
public class ZipUtil {

  public static void unzip(File zipFile, File destDir) throws IOException {
    if (!destDir.exists()) {
      if (!destDir.mkdirs()) {
        throw new IOException("Can not create dir: " + destDir.getAbsolutePath());
      }
    }
    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry entry = zipIn.getNextEntry();

    while (entry != null) {
      String filePath = destDir + File.separator + entry.getName();
      if (!entry.isDirectory()) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));) {
          IOUtils.copy(zipIn, bos);
        }
      } else {
        new File(filePath).mkdir();
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  public static File zipFolder(File folder, String fileName) throws IOException {
    File zipFile = File.createTempFile(fileName, ".zip");
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      for (File f : listAllFiles(folder)) {
        addToZipFile(folder, f, zos);
      }
    }
    return zipFile;
  }

  private static List<File> listAllFiles(File outputDir) {
    List<File> files = Lists.newArrayList();
    listAllFiles(outputDir, files);
    return files;
  }

  private static void listAllFiles(File current, List<File> files) {
    if (current.isDirectory()) {
      for( File f : current.listFiles()) {
        listAllFiles(f, files);
      }
    } else if (current.isFile()) {
      files.add(current);
    }
  }

  private static void addToZipFile(File baseDir, File fileToWrite, ZipOutputStream zos)
      throws FileNotFoundException, IOException {
    String relativeName =
        fileToWrite.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
    FileInputStream fis = new FileInputStream(fileToWrite);
    ZipEntry zipEntry = new ZipEntry(relativeName);
    zos.putNextEntry(zipEntry);
    IOUtils.copy(fis, zos);
    zos.closeEntry();
    fis.close();
  }
}
