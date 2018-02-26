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

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import com.graphaware.offheap.map.Key;
import com.graphaware.offheap.map.SharedOffHeapMap;
import com.graphaware.offheap.map.Value;

/**
 * @author vince
 */
public class SharedOffHeapMapTest {

    @Test
    public void save() {

        SharedOffHeapMap sharedOffHeapMap = new SharedOffHeapMap(2048);

        String k = "key";

        Map<String, Object> v = new HashMap<>();

        v.put("x", "x-value");
        v.put("y", "y-value");

        sharedOffHeapMap.put(Key.of(k), Value.of(v));

        Value found = sharedOffHeapMap.get(Key.of(k));

        assertEquals(v, found.get());

    }

    @Test
    public void saveToSpecifiedFile() {

        File file = new File("sharedmap.map");
        file.deleteOnExit();

        SharedOffHeapMap sharedOffHeapMap = new SharedOffHeapMap(file, 2048);

        String k = "key";

        Map<String, Object> v = new HashMap<>();

        v.put("x", "x-value");
        v.put("y", "y-value");

        sharedOffHeapMap.put(Key.of(k), Value.of(v));

        Value found = sharedOffHeapMap.get(Key.of(k));

        assertEquals(v, found.get());

        Map<String, Object> retrieved = (Map<String, Object>) found.get();
        assertEquals("x-value", retrieved.get("x"));
        assertEquals("y-value", retrieved.get("y"));



    }

}
