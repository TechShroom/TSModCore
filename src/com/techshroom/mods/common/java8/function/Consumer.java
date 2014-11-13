package com.techshroom.mods.common.java8.function;

/**
 * Java 8 based consumer interface.
 * 
 * @author Kenzie Togami
 * @param <T> - type to consume
 */
public interface Consumer<T> {
    void accept(T input);
}
