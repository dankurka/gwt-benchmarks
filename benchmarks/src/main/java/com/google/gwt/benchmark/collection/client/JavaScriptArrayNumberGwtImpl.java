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
package com.google.gwt.benchmark.collection.client;

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT implementation of {@link JavaScriptArrayNumber}.
 * <p>
 * These collections exist to write benchmarks that do not depend on the performance of GWT
 * emulated collection classes. Benchmarks should prefer these collection over the standard
 * Java ones.
 * <p>
 * See also {@link CollectionFactory} for a way to instantiate the right implementation
 * depending on running on the JVM or in JavaScript.
 */
public final class JavaScriptArrayNumberGwtImpl<T> extends JavaScriptObject implements
    JavaScriptArrayNumber {

  public static <T> JavaScriptArrayNumberGwtImpl<T> create() {
    return JavaScriptObject.createArray().cast();
  }

  public static native <T> JavaScriptArrayNumberGwtImpl<T> create(int initialCapacity) /*-{
    return new Array(initialCapacity);
  }-*/;

  @Override
  public native double get(int i) /*-{
    return this[i];
  }-*/;

  @Override
  public native int length() /*-{
    return this.length;
  }-*/;

  @Override
  public native void push(double t) /*-{
    return this.push(t);
  }-*/;

  @Override
  public native void set(int index, double value) /*-{
    this[index] = value;
  }-*/;

  protected JavaScriptArrayNumberGwtImpl() {
  }
}
