package org.mambofish.offheap.map.client;

import org.mambofish.offheap.map.TransientSharedMap;

/**
 * @author vince
 */
public class TransientSharedMapClient extends AbstractMapClient {

    public TransientSharedMapClient(TransientSharedMap sharedMap) {
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

        AbstractMapClient client = new TransientSharedMapClient(new TransientSharedMap(size));

        client.listen();

    }
}
