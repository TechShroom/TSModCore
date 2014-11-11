package com.techshroom.mods.common.java8.supplier;

import com.google.common.annotations.GwtCompatible;

/**
 * A class that can supply floats. Semantically, this could be a generator,
 * closure, or something else entirely. No guarantees are implied by this
 * interface.
 *
 * @author Kenzie Togami
 */
@GwtCompatible
public interface FloatSupplier {
    /**
     * Retrieves a float. The returned float may or may not be different,
     * depending on the implementation.
     *
     * @return a float
     */
    float get();
}
