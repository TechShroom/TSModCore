package com.techshroom.mods.common.java8.supplier;

import com.google.common.annotations.GwtCompatible;

/**
 * A class that can supply integers. Semantically, this could be a generator,
 * closure, or something else entirely. No guarantees are implied by this
 * interface.
 *
 * @author Kenzie Togami
 */
@GwtCompatible
public interface IntSupplier {
    /**
     * Retrieves an integer. The returned integer may or may not be different,
     * depending on the implementation.
     *
     * @return an integer
     */
    int get();
}
