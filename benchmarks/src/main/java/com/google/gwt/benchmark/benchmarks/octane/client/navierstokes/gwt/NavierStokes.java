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

import com.google.gwt.benchmark.benchmarks.octane.client.navierstokes.gwt.FluidField.DisplayFunction;

public class NavierStokes implements FluidField.UICallback, DisplayFunction {
  private FluidField solver = null;
  int nsFrameCounter = 0;

  public void runNavierStokes() {
    solver.update();
    nsFrameCounter++;

    if (nsFrameCounter == 15) {
      checkResult(solver.getDens());
    }

  }

  public void checkResult(double[] dens) {

    int result = 0;
    for (int i = 7000; i < 7100; i++) {
      result += dens[i] * 10;
    }

    if (result != 77) {
      throw new RuntimeException("checksum failed " + result);
    }
  }

  public void setupNavierStokes() {
    framesTillAddingPoints = 0;
    framesBetweenAddingPoints = 5;

    solver = new FluidField();
    solver.setResolution(128, 128);
    solver.setIterations(20);
    solver.setUICallback(this);
    solver.setDisplayFunction(this);
    solver.reset();
  }

  public void tearDownNavierStokes() {
    solver = null;
  }

  public void addPoints(Field field) {
    int n = 64;
    for (int i = 1; i <= n; i++) {
      field.setVelocity(i, i, n, n);
      field.setDensity(i, i, 5);
      field.setVelocity(i, n - i, -n, -n);
      field.setDensity(i, n - i, 20);
      field.setVelocity(128 - i, n + i, -n, -n);
      field.setDensity(128 - i, n + i, 30);
    }
  }

  int framesTillAddingPoints = 0;
  int framesBetweenAddingPoints = 5;

  public void prepareFrame(Field field) {
    if (framesTillAddingPoints == 0) {
      addPoints(field);
      framesTillAddingPoints = framesBetweenAddingPoints;
      framesBetweenAddingPoints++;
    } else {
      framesTillAddingPoints--;
    }
  }

  public void call(Field f) {

  }
}
