package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.jexl2.internal.AbstractExecutor.Set;

public final class MapSetExecutor extends Set {
    private static final Method MAP_SET = AbstractExecutor.initMarker(Map.class, "put", Object.class, Object.class);
    private final Object property;

    public MapSetExecutor(Introspector is, Class<?> clazz, Object key, Object value) {
        super(clazz, discover(clazz));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj, Object value) throws IllegalAccessException, InvocationTargetException {
        ((Map) obj).put(this.property, value);
        return value;
    }

    public Object tryExecute(Object obj, Object key, Object value) {
        if (!(obj == null || this.method == null || !this.objectClass.equals(obj.getClass()))) {
            if (key == null || this.property.getClass().equals(key.getClass())) {
                ((Map) obj).put(key, value);
                return value;
            }
        }
        return TRY_FAILED;
    }

    static Method discover(Class<?> clazz) {
        return !Map.class.isAssignableFrom(clazz) ? null : MAP_SET;
    }
}
