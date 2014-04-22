/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.benchmark.benchmarks.octane.client.raytrace.gwt;

public class Engine {

  public static class Options {
    public int canvasHeight = 100;
    public int canvasWidth = 100;
    public int pixelWidth = 2;
    public int pixelHeight = 2;
    public boolean renderDiffuse = false;
    public boolean renderShadows = false;
    public boolean renderHighlights = false;
    public boolean renderReflections = false;
    public int rayDepth = 2;
  }

  private Options options;
  private Object canvas;

  public Engine(Options options) {
    this.options = options;
    this.options.canvasHeight /= this.options.pixelHeight;
    this.options.canvasWidth /= this.options.pixelWidth;
  }

  @SuppressWarnings("unused")
  public void setPixel(double x, double y, Color color) {
    double pxW, pxH;
    pxW = this.options.pixelWidth;
    pxH = this.options.pixelHeight;

    if (this.canvas != null) {
      // this.canvas.fillStyle = color.toString();
      // this.canvas.fillRect (x * pxW, y * pxH, pxW, pxH);
    } else {
      if (x == y) {
        Global.checkNumber += color.brightness();
      }
      // print(x * pxW, y * pxH, pxW, pxH);
    }
  }

  public void renderScene(Scene scene, Object canvas) {
    Global.checkNumber = 0;
    /* Get canvas */
    if (canvas != null) {
      // this.canvas = canvas.getContext("2d");
    } else {
      this.canvas = null;
    }

    double canvasHeight = this.options.canvasHeight;
    double canvasWidth = this.options.canvasWidth;

    for (int y = 0; y < canvasHeight; y++) {
      for (int x = 0; x < canvasWidth; x++) {
        double yp = y * 1.0 / canvasHeight * 2 - 1;
        double xp = x * 1.0 / canvasWidth * 2 - 1;

        Ray ray = scene.getCamera().getRay(xp, yp);

        Color color = this.getPixelColor(ray, scene);

        this.setPixel(x, y, color);
      }
    }
    if (Global.checkNumber != 2321) {
      throw new RuntimeException("Scene rendered incorrectly");
    }
  }

  public Color getPixelColor(Ray ray, Scene scene) {
    IntersectionInfo info = this.testIntersection(ray, scene, null);
    if (info.isHit()) {
      Color color = this.rayTrace(info, ray, scene, 0);
      return color;
    }
    return scene.getBackground().getColor();
  }

  public IntersectionInfo testIntersection(Ray ray, Scene scene, Shape exclude) {
    int hits = 0;
    IntersectionInfo best = new IntersectionInfo();
    best.setDistance(2000);

    for (int i = 0; i < scene.getShapes().length(); i++) {
      Shape shape = scene.getShapes().get(i);

      if (shape != exclude) {
        IntersectionInfo info = shape.intersect(ray);
        if (info.isHit() && info.getDistance() >= 0 && info.getDistance() < best.getDistance()) {
          best = info;
          hits++;
        }
      }
    }
    best.setHitCount(hits);
    return best;
  }

  public Ray getReflectionRay(Vector P, Vector N, Vector V) {
    double c1 = -N.dot(V);
    Vector R1 = Vector.add(Vector.multiplyScalar(N, 2 * c1), V);
    return new Ray(P, R1);
  }

  public Color rayTrace(IntersectionInfo info, Ray ray, Scene scene, double depth) {
    // Calc ambient
    Color color = Color.multiplyScalar(info.getColor(), scene.getBackground().getAmbience());
    @SuppressWarnings("unused")
    Color oldColor = color;
    double shininess = Math.pow(10, info.getShape().getMaterial().gloss + 1);

    for (int i = 0; i < scene.getLights().length(); i++) {
      Light light = scene.getLights().get(i);

      // Calc diffuse lighting
      Vector v = Vector.subtract(light.getPosition(), info.getPosition()).normalize();

      if (this.options.renderDiffuse) {
        double L = v.dot(info.getNormal());
        if (L > 0.0) {
          color = Color.add(color,
              Color.multiply(info.getColor(), Color.multiplyScalar(light.getColor(), L)));
        }
      }

      // The greater the depth the more accurate the colours, but
      // this is exponentially (!) expensive
      if (depth <= this.options.rayDepth) {
        // calculate reflection ray
        if (this.options.renderReflections && info.getShape().getMaterial().reflection > 0) {
          Ray reflectionRay =
              this.getReflectionRay(info.getPosition(), info.getNormal(), ray.getDirection());
          IntersectionInfo refl = this.testIntersection(reflectionRay, scene, info.getShape());

          if (refl.isHit() && refl.getDistance() > 0) {
            refl.setColor(this.rayTrace(refl, reflectionRay, scene, depth + 1));
          } else {
            refl.setColor(scene.getBackground().getColor());
          }

          color = Color.blend(color, refl.getColor(), info.getShape().getMaterial().reflection);
        }
      }

      /* Render shadows and highlights */

      IntersectionInfo shadowInfo = new IntersectionInfo();

      if (this.options.renderShadows) {
        Ray shadowRay = new Ray(info.getPosition(), v);

        shadowInfo = this.testIntersection(shadowRay, scene, info.getShape());
        if (shadowInfo.isHit() && shadowInfo.getShape() != info.getShape() ) {
          Color vA = Color.multiplyScalar(color, 0.5);
          double dB = (0.5 * Math.pow(shadowInfo.getShape().getMaterial().transparency, 0.5));
          color = Color.addScalar(vA, dB);
        }
      }

      // Phong specular highlights
      if (this.options.renderHighlights && !shadowInfo.isHit()
          && info.getShape().getMaterial().gloss > 0) {
        Vector Lv = Vector.subtract(info.getShape().getPosition(), light.getPosition()).normalize();

        Vector E = Vector.subtract(scene.getCamera().getPosition(), info.getShape().getPosition())
            .normalize();

        Vector H = Vector.subtract(E, Lv).normalize();

        double glossWeight = Math.pow(Math.max(info.getNormal().dot(H), 0), shininess);
        color = Color.add(Color.multiplyScalar(light.getColor(), glossWeight), color);
      }
    }
    color.limit();
    return color;
  }
}
