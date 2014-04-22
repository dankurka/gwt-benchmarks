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


public class Light {

  private Vector position;
  private Color color;
  @SuppressWarnings("unused")
  private double intensity;

  public Light(Vector pos, Color color) {
    this(pos, color, 10.0);
  }

  public Light(Vector pos, Color color, double intensity) {
    this.position = pos;
    this.color = color;
    this.intensity = intensity;
  }

  @Override
  public String toString() {
    return "Light [" + this.position.getX() + "," + this.position.getY() + "," + this.position.getZ() + "]";
  }

  public Vector getPosition() {
    return position;
  }

  public Color getColor() {
    return color;
  }
}
