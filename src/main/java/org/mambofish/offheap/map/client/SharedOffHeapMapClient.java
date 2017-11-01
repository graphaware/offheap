package org.mambofish.offheap.map.client;

import org.mambofish.offheap.map.SharedOffHeapMap;

/**
 * @author vince
 */
public class SharedOffHeapMapClient extends AbstractMapClient {

    public SharedOffHeapMapClient(SharedOffHeapMap sharedMap) {
        map = sharedMap;
    }

    @Override
    public Object key(String k) {
        return k;
    }

    @Override
    public Object value(Object v) {
        return v;
    }

    @Override
    public Object get(Object o) {
        return o;
    }
    
    public static void main(String[] args) {

        int size = Integer.parseInt(System.getProperty("size", "10000"));

        AbstractMapClient client = new SharedOffHeapMapClient(new SharedOffHeapMap(size));

        client.listen();

    }
}
