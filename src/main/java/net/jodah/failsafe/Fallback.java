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

import net.jodah.failsafe.function.*;
import net.jodah.failsafe.internal.executor.FallbackExecutor;
import net.jodah.failsafe.internal.util.Assert;

import java.util.concurrent.Callable;

/**
 * A FailsafePolicy that handles failures using a fallback.
 *
 * @author Jonathan Halterman
 */
public class Fallback implements FailsafePolicy {
  private final CheckedBiFunction<Object, Throwable, Object> fallback;

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("unchecked")
  private Fallback(CheckedBiFunction<Object, Throwable, Object> fallback) {
    this.fallback = Assert.notNull(fallback, "fallback");
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("unchecked")
  public static <R> Fallback of(CheckedBiFunction<? extends R, ? extends Throwable, ? extends R> fallback) {
    return new Fallback((CheckedBiFunction<Object, Throwable, Object>) fallback);
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static <R> Fallback of(Callable<? extends R> fallback) {
    return new Fallback((CheckedBiFunction) Functions.fnOf((Callable<R>) Assert.notNull(fallback, "fallback")));
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static <R> Fallback of(CheckedBiConsumer<? extends R, ? extends Throwable> fallback) {
    return new Fallback(
        (CheckedBiFunction) Functions.fnOf((CheckedBiConsumer<R, Throwable>) Assert.notNull(fallback, "fallback")));
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static Fallback of(CheckedConsumer<? extends Throwable> fallback) {
    return new Fallback(
        (CheckedBiFunction) Functions.fnOf((CheckedConsumer<Throwable>) Assert.notNull(fallback, "fallback")));
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static <R> Fallback of(CheckedFunction<? extends Throwable, ? extends R> fallback) {
    return new Fallback(
        (CheckedBiFunction) Functions.fnOf((CheckedFunction<Throwable, R>) Assert.notNull(fallback, "fallback")));
  }

  /**
   * Configures the {@code fallback} action to be executed if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static Fallback of(CheckedRunnable fallback) {
    return new Fallback((CheckedBiFunction) Functions.fnOf(Assert.notNull(fallback, "fallback")));
  }

  /**
   * Configures the {@code fallback} result to be returned if execution fails.
   *
   * @throws NullPointerException if {@code fallback} is null
   */
  @SuppressWarnings("rawtypes")
  public static Fallback of(Object fallback) {
    return new Fallback((CheckedBiFunction) Functions.fnOf(Assert.notNull(fallback, "fallback")));
  }

  @Override
  public PolicyExecutor toExecutor() {
    return new FallbackExecutor(fallback);
  }
}
