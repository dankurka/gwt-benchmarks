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
package com.google.gwt.benchmark.collection.shared;

/**
 * Java implementation of {@link JavaScriptArrayBoolean}.
 */
public class JavaScriptArrayBooleanJavaImpl implements JavaScriptArrayBoolean {

  private boolean[] array;
  private int length = 0;

  public JavaScriptArrayBooleanJavaImpl() {
    array = new boolean[64];
  }

  public JavaScriptArrayBooleanJavaImpl(int capacity) {
    array = new boolean[capacity];
  }

  public boolean get(int i) {
    return array[i];
  }

  public int length() {
    return length;
  }

  public void push(boolean t) {
    if(length > array.length) {
      boolean[] newArray = new boolean[array.length * 2];
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }

    array[length] = t;
    length++;
  }
}
