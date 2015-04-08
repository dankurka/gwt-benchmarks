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
package com.google.gwt.benchmark.launcher;

import com.google.api.client.util.Charsets;
import com.google.common.io.Files;
import com.google.gwt.benchmark.oauth2.server.OAuthHelper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * A command line utility to perform OAuth and save credentials.
 */
public class OAuthWriter {

  public static void main(String[] args) {

    if (args.length != 2) {
      System.err.println("Wrong number of arguments");
      printUsageAndExit();
    }

    String clientSecretJson = loadClientSecret(args[1]);

    File oauthDirectoryHandle = new File(args[0]);
    if (!oauthDirectoryHandle.isDirectory()) {
      System.err.println("OAuth directory does not point to a directory" + args[0]);
      printUsageAndExit();
    }

    try {
      OAuthHelper.authorize(oauthDirectoryHandle, clientSecretJson);
    } catch (IOException | GeneralSecurityException e) {
      System.err.println("OAuth failed");
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private static void printUsageAndExit() {
    System.err.println("// TODO(dankurka): Print usage");
    System.exit(-1);
  }

  private static String loadClientSecret(String clientSecretFileName) {
    try {
      return Files.toString(new File(clientSecretFileName), Charsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Can not read clientSecrets: " + clientSecretFileName);
      e.printStackTrace(System.err);
      System.exit(-1);
    }
    throw new IllegalStateException();
  }
}
