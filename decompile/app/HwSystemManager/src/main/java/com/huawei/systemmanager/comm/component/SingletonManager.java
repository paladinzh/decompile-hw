package com.huawei.systemmanager.comm.component;

import com.huawei.systemmanager.comm.collections.HsmCollections;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class SingletonManager<T extends Singletoner> {
    private final Map<Long, WeakReference<T>> sSingletonMaps = HsmCollections.newArrayMap();

    public interface Singletoner {
        long getId();
    }

    protected abstract T onCreate(long j);

    public synchronized T createNewInstance() {
        T instance;
        long randomId = new SecureRandom().nextLong();
        instance = onCreate(randomId);
        if (instance.getId() != randomId) {
            throw new IllegalArgumentException("createNewInstance, id not correct!!");
        }
        putSingleton(instance);
        return instance;
    }

    private synchronized void putSingleton(T singleton) {
        checkMap();
        this.sSingletonMaps.put(Long.valueOf(singleton.getId()), new WeakReference(singleton));
    }

    public synchronized T getSingleton(long id) {
        checkMap();
        WeakReference<T> ref = (WeakReference) this.sSingletonMaps.get(Long.valueOf(id));
        if (ref == null) {
            return null;
        }
        return (Singletoner) ref.get();
    }

    private synchronized void checkMap() {
        Iterator<Entry<Long, WeakReference<T>>> it = this.sSingletonMaps.entrySet().iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = (WeakReference) ((Entry) it.next()).getValue();
            if (ref == null) {
                it.remove();
            } else if (ref.get() == null) {
                it.remove();
            }
        }
    }
}
