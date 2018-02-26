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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.graphaware.offheap.map.SharedOffHeapMap;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * This cache manager is backed by one or more {@link SharedOffHeapMap} caches
 *
 * TransientSharedMap allows multiple processes in different JVMS running on a single machine to access map entries via
 * shared memory. The shared map is also persisted to disk using a memory-mapped file and will survive a restart of any
 * process, but will not survive a restart of the operating system.
 *
 * In order for two or more processes to access the same shared map, they must agree on its name.  
 *
 */
public class SharedMapCacheManager implements CacheManager {

    private Map<String, Cache> cacheMap = new HashMap();

    public SharedMapCacheManager(String... cacheNames) {
        Arrays.asList(cacheNames).forEach(cacheName -> cacheMap.put(cacheName, new SharedMapCache(cacheName)));
    }

    @Override
    public Cache getCache(String s) {
        return cacheMap.get(s);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }


}
