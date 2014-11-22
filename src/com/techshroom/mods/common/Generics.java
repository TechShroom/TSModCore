package com.techshroom.mods.common;

import java.lang.reflect.Array;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

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

    private static final Map<Class<?>, Object> ARRAYS = Maps.newConcurrentMap();

    /**
     * Generic empty array sharing, for performance reasons.
     * 
     * @param forceT
     *            - force type parameter with this class
     * 
     * @return an empty array of type T
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> forceT) {
        if (!ARRAYS.containsKey(forceT)) {
            ARRAYS.put(forceT, Array.newInstance(forceT, 0));
        }
        return (T[]) ARRAYS.get(forceT);
    }

    private Generics() {
        throw new AssertionError();
    }
}
