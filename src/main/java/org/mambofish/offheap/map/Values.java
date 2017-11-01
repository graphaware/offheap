package org.mambofish.offheap.map;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author vince
 */
class Values implements Collection<Value> {

    private final OffHeapMap map;

    Values(OffHeapMap map) {
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
        return map.containsValue(o);
    }

    @Override
    public Iterator<Value> iterator() {
        return new ValuesIterator(map);
    }

    @Override
    public Object[] toArray() {
        Object[] values = new Object[map.size()];

        Iterator<Value> it = iterator();
        for (int i = 0; i < values.length && it.hasNext(); i++) {
            values[i] = it.next();
        }

        return values;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] r = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

        Iterator<Value> it = iterator();
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
    public boolean add(Value e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().noneMatch((v) -> (!contains(v)));
    }

    @Override
    public boolean addAll(Collection<? extends Value> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        map.clear();
    }
}

