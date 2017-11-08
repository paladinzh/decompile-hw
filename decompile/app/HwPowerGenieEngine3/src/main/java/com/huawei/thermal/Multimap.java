package com.huawei.thermal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class Multimap<K, V> {
    private HashMap<K, List<V>> store = new HashMap();

    public List<V> getAll(K key) {
        List<V> values = (List) this.store.get(key);
        return values == null ? Collections.emptyList() : values;
    }

    public void put(K key, V val) {
        List<V> curVals = (List) this.store.get(key);
        if (curVals == null) {
            curVals = new ArrayList(3);
            this.store.put(key, curVals);
        }
        curVals.add(val);
    }

    public void removeAll(K key) {
        this.store.remove(key);
    }

    public boolean containsEntry(K key, V val) {
        List<V> curVals = (List) this.store.get(key);
        if (curVals != null && curVals.contains(val)) {
            return true;
        }
        return false;
    }
}
