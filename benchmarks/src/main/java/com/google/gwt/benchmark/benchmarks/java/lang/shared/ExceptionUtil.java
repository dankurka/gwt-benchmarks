package com.google.gwt.benchmark.benchmarks.java.lang.shared;

/**
 * Helper class for exception benchmarks.
 */
class ExceptionUtil {
  public static void throwRecursive(int count) throws Exception {
    if (count > 1) {
      throwRecursive(count - 1);
    } else {
      throw new Exception("message");
    }
  }
}
