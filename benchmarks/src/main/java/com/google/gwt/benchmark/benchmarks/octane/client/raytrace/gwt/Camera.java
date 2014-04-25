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

public class Camera {
  private Vector position;
  private Vector lookAt;
  private Vector up;
  private Vector equator;
  private Vector screen;

  public Camera(Vector pos, Vector lookAt, Vector up) {
    this.position = pos;
    this.lookAt = lookAt;
    this.up = up;
    this.equator = lookAt.normalize().cross(this.up);
    this.screen = Vector.add(this.position, this.lookAt);
  }

  public Ray getRay(double vx, double vy) {
    Vector pos = Vector.subtract(this.screen, Vector.subtract(
        Vector.multiplyScalar(this.equator, vx), Vector.multiplyScalar(this.up, vy)));
    pos.setY(pos.getY() * -1);
    Vector dir = Vector.subtract(pos, this.position);

    Ray ray = new Ray(pos, dir.normalize());

    return ray;
  }

  @Override
  public String toString() {
    return "Ray []";
  }

  public Vector getPosition() {
    return this.position;
  }
}
