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
package com.google.gwt.benchmark.framework.shared;

/**
 * Abstraction for current time.
 *
 * Implementations should use window.performance.now if available otherwise fall back to
 * {@link System#currentTimeMillis()}.
 */
public interface Performance {
  /**
   * Get the current (relative) time.
   *
   * <p>The time is not guaranteed to be absolute to 1. Jan 1970.
   *
   * @return the current relative time.
   */
  public double now();
}
