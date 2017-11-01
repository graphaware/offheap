package org.mambofish.offheap.map;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author vince
 */

class EntrySetIterator extends AbstractSharedMapIterator implements Iterator<Map.Entry<Key, Value>> {

    EntrySetIterator(OffHeapMap map) {
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
    public Map.Entry<Key, Value> next() {
        if (offset >= map.partitionCount())
            throw new NoSuchElementException();

        long locationAddress = nextLocationAddress();

        // Read how many entries we expect in this partition
        int entryCount = map.getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        long keyAddress = map.getAddress(locationAddress + (locationOffset * map.addressSize() * 2));
        long valueAddress = map.getAddress(locationAddress + (locationOffset * map.addressSize() * 2) + map.addressSize());

        locationOffset++;
        if (locationOffset >= entryCount) {
            locationOffset = 0;
            offset++;
        }

        // Get size of key
        int keyDataSize = map.getInt(keyAddress);

        // Move pointer past size int
        keyAddress += Integer.BYTES;

        byte[] keyData = new byte[keyDataSize];
        for (int keyOffset = 0; keyOffset < keyDataSize; keyOffset++) {
            keyData[keyOffset] = map.getByte(keyAddress + keyOffset);
        }

        final Key key = new Key(keyData);

        // Check if null value
        if (valueAddress == 0) {
            return new Map.Entry<Key, Value>() {
                @Override
                public Key getKey() {
                    return key;
                }

                @Override
                public Value getValue() {
                    return null;
                }

                @Override
                public Value setValue(Value value) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        // Get size of value
        int valueDataSize = map.getInt(valueAddress);

        // Move pointer past size int
        valueAddress += Integer.BYTES;

        byte[] valueData = new byte[valueDataSize];
        for (int valueOffset = 0; valueOffset < valueDataSize; valueOffset++) {
            valueData[valueOffset] = map.getByte(valueAddress + valueOffset);
        }

        final Value value = new Value(valueData);

        return new Map.Entry<Key, Value>() {
            @Override
            public Key getKey() {
                return key;
            }

            @Override
            public Value getValue() {
                return value;
            }

            @Override
            public Value setValue(Value value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
