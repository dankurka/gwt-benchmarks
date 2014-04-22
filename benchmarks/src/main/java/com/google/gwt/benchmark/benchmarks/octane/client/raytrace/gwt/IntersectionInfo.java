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

public class IntersectionInfo {

  private boolean isHit;
  @SuppressWarnings("unused")
  private int hitCount;
  private Shape shape;
  private Vector position;
  private Vector normal;
  private Color color;
  private double distance;

  public IntersectionInfo() {
    this.color = new Color(0, 0, 0);
  }

  public void isHit(boolean b) {
    this.isHit = b;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public void setShape(Shape shape) {
    this.shape = shape;
  }

  public double getDistance() {
    return this.distance;
  }

  public void setPosition(Vector position) {
    this.position = position;
  }

  public Vector getPosition() {
    return this.position;
  }

  public void setNormal(Vector normal) {
    this.normal = normal;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public String toString() {
    return "Intersection [" + this.position + "]";
  }

  public boolean isHit() {
    return isHit;
  }

  public void setHitCount(int hits) {
    this.hitCount = hits;
  }

  public Color getColor() {
    return color;
  }

  public Shape getShape() {
    return shape;
  }

  public Vector getNormal() {
    return normal;
  }
}
