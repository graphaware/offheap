import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mambofish.offheap.map.Key;
import org.mambofish.offheap.map.OffHeapMap;
import org.mambofish.offheap.map.Value;

/**
 * @author vince
 */
public class OffHeapMapTest {

    private File file = new File("test.dat");

    @Before
    public void init() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void create() {

        OffHeapMap sharedMap = new OffHeapMap(file,20, 0);
        sharedMap.clear();

        sharedMap.put(Key.of("key"), Value.of("Value"));

        Value value = sharedMap.get("key");

        assertEquals("Value", value.get());

    }

    @Test
    public void createTwo() {
        OffHeapMap sharedMap = new OffHeapMap(file,20, 0);
        sharedMap.clear();

        sharedMap.put(Key.of("one"), Value.of("one"));
        sharedMap.put(Key.of("two"), Value.of("two"));

        Value value = sharedMap.get("one");
        assertEquals("one", value.get());

        assertEquals("two", sharedMap.get("two").get());
    }

    @Test
    public void saveMany() throws IOException {

        long elapsed = - System.currentTimeMillis();
        long prev = System.currentTimeMillis();

        int count = 10_000_000;

        OffHeapMap sharedMap = new OffHeapMap(file,1_000_000, 0);
        sharedMap.clear();

        for (int i = 0; i < count; i ++) {
            sharedMap.put(Key.of("key" + i), Value.of(i));
            if ((i % 1_000_000) == 0) {
                long now = System.currentTimeMillis();
                System.out.printf("Average ms to insert per mi #%d: %d\n",
                        (i / 1_000_000), now - prev);
                prev = now;
            }

        }
        elapsed += System.currentTimeMillis();
        System.out.printf("Total elapsed ms %d\n", elapsed);
    }


}
