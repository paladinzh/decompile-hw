package org.apache.commons.jexl2.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;

public final class ListGetExecutor extends Get {
    private static final Method ARRAY_GET = AbstractExecutor.initMarker(Array.class, "get", Object.class, Integer.TYPE);
    private static final Method LIST_GET = AbstractExecutor.initMarker(List.class, "get", Integer.TYPE);
    private final Integer property;

    public ListGetExecutor(Introspector is, Class<?> clazz, Integer key) {
        super(clazz, discover(clazz));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj) {
        if (this.method != ARRAY_GET) {
            return ((List) obj).get(this.property.intValue());
        }
        return Array.get(obj, this.property.intValue());
    }

    public Object tryExecute(Object obj, Object key) {
        if (obj == null || this.method == null || !this.objectClass.equals(obj.getClass()) || !(key instanceof Integer)) {
            return TRY_FAILED;
        }
        if (this.method != ARRAY_GET) {
            return ((List) obj).get(((Integer) key).intValue());
        }
        return Array.get(obj, ((Integer) key).intValue());
    }

    static Method discover(Class<?> clazz) {
        if (clazz.isArray()) {
            return ARRAY_GET;
        }
        if (List.class.isAssignableFrom(clazz)) {
            return LIST_GET;
        }
        return null;
    }
}
