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

import com.google.gwt.benchmark.benchmarks.octane.client.deltablue.gwt.helper.Abstraction;

/**
 * A constrained variable. In addition to its value, it maintain the
 * structure of the constraint graph, the current dataflow graph, and
 * various parameters of interest to the DeltaBlue incremental
 * constraint solver.
 **/
public class Variable {

  public int mark;
  public String name;
  public int value;
  public AbstractOrderedCollection<Constraint> constraints;
  public Constraint determinedBy;
  public Strength walkStrength;
  public boolean stay;

  public Variable(String name) {
    this(name, 0);
  }

  public Variable(String name, int intialValue) {
    this.value = intialValue;
    this.constraints = Abstraction.create();
    this.determinedBy = null;
    this.walkStrength = Strength.WEAKEST;
    this.stay = true;
    this.name = name;
  }

  /**
   * Add the given constraint to the set of all constraints that refer this variable.
   */
  public void addConstraint(Constraint c) {
    this.constraints.add(c);
  }

  /**
   * Removes all traces of c from this variable.
   */
  public void removeConstraint(Constraint c) {
    this.constraints.remove(c);
    if (this.determinedBy == null && c == null
        || (this.determinedBy != null && this.determinedBy.equals(c))) {
      c = null;
    }
  }
}
