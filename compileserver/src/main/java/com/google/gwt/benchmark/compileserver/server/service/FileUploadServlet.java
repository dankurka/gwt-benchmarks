package com.google.gwt.benchmark.compileserver.server.service;

import com.google.inject.Provider;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for SDK uploads.
 */
public class FileUploadServlet extends HttpServlet {

  private final Provider<String> randomStringProvider;

  private static File sdkFolder;

  public static File getSdkFolder() {
    return sdkFolder;
  }

  @Inject
  public FileUploadServlet(@Named("randomStringProvider") Provider<String> randomStringProvider) {
    this.randomStringProvider = randomStringProvider;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
      for (FileItem item : items) {
        if (!item.isFormField()) {
          InputStream fileContent = item.getInputStream();
          String randomString = randomStringProvider.get();
          File tempFile = File.createTempFile(randomString + "-gwt-sdk", ".zip");
          FileOutputStream tempOutputStream = new FileOutputStream(tempFile);
          File folder = new File(randomString + "-gwt-sdk/");
          folder.mkdirs();

          try {
            IOUtils.copy(fileContent, tempOutputStream);
            tempOutputStream.close();

            ZipFile zipFile = new ZipFile(tempFile.getAbsolutePath());
            zipFile.extractAll(folder.getAbsolutePath());
          } catch (ZipException | IOException e ) {
           throw new ServletException(e);
          } finally {
            tempFile.delete();
          }

          sdkFolder = new File(folder, "gwt-0.0.0");
          response.getWriter().write(folder.getAbsolutePath());
        }
      }
    } catch (FileUploadException e) {
      throw new ServletException("Upload failed.", e);
    }
  }
}
