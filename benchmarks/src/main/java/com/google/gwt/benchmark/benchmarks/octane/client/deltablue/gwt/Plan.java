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
 * A Plan is an ordered list of constraints to be executed in sequence to resatisfy all currently
 * satisfiable constraints in the face of one or more changing inputs.
 */
public class Plan {
  private AbstractOrderedCollection<Constraint> v = Abstraction.create();

  public void addConstraint(Constraint c) {
    this.v.add(c);
  }

  public int size() {
    return this.v.size();
  }

  public Constraint constraintAt(int index) {
    return this.v.at(index);
  }

  public void execute() {
    for (int i = 0; i < this.size(); i++) {
      Constraint c = this.constraintAt(i);
      c.execute();
    }
  }
}
