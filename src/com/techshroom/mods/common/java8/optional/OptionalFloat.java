package com.techshroom.mods.common.java8.optional;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.google.common.annotations.GwtCompatible;
import com.techshroom.mods.common.java8.function.FloatUnaryOperator;
import com.techshroom.mods.common.java8.supplier.FloatSupplier;

/**
 * An immutable object that may contain a reference to an float. Each instance
 * of this type either contains an float, or contains nothing.
 *
 * <p>
 * An {@code OptionalFloat} reference can be used as a replacement for a special
 * value float. It allows you to represent "an {@code float} that must be
 * present" and "an {@code float} that might be absent" as two distinct types in
 * your program, which can aid clarity.
 *
 * <p>
 * Some uses of this class include
 *
 * <ul>
 * <li>As a method return type, as an alternative to returning a special value
 * to indicate that no value was available
 * <li>To distinguish between "unknown" (for example, not present in a map) and
 * "known to have no value" (present in the map, with value
 * {@code OptionalFloat.absent()})
 * </ul>
 *
 * <p>
 * This class is not intended as a direct analogue of any existing "option" or
 * "maybe" construct from other programming environments, though it may bear
 * some similarities.
 *
 * <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/UsingAndAvoidingNullExplained#Optional"
 * > using {@code Optional}</a> for tips on how to similarly use
 * {@code OptionalFloat}.
 * 
 * @author Kenzie Togami
 */
@GwtCompatible(serializable = true)
public abstract class OptionalFloat implements Serializable {
    private static final class Present
            extends OptionalFloat {
        private static final long serialVersionUID = -2342339346879868880L;

        private final float value;

        private Present(float v) {
            value = v;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public float get() {
            return value;
        }

        @Override
        public float or(float defaultValue) {
            return value;
        }

        @Override
        public OptionalFloat or(OptionalFloat secondChoice) {
            checkNotNull(secondChoice);
            return this;
        }

        @Override
        public float or(FloatSupplier supplier) {
            checkNotNull(supplier);
            return value;
        }

        @Override
        public OptionalFloat transform(FloatUnaryOperator function) {
            return OptionalFloat.of(function.applyAsFloat(value));
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Present) {
                Present that = (Present) object;
                return Float.compare(value, that.value) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Float.floatToIntBits(value);
        }

        @Override
        public String toString() {
            return "OptionalFloat.of(" + value + ")";
        }

    }

    private static class Absent
            extends OptionalFloat {
        private static final long serialVersionUID = -5294040870318860798L;
        private static final OptionalFloat INSTANCE = new Absent();

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public float get() {
            throw new IllegalStateException("no value");
        }

        @Override
        public float or(float defaultValue) {
            return defaultValue;
        }

        @Override
        public OptionalFloat or(OptionalFloat secondChoice) {
            checkNotNull(secondChoice);
            return secondChoice;
        }

        @Override
        public float or(FloatSupplier supplier) {
            checkNotNull(supplier);
            return supplier.get();
        }

        @Override
        public OptionalFloat transform(FloatUnaryOperator function) {
            return this;
        }

        @Override
        public boolean equals(Object object) {
            return object == INSTANCE;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "OptionalFloat.absent()";
        }
    }

    /**
     * Returns an {@code OptionalFloat} instance with no contained float.
     * 
     * @return an OptionalFloat with no value.
     */
    public static OptionalFloat absent() {
        return Absent.INSTANCE;
    }

    /**
     * Returns an {@code OptionalFloat} instance containing the given float.
     * 
     * @param value
     *            - value to wrap
     * @return an OptionalFloat containing the given value
     */
    public static OptionalFloat of(float value) {
        return new Present(value);
    }

    private OptionalFloat() {
    }

    /**
     * Returns {@code true} if there is a value.
     * 
     * @return {@code true} if this holder contains a float
     */
    public abstract boolean isPresent();

    /**
     * Returns the contained float, which must be present. If the float might be
     * absent, use {@link #or(float)} instead.
     * 
     * @return the contained float
     *
     * @throws IllegalStateException
     *             if the instance is absent ({@link #isPresent} returns
     *             {@code false})
     */
    public abstract float get();

    /**
     * Returns the contained float if it is present; {@code defaultValue}
     * otherwise. If no default value should be required because the float is
     * known to be present, use {@link #get()} instead.
     * 
     * @param defaultValue
     *            - the value to return if this holder does not have a value
     * @return the contained float if it is present, otherwise defaultValue
     */
    public abstract float or(float defaultValue);

    /**
     * Returns this {@code OptionalFloat} if it has a value present;
     * {@code secondChoice} otherwise.
     * 
     * @param secondChoice
     *            - the second OptionalFloat to use
     * @return this if {@code this.isPresent()}, otherwise secondChoice
     */
    public abstract OptionalFloat or(OptionalFloat secondChoice);

    /**
     * Returns the contained float if it is present; {@code supplier.get()}
     * otherwise.
     * 
     * @param supplier
     *            - supplier to use if this value is not present
     * @return {@code if (this.isPresent()) this.get(); else supplier.get();}
     */
    public abstract float or(FloatSupplier supplier);

    /**
     * If the instance is present, it is transformed with the given
     * {@link FloatUnaryOperator}; otherwise, {@link OptionalFloat#absent} is
     * returned.
     * 
     * @param function
     *            - function to transform this OptionalInt by
     * @return {@code if (this.isPresent()) OptionalFloat.of(function(this.get)); else OptionalFloat.absent();}
     */
    public abstract OptionalFloat transform(FloatUnaryOperator function);

    /**
     * Returns {@code true} if {@code object} is an {@code OptionalFloat}
     * instance, and either the contained float are equal to each other or both
     * are absent.
     */
    @Override
    public abstract boolean equals(Object object);

    /**
     * Returns a hash code for this instance.
     */
    @Override
    public abstract int hashCode();

    /**
     * Returns a string representation for this instance. The form of this
     * string representation is unspecified.
     */
    @Override
    public abstract String toString();

    private static final long serialVersionUID = 0;
}
