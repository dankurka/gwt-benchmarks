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

import com.google.gwt.benchmark.collection.client.JavaScriptArrayBooleanGwtImpl;
import com.google.gwt.benchmark.collection.client.JavaScriptArrayGwtImpl;
import com.google.gwt.benchmark.collection.client.JavaScriptArrayIntGwtImpl;
import com.google.gwt.benchmark.collection.client.JavaScriptArrayNumberGwtImpl;
import com.google.gwt.benchmark.collection.shared.impl.JavaScriptArrayBooleanJavaImpl;
import com.google.gwt.benchmark.collection.shared.impl.JavaScriptArrayIntJavaImpl;
import com.google.gwt.benchmark.collection.shared.impl.JavaScriptArrayJavaImpl;
import com.google.gwt.benchmark.collection.shared.impl.JavaScriptArrayNumberJavaImpl;
import com.google.gwt.core.client.GWT;

/**
 * CollectionFactory creates instances for collection interfaces.
 * <p>
 * The Factory instantiates different implementation for JavaScript vs. JVM and should be
 * used inside of benchmarks.
 */
public class CollectionFactory {
  public static <T> JavaScriptArray<T> create() {
    if (GWT.isScript()) {
      return JavaScriptArrayGwtImpl.create();
    } else {
      return new JavaScriptArrayJavaImpl<T>();
    }
  }

  public static <T> JavaScriptArray<T> create(int initialCapacity) {
    if (GWT.isScript()) {
      return JavaScriptArrayGwtImpl.create(initialCapacity);
    } else {
      return new JavaScriptArrayJavaImpl<T>(initialCapacity);
    }
  }

  public static JavaScriptArrayBoolean createBoolean() {
    if (GWT.isScript()) {
      return JavaScriptArrayBooleanGwtImpl.create();
    } else {
      return new JavaScriptArrayBooleanJavaImpl();
    }
  }

  public static JavaScriptArrayBoolean createBoolean(int initialCapacity) {
    if (GWT.isScript()) {
      return JavaScriptArrayBooleanGwtImpl.create(initialCapacity);
    } else {
      return new JavaScriptArrayBooleanJavaImpl(initialCapacity);
    }
  }

  public static JavaScriptArrayInt createInt(int initialCapacity) {
    if (GWT.isScript()) {
      return JavaScriptArrayIntGwtImpl.create(initialCapacity);
    } else {
      return new JavaScriptArrayIntJavaImpl(initialCapacity);
    }
  }

  public static JavaScriptArrayNumber createNumber() {
    if (GWT.isScript()) {
      return JavaScriptArrayNumberGwtImpl.create();
    } else {
      return new JavaScriptArrayNumberJavaImpl();
    }
  }

  public static JavaScriptArrayNumber createNumber(int initialCapacity) {
    if (GWT.isScript()) {
      return JavaScriptArrayNumberGwtImpl.create(initialCapacity);
    } else {
      return new JavaScriptArrayNumberJavaImpl(initialCapacity);
    }
  }
}
