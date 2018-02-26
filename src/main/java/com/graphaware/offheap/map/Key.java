package com.graphaware.offheap.map;

import java.util.Arrays;

/**
 * @author vince
 */
public class Key {

    private final byte[] bytes;

    public Key(String key) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }

        try {
            this.bytes = key.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Key(byte[] key) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        this.bytes = key;
    }

    public byte[] bytes() {
        return bytes;
    }

    public String get() {
        return new String(bytes);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Arrays.hashCode(this.bytes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Key other = (Key) obj;

        return Arrays.equals(this.bytes, other.bytes);
    }

    public static Key of(String key) {
        return new Key(key);
    }

    public static Key of(byte[] key) {
        return new Key(key);
    }
}
