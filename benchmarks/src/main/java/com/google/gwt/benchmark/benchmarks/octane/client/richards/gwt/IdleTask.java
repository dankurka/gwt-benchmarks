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
package com.google.gwt.benchmark.benchmarks.octane.client.richards.gwt;

public class IdleTask implements Task {

  private Scheduler scheduler;
  private int v1;
  private int count;
  /**
   * An idle task doesn't do any work itself but cycles control between the two
   * device tasks.
   * @param {Scheduler} scheduler the scheduler that manages this task
   * @param {int} v1 a seed value that controls how the device tasks are scheduled
   * @param {int} count the number of times this task should be scheduled
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public IdleTask(Scheduler scheduler, int v1, int count) {
    this.scheduler = scheduler;
    this.v1 = v1;
    this.count = count;
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    this.count--;
    if (this.count == 0) return this.scheduler.holdCurrent();
    if ((this.v1 & 1) == 0) {
      this.v1 = this.v1 >> 1;
      return this.scheduler.release(Scheduler.ID_DEVICE_A);
    } else {
      this.v1 = (this.v1 >> 1) ^ 0xD008;
      return this.scheduler.release(Scheduler.ID_DEVICE_B);
    }
  }

  @Override
  public String toString() {
    return "IdleTask";
  }
}
