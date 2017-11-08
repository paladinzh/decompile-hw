package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import java.util.Comparator;
import java.util.Map.Entry;
import javax.annotation.Nullable;

@GwtCompatible(emulated = true)
final class EmptyImmutableSortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private final transient ImmutableSortedSet<K> keySet;

    EmptyImmutableSortedMap(Comparator<? super K> comparator) {
        this.keySet = ImmutableSortedSet.emptySet(comparator);
    }

    EmptyImmutableSortedMap(Comparator<? super K> comparator, ImmutableSortedMap<K, V> descendingMap) {
        super(descendingMap);
        this.keySet = ImmutableSortedSet.emptySet(comparator);
    }

    public V get(@Nullable Object key) {
        return null;
    }

    public ImmutableSortedSet<K> keySet() {
        return this.keySet;
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }

    public ImmutableCollection<V> values() {
        return ImmutableList.of();
    }

    public String toString() {
        return "{}";
    }

    boolean isPartialView() {
        return false;
    }

    public ImmutableSet<Entry<K, V>> entrySet() {
        return ImmutableSet.of();
    }

    ImmutableSet<Entry<K, V>> createEntrySet() {
        throw new AssertionError("should never be called");
    }

    public ImmutableSortedMap<K, V> headMap(K toKey, boolean inclusive) {
        Preconditions.checkNotNull(toKey);
        return this;
    }

    public ImmutableSortedMap<K, V> tailMap(K fromKey, boolean inclusive) {
        Preconditions.checkNotNull(fromKey);
        return this;
    }

    ImmutableSortedMap<K, V> createDescendingMap() {
        return new EmptyImmutableSortedMap(Ordering.from(comparator()).reverse(), this);
    }
}
