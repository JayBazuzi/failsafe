/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.jodah.failsafe;

import net.jodah.failsafe.PolicyExecutor.PolicyResult;
import net.jodah.failsafe.internal.util.Assert;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Tracks executions and determines when an execution can be performed for a {@link RetryPolicy}.
 *
 * @author Jonathan Halterman
 */
public class Execution extends AbstractExecution {
  /**
   * Creates a new {@link Execution} that will use the {@code policies} to handle failures. Policies are applied in
   * reverse order, with the last policy being applied first.
   *
   * @throws NullPointerException if {@code policies} is null
   * @throws IllegalArgumentException if {@code policies} is empty
   */
  public Execution(FailsafePolicy... policies) {
    super(new FailsafeConfig<Object, FailsafeConfig<Object, ?>>(Arrays.asList(Assert.notNull(policies, "policies"))));
  }

  Execution(Callable<Object> callable, FailsafeConfig<Object, ?> config) {
    super(callable, config);
  }

  /**
   * Records an execution and returns true if a retry can be performed for the {@code result}, else returns false and
   * marks the execution as complete.
   *
   * @throws IllegalStateException if the execution is already complete
   */
  public boolean canRetryFor(Object result) {
    return !complete(result, null, false);
  }

  /**
   * Records an execution and returns true if a retry can be performed for the {@code result} or {@code failure}, else
   * returns false and marks the execution as complete.
   *
   * @throws IllegalStateException if the execution is already complete
   */
  public boolean canRetryFor(Object result, Throwable failure) {
    return !complete(result, failure, false);
  }

  /**
   * Records an execution and returns true if a retry can be performed for the {@code failure}, else returns false and
   * marks the execution as complete.
   *
   * @throws NullPointerException if {@code failure} is null
   * @throws IllegalStateException if the execution is already complete
   */
  public boolean canRetryOn(Throwable failure) {
    Assert.notNull(failure, "failure");
    return !complete(null, failure, false);
  }

  /**
   * Records and completes the execution.
   *
   * @throws IllegalStateException if the execution is already complete
   */
  public void complete() {
    complete(null, null, true);
  }

  /**
   * Records and attempts to complete the execution with the {@code result}. Returns true on success, else false if
   * completion failed and execution should be retried.
   *
   * @throws IllegalStateException if the execution is already complete
   */
  public boolean complete(Object result) {
    return complete(result, null, false);
  }

  private boolean complete(Object result, Throwable failure, boolean noResult) {
    return postExecute(new PolicyResult(result, failure, noResult));
  }

  /**
   * Records a failed execution and returns true if a retry can be performed for the {@code failure}, else returns false
   * and completes the execution.
   *
   * <p>
   * Alias of {@link #canRetryOn(Throwable)}
   *
   * @throws NullPointerException if {@code failure} is null
   * @throws IllegalStateException if the execution is already complete
   */
  public boolean recordFailure(Throwable failure) {
    return canRetryOn(failure);
  }
}
