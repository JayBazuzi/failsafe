/*
 * Copyright 2021 the original author or authors.
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
package net.jodah.failsafe.functional;

import net.jodah.failsafe.*;
import net.jodah.failsafe.function.CheckedRunnable;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Tests various policy composition scenarios.
 */
@Test
public class PolicyCompositionTest {
  static class FooException extends Exception {
  }

  public void testFailsafeWithCircuitBreakerThenRetryThenFallback() {
    RetryPolicy<Object> rp = new RetryPolicy<>().withMaxRetries(2);
    CircuitBreaker<Object> cb = new CircuitBreaker<>().withFailureThreshold(5);
    Fallback<Object> fb = Fallback.of("test");

    Object result = Testing.ignoreExceptions(
      () -> Failsafe.with(fb, rp, cb).onComplete(e -> assertEquals(e.getAttemptCount(), 3)).get(() -> {
        throw new FooException();
      }));

    assertEquals(result, "test");
    assertTrue(cb.isClosed());
  }

  public void testFailsafeWithRetryThenCircuitBreaker() {
    RetryPolicy<Object> rp = new RetryPolicy<>().withMaxRetries(2);
    CircuitBreaker<Object> cb = new CircuitBreaker<>().withFailureThreshold(5);

    Testing.ignoreExceptions(
      () -> Failsafe.with(cb, rp).onComplete(e -> assertEquals(e.getAttemptCount(), 3)).run(() -> {
        throw new Exception();
      }));

    assertTrue(cb.isClosed());
  }

  public void testExecutionWithCircuitBreakerThenRetry() {
    RetryPolicy<Object> rp = new RetryPolicy<>().withMaxRetries(2);
    CircuitBreaker<Object> cb = new CircuitBreaker<>().withFailureThreshold(5);

    Execution execution = new Execution(rp, cb);
    execution.recordFailure(new Exception());
    execution.recordFailure(new Exception());
    assertFalse(execution.isComplete());
    execution.recordFailure(new Exception());
    assertTrue(execution.isComplete());

    assertTrue(cb.isClosed());
  }

  public void testExecutionWithRetryThenCircuitBreaker() {
    RetryPolicy rp = new RetryPolicy().withMaxRetries(1);
    CircuitBreaker cb = new CircuitBreaker().withFailureThreshold(5);

    Execution execution = new Execution(cb, rp);
    execution.recordFailure(new Exception());
    assertFalse(execution.isComplete());
    execution.recordFailure(new Exception());
    assertTrue(execution.isComplete());
    assertTrue(cb.isClosed());
  }

  public void testFailsafeWithRetryThenFallback() {
    RetryPolicy<Object> rp = new RetryPolicy<>().withMaxRetries(2);
    Fallback<Object> fb = Fallback.of("test");
    AtomicInteger executions = new AtomicInteger();

    assertEquals(Failsafe.with(fb, rp).onComplete(e -> executions.set(e.getAttemptCount())).get(() -> {
      throw new IllegalStateException();
    }), "test");
    assertEquals(executions.get(), 3);
  }

  public void testFailsafeWithFallbackThenRetry() {
    RetryPolicy<Object> rp = new RetryPolicy<>().withMaxRetries(2);
    Fallback<Object> fb = Fallback.of("test");
    AtomicInteger executions = new AtomicInteger();

    assertEquals(Failsafe.with(rp, fb).onComplete(e -> executions.set(e.getAttemptCount())).get(() -> {
      throw new IllegalStateException();
    }), "test");
    assertEquals(executions.get(), 1);
  }

  /**
   * Tests that multiple circuit breakers handle failures as expected, regardless of order.
   */
  public void testDuplicateCircuitBreakers() {
    CircuitBreaker cb1 = new CircuitBreaker<>().handle(IllegalArgumentException.class);
    CircuitBreaker cb2 = new CircuitBreaker<>().handle(IllegalStateException.class);

    CheckedRunnable runnable = () -> {
      throw new IllegalArgumentException();
    };
    Testing.ignoreExceptions(() -> Failsafe.with(cb2, cb1).run(runnable));
    assertTrue(cb1.isOpen());
    assertTrue(cb2.isClosed());

    cb1.close();
    Testing.ignoreExceptions(() -> Failsafe.with(cb1, cb2).run(runnable));
    assertTrue(cb1.isOpen());
    assertTrue(cb2.isClosed());
  }
}
