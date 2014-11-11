package com.techshroom.mods.common.java8.optional;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.google.common.annotations.GwtCompatible;
import com.techshroom.mods.common.java8.function.IntUnaryOperator;
import com.techshroom.mods.common.java8.supplier.IntSupplier;

/**
 * An immutable object that may contain a reference to an integer. Each instance
 * of this type either contains an integer, or contains nothing.
 *
 * <p>
 * An {@code OptionalInt} reference can be used as a replacement for a special
 * value integer. It allows you to represent "an {@code int} that must be
 * present" and "an {@code int} that might be absent" as two distinct types in
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
 * {@code OptionalInt.absent()})
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
 * {@code OptionalInt}.
 * 
 * @author Kenzie Togami
 */
@GwtCompatible(serializable = true)
public abstract class OptionalInt implements Serializable {
    private static final class Present
            extends OptionalInt {
        private static final long serialVersionUID = -2342339346879868880L;

        private final int value;

        private Present(int v) {
            value = v;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public int get() {
            return value;
        }

        @Override
        public int or(int defaultValue) {
            return value;
        }

        @Override
        public OptionalInt or(OptionalInt secondChoice) {
            checkNotNull(secondChoice);
            return this;
        }

        @Override
        public int or(IntSupplier supplier) {
            checkNotNull(supplier);
            return value;
        }

        @Override
        public OptionalInt transform(IntUnaryOperator function) {
            return OptionalInt.of(function.applyAsInt(value));
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Present) {
                Present that = (Present) object;
                return value == that.value;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public String toString() {
            return "OptionalInt.of(" + value + ")";
        }

    }

    private static class Absent
            extends OptionalInt {
        private static final long serialVersionUID = -5294040870318860798L;
        private static final OptionalInt INSTANCE = new Absent();

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public int get() {
            throw new IllegalStateException("no value");
        }

        @Override
        public int or(int defaultValue) {
            return defaultValue;
        }

        @Override
        public OptionalInt or(OptionalInt secondChoice) {
            checkNotNull(secondChoice);
            return secondChoice;
        }

        @Override
        public int or(IntSupplier supplier) {
            checkNotNull(supplier);
            return supplier.get();
        }

        @Override
        public OptionalInt transform(IntUnaryOperator function) {
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
            return "OptionalInt.absent()";
        }
    }

    /**
     * Returns an {@code Optional} instance with no contained reference.
     */
    public static OptionalInt absent() {
        return Absent.INSTANCE;
    }

    /**
     * Returns an {@code Optional} instance containing the given non-null
     * reference.
     */
    public static OptionalInt of(int value) {
        return new Present(value);
    }

    private OptionalInt() {
    }

    /**
     * Returns {@code true} if this holder contains a (non-null) instance.
     */
    public abstract boolean isPresent();

    /**
     * Returns the contained instance, which must be present. If the instance
     * might be absent, use {@link #or(Object)} or {@link #orNull} instead.
     *
     * @throws IllegalStateException
     *             if the instance is absent ({@link #isPresent} returns
     *             {@code false})
     */
    public abstract int get();

    /**
     * Returns the contained int if it is present; {@code defaultValue}
     * otherwise. If no default value should be required because the instance is
     * known to be present, use {@link #get()} instead. For a default value of
     * {@code null}, use {@link #orNull}.
     */
    public abstract int or(int defaultValue);

    /**
     * Returns this {@code OptionalInt} if it has a value present;
     * {@code secondChoice} otherwise.
     */
    public abstract OptionalInt or(OptionalInt secondChoice);

    /**
     * Returns the contained int if it is present; {@code supplier.get()}
     * otherwise.
     */
    public abstract int or(IntSupplier supplier);

    /**
     * If the instance is present, it is transformed with the given
     * {@link IntUnaryOperator}; otherwise, {@link OptionalInt#absent} is
     * returned. If the function returns {@code null}, a
     * {@link NullPointerException} is thrown.
     *
     * @throws NullPointerException
     *             if the function returns {@code null}
     *
     * @since 12.0
     */
    public abstract OptionalInt transform(IntUnaryOperator function);

    /**
     * Returns {@code true} if {@code object} is an {@code Optional} instance,
     * and either the contained references are {@linkplain Object#equals equal}
     * to each other or both are absent. Note that {@code Optional} instances of
     * differing parameterized types can be equal.
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
