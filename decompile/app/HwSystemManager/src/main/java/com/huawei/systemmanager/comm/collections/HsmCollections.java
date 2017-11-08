package com.huawei.systemmanager.comm.collections;

import android.annotation.TargetApi;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@TargetApi(19)
public class HsmCollections {
    public static <K, V> ArrayMap<K, V> newArrayMap() {
        return new ArrayMap();
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap();
    }

    @TargetApi(19)
    public static <K, V> ArrayMap<K, V> newArrayMap(ArrayMap<K, V> map) {
        return new ArrayMap(map);
    }

    public static <K, V> ArrayMap<K, V> newArrayMapWithCapacity(int initialMapSize) {
        return new ArrayMap(initialMapSize);
    }

    public static <E> ArraySet<E> newArraySet() {
        return new ArraySet();
    }

    public static <E> ArrayList<E> newArrayList(Collection<E> collection) {
        Preconditions.checkNotNull(collection);
        ArrayList<E> list = new ArrayList(collection.size());
        list.addAll(collection);
        return list;
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList(elements.length);
        for (E e : elements) {
            list.add(e);
        }
        return list;
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList();
    }

    public static <E> ArrayList<E> newArrayListWithCapacity(int initialMapSize) {
        return new ArrayList(initialMapSize);
    }

    public static boolean isEmpty(Collection<?> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    public static boolean isMapEmpty(Map<?, ?> map) {
        if (map == null) {
            return true;
        }
        return map.isEmpty();
    }

    public static <T> boolean isArrayEmpty(T[] array) {
        if (array == null || array.length <= 0) {
            return true;
        }
        return false;
    }
}
