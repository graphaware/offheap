# JVM off-heap maps and cache library

The components in the OffHeap library allow processes running in different JVMs on the same machine to share data using off-heap memory structures.

---

## SharedMapCacheManager

A Spring-compatible CacheManager:

```
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String[] CACHE_NAMES = {"users", "profiles"};

    @Bean
    public CacheManager cacheManager() {
        return new SharedMapCacheManager(CACHE_NAMES);
    }

}
```

---

## SharedOffHeapMap

A shared off heap memory map that acts as the underlying store for a `SharedMapCacheManager`. 

This map allows multiple processes (e.g in different JVMs) running on a single machine to access a common map via shared memory. The map is persisted to disk via a memory-mapped file.

The map stores all its entries off-heap, which eliminates GC pressure and allows the map's size to be larger than the amount of available physical memory. Note that for best performance, the map should not be larger than twice the amount of main memory.

To use a `SharedOffHeapMap`, clients must agree on the file name the map uses, and the max number of entries it should hold:


```
// creates a named file in the temp directory, which will persist until the server is rebooted
Map<Key, Value> sharedMap = new SharedOffHeapMap(new File(System.getProperty("java.io.tmpdir"), "test.dat"), 10_000);
``` 

*Warning: this map will not grow*. You need to know approximately the number of entries you want to hold in it before creating it. It is therefore ideal for using as a shared cache implementation for processes running on the same machine. 

If you need an off-heap map that can grow arbitrarily, see the next section

---

## OffHeapMap

A map that stores all its entries off-heap. The entries in this map cannot be shared with other processes, but unlike the `SharedOffHeapMap` this map will grow as required.

An `OffHeapMap` is created with a number of partitions. For optimum performance, the number of partitions should be around 10% of the maximum number of entries the map is expected to hold:

```
// create a map with 10k partitions, which is efficient for storing approximately 100k Key-Value pairs.
Map<Key, Value> offHeapMap = new OffHeapMap(10_000);
```

---

## Constraints

In both maps keys must be Strings, and null keys are not supported. Values can be any type, and again null values are not permitted. 

---

## Key and Value types

Both maps implement `Map<Key, Value>`, where Key and Value are wrappers for your actual keys and values. The Key and Value classes perform serialisation and de-serialising of the keys and values in the map, so you don't need to. 

_To add an entry to the map_:
 
```
String key = "friends";
List<String> value = Arrays.asList("phoebe", "rachel", "monica", "joey", "chandler", "ross");
map.put(Key.of(key), Value.of(value))
```

However, if your key and value objects are already byte[] arrays, you do not need to create Key and Value wrappers.

_To retrieve an entry from the map_:

```
List<String> friends = map.get(Key.of("friends")).get();
```

---
## Licensing
OffHeap is licensed under the Apache 2.0 License. You should have received a copy of the license with this code. 

---

## Acknowledgements
Inspiration, ideas and some code for this library have come from a number of open-source projects, including:

- https://github.com/cfelde/BinaryOffheapHashMap
- https://github.com/OpenHFT/Chronicle-Map 

The SharedOffHeapMap currently uses a Chronicle map under the hood. 

---

## Using the offheap library in your own projects

Releases are published on Maven Central. The latest release version is 1.0.0

```
# maven example

<dependencies>
    <dependency>
        <groupId>com.graphaware.offheap</groupId>
        <artifactId>offheap</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

```
