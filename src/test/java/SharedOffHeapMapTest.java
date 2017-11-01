import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mambofish.offheap.map.Key;
import org.mambofish.offheap.map.SharedOffHeapMap;
import org.mambofish.offheap.map.Value;

/**
 * @author vince
 */
public class SharedOffHeapMapTest {

    @Test
    public void save() {

        SharedOffHeapMap sharedOffHeapMap = new SharedOffHeapMap(2048);

        String k = "key";

        Map<String, Object> v = new HashMap<>();

        v.put("x", "x-value");
        v.put("y", "y-value");

        sharedOffHeapMap.put(Key.of(k), Value.of(v));

        Value found = sharedOffHeapMap.get(Key.of(k));

        assertEquals(v, found.get());

    }
}
