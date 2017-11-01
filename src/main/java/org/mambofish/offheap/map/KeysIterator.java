package org.mambofish.offheap.map;

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



