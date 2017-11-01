package org.mambofish.offheap.map;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author vince
 */
public class SharedOffHeapMap implements Map<Key, Value> {

    private Map map;

    public SharedOffHeapMap(File file, long size) {
        map(file, size);
    }

    public SharedOffHeapMap(long size) {

        ChronicleMap<byte[], byte[]> chronicleMap = null;
        String filename = System.getProperty("file");
        File file = null;
        try {
            if (filename == null) {
                file = File.createTempFile("epsilon-shared", ".map");
                file.deleteOnExit();
                System.out.println("Using transient map: " + file.getAbsolutePath());
            } else {
                file = new File(filename);
                System.out.println("Using persistent map: " + file.getAbsolutePath());
            }
            map(file, size);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        this.map = chronicleMap;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(((Key) key).bytes());
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(((Value) value).bytes());
    }

    @Override
    public Value get(Object key) {
        return Value.of(map.get(((Key) key).bytes()));
    }

    @Override
    public Value put(Key key, Value value) {
        return Value.of(map.put(key.bytes(), value.bytes()));
    }

    @Override
    public Value remove(Object key) {
        return Value.of(map.remove(((Key) key).bytes()));
    }

    @Override
    public void putAll(@NotNull Map<? extends Key, ? extends Value> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<Key> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<Value> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<Key, Value>> entrySet() {
        return map.entrySet();
    }

    private void map(File file, long size) {

        ChronicleMap<byte[], byte[]> chronicleMap = null;

        try {
            chronicleMap = ChronicleMapBuilder
                    .of(byte[].class, byte[].class)
                    .entries(size)
                    .averageKeySize(10.0d)
                    .averageValueSize(20.0d)
                    .checksumEntries(false)
                    .createPersistedTo(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        this.map = chronicleMap;

    }
}
