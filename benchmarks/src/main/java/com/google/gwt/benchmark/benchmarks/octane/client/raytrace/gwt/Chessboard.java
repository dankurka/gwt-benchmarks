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

public class Chessboard extends BaseMaterial {
  private double density;
  private Color colorOdd;
  private Color colorEven;

  public Chessboard(Color colorEven, Color colorOdd, double reflection, double transparency,
      double gloss, double density) {
    this.colorEven = colorEven;
    this.colorOdd = colorOdd;
    this.reflection = reflection;
    this.transparency = transparency;
    this.gloss = gloss;
    this.density = density;
    this.hasTexture = true;
  }

  @Override
  public Color getColor(double u, double v) {
    double t = this.wrapUp(u * this.density) * this.wrapUp(v * this.density);

    if (t < 0.0)
      return this.colorEven;
    else
      return this.colorOdd;
  }

  @Override
  public String toString() {
    return "ChessMaterial [gloss=" + this.gloss + ", transparency=" + this.transparency
        + ", hasTexture=" + this.hasTexture + "]";
  }
}
