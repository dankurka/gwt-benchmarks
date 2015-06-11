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

/**
 * Settings relevant for sending an email.
 */
public class MailSettings {
  private String to;
  private String from;
  private String host;
  private String username;
  private String password;

  public MailSettings(String from, String to, String host, String username, String password) {
    this.from = from;
    this.to = to;
    this.host = host;
    this.username = username;
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }
}
