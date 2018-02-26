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

import java.io.*;

import com.graphaware.offheap.map.Key;
import com.graphaware.offheap.map.OffHeapMap;
import com.graphaware.offheap.map.Value;

/**
 * @author vince
 */
public class OffHeapMapClient extends AbstractMapClient {

    public OffHeapMapClient(OffHeapMap sharedMap) {
        this.map = sharedMap;
    }

    @Override
    public Object key(String k) {
        return Key.of(k);
    }

    @Override
    public Object value(Object v) {
        return Value.of(v);
    }

    @Override
    public Object get(Object v) {
        return ((Value) v).get();
    }
    public static void main(String[] args) {

        File file = new File(System.getProperty("file", "test.map"));
        int partitionCount = Integer.parseInt(System.getProperty("partitions", "10000"));
        long address = Long.parseLong(System.getProperty("address", "0"));

        AbstractMapClient client = new OffHeapMapClient(new OffHeapMap(file, partitionCount, address));

        client.listen();
    }


}
