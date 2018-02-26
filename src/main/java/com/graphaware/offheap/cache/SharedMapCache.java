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

package com.graphaware.offheap.cache;

import java.util.Map;
import java.util.concurrent.Callable;

import com.graphaware.offheap.map.Key;
import com.graphaware.offheap.map.SharedOffHeapMap;
import com.graphaware.offheap.map.Value;
import org.springframework.cache.support.AbstractValueAdaptingCache;

public class SharedMapCache extends AbstractValueAdaptingCache {

    private final Map<Key, Value> store;
    private final String name;

    protected SharedMapCache(String name) {
        super(false);
        this.name = name;
        this.store = new SharedOffHeapMap(2048);
    }

    @Override
    protected Object lookup(Object o) {
        Key key = Key.of((String) o);
        Value value = store.get(key);
        return (value == null ? null : value.get());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return store;
    }

    @Override
    public <T> T get(Object o, Callable<T> callable) {
        if (this.store.containsKey(Key.of((String) o))) {
            return (T) this.get(o).get();
        } else {
            synchronized(this.store) {
                if (this.store.containsKey(Key.of((String) o))) {
                    return (T) this.get(o).get();
                } else {
                    Object value;
                    try {
                        value = callable.call();
                    } catch (Throwable t) {
                        throw new ValueRetrievalException(o, callable, t);
                    }

                    this.put(o, value);
                    return (T) value;
                }
            }
        }

    }

    @Override
    public void put(Object o, Object o1) {
        store.put(Key.of((String) o), Value.of(o1));
    }

    @Override
    public ValueWrapper putIfAbsent(Object o, Object o1) {
        Object existing = this.store.putIfAbsent(Key.of((String) o), Value.of(this.toStoreValue(o1)));
        return this.toValueWrapper(existing);
    }

    @Override
    public void evict(Object o) {
        store.remove(Key.of((String) o));
    }

    @Override
    public void clear() {
        store.clear();
    }
}
