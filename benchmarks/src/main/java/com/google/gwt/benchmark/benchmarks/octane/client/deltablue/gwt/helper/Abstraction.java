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
import com.google.gwt.core.client.GWT;

/**
 * Abstraction allows to construct different objects for JS code and JVM code.
 */
public class Abstraction {

 public static <T> AbstractOrderedCollection<T>  create(){
   if(GWT.isScript()) {
     // Allow GWT test to select different implementations
     return GWT.create(AbstractOrderedCollection.class);
   } else{
     return new OrderedCollectionJavaImpl<T>();
   }
 }

 public static boolean jsNotEquals(Object a, Object b) {
   if(GWT.isScript()) {
     return notEquals(a, b);
   } else {
     return a != b;
   }
 }

 private static native boolean notEquals(Object a, Object b) /*-{
   return a != b
 }-*/;
}