package com.google.common.collect;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;

abstract class AbstractNavigableMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V> {

    private final class DescendingMap extends DescendingMap<K, V> {
        private DescendingMap() {
        }

        NavigableMap<K, V> forward() {
            return AbstractNavigableMap.this;
        }

        Iterator<Entry<K, V>> entryIterator() {
            return AbstractNavigableMap.this.descendingEntryIterator();
        }
    }

    abstract Iterator<Entry<K, V>> descendingEntryIterator();

    abstract Iterator<Entry<K, V>> entryIterator();

    @Nullable
    public abstract V get(@Nullable Object obj);

    public abstract int size();

    AbstractNavigableMap() {
    }

    @Nullable
    public Entry<K, V> firstEntry() {
        return (Entry) Iterators.getNext(entryIterator(), null);
    }

    @Nullable
    public Entry<K, V> lastEntry() {
        return (Entry) Iterators.getNext(descendingEntryIterator(), null);
    }

    @Nullable
    public Entry<K, V> pollFirstEntry() {
        return (Entry) Iterators.pollNext(entryIterator());
    }

    @Nullable
    public Entry<K, V> pollLastEntry() {
        return (Entry) Iterators.pollNext(descendingEntryIterator());
    }

    public K firstKey() {
        Entry<K, V> entry = firstEntry();
        if (entry != null) {
            return entry.getKey();
        }
        throw new NoSuchElementException();
    }

    public K lastKey() {
        Entry<K, V> entry = lastEntry();
        if (entry != null) {
            return entry.getKey();
        }
        throw new NoSuchElementException();
    }

    @Nullable
    public Entry<K, V> lowerEntry(K key) {
        return headMap(key, false).lastEntry();
    }

    @Nullable
    public Entry<K, V> floorEntry(K key) {
        return headMap(key, true).lastEntry();
    }

    @Nullable
    public Entry<K, V> ceilingEntry(K key) {
        return tailMap(key, true).firstEntry();
    }

    @Nullable
    public Entry<K, V> higherEntry(K key) {
        return tailMap(key, false).firstEntry();
    }

    public K lowerKey(K key) {
        return Maps.keyOrNull(lowerEntry(key));
    }

    public K floorKey(K key) {
        return Maps.keyOrNull(floorEntry(key));
    }

    public K ceilingKey(K key) {
        return Maps.keyOrNull(ceilingEntry(key));
    }

    public K higherKey(K key) {
        return Maps.keyOrNull(higherEntry(key));
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    public SortedMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    public NavigableSet<K> navigableKeySet() {
        return new NavigableKeySet(this);
    }

    public Set<K> keySet() {
        return navigableKeySet();
    }

    public Set<Entry<K, V>> entrySet() {
        return new EntrySet<K, V>() {
            Map<K, V> map() {
                return AbstractNavigableMap.this;
            }

            public Iterator<Entry<K, V>> iterator() {
                return AbstractNavigableMap.this.entryIterator();
            }
        };
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    public NavigableMap<K, V> descendingMap() {
        return new DescendingMap();
    }
}
