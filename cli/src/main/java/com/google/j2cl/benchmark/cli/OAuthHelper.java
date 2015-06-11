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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AbstractPromptReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * OAuthHelper enables easy OAuth.
 */
public class OAuthHelper {
  private static class MyReceiver extends AbstractPromptReceiver {
    @Override
    public String getRedirectUri() throws IOException {
      // this just emits the code so that we can paste it back into the console
      return "urn:ietf:wg:oauth:2.0:oob";
    }
  }

  public static Credential authorize(File dataDirectory, String clientJsonSecret)
      throws IOException, GeneralSecurityException {
    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataDirectory);

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientJsonSecret));

    Set<String> scopes =
        Sets.newHashSet("https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds");

    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
        JSON_FACTORY, clientSecrets, scopes).setDataStoreFactory(dataStoreFactory).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new MyReceiver()).authorize("user");
  }
}
