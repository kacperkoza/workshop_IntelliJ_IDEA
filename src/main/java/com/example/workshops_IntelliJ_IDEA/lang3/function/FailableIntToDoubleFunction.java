/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.workshops_IntelliJ_IDEA.lang3.function;

import java.util.function.IntToDoubleFunction;

/**
 * A functional interface like {@link IntToDoubleFunction} that declares a {@code Throwable}.
 *
 * @param <E> Thrown exception.
 * @since 3.11
 */
@FunctionalInterface
public interface FailableIntToDoubleFunction<E extends Throwable> {

    /**
     * NOP singleton
     */
    @SuppressWarnings("rawtypes")
    FailableIntToDoubleFunction NOP = t -> 0d;

    /**
     * Returns The NOP singleton.
     *
     * @param <E> Thrown exception.
     * @return The NOP singleton.
     */
    static <E extends Throwable> FailableIntToDoubleFunction<E> nop() {
        return NOP;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     * @throws E Thrown when the function fails.
     */
    double applyAsDouble(int value) throws E;
}
