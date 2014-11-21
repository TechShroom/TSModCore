package com.techshroom.mods.common;

import com.google.common.base.Optional;

/**
 * Generic functionalities for cleaner generic code.
 * 
 * @author Kenzie Togami
 */
public final class Generics {
    /**
     * Common {@code Optional<X>} as {@code Optional<Y>} functionality.
     * 
     * @param original
     *            - original Optional instance
     * @param castTo
     *            - class of Y
     * @return the original Optional as an {@code Optional<Y>} if X is actually
     *         a Y, otherwise {@link Optional#absent()}.
     */
    @SuppressWarnings("unchecked")
    public static <X, Y> Optional<Y> castOptional(Optional<X> original,
            Class<Y> castTo) {
        Optional<Y> itemStackOpt = Optional.absent();
        if (castTo.isInstance(original.orNull())) {
            // unsafe cast for less object creation
            itemStackOpt = (Optional<Y>) (Object) original;
        }
        return itemStackOpt;
    }

    private Generics() {
        throw new AssertionError();
    }
}
