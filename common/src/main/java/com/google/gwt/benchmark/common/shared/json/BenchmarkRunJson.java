/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.common.shared.json;

import java.util.List;
import java.util.Map;

/**
 * A JSON representation of a set of benchmark runs.
 */
public interface BenchmarkRunJson {

  String getCommitId();

  /**
   * Get the commit time of the patch in milliseconds since 1970.
   * <p>
   * Note: This is not the author time of the commit, but the time it has been merged into the
   * repository
   */
  double getCommitTimeMsEpoch();

  Map<String, List<BenchmarkResultJson>> getResultByBenchmarkName();

  void setResultByBenchmarkName(Map<String, List<BenchmarkResultJson>> results);

  void setCommitId(String commitId);

  void setCommitTime(String commitTime);

  /**
   * Set the commit time of the patch in milliseconds since 1970.
   * <p>
   * Note: This is not the author time of the commit, but the time it has been merged into the
   * repository
   */
  void setCommitTimeMsEpoch(double commitMsEpoch);
}
