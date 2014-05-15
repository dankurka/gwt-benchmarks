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
package com.google.gwt.benchmark.dashboard.client.ui;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

/**
 * HistoryAccessor is a thin wrapper around GWT's history implementation.
 */
public class HistoryAccessor {
  public String getToken() {
    return History.getToken();
  }

  public void newItem(String token, boolean issueEvent) {
    History.newItem(token, issueEvent);
  }

  public void replaceItem(String token, @SuppressWarnings("unused") boolean issueEvent) {
    // TODO switch to new History implementation in GWT 2.7
    // This will not work without xsiframe linker on some browsers
    Window.Location.replace("!#" + token);
  }
}
