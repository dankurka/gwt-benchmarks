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
 * Abstract superclass for constraints having a single possible output
 * variable.
 */
public abstract class  UnaryConstraint extends Constraint {

  private Variable myOutput;
  private boolean satisfied;

  public UnaryConstraint(Variable v, Strength strength) {
    super(strength);
    this.myOutput = v;
    this.satisfied = false;
    this.addConstraint();
  }

  /**
   * Adds this constraint to the constraint graph
   */
  @Override
  public void addToGraph() {
    this.myOutput.addConstraint(this);
    this.satisfied = false;
  }

  /**
   * Decides if this constraint can be satisfied and records that
   * decision.
   */
  @Override
  public void chooseMethod(int mark) {
    this.satisfied = (this.myOutput.mark != mark)
      && Strength.stronger(this.strength, this.myOutput.walkStrength);
  }

  /**
   * Returns true if this constraint is satisfied in the current solution.
   */
  @Override
  public boolean isSatisfied() {
    return this.satisfied;
  }

  @Override
  public void markInputs(int mark) {
    // has no inputs
  }

  /**
   * Returns the current output variable.
   */
  @Override
  public Variable output() {
    return this.myOutput;
  }

  /**
   * Calculate the walkabout strength, the stay flag, and, if it is
   * 'stay', the value for the current output of this constraint. Assume
   * this constraint is satisfied.
   */
  @Override
  public void recalculate() {
    this.myOutput.walkStrength = this.strength;
    this.myOutput.stay = !this.isInput();
    if (this.myOutput.stay) this.execute(); // Stay optimization
  }

  /**
   * Records that this constraint is unsatisfied
   */
  @Override
  public void markUnsatisfied() {
    this.satisfied = false;
  }

  @Override
  public boolean inputsKnown(int mark) {
    return true;
  }

  @Override
  public void removeFromGraph() {
    if (this.myOutput != null) this.myOutput.removeConstraint(this);
    this.satisfied = false;
  }
}
