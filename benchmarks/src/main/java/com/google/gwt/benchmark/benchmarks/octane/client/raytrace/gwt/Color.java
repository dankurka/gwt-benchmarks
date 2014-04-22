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

public class Color {

  private double red;
  private double green;
  private double blue;

  public Color(double r, double g, double b) {
    this.red = r;
    this.green = g;
    this.blue = b;
  }

  public static Color add(Color c1, Color c2) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red + c2.red;
    result.green = c1.green + c2.green;
    result.blue = c1.blue + c2.blue;

    return result;
  }

  public static Color addScalar(Color c1, double s) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red + s;
    result.green = c1.green + s;
    result.blue = c1.blue + s;

    result.limit();

    return result;
  }

  public Color subtract(Color c1, Color c2) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red - c2.red;
    result.green = c1.green - c2.green;
    result.blue = c1.blue - c2.blue;

    return result;
  }

  public static Color multiply(Color c1, Color c2) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red * c2.red;
    result.green = c1.green * c2.green;
    result.blue = c1.blue * c2.blue;

    return result;
  }

  public static Color multiplyScalar(Color c1, double f) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red * f;
    result.green = c1.green * f;
    result.blue = c1.blue * f;

    return result;
  }

  public Color divideFactor(Color c1, double f) {
    Color result = new Color(0, 0, 0);

    result.red = c1.red / f;
    result.green = c1.green / f;
    result.blue = c1.blue / f;

    return result;
  }

  public void limit() {
    this.red = (this.red > 0.0) ? ((this.red > 1.0) ? 1.0 : this.red) : 0.0;
    this.green = (this.green > 0.0) ? ((this.green > 1.0) ? 1.0 : this.green) : 0.0;
    this.blue = (this.blue > 0.0) ? ((this.blue > 1.0) ? 1.0 : this.blue) : 0.0;
  }

  public double distance(Color color) {
    double d = Math.abs(this.red - color.red) + Math.abs(this.green - color.green)
        + Math.abs(this.blue - color.blue);
    return d;
  }

  public static Color blend(Color c1, Color c2, double w) {
    Color result = new Color(0, 0, 0);
    result = add(multiplyScalar(c1, 1 - w), multiplyScalar(c2, w));
    return result;
  }

  public double brightness() {
    int r = (int) Math.floor(this.red * 255);
    int g = (int) Math.floor(this.green * 255);
    int b = (int) Math.floor(this.blue * 255);
    return (r * 77 + g * 150 + b * 29) >> 8;
  }

  @Override
  public String toString() {
    double r = Math.floor(this.red * 255);
    double g = Math.floor(this.green * 255);
    double b = Math.floor(this.blue * 255);

    return "rgb(" + r + "," + g + "," + b + ")";
  }
}
