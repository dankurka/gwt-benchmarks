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

import com.google.gwt.benchmark.collection.shared.CollectionFactory;
import com.google.gwt.benchmark.collection.shared.JavaScriptArray;


public class Scene {

  private Camera camera;
  private JavaScriptArray<Shape> shapes;
  private JavaScriptArray<Light> lights;
  private Background background;

  public Scene() {
    this.camera = new Camera(
        new Vector(0, 0, -5),
        new Vector(0, 0, 1),
        new Vector(0, 1, 0));
    this.shapes = CollectionFactory.create();
    this.lights = CollectionFactory.create();
    this.background = new Background(new Color(0,0,0.5), 0.2);
  }

  public Camera getCamera() {
    return camera;
  }

  public Background getBackground() {
    return background;
  }

  public JavaScriptArray<Shape> getShapes() {
    return shapes;
  }

  public JavaScriptArray<Light> getLights() {
    return lights;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void setBackground(Background background) {
    this.background = background;
  }
}
