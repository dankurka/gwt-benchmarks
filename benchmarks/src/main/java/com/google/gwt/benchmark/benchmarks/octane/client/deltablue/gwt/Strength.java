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
 * Strengths are used to measure the relative importance of constraints.
 * New strengths may be inserted in the strength hierarchy without
 * disrupting current constraints.  Strengths cannot be created outside
 * this class, so pointer comparison can be used for value comparison.
 */
public class Strength {

  public static final Strength REQUIRED = new Strength(0, "required");
  public static final Strength STONG_PREFERRED = new Strength(1, "strongPreferred");
  public static final Strength PREFERRED = new Strength(2, "preferred");
  public static final Strength STRONG_DEFAULT = new Strength(3, "strongDefault");
  public static final Strength NORMAL = new Strength(4, "normal");
  public static final Strength WEAK_DEFAULT = new Strength(5, "weakDefault");
  public static final Strength WEAKEST = new Strength(6, "weakest");

  public static boolean stronger(Strength s1, Strength s2) {
    return s1.strengthValue < s2.strengthValue;
  }

  public static boolean weaker(Strength s1, Strength s2) {
    return s1.strengthValue > s2.strengthValue;
  }

  public static Strength weakestOf(Strength s1, Strength s2) {
    return weaker(s1, s2) ? s1 : s2;
  }

  public static Strength strongest(Strength s1, Strength s2) {
    return stronger(s1, s2) ? s1 : s2;
  }

  private int strengthValue;
  private String name;

  private Strength(int strengthValue, String name) {
    this.strengthValue = strengthValue;
    this.name = name;
  }

  public Strength nextWeaker() {
    switch (this.strengthValue) {
      case 0:
        return Strength.WEAKEST;
      case 1:
        return Strength.WEAK_DEFAULT;
      case 2:
        return Strength.NORMAL;
      case 3:
        return Strength.STRONG_DEFAULT;
      case 4:
        return Strength.PREFERRED;
      case 5:
        return Strength.REQUIRED;
    }
    throw new RuntimeException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + strengthValue;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    Strength other = (Strength) obj;
    if (strengthValue != other.strengthValue)
      return false;
    return true;
  }
}
