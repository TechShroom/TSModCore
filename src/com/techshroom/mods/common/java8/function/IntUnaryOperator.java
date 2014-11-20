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
public interface IntUnaryOperator {
    /**
     * Returns the result of applying this function to {@code input}. This
     * method is <i>generally expected</i>, but not absolutely required, to have
     * the following properties:
     *
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals </i>; that is,
     * {@code a == b} implies that
     * {@code function.apply(a) == function.apply(b)}.
     * </ul>
     * 
     * @param input
     *            - input
     * @return int result
     */
    int applyAsInt(int input);
}
