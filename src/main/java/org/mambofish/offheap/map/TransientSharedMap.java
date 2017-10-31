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
public class TransientSharedMap implements Map<String, String> {

    private Map map;

    public TransientSharedMap(long size) {

        ChronicleMap<CharSequence, CharSequence> chronicleMap = null;
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

            chronicleMap = ChronicleMapBuilder
                    .of(CharSequence.class, CharSequence.class)
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
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return String.valueOf(map.get(key));
    }

    @Override
    public String put(String key, String value) {
        return String.valueOf(map.put(key, value));
    }

    @Override
    public String remove(Object key) {
        return String.valueOf(map.remove(key));
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return map.entrySet();
    }
}
