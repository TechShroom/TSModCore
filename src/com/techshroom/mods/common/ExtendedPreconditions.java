package com.techshroom.mods.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;

/**
 * Extra preconditions for use with {@link Preconditions}
 * 
 * @author Kenzie Togami
 */
@GwtCompatible
public final class ExtendedPreconditions {
    private ExtendedPreconditions() {
    }

    /**
     * Checks that the passed in array is not null and does not contain any null
     * objects.
     * 
     * @param objects
     *            - array to check
     * @return the passed in array if it passes
     */
    public static <T> T[] checkMultiNotNull(T[] objects) {
        checkNotNull(objects);
        for (Object object : objects) {
            checkNotNull(object);
        }
        return objects;
    }

    /**
     * Checks that the passed in array is not null and does not contain any null
     * objects.
     * 
     * @param objects
     *            - array to check
     * @param errorMessage
     *            - message to use with exception
     * @return the passed in array if it passes
     */
    public static <T> T[] checkMultiNotNull(T[] objects, Object errorMessage) {
        checkNotNull(objects, errorMessage);
        for (Object object : objects) {
            checkNotNull(object, errorMessage);
        }
        return objects;
    }

    /**
     * Checks that the passed in array is not null and does not contain any null
     * objects.
     * 
     * @param objects
     *            - array to check
     * @param errorMessageTemplate
     *            - template for error message (uses %s substitution only)
     * @param errorMessageArgs
     *            - objects to substitute
     * @return the passed in array if it passes
     */
    public static <T> T[] checkMultiNotNull(T[] objects,
            String errorMessageTemplate, Object... errorMessageArgs) {
        checkNotNull(objects, errorMessageTemplate, errorMessageArgs);
        for (Object object : objects) {
            checkNotNull(object, errorMessageTemplate, errorMessageArgs);
        }
        return objects;
    }
}
