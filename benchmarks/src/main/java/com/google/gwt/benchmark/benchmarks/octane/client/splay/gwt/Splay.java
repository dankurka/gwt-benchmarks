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
package com.google.gwt.benchmark.benchmarks.octane.client.splay.gwt;

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArrayNumber;
import com.google.gwt.core.client.GWT;

public class Splay {
  // Configuration.
  private final int kSplayTreeSize = 8000;
  private final int kSplayTreeModifications = 80;
  private final int kSplayTreePayloadDepth = 5;

  private SplayTree splayTree = null;
  private double splaySampleTimeStart = 0.0;

  private static class Payload {

    @SuppressWarnings("unused")
    private JavaScriptArrayNumber array;
    @SuppressWarnings("unused")
    private String string;
    @SuppressWarnings("unused")
    private Payload left;
    @SuppressWarnings("unused")
    private Payload right;

    public Payload(String tag) {
      JavaScriptArrayNumber numbers = CollectionFactory.createNumber(10);
      numbers.push(0);
      numbers.push(1);
      numbers.push(2);
      numbers.push(3);
      numbers.push(4);
      numbers.push(5);
      numbers.push(6);
      numbers.push(7);
      numbers.push(8);
      numbers.push(9);
      this.array = numbers;
      this.string = "String for key " + tag + " in leaf node";
    }

    public Payload(Payload left, Payload right) {
      this.left = left;
      this.right = right;
    }
  }

  private Payload GeneratePayloadTree(int depth, String tag) {
    if (depth == 0) {
      return new Payload(tag);
    } else {
      return new Payload(GeneratePayloadTree(depth - 1, tag), GeneratePayloadTree(depth - 1, tag));
    }
  }

  private double GenerateKey() {
    // original code:
    // // The benchmark framework guarantees that Math.random is
    // // deterministic; see base.js.
    // return Math.random();

    // making sure we do not introduce randomness
    return BenchmarkMath.random();
  }

  private int splaySamples = 0;
  private int splaySumOfSquaredPauses = 0;

  private int SplayRMS() {
    return (int) Math.round(Math.sqrt(splaySumOfSquaredPauses / splaySamples) * 10000);
  }

  private void SplayUpdateStats(double time) {
    double pause = time - splaySampleTimeStart;
    splaySampleTimeStart = time;
    splaySamples++;
    splaySumOfSquaredPauses += pause * pause;
  }

  private double InsertNewNode() {
    // Insert new node with a unique key.
    double key;
    do {
      key = GenerateKey();
    } while (splayTree.find(key) != null);

    Payload payload = GeneratePayloadTree(kSplayTreePayloadDepth, key + "");
    splayTree.insert(key, payload);
    return key;
  }

  private boolean hasPerformanceNow() {
    if (GWT.isScript()) {
      return hasPerformanceNow0();
    } else {
      return true;
    }

  }

  private native boolean hasPerformanceNow0() /*-{
    return !!$wnd.performance && $wnd.performance.now;
  }-*/;

  private double now() {
    if (GWT.isScript()) {
      return now0();
    } else {
      return System.currentTimeMillis();
    }
  }

  private native double now0() /*-{
                               return $wnd.performance.now()
                               }-*/;

  void SplaySetup() {
    // Check if the platform has the performance.now high resolution timer.
    // If not, throw exception and quit.
    if (!hasPerformanceNow()) {
      throw new RuntimeException("PerformanceNowUnsupported");
    }

    splayTree = new SplayTree();
    splaySampleTimeStart = now();
    for (int i = 0; i < kSplayTreeSize; i++) {
      InsertNewNode();
      if ((i + 1) % 20 == 19) {
        SplayUpdateStats(now());
      }
    }
  }

  void SplayTearDown() {
    // Allow the garbage collector to reclaim the memory
    // used by the splay tree no matter how we exit the
    // tear down function.
    JavaScriptArrayNumber keys = splayTree.exportKeys();
    splayTree = null;

    splaySamples = 0;
    splaySumOfSquaredPauses = 0;

    // Verify that the splay tree has the right size.
    int length = keys.length();
    if (length != kSplayTreeSize) {
      throw new RuntimeException("Splay tree has wrong size");
    }

    // Verify that the splay tree has sorted, unique keys.
    for (int i = 0; i < length - 1; i++) {
      if (keys.get(i) >= keys.get(i + 1)) {
        throw new RuntimeException("Splay tree not sorted");
      }
    }
  }

  void SplayRun() {
    // Replace a few nodes in the splay tree.
    for (int i = 0; i < kSplayTreeModifications; i++) {
      double key = InsertNewNode();
      SplayTree.Node greatest = splayTree.findGreatestLessThan(key);
      if (greatest == null)
        splayTree.remove(key);
      else
        splayTree.remove(greatest.key);
    }
    SplayUpdateStats(now());
  }
}
