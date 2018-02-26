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

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.graphaware.offheap.io.MemoryMappedFile;
import sun.misc.Unsafe;

/**
 * @author vince
 */
public class OffHeapMap implements Map<Key, Value> {

    private final Unsafe unsafe;
    private final int addressSize;
    private final int partitionCount;
    private final long partitionAddress;
    private final Function<byte[], Integer> hashFunction;

    private MemoryMappedFile file;
    private boolean create;

    private boolean closed = false;
    private long itemCount;

    public OffHeapMap(File file, int partitionCount, long address) {
        this(file, partitionCount, address, Arrays::hashCode);
    }

    private OffHeapMap(File file, int partitionCount, long address, Function<byte[], Integer> hashFunction) {

        this.unsafe = theUnsafe();
        this.addressSize = unsafe.addressSize();
        this.partitionCount = partitionCount;
        this.hashFunction = hashFunction;

        if (file != null) {
            this.create = !file.exists();
            try {
                if (this.create) {
                    System.out.println("Creating new shared map");
                    file.deleteOnExit();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));
                } else {
                    System.out.println("Opening shared map");
                }
                this.file = new MemoryMappedFile(file, partitionCount * addressSize, address);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (address == 0) {
            this.partitionAddress = allocate(this.partitionCount * addressSize, true); // base from file.
        } else {
            this.partitionAddress = address;
        }
    }

