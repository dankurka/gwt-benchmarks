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
package com.google.gwt.benchmark.compileserver.shared.dto;

import java.io.Serializable;

/**
 * BenchmarkRunDTO represents one exact execution on a specific Runner on the client
 * side. For all the data of one run take a look at {@link BenchmarkOverviewResponseDTO}.
 */
public class BenchmarkRunDTO implements Serializable {

  public enum State {
    NOT_RUN, FAILED_RUN, DONE
  }

  private double runsPerMinute;
  private State state;
  private String errorMessage;

  public double getRunsPerMinute() {
    return runsPerMinute;
  }

  public void setRunsPerMinute(double runsPerMinute) {
    this.runsPerMinute = runsPerMinute;
  }

  public void setState(State state) {
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
