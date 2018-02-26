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

import java.util.*;

/**
 * @author vince
 */
class EntrySet implements Set<Map.Entry<Key, Value>> {
    private final OffHeapMap map;

    protected EntrySet(OffHeapMap map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry))
            return false;

        Object oKey = ((Map.Entry<Object, Object>)o).getKey();
        Object oValue = ((Map.Entry<Object, Object>)o).getValue();

        if (!(oKey instanceof Map) || (oValue != null && !(oValue instanceof Value)))
            return false;

        Key key = (Key) oKey;
        Value value = (Value) oValue;

        if (value == null) {
            return map.containsKey(key) && map.get(key) == null;
        }

        Value mapValue = map.get(key);

        return Objects.equals(value, mapValue);
    }

    @Override
    public Iterator<Map.Entry<Key, Value>> iterator() {
        return new EntrySetIterator(map);
    }

    @Override
    public Object[] toArray() {
        Object[] values = new Object[map.size()];

        Iterator<Map.Entry<Key, Value>> it = iterator();
        for (int i = 0; i < values.length && it.hasNext(); i++) {
            values[i] = it.next();
        }

        return values;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] r = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

        Iterator<Map.Entry<Key, Value>> it = iterator();
        for (int i = 0; i < r.length; i++) {
            if (!it.hasNext()) {
                // Null terminate
                r[i] = null;
                return r;
            }

            r[i] = (T) it.next();
        }

        return r;
    }

    @Override
    public boolean add(Map.Entry<Key, Value> e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().noneMatch((e) -> (!contains(e)));
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<Key, Value>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        map.clear();
    }
}

