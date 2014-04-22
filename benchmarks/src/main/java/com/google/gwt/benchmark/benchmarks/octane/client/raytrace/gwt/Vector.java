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

public class Vector {
  private double x;
  private double y;
  private double z;

  public Vector(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void copy(Vector vector) {
    this.x = vector.x;
    this.y = vector.y;
    this.z = vector.z;
  }

  public Vector normalize() {
    double m = this.magnitude();
    return new Vector(this.x / m, this.y / m, this.z / m);
  }

  public double magnitude() {
    return Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
  }

  public Vector cross(Vector w) {
    return new Vector(-this.z * w.y + this.y * w.z, this.z * w.x - this.x * w.z,
        -this.y * w.x + this.x * w.y);
  }

  public double dot(Vector w) {
    return this.x * w.x + this.y * w.y + this.z * w.z;
  }

  public static Vector add(Vector v, Vector w) {
    return new Vector(w.x + v.x, w.y + v.y, w.z + v.z);
  }

  public static Vector subtract(Vector v, Vector w) {
    return new Vector(v.x - w.x, v.y - w.y, v.z - w.z);
  }

  public Vector multiplyVector(Vector v, Vector w) {
    return new Vector(v.x * w.x, v.y * w.y, v.z * w.z);
  }

  public static Vector multiplyScalar(Vector v, double w) {
    return new Vector(v.x * w, v.y * w, v.z * w);
  }

  @Override
  public String toString() {
    return "Vector [" + this.x + "," + this.y + "," + this.z + "]";
  }

  // getters are added for a real Java style benchmark
  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public void setY(double y) {
    this.y = y;
  }
}
