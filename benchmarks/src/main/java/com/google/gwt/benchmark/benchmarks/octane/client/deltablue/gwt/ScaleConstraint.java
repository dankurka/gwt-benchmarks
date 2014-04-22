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
package com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt;

/**
 * Relates two variables by the linear scaling relationship: "v2 =
 * (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
 * this relationship but the scale factor and offset are considered
 * read-only.
 */
public class ScaleConstraint extends BinaryConstraint {

  private Variable scale;
  private Variable offset;

  public ScaleConstraint(Variable src, Variable scale, Variable offset, Variable dest, Strength strength) {
    super(src, dest, strength);
    this.direction = Direction.NONE;
    this.scale = scale;
    this.offset = offset;
    // this line needed to be added see comment in BinaryConstraint's constructor
    this.addConstraint();
  }

  /**
   * Adds this constraint to the constraint graph.
   */
  @Override
  public void addToGraph() {
    super.addToGraph();
    this.scale.addConstraint(this);
    this.offset.addConstraint(this);
  }

  @Override
  public void removeFromGraph() {
    super.removeFromGraph();
    if (this.scale != null) this.scale.removeConstraint(this);
    if (this.offset != null) this.offset.removeConstraint(this);
  }

  @Override
  public void markInputs(int mark) {
    super.markInputs(mark);
    this.scale.mark = this.offset.mark = mark;
  }

  /**
   * Enforce this constraint. Assume that it is satisfied.
   */
  @Override
  public void execute() {
    if (this.direction == Direction.FORWARD) {
      this.v2.value = this.v1.value * this.scale.value + this.offset.value;
    } else {
      this.v1.value = (this.v2.value - this.offset.value) / this.scale.value;
    }
  }

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is
   * 'stay', the value for the current output of this constraint. Assume
   * this constraint is satisfied.
   */
  @Override
  public void recalculate() {
    Variable ihn = this.input();
    Variable out = this.output();
    out.walkStrength = Strength.weakestOf(this.strength, ihn.walkStrength);
    out.stay = ihn.stay && this.scale.stay && this.offset.stay;
    if (out.stay) this.execute();
  }

}
