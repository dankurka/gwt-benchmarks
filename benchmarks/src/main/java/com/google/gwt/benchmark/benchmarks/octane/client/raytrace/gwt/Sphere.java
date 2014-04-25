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

public class Sphere extends Shape {

  private double radius;
  private Vector position;
  private BaseMaterial material;

  public Sphere(Vector pos, double radius, BaseMaterial material) {
    this.radius = radius;
    this.position = pos;
    this.material = material;
  }

  @Override
  public IntersectionInfo intersect(Ray ray) {
    IntersectionInfo info = new IntersectionInfo();
    info.setShape(this);

    Vector dst = Vector.subtract(ray.getPosition(), this.position);

    double B = dst.dot(ray.getDirection());
    double C = dst.dot(dst) - (this.radius * this.radius);
    double D = (B * B) - C;

    if (D > 0) { // intersection!
      info.isHit(true);
      info.setDistance((-B) - Math.sqrt(D));
      info.setPosition(Vector.add(ray.getPosition(),
          Vector.multiplyScalar(ray.getDirection(), info.getDistance())));

      info.setNormal(Vector.subtract(info.getPosition(), this.position).normalize());

      info.setColor(this.material.getColor(0, 0));
    } else {
      info.isHit(false);
    }
    return info;
  }

  @Override
  public BaseMaterial getMaterial() {
    return material;
  }

  @Override
  public Vector getPosition() {
    return this.position;
  }
}
