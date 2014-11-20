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
import com.techshroom.mods.common.java8.supplier.FloatSupplier;

/**
 * Static utility methods pertaining to float operators.
 *
 * <p>
 * All methods return serializable functions as long as they're given
 * serializable parameters.
 * 
 * <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">the use
 * of {@code Function}</a> for tips on how to similarly use
 * {@code FloatOperators}.
 *
 * @author Kenzie Togami
 */
@GwtCompatible
public final class FloatOperators {
    private FloatOperators() {
    }

    /**
     * Returns the identity function.
     * 
     * @return f(x) = x
     */
    public static FloatUnaryOperator identity() {
        return IdentityOperator.INSTANCE;
    }

    // enum singleton pattern
    private enum IdentityOperator implements FloatUnaryOperator {
        INSTANCE;

        @Override
        public float applyAsFloat(float input) {
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
     * exist in the map. See also {@link #forMap(Map, float)}, which returns a
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
    public static <K> ToFloatFunction<K> forMap(Map<K, Float> map) {
        return new ToFloatFunctionForMapNoDefault<K>(map);
    }

    private static class ToFloatFunctionForMapNoDefault<K> implements
            ToFloatFunction<K>, Serializable {
        final Map<K, Float> map;

        ToFloatFunctionForMapNoDefault(Map<K, Float> map) {
            this.map = checkNotNull(map);
        }

        @Override
        public float applyAsFloat(K key) {
            checkArgument(map.containsKey(key), "Key '%s' not present in map",
                          key);
            Float result = map.get(key);
            checkNotNull(result, "Value cannot be null for key '%s'", key);
            return result.floatValue();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ToFloatFunctionForMapNoDefault) {
                ToFloatFunctionForMapNoDefault<?> that =
                        (ToFloatFunctionForMapNoDefault<?>) o;
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
    public static <K> ToFloatFunction<K> forMap(Map<K, Float> map,
            float defaultValue) {
        return new ToIntFunctionForMapWithDefault<K>(map, defaultValue);
    }

    private static class ToIntFunctionForMapWithDefault<K> implements
            ToFloatFunction<K>, Serializable {
        final Map<K, Float> map;
        final float defaultValue;

        ToIntFunctionForMapWithDefault(Map<K, Float> map, float defaultValue) {
            this.map = checkNotNull(map);
            this.defaultValue = defaultValue;
        }

        @Override
        public float applyAsFloat(K key) {
            Float result = map.get(key);
            return (result != null) ? result : defaultValue;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ToIntFunctionForMapWithDefault) {
                ToIntFunctionForMapWithDefault<?> that =
                        (ToIntFunctionForMapWithDefault<?>) o;
                return map.equals(that.map)
                        && Float.compare(defaultValue, that.defaultValue) == 0;
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
     * {@code h(a) == g(f(a))} for each {@code a}. This may not hold true when
     * dealing with floats.
     *
     * @param ops
     *            - a series of {@link FloatUnaryOperator FloatUnaryOperators}.
     * @return the composition of operators
     * @see <a href="//en.wikipedia.org/wiki/Function_composition">function
     *      composition</a>
     */
    public static FloatUnaryOperator compose(FloatUnaryOperator... ops) {
        return new FloatUnaryOperatorComposition(ops);
    }

    private static class FloatUnaryOperatorComposition implements
            FloatUnaryOperator, Serializable {
        private final FloatUnaryOperator[] ops;

        public FloatUnaryOperatorComposition(FloatUnaryOperator[] operators) {
            ops = checkMultiNotNull(operators);
        }

        @Override
        public float applyAsFloat(float a) {
            float result = a;
            for (FloatUnaryOperator operator : ops) {
                result = operator.applyAsFloat(result);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FloatUnaryOperatorComposition) {
                FloatUnaryOperatorComposition that =
                        (FloatUnaryOperatorComposition) obj;
                return Arrays.equals(ops, that.ops);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (FloatUnaryOperator op : ops) {
                hash ^= op.hashCode();
            }
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            int stack = 0;
            for (FloatUnaryOperator op : ops) {
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
    public static FloatUnaryOperator constant(int value) {
        return new ConstantFloatUnaryOperator(value);
    }

    private static class ConstantFloatUnaryOperator implements
            FloatUnaryOperator, Serializable {
        private final int value;

        public ConstantFloatUnaryOperator(int value) {
            this.value = value;
        }

        @Override
        public float applyAsFloat(float from) {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ConstantFloatUnaryOperator) {
                ConstantFloatUnaryOperator that =
                        (ConstantFloatUnaryOperator) obj;
                return Float.compare(value, that.value) == 0;
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
     * {@link FloatSupplier#get} on {@code supplier}, regardless of its input.
     * 
     * @param supplier
     *            - supplier
     * @return f(x) = supplier.get()
     */
    @Beta
    public static FloatUnaryOperator forSupplier(FloatSupplier supplier) {
        return new SupplierFloatUnaryOperator(supplier);
    }

    /** @see FloatOperators#forSupplier */
    private static class SupplierFloatUnaryOperator implements
            FloatUnaryOperator, Serializable {

        private final FloatSupplier supplier;

        private SupplierFloatUnaryOperator(FloatSupplier supplier) {
            this.supplier = checkNotNull(supplier);
        }

        @Override
        public float applyAsFloat(float input) {
            return supplier.get();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SupplierFloatUnaryOperator) {
                SupplierFloatUnaryOperator that =
                        (SupplierFloatUnaryOperator) obj;
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
