package com.techshroom.mods.common.javaimpls;

import java.util.Map.Entry;

import com.google.common.base.Objects;

/**
 * An immutable Entry implementation.
 * 
 * @author Kenzie Togami
 *
 * @param <K>
 *            - key type
 * @param <V>
 *            - value type
 */
public final class EntryImpl<K, V> implements Entry<K, V> {
    /**
     * Create a new Entry.
     * 
     * @param key
     *            - key
     * @param value
     *            - value
     * @return Entry(key, value)
     */
    public static final <K, V> Entry<K, V> createEntry(K key, V value) {
        return new EntryImpl<K, V>(key, value);
    }

    private final K key;
    private final V value;

    private EntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "EntryImpl{" + "key=" + key + ", " + "value=" + value + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Entry) {
            Entry<?, ?> that = (Entry<?, ?>) o;
            return Objects.equal(that.getKey(), this.getKey())
                    && Objects.equal(that.getValue(), this.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= key.hashCode();
        h *= 1000003;
        h ^= value.hashCode();
        return h;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("Immutable");
    }
}
