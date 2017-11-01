package org.mambofish.offheap.map;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author vince
 */
class ValuesIterator extends AbstractSharedMapIterator implements Iterator<Value> {

    ValuesIterator(OffHeapMap map) {
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
    public Value next() {
        if (offset >= map.partitionCount())
            throw new NoSuchElementException();

        long locationAddress = nextLocationAddress();

        // Read how many entries we expect in this partition
        int entryCount = map.getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        long valueAddress = map.getAddress(locationAddress + (locationOffset * map.addressSize() * 2) + map.addressSize());

        locationOffset++;
        if (locationOffset >= entryCount) {
            locationOffset = 0;
            offset++;
        }

        // Check if null value
        if (valueAddress == 0)
            return null;

        // Get size of value
        int valueDataSize = map.getInt(valueAddress);

        // Move pointer past size int
        valueAddress += Integer.BYTES;

        byte[] valueData = new byte[valueDataSize];
        for (int valueOffset = 0; valueOffset < valueDataSize; valueOffset++) {
            valueData[valueOffset] = map.getByte(valueAddress + valueOffset);
        }

        return new Value(valueData);
    }
}

