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
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;

/**
 * Java implementation of {@link JavaScriptArrayNumber}.
 * <p>
 * These collections exist to write benchmarks that do not depend on the performance of GWT
 * emulated collection classes. Benchmarks should prefer these collection over the standard
 * Java ones.
 * <p>
 * See also {@link CollectionFactory} for a way to instantiate the right implementation
 * depending on running on the JVM or in JavaScript.
 */
public class JavaScriptArrayNumberJavaImpl implements JavaScriptArrayNumber {

  private double[] array;
  private int length;

  public JavaScriptArrayNumberJavaImpl() {
    array = new double[64];
  }

  public JavaScriptArrayNumberJavaImpl(int initialCapacity) {
    array = new double[initialCapacity];
  }

  public double get(int i) {
    return array[i];
  }

  public int length() {
    return length;
  }

  public void push(double t) {
    if (length >= array.length) {
      double[] newArray = new double[array.length * 2];
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }

    array[length] = t;
    length++;
  }

  public void set(int index, double value) {
    // This does not handle out of bounds access currently.
    // No benchmark has run into trouble so far.
    array[index] = value;
  }
}
