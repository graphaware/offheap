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

import java.util.NoSuchElementException;

/**
 * @author vince
 */
public abstract class AbstractSharedMapIterator {

    OffHeapMap map;

    long offset, locationOffset;

    long nextLocationAddress() {

        long locationAddress = map.locationAddress(map.partitionAddress() + (offset * map.addressSize()));

        while (locationAddress == 0) {
            offset++;

            if (offset >= map.partitionCount())
                throw new NoSuchElementException();

            locationAddress = map.locationAddress(map.partitionAddress() + (offset * map.addressSize()));
        }

        return locationAddress;
    }
}
