package org.apache.commons.jexl2.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.jexl2.internal.AbstractExecutor.Set;

public final class ListSetExecutor extends Set {
    private static final Method ARRAY_SET = AbstractExecutor.initMarker(Array.class, "set", Object.class, Integer.TYPE, Object.class);
    private static final Method LIST_SET = AbstractExecutor.initMarker(List.class, "set", Integer.TYPE, Object.class);
    private final Integer property;

    public ListSetExecutor(Introspector is, Class<?> clazz, Integer key, Object value) {
        super(clazz, discover(clazz));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj, Object value) {
        if (this.method != ARRAY_SET) {
            ((List) obj).set(this.property.intValue(), value);
        } else {
            Array.set(obj, this.property.intValue(), value);
        }
        return value;
    }

    public Object tryExecute(Object obj, Object key, Object value) {
        if (obj == null || this.method == null || !this.objectClass.equals(obj.getClass()) || !(key instanceof Integer)) {
            return TRY_FAILED;
        }
        if (this.method != ARRAY_SET) {
            ((List) obj).set(((Integer) key).intValue(), value);
        } else {
            Array.set(obj, ((Integer) key).intValue(), value);
        }
        return value;
    }

    static Method discover(Class<?> clazz) {
        if (clazz.isArray()) {
            return ARRAY_SET;
        }
        if (List.class.isAssignableFrom(clazz)) {
            return LIST_SET;
        }
        return null;
    }
}
