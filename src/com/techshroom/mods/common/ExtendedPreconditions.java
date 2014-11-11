package com.techshroom.mods.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
public final class ExtendedPreconditions {
    private ExtendedPreconditions() {
    }

    public static <T> T[] checkMultiNotNull(T[] objects) {
        checkNotNull(objects);
        for (Object object : objects) {
            checkNotNull(object);
        }
        return objects;
    }

    public static <T> T[] checkMultiNotNull(T[] objects, Object errorMessage) {
        checkNotNull(objects, errorMessage);
        for (Object object : objects) {
            checkNotNull(object, errorMessage);
        }
        return objects;
    }

    public static <T> T[] checkMultiNotNull(T[] objects,
            String errorMessageTemplate, Object... errorMessageArgs) {
        checkNotNull(objects, errorMessageTemplate, errorMessageArgs);
        for (Object object : objects) {
            checkNotNull(object, errorMessageTemplate, errorMessageArgs);
        }
        return objects;
    }
}
