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

package com.graphaware.offheap.map.client;

import com.graphaware.offheap.map.SharedOffHeapMap;

/**
 * @author vince
 */
public class SharedOffHeapMapClient extends AbstractMapClient {

    public SharedOffHeapMapClient(SharedOffHeapMap sharedMap) {
        map = sharedMap;
    }

    @Override
    public Object key(String k) {
        return k;
    }

    @Override
    public Object value(Object v) {
        return v;
    }

    @Override
    public Object get(Object o) {
        return o;
    }
    
    public static void main(String[] args) {

        int size = Integer.parseInt(System.getProperty("size", "10000"));

        AbstractMapClient client = new SharedOffHeapMapClient(new SharedOffHeapMap(size));

        client.listen();

    }
}
