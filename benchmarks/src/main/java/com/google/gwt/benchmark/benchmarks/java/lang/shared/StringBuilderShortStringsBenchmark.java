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
package com.google.gwt.benchmark.benchmarks.java.lang.shared;

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;
import com.google.gwt.benchmark.framework.client.AbstractBenchmarkEntryPoint;
import com.google.gwt.benchmark.framework.shared.AbstractBenchmark;

/**
 * StringBuilder performance test with short strings.
 */
public class StringBuilderShortStringsBenchmark extends AbstractBenchmark {
  public static class EntryPoint extends AbstractBenchmarkEntryPoint {
    @Override
    protected AbstractBenchmark getBenchmark() {
      return new StringBuilderShortStringsBenchmark();
    }
  }

  private JavaScriptArray<String> array;

  public StringBuilderShortStringsBenchmark() {
    super("StringBuilderShortString");
  }

  @Override
  public Object run() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < array.length(); i++) {
      builder.append(array.get(i));
    }
    return builder.toString();
  }

  @Override
  public void setupOneTime() {
    int length = 1000;
    array = CollectionFactory.create(length);

    for (int i = 0; i < length; i++) {
      array.push("" + i);
    }
  }
}
