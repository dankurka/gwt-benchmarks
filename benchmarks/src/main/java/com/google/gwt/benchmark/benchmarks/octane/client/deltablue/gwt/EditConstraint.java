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
 * A unary input constraint used to mark a variable that the client
 * wishes to change.
 */
public class EditConstraint extends UnaryConstraint {

  public EditConstraint(Variable v, Strength str) {
    super(v, str);
  }

  /**
   * Edits indicate that a variable is to be changed by imperative code.
   */
  @Override
  public boolean isInput() {
    return true;
  }

  @Override
  public void execute() {
    // Edit constraints do nothing
  }
}
