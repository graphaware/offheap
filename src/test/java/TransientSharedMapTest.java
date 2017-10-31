import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mambofish.offheap.map.Key;
import org.mambofish.offheap.map.TransientSharedMap;
import org.mambofish.offheap.map.Value;

/**
 * @author vince
 */
public class TransientSharedMapTest {

    @Test
    public void save() {

        TransientSharedMap transientSharedMap = new TransientSharedMap(2048);

        String k = "key";

        Map<String, Object> v = new HashMap<>();

        v.put("x", "x-value");
        v.put("y", "y-value");

        transientSharedMap.put(Key.of(k), Value.of(v));

        Value found = transientSharedMap.get(Key.of(k));

        assertEquals(v, found.get());

    }
}
