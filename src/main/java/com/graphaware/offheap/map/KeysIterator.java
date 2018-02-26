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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author vince
 */
class KeysIterator extends AbstractSharedMapIterator implements Iterator<Key> {

    KeysIterator(OffHeapMap map) {
        this.map = map;
    }

    @Override
    public boolean hasNext() {
        while (offset < map.partitionCount()) {
            long locationAddress = map.locationAddress(map.partitionAddress() + (offset * map.addressSize()));

            if (locationAddress == 0) {
                offset++;
                continue;
            }

            break;
        }

        return offset < map.partitionCount();
    }

    @Override
    public Key next() {
        if (offset >= map.partitionCount())
            throw new NoSuchElementException();

        long locationAddress = nextLocationAddress();

        // Read how many entries we expect in this partition
        int entryCount = map.getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        long keyAddress = map.getAddress(locationAddress + (locationOffset * map.addressSize() * 2));

        // Get size of key
        int keyDataSize = map.getInt(keyAddress);

        // Move pointer past size int
        keyAddress += Integer.BYTES;

        locationOffset++;
        if (locationOffset >= entryCount) {
            locationOffset = 0;
            offset++;
        }

        byte[] keyData = new byte[keyDataSize];
        for (int keyOffset = 0; keyOffset < keyDataSize; keyOffset++) {
            keyData[keyOffset] = map.getByte(keyAddress + keyOffset);
        }

        return new Key(keyData);
    }
}



