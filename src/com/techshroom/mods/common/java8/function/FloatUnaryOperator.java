package com.techshroom.mods.common.java8.function;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Functions;

/**
 * Determines an output value based on an input value.
 *
 * <p>
 * The {@link Functions} class provides common functions and related utilities.
 *
 * <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">the use
 * of {@code Function}</a> for tip on how to similarly use
 * {@code IntUnaryOperator}.
 *
 * @author Kenzie Togami
 */
@GwtCompatible
public interface FloatUnaryOperator {
    /**
     * Returns the result of applying this function to {@code input}. This
     * method is <i>generally expected</i>, but not absolutely required, to have
     * the following properties:
     *
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with
     * {@link Float#compare(float, float)} </i>; that is,
     * {@code Float.compare(a, b) == 0} implies that
     * {@code Float.compare(function.apply(a), function.apply(b)) == 0}.
     * </ul>
     */
    float applyAsFloat(float input);
}
