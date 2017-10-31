package org.mambofish.offheap.map;

import java.util.NoSuchElementException;

/**
 * @author vince
 */
public abstract class AbstractSharedMapIterator {

    SharedMap map;

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
