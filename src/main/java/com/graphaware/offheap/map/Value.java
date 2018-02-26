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

import java.io.*;
import java.util.Arrays;

/**
 * @author vince
 */
public class Value {

    private final byte[] bytes;

    public Value(Object value) {

        if (value == null)
            throw new NullPointerException("value may not be null");

        if (!(value instanceof byte[])) {
            this.bytes = toBytes(value);
        } else {
            this.bytes = (byte[]) value;
        }
    }

    public byte[] bytes() {
        return bytes;
    }

    public Object get() {
        return toObject();
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
        final Value other = (Value) obj;

        return Arrays.equals(this.bytes, other.bytes);
    }

    private byte[] toBytes(Object o) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private Object toObject() {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static Value of(Object value) {
        if (value != null) {
            return new Value(value);
        } return null;
    }

}
