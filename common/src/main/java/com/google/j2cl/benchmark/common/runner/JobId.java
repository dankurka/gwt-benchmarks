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
package com.google.j2cl.benchmark.common.runner;

import com.google.common.base.Objects;

/**
 * A unique id for a {@link Job}.
 */
public class JobId {
  private final String id;

  public JobId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "JobId [id=" + id + "]";
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }

    if (!(other instanceof JobId)) {
      return false;
    }
    JobId jobId = (JobId) other;

    return Objects.equal(id, jobId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
