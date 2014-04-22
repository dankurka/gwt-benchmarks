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
 * An abstract class representing a system-maintainable relationship
 * (or "constraint") between a set of variables. A constraint supplies
 * a strength instance variable; concrete subclasses provide a means
 * of storing the constrained variables and other information required
 * to represent a constraint.
 */
public abstract class Constraint {
  protected Strength strength;

  public Constraint(Strength strength) {
    this.strength = strength;
  }

  /**
   * Activate this constraint and attempt to satisfy it.
   */
  public void addConstraint() {
    this.addToGraph();
    DeltaBlueBenchmark.planner.incrementalAdd(this);
  }

  protected abstract void addToGraph();

  /**
   * Attempt to find a way to enforce this constraint. If successful,
   * record the solution, perhaps modifying the current dataflow
   * graph. Answer the constraint that this constraint overrides, if
   * there is one, or nil, if there isn't.
   * Assume: I am not already satisfied.
   */
  public Constraint satisfy(int mark) {
    this.chooseMethod(mark);
    if (!this.isSatisfied()) {
      if (this.strength == Strength.REQUIRED) {
        // removed console and added RuntimeException
        // Console.log("Could not satisfy a required constraint!");
        throw new RuntimeException("Could not satisfy a required constraint!");
      }
      return null;
    }
    this.markInputs(mark);
    Variable out = this.output();
    Constraint overridden = out.determinedBy;
    if (overridden != null) overridden.markUnsatisfied();
    out.determinedBy = this;
    if (!DeltaBlueBenchmark.planner.addPropagate(this, mark)) {
      // removed console and added RuntimeException
      // Console.log("Cycle encountered");
      throw new RuntimeException("Cycle encountered");
    }

    out.mark = mark;
    return overridden;
  }

  public void destroyConstraint() {
    if (this.isSatisfied()) {
      DeltaBlueBenchmark.planner.incrementalRemove(this);
    } else {
      this.removeFromGraph();
    }
  }

  /**
   * Normal constraints are not input constraints.  An input constraint
   * is one that depends on external state, such as the mouse, the
   * keybord, a clock, or some arbitraty piece of imperative code.
   */
  public boolean isInput() {
    return false;
  }

  protected abstract void removeFromGraph();

  protected abstract void markInputs(int mark);

  protected abstract boolean isSatisfied();

  protected  abstract void chooseMethod(int mark);

  public abstract Variable output();

  public abstract void markUnsatisfied();

  public abstract void execute();

  public abstract void recalculate();

  public abstract boolean inputsKnown(int mark);
}
