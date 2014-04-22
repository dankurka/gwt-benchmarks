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
package com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper;

import com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.AbstractOrderedCollection;

import java.util.ArrayList;

/**
 * Java implementation of {@link AbstractOrderedCollection}.
 *
 * This enables us to run the benchmark on a JVM
 */
public class OrderedCollectionJavaImpl<T> extends AbstractOrderedCollection<T> {

  private ArrayList<T> list = new ArrayList<T>();

  @Override
  public void add(T o) {
    list.add(o);
  }

  @Override
  public T at(int index) {
    return list.get(index);
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public T removeFirst() {
    return list.remove(0);
  }

  @Override
  protected void set(int index, T o) {
    list.set(index, o);
  }

  @Override
  protected boolean notEquals(T a, T b) {
    return a != b;
  }
}
