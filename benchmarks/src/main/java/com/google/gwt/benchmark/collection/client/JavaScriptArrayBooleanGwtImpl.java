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
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayBoolean;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT implementation of {@link JavaScriptArrayBoolean}.
 * <p>
 * These collections exist to write benchmarks that do not depend on the performance of GWT
 * emulated collection classes. Benchmarks should prefer these collection over the standard
 * Java ones.
 * <p>
 * See also {@link CollectionFactory} for a way to instantiate the right implementation
 * depending on running on the JVM or in JavaScript.
 */
public final class JavaScriptArrayBooleanGwtImpl<T> extends JavaScriptObject implements
    JavaScriptArrayBoolean {

  public static <T> JavaScriptArrayBooleanGwtImpl<T> create() {
    return JavaScriptObject.createArray().cast();
  }

  public static native <T> JavaScriptArrayBooleanGwtImpl<T> create(int initialCapacity) /*-{
    return new Array(initialCapacity);
  }-*/;

  public native boolean get(int i) /*-{
    return this[i];
  }-*/;

  public native int length() /*-{
    return this.length;
  }-*/;

  public native void push(boolean t) /*-{
    return this.push(t);
  }-*/;

  public native void set(int index, boolean value) /*-{
    this[index] = value;
  }-*/;

  protected JavaScriptArrayBooleanGwtImpl() {
  }
}
