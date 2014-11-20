/*
 * Copyright (C) 2007 The Guava Authors
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
 * limitations under the License.
 */

package com.techshroom.mods.common.java8.function;

import static com.google.common.base.Preconditions.*;
import static com.techshroom.mods.common.ExtendedPreconditions.checkMultiNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.techshroom.mods.common.java8.supplier.IntSupplier;

/**
 * Static utility methods pertaining to integer operators.
 *
 * <p>
 * All methods return serializable functions as long as they're given
 * serializable parameters.
 * 
 * <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">the use
 * of {@code Function}</a> for tips on how to similarly use {@code IntOperators}.
 *
 * @author Kenzie Togami
 */
@GwtCompatible
public final class IntOperators {
    private IntOperators() {
    }

    /**
     * Returns the identity function.
     * 
     * @return f(x) = x
     */
    public static IntUnaryOperator identity() {
        return IdentityOperator.INSTANCE;
    }

    // enum singleton pattern
    private enum IdentityOperator implements IntUnaryOperator {
        INSTANCE;

        @Override
        public int applyAsInt(int input) {
            return input;
        }

        @Override
        public String toString() {
            return "identity";
        }
    }

    /**
     * Returns a function which performs a map lookup. The returned function
     * throws an {@link IllegalArgumentException} if given a key that does not
     * exist in the map. See also {@link #forMap(Map, int)}, which returns a
     * default value in this case.
     *
     * <p>
     * Note: if {@code map} is a {@link com.google.common.collect.BiMap BiMap}
     * (or can be one), you can use
     * {@link com.google.common.collect.Maps#asConverter Maps.asConverter}
     * instead to get a function that also supports reverse conversion.
     * 
     * @param map
     *            - map to use
     * @return f(x) = map.get(x)
     */
    public static <K> ToIntFunction<K> forMap(Map<K, Integer> map) {
        return new ToIntFunctionForMapNoDefault<K>(map);
    }

    private static class ToIntFunctionForMapNoDefault<K> implements
            ToIntFunction<K>, Serializable {
        final Map<K, Integer> map;

        ToIntFunctionForMapNoDefault(Map<K, Integer> map) {
            this.map = checkNotNull(map);
        }

        @Override
        public int applyAsInt(K key) {
            checkArgument(map.containsKey(key), "Key '%s' not present in map",
                          key);
            Integer result = map.get(key);
            checkNotNull(result, "Value cannot be null for key '%s'", key);
            return result.intValue();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ToIntFunctionForMapNoDefault) {
                ToIntFunctionForMapNoDefault<?> that =
                        (ToIntFunctionForMapNoDefault<?>) o;
                return map.equals(that.map);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }

        @Override
        public String toString() {
            return "forMap(" + map + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a function which performs a map lookup with a default value. The
     * function created by this method returns {@code defaultValue} for all
     * inputs that do not belong to the map's key set. See also
     * {@link #forMap(Map)}, which throws an exception in this case.
     *
     * @param map
     *            source map that determines the function behavior
     * @param defaultValue
     *            the value to return for inputs that aren't map keys
     * @return function that returns {@code map.get(a)} when {@code a} is a key,
     *         or {@code defaultValue} otherwise
     */
    public static <K> ToIntFunction<K> forMap(Map<K, Integer> map,
            int defaultValue) {
        return new ToIntFunctionForMapWithDefault<K>(map, defaultValue);
    }

    private static class ToIntFunctionForMapWithDefault<K> implements
            ToIntFunction<K>, Serializable {
        final Map<K, Integer> map;
        final int defaultValue;

        ToIntFunctionForMapWithDefault(Map<K, Integer> map, int defaultValue) {
            this.map = checkNotNull(map);
            this.defaultValue = defaultValue;
        }

        @Override
        public int applyAsInt(K key) {
            Integer result = map.get(key);
            return (result != null) ? result : defaultValue;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ToIntFunctionForMapWithDefault) {
                ToIntFunctionForMapWithDefault<?> that =
                        (ToIntFunctionForMapWithDefault<?>) o;
                return map.equals(that.map)
                        && defaultValue == that.defaultValue;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(map, defaultValue);
        }

        @Override
        public String toString() {
            return "forMap(" + map + ", defaultValue=" + defaultValue + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns the composition of two functions. For {@code f: A->B} and
     * {@code g: B->C}, composition is defined as the function h such that
     * {@code h(a) == g(f(a))} for each {@code a}.
     *
     * @param ops
     *            - a series of {@link IntUnaryOperator IntUnaryOperators}.
     * @return the composition of operators
     * @see <a href="//en.wikipedia.org/wiki/Function_composition">function
     *      composition</a>
     */
    public static IntUnaryOperator compose(IntUnaryOperator... ops) {
        return new IntUnaryOperatorComposition(ops);
    }

    private static class IntUnaryOperatorComposition implements
            IntUnaryOperator, Serializable {
        private final IntUnaryOperator[] ops;

        public IntUnaryOperatorComposition(IntUnaryOperator[] operators) {
            ops = checkMultiNotNull(operators);
        }

        @Override
        public int applyAsInt(int a) {
            int result = a;
            for (IntUnaryOperator operator : ops) {
                result = operator.applyAsInt(result);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IntUnaryOperatorComposition) {
                IntUnaryOperatorComposition that =
                        (IntUnaryOperatorComposition) obj;
                return Arrays.equals(ops, that.ops);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (IntUnaryOperator op : ops) {
                hash ^= op.hashCode();
            }
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            int stack = 0;
            for (IntUnaryOperator op : ops) {
                stack++;
                // first function no (
                if (stack > 1)
                    builder.append('(');
                builder.append(op);
            }
            while (stack > 1) {
                stack--;
                builder.append(')');
            }
            return builder.toString();
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Creates a function that returns {@code value} for any input.
     *
     * @param value
     *            the constant value for the function to return
     * @return a function that always returns {@code value}
     */
    public static IntUnaryOperator constant(int value) {
        return new ConstantIntUnaryOperator(value);
    }

    private static class ConstantIntUnaryOperator implements IntUnaryOperator,
            Serializable {
        private final int value;

        public ConstantIntUnaryOperator(int value) {
            this.value = value;
        }

        @Override
        public int applyAsInt(int from) {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConstantIntUnaryOperator) {
                ConstantIntUnaryOperator that = (ConstantIntUnaryOperator) obj;
                return Objects.equal(value, that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public String toString() {
            return "constant(" + value + ")";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a function that always returns the result of invoking
     * {@link IntSupplier#get} on {@code supplier}, regardless of its input.
     * 
     * @param supplier
     *            - supplier
     * @return f(x) = supplier.get()
     */
    @Beta
    public static IntUnaryOperator forSupplier(IntSupplier supplier) {
        return new SupplierIntUnaryOperator(supplier);
    }

    /** @see IntOperators#forSupplier */
    private static class SupplierIntUnaryOperator implements IntUnaryOperator,
            Serializable {

        private final IntSupplier supplier;

        private SupplierIntUnaryOperator(IntSupplier supplier) {
            this.supplier = checkNotNull(supplier);
        }

        @Override
        public int applyAsInt(int input) {
            return supplier.get();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SupplierIntUnaryOperator) {
                SupplierIntUnaryOperator that = (SupplierIntUnaryOperator) obj;
                return this.supplier.equals(that.supplier);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return supplier.hashCode();
        }

        @Override
        public String toString() {
            return "forSupplier(" + supplier + ")";
        }

        private static final long serialVersionUID = 0;
    }
}
