import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import com.graphaware.offheap.map.Key;
import com.graphaware.offheap.map.SharedOffHeapMap;
import com.graphaware.offheap.map.Value;

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

    @Test
    public void saveToSpecifiedFile() {

        File file = new File("sharedmap.map");
        file.deleteOnExit();

        SharedOffHeapMap sharedOffHeapMap = new SharedOffHeapMap(file, 2048);

        String k = "key";

        Map<String, Object> v = new HashMap<>();

        v.put("x", "x-value");
        v.put("y", "y-value");

        sharedOffHeapMap.put(Key.of(k), Value.of(v));

        Value found = sharedOffHeapMap.get(Key.of(k));

        assertEquals(v, found.get());

        Map<String, Object> retrieved = (Map<String, Object>) found.get();
        assertEquals("x-value", retrieved.get("x"));
        assertEquals("y-value", retrieved.get("y"));



    }

}
