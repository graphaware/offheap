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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author vince
 */
class Keys implements Set<Key> {

    private final OffHeapMap map;

    Keys(OffHeapMap map) {
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
        return map.containsKey(o);
    }

    @Override
    public Iterator<Key> iterator() {
        return new KeysIterator(map);
    }

    @Override
    public Object[] toArray() {
        Object[] keys = new Object[map.size()];

        Iterator<Key> it = iterator();
        for (int i = 0; i < keys.length && it.hasNext(); i++) {
            keys[i] = it.next();
        }

        return keys;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] r = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

        Iterator<Key> it = iterator();
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
    public boolean add(Key e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().noneMatch((k) -> (!map.containsKey(k)));
    }

    @Override
    public boolean addAll(Collection<? extends Key> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object k : c) {
            changed = changed || map.containsKey(k);
            map.remove(k);
        }

        return changed;
    }

    @Override
    public void clear() {
        map.clear();
    }
}

