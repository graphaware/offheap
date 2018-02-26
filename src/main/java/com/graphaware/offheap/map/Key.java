/*
 * Copyright 2013-2018 GraphAware.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
