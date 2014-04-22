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

public class Plane extends Shape {
  private Vector position;
  private double d;
  private BaseMaterial material;

  public Plane(Vector pos, double d, BaseMaterial material) {
    this.position = pos;
    this.d = d;
    this.material = material;
  }

  @Override
  public IntersectionInfo intersect(Ray ray) {
    IntersectionInfo info = new IntersectionInfo();

    double Vd = this.position.dot(ray.getDirection());
    if (Vd == 0)
      return info; // no intersection

    double t = -(this.position.dot(ray.getPosition()) + this.d) / Vd;
    if (t <= 0)
      return info;

    info.setShape(this);
    info.isHit(true);
    info.setPosition(Vector.add(ray.getPosition(), Vector.multiplyScalar(ray.getDirection(), t)));
    info.setNormal(this.position);
    info.setDistance(t);

    if (this.material.hasTexture) {
      Vector vU = new Vector(this.position.getY(), this.position.getZ(), -this.position.getX());
      Vector vV = vU.cross(this.position);
      double u = info.getPosition().dot(vU);
      double v = info.getPosition().dot(vV);
      info.setColor(this.material.getColor(u, v));
    } else {
      info.setColor(this.material.getColor(0, 0));
    }

    return info;
  }

  @Override
  public String toString() {
    return "Plane [" + this.position + ", d=" + this.d + "]";
  }

  @Override
  public BaseMaterial getMaterial() {
    return material;
  }

  @Override
  public Vector getPosition() {
    return position;
  }
}
