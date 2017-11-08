package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public interface Multimap<K, V> {
    Map<K, Collection<V>> asMap();

    void clear();

    boolean containsEntry(@Nullable Object obj, @Nullable Object obj2);

    boolean remove(@Nullable Object obj, @Nullable Object obj2);

    int size();
}
