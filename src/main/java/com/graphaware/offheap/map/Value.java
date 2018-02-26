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
