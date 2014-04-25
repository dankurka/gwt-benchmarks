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
package com.google.gwt.benchmark.benchmarks.octane.client.navierstokes.gwt;

public class Field {

  private double[] dens;
  private double[] u;
  private double[] v;
  private int rowSize;

  public Field(double[] dens, double[] u, double[] v, int rowSize) {
    this.dens = dens;
    this.u = u;
    this.v = v;
    this.rowSize = rowSize;
  }

  public void setDensity(int x, int y, double d) {
    dens[(x + 1) + (y + 1) * rowSize] = d;
  }

  public double getDensity(int x, int y) {
    return dens[(x + 1) + (y + 1) * rowSize];
  }

  public void setVelocity(int x, int y, double xv, double yv) {
    u[(x + 1) + (y + 1) * rowSize] = xv;
    v[(x + 1) + (y + 1) * rowSize] = yv;
  }

  public double getXVelocity(int x, int y) {
    return u[(x + 1) + (y + 1) * rowSize];
  }

  public double getYVelocity(int x, int y) {
    return v[(x + 1) + (y + 1) * rowSize];
  }
}
