package org.apache.commons.jexl2.internal;

import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;

public final class MapGetExecutor extends Get {
    private static final Method MAP_GET = AbstractExecutor.initMarker(Map.class, "get", Object.class);
    private final Object property;

    public MapGetExecutor(Introspector is, Class<?> clazz, Object key) {
        super(clazz, discover(clazz));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj) {
        return ((Map) obj).get(this.property);
    }

    public Object tryExecute(Object obj, Object key) {
        if (!(obj == null || this.method == null || !this.objectClass.equals(obj.getClass()))) {
            if (key == null || this.property.getClass().equals(key.getClass())) {
                return ((Map) obj).get(key);
            }
        }
        return TRY_FAILED;
    }

    static Method discover(Class<?> clazz) {
        return !Map.class.isAssignableFrom(clazz) ? null : MAP_GET;
    }
}
