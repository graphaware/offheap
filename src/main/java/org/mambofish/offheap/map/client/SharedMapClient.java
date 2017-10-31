package org.mambofish.offheap.map.client;

import java.io.*;

import org.mambofish.offheap.map.Key;
import org.mambofish.offheap.map.SharedMap;
import org.mambofish.offheap.map.Value;

/**
 * @author vince
 */
public class SharedMapClient extends AbstractMapClient {

    public SharedMapClient(SharedMap sharedMap) {
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

        AbstractMapClient client = new SharedMapClient(new SharedMap(file, partitionCount, address));

        client.listen();
    }


}