    private Unsafe theUnsafe() {
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);
        } catch (IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public long locationAddress(long position) {
        if (this.partitionAddress == 0) {
            return file.getLong(position);
        } else {
            return getAddress(position);
        }
    }

    int partitionCount() {
        return partitionCount;
    }

    long partitionAddress() {
        return partitionAddress;
    }

    int addressSize() {
        return addressSize;
    }

    private long allocate(long size, boolean init) {
        final long address = unsafe.allocateMemory(size);

        for (long offset = 0; init && offset < size; offset++) {
            unsafe.putByte(address + offset, (byte) 0);
        }

        return address;
    }

    private long getPartitionOffset(byte[] keyData) {
        return Math.abs(hashFunction.apply(keyData) % partitionCount);
    }

    private long saveValue(Value value) {

        long valueAddress = 0;

        if (value != null) {
            // Deal with inserting new value
            byte[] valueData = value.bytes();
            int valueSize = valueData.length;

            // Allocate new value space
            valueAddress = allocate(Integer.BYTES + valueSize, false);

            // Copy data over
            putInt(valueAddress, valueSize);

            for (int valueOffset = 0; valueOffset < valueSize; valueOffset++) {
                putByte(valueAddress + valueOffset + Integer.BYTES, valueData[valueOffset]);
            }
        }

        return valueAddress;

    }

    private Value removeValue(long valueAddress) {

        Value value = null;

        // Remove value at this address if we have one
        if (valueAddress != 0) {
            int valueSize = getInt(valueAddress);

            byte[] valueData = new byte[valueSize];

            // Move pointer past size int
            valueAddress += Integer.BYTES;

            for (int valueOffset = 0; valueOffset < valueSize; valueOffset++) {
                valueData[valueOffset] = getByte(valueAddress + valueOffset);
            }

            value = new Value(valueData);

            // Move pointer back to start
            valueAddress -= Integer.BYTES;

            // Free old value
            free(valueAddress);
        }

        return value;
    }

    long getAddress(long position) {
        return unsafe.getAddress(position);
    }

    int getInt(long position) {
        return unsafe.getInt(position);
    }

    byte getByte(long position) {
        return unsafe.getByte(position);
    }

    long reallocateMemory(long locationAddress, int i) {
        return unsafe.reallocateMemory(locationAddress, i);
    }

    void putAddress(long l, long locationAddress) {
        unsafe.putAddress(l, locationAddress);
    }

    void putInt(long valueAddress, int valueSize) {
        unsafe.putInt(null, valueAddress, valueSize);
    }

    void putByte(long l, byte valueDatum) {
        unsafe.putByte(null, l, valueDatum);
    }

    void free(long keyAddress) {
        unsafe.freeMemory(keyAddress);
    }

    @Override
    public int size() {
        if (itemCount > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;

        return (int) itemCount;
    }

    @Override
    public boolean isEmpty() {
        return itemCount == 0;
    }

    @Override
    public boolean containsKey(Object key) {

        final Key bKey = new Key(String.valueOf(key));

        final byte[] keyData = bKey.bytes();
        final int keySize = keyData.length;

        final long offset = getPartitionOffset(keyData);

        // This is the location of the partition on which the entry key belongs
        long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

        // Skip if unallocated
        if (locationAddress == 0)
            return false;

        // Read how many entries we expect in this partition
        int entryCount = getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
            // Address of key within partition
            long keyAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

            // Get size of key
            int size = getInt(keyAddress);

            // If size of this key is different than the one
            // we're looking for, continue..
            if (size != keySize)
                continue;

            // Move pointer past size int
            keyAddress += Integer.BYTES;

            // Scan each byte to check for differences
            boolean isEqual = true;
            for (int keyOffset = 0; keyOffset < keySize; keyOffset++) {
                if (keyData[keyOffset] != getByte(keyAddress + keyOffset)) {
                    isEqual = false;
                    break;
                }
            }

            // Check if we found the key
            if (isEqual)
                return true;
        }

        return false;
    }

    @Override
    public boolean containsValue(Object value) {

        final Value bValue = new Value(value);

        final byte[] valueData = bValue == null ? null : bValue.bytes();
        final int valueSize = valueData == null ? -1 : valueData.length;

        // For each partition..
        for (long offset = 0; offset < partitionCount; offset++) {
            // ..get partition address
            long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

            // Skip if unallocated
            if (locationAddress == 0)
                continue;

            // Read how many entries we expect in this partition
            int entryCount = getInt(locationAddress);

            // Move pointer past size int
            locationAddress += Integer.BYTES;

            // Move pointer past key address in key/value pair
            locationAddress += addressSize;

            for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
                // Address of value within partition
                long valueAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

                // Check if null value
                if (valueAddress == 0) {
                    if (bValue == null)
                        return true;
                    else
                        continue;
                }

                // Size of value
                int size = getInt(valueAddress);

                // If size of this value is different than the one
                // we're looking for, continue..
                if (size != valueSize)
                    continue;

                // Move pointer past size int
                valueAddress += Integer.BYTES;

                // Scan each byte to check for differences
                boolean isEqual = true;
                for (int valueOffset = 0; valueOffset < valueSize; valueOffset++) {
                    if (valueData[valueOffset] != getByte(valueAddress + valueOffset)) {
                        isEqual = false;
                        break;
                    }
                }

                if (isEqual)
                    return true;
            }
        }

        return false;
    }

    @Override
    public Value get(Object key) {

        final Key bKey = new Key(String.valueOf(key));
        final byte[] keyData = bKey.bytes();
        final int keySize = keyData.length;

        final long offset = getPartitionOffset(keyData);

        // This is the location of the partition on which the entry key belongs
        long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

        // Skip if unallocated
        if (locationAddress == 0)
            return null;

        // Read how many entries we expect in this partition
        int entryCount = getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
            // Address of key within partition
            long keyAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

            // Get size of key
            int size = getInt(keyAddress);

            // If size of this key is different than the one
            // we're looking for, continue..
            if (size != keySize)
                continue;

            // Move pointer past size int
            keyAddress += Integer.BYTES;

            // Scan each byte to check for differences
            boolean isEqual = true;
            for (int keyOffset = 0; keyOffset < keySize; keyOffset++) {
                if (keyData[keyOffset] != getByte(keyAddress + keyOffset)) {
                    isEqual = false;
                    break;
                }
            }

            // Check if we found the key
            if (isEqual) {
                long valueAddress = getAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize);

                // Check if this is a null value
                if (valueAddress == 0)
                    return null;

                int valueSize = getInt(valueAddress);

                byte[] valueData = new byte[valueSize];

                // Move pointer past size int
                valueAddress += Integer.BYTES;

                for (int valueOffset = 0; valueOffset < valueSize; valueOffset++) {
                    valueData[valueOffset] = getByte(valueAddress + valueOffset);
                }

                return new Value(valueData);
            }
        }

        return null;
    }

    @Override
    public Value put(Key key, Value value) {
        final byte[] keyData = key.bytes();
        final int keySize = keyData.length;

        final long offset = getPartitionOffset(keyData);

        // This is the location of the partition on which the entry key belongs
        long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

        // Read how many entries we expect in this partition
        int entryCount = locationAddress == 0 ? 0 : getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
            // Address of key within partition
            long keyAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

            // Get size of key
            int size = getInt(keyAddress);

            // If size of this key is different than the one
            // we're looking for, continue..
            if (size != keySize)
                continue;

            // Move pointer past size int
            keyAddress += Integer.BYTES;

            // Scan each byte to check for differences
            boolean isEqual = true;
            for (int keyOffset = 0; keyOffset < keySize; keyOffset++) {
                if (keyData[keyOffset] != getByte(keyAddress + keyOffset)) {
                    isEqual = false;
                    break;
                }
            }

            // Check if we found the key
            if (isEqual) {
                long valueAddress = getAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize);

                Value oldValue = removeValue(valueAddress);
                valueAddress = saveValue(value);
                // Update value address in partition
                putAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize, valueAddress);

                // Return old value
                return oldValue;
            }
        }

        // Existing entry not found on key, insert new
        itemCount++;

        // Move partition pointer back to start
        locationAddress -= Integer.BYTES;

        // Allocate and copy key
        long keyAddress = allocate(Integer.BYTES + keySize, false);
        putInt(keyAddress, keySize);
        for (int keyOffset = 0; keyOffset < keySize; keyOffset++) {
            putByte(keyAddress + Integer.BYTES + keyOffset, keyData[keyOffset]);
        }

        long valueAddress = saveValue(value);

        // Allocate or reallocate partition
        if (locationAddress == 0) {
            locationAddress = allocate(Integer.BYTES + addressSize + addressSize, false);
        } else {
            locationAddress = reallocateMemory(locationAddress, Integer.BYTES + (addressSize * 2 * (entryCount + 1)));
        }

        // Insert key and value pointers
        putAddress(locationAddress + Integer.BYTES + (addressSize * 2 * entryCount), keyAddress);
        putAddress(locationAddress + Integer.BYTES + (addressSize * 2 * entryCount) + addressSize, valueAddress);

        // Update entry count
        putInt(locationAddress, entryCount + 1);

        // Update pointer to partition
        setLocation(partitionAddress + (offset * addressSize), locationAddress);

        return null;
    }

    private void setLocation(long pointer, long address) {
        if (partitionAddress == 0) {
            file.putLong(pointer, address);
        } else {
            putAddress(pointer, address);
        }
    }

    @Override
    public Value remove(Object key) {
        final Key bKey = new Key(String.valueOf(key));
        final byte[] keyData = bKey.bytes();
        final int keySize = keyData.length;

        final long offset = getPartitionOffset(keyData);

        // This is the location of the partition on which the entry key belongs
        long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

        // Skip if unallocated
        if (locationAddress == 0)
            return null;

        // Read how many entries we expect in this partition
        int entryCount = getInt(locationAddress);

        // Move pointer past size int
        locationAddress += Integer.BYTES;

        for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
            // Address of key within partition
            long keyAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

            // Get size of key
            int size = getInt(keyAddress);

            // If size of this key is different than the one
            // we're looking for, continue..
            if (size != keySize)
                continue;

            // Move pointer past size int
            keyAddress += Integer.BYTES;

            // Scan each byte to check for differences
            boolean isEqual = true;
            for (int keyOffset = 0; keyOffset < keySize; keyOffset++) {
                if (keyData[keyOffset] != getByte(keyAddress + keyOffset)) {
                    isEqual = false;
                    break;
                }
            }

            // Check if we found the key
            if (isEqual) {
                // Move key address back to start + free it
                keyAddress -= Integer.BYTES;
                free(keyAddress);

                long valueAddress = getAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize);

                Value removedValue = removeValue(valueAddress);

                // Next remove entry and shrink the partition
                // But only move if the entry we're removing isn't already
                // the last one in the partition
                if (locationOffset < entryCount - 1) {
                    // Move last entry to this entry position
                    // Key
                    long address = getAddress(locationAddress + ((entryCount - 1) * addressSize * 2));
                    putAddress(locationAddress + (locationOffset * addressSize * 2), address);

                    // Value
                    address = getAddress(locationAddress + ((entryCount - 1) * addressSize * 2) + addressSize);
                    putAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize, address);
                }

                // Move location back to start
                locationAddress -= Integer.BYTES;

                // Decrease partition counter value
                putInt(locationAddress, entryCount - 1);

                // Shrink partition memory
                locationAddress = reallocateMemory(locationAddress, Integer.BYTES + (addressSize * 2 * (entryCount - 1)));
                setLocation(partitionAddress + (offset * addressSize), locationAddress);

                itemCount--;

                return removedValue;
            }
        }

        return null;
    }

    @Override
    public void clear() {
        // For each partition..
        for (long offset = 0; offset < partitionCount; offset++) {
            // ..get partition address
            long locationAddress = locationAddress(partitionAddress + (offset * addressSize));

            // Skip if unallocated
            if (locationAddress == 0)
                continue;

            // Read how many entries we expect in this partition
            int entryCount = getInt(locationAddress);

            // Move pointer past size int
            locationAddress += Integer.BYTES;

            for (long locationOffset = 0; locationOffset < entryCount; locationOffset++) {
                long keyAddress = getAddress(locationAddress + (locationOffset * addressSize * 2));

                if (keyAddress != 0)
                    free(keyAddress);

                long valueAddress = getAddress(locationAddress + (locationOffset * addressSize * 2) + addressSize);

                if (valueAddress != 0)
                    free(valueAddress);
            }

            locationAddress -= Integer.BYTES;

            free(locationAddress);

            setLocation(partitionAddress + (offset * addressSize), 0);
        }

        // Reset item counter
        itemCount = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void putAll(Map<? extends Key, ? extends Value> m) {
        m.forEach((k, v) -> put(k, v));
    }

    @Override
    public Set<Key> keySet() {
        return new Keys(this);
    }

    @Override
    public Collection<Value> values() {
        return new Values(this);
    }

    @Override
    public Set<Entry<Key, Value>> entrySet() {
        return new EntrySet(this);
    }

    public void close() {
        if (create) {
            if (!closed) {
                closed = true;
                System.out.println("Closing map...");
                clear();
                try {
                    this.file.unmap();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
