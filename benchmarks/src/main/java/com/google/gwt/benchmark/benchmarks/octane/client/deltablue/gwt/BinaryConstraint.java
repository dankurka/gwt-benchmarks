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
 * Abstract superclass for constraints having two possible output
 * variables.
 */
public abstract class BinaryConstraint extends Constraint {

  protected Variable v1;
  protected Variable v2;
  protected int direction;

  public BinaryConstraint(Variable var1, Variable var2, Strength strength) {
    super(strength);
    this.v1 = var1;
    this.v2 = var2;
    this.direction = Direction.NONE;
    // for the java version we have to move the call to addConstraint()
    // to the child classes since we need to call the parent constructor
    // first and this would cause the benchmark to fail
    // this.addConstraint();
  }

  /**
   * Decides if this constraint can be satisfied and which way it should flow based on the relative
   * strength of the variables related, and record that decision.
   */
  @Override
  public void chooseMethod(int mark) {
    if (this.v1.mark == mark) {
      this.direction = (this.v2.mark != mark
          && Strength.stronger(this.strength, this.v2.walkStrength)) ? Direction.FORWARD
          : Direction.NONE;
    }
    if (this.v2.mark == mark) {
      this.direction = (this.v1.mark != mark
          && Strength.stronger(this.strength, this.v1.walkStrength)) ? Direction.BACKWARD
          : Direction.NONE;
    }
    if (Strength.weaker(this.v1.walkStrength, this.v2.walkStrength)) {
      this.direction = Strength.stronger(this.strength, this.v1.walkStrength) ? Direction.BACKWARD
          : Direction.NONE;
    } else {
      this.direction = Strength.stronger(this.strength, this.v2.walkStrength) ? Direction.FORWARD
          : Direction.BACKWARD;
    }
  }

  /**
   * Add this constraint to the constraint graph
   */
  @Override
  public void addToGraph() {
    this.v1.addConstraint(this);
    this.v2.addConstraint(this);
    this.direction = Direction.NONE;
  }

  /**
   * Answer true if this constraint is satisfied in the current solution.
   */
  @Override
  public boolean isSatisfied() {
    return this.direction != Direction.NONE;
  }

  /**
   * Mark the input variable with the given mark.
   */
  @Override
  public void markInputs(int mark) {
    this.input().mark = mark;
  }

  /**
   * Returns the current input variable
   */
  public Variable input() {
    return (this.direction == Direction.FORWARD) ? this.v1 : this.v2;
  }

  /**
   * Returns the current output variable
   */

  @Override
  public Variable output() {
    return (this.direction == Direction.FORWARD) ? this.v2 : this.v1;
  }

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is 'stay', the value for the
   * current output of this constraint. Assume this constraint is satisfied.
   */
  @Override
  public void recalculate() {
    Variable ihn = this.input();
    Variable out = this.output();
    out.walkStrength = Strength.weakestOf(this.strength, ihn.walkStrength);
    out.stay = ihn.stay;
    if (out.stay)
      this.execute();
  }

  /**
   * Record the fact that this constraint is unsatisfied.
   */
  @Override
  public void markUnsatisfied() {
    this.direction = Direction.NONE;
  }

  @Override
  public boolean inputsKnown(int mark) {
    Variable i = this.input();
    return i.mark == mark || i.stay || i.determinedBy == null;
  }

  @Override
  public void removeFromGraph() {
    if (this.v1 != null)
      this.v1.removeConstraint(this);
    if (this.v2 != null)
      this.v2.removeConstraint(this);
    this.direction = Direction.NONE;
  }
}
