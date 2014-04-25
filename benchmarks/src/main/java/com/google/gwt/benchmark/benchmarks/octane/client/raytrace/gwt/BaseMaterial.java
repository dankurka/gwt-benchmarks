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
package com.google.gwt.benchmark.benchmarks.octane.client.raytrace.gwt;

public abstract class BaseMaterial {
  protected double gloss = 2.0;
  protected double transparency = 0.0; // 0=opaque
  protected double reflection = 0.0; // [0...infinity] 0 = no reflection
  @SuppressWarnings("unused")
  private double refraction = 0.50;
  protected boolean hasTexture = false;

  public abstract Color getColor(double u, double v);

  public double wrapUp(double t) {
    t = t % 2.0;
    if (t < -1)
      t += 2.0;
    if (t >= 1)
      t -= 2.0;
    return t;
  }

  @Override
  public String toString() {
    return "Material [gloss=" + this.gloss + ", transparency=" + this.transparency + ", hasTexture="
        + this.hasTexture + "]";
  }
}
