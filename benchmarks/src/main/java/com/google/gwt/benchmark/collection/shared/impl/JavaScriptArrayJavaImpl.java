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
package com.google.gwt.benchmark.collection.shared.impl;

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Java implementation of {@link JavaScriptArray}.
 * <p>
 * These collections exist to write benchmarks that do not depend on the performance of GWT
 * emulated collection classes. Benchmarks should prefer these collection over the standard
 * Java ones.
 * <p>
 * See also {@link CollectionFactory} for a way to instantiate the right implementation
 * depending on running on the JVM or in JavaScript.
 */
public class JavaScriptArrayJavaImpl<T> implements JavaScriptArray<T> {

  private List<T> list = new ArrayList<T>();

  public JavaScriptArrayJavaImpl() {
    list = new ArrayList<T>();
  }

  public JavaScriptArrayJavaImpl(int initialCapacity) {
    list = new ArrayList<T>(initialCapacity);
  }

  public T get(int i) {
    return list.get(i);
  }

  public int length() {
    return list.size();
  }

  public void push(T t) {
    list.add(t);
  }

  public void set(int index, T value) {
    // This does not handle out of bounds access currently.
    // No benchmark has run into trouble so far.
    if(index == list.size()) {
      list.add(value);
    } else {
      list.set(index, value);
    }
  }
}
