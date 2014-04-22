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
import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript implementation of {@link AbstractOrderedCollection}.
 *
 * This is implemented this way to exactly mimic the benchmark from JavaScript
 */
public class OrderedCollectionJsImpl<T> extends AbstractOrderedCollection<T> {

  private JavaScriptObject array = JavaScriptObject.createArray();

  @Override
  public native void add(T o) /*-{
    this.@com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.OrderedCollectionJsImpl::array.push(o);
  }-*/;

  @Override
  public native T at(int index) /*-{
    return this.@com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.OrderedCollectionJsImpl::array[index];
  }-*/;

  @Override
  public native int size() /*-{
    return this.@com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.OrderedCollectionJsImpl::array.length;
  }-*/;

  @Override
  public native T removeFirst() /*-{
    return this.@com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.OrderedCollectionJsImpl::array.pop();
  }-*/;

  @Override
  public native void set(int index, T o) /*-{
    this.@com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.OrderedCollectionJsImpl::array[index] = o;
  }-*/;

  @Override
  public native boolean notEquals(T a, T b) /*-{
    return a != b
  }-*/;
}
