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
package com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt;

/**
 * This class only exists to enable us to run on top of a JVM as well.
 *
 * <p>There are two concrete subclasses one for execution in JS environment and
 * one for execution on the JVM
 */
public abstract class AbstractOrderedCollection<T> {

  public abstract void add(T o);

  public abstract T at(int index);

  public abstract int size();

  public abstract T removeFirst();

  public void remove(T elm) {
    // The whole method seems to be incorrect, but it is a line by line port from the octane
    // benchmark. This is not a simple remove element method.
    // We should follow up with the V8 team and maybe fix the original benchmark.
    int index = 0;
    int skipped = 0;
    for (int i = 0; i < size(); i++) {
      T value = this.at(i);
      if (notEquals(value, elm)) {
        set(index, value);
        index++;
      } else {
        skipped++;
      }
    }
    for (int i = 0; i < skipped; i++) {
      removeFirst();
    }
  }

  protected abstract void set(int index, T o);

  protected abstract boolean notEquals(T a, T b);
}
