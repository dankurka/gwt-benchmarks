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

public class MaterialSolid extends BaseMaterial {

  private Color color;

  public MaterialSolid(Color color, double reflection,
      @SuppressWarnings("unused") double refraction, double transparency, double gloss) {
    this.color = color;
    this.reflection = reflection;
    this.transparency = transparency;
    this.gloss = gloss;
    this.hasTexture = false;
  }

  @Override
  public Color getColor(double u, double v) {
    return color;
  }

  @Override
  public String toString() {
    return "SolidMaterial [gloss=" + this.gloss + ", transparency=" + this.transparency
        + ", hasTexture=" + this.hasTexture + "]";
  }
}
