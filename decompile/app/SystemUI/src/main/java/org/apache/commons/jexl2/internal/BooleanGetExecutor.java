package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;

public final class BooleanGetExecutor extends Get {
    private final String property;

    public BooleanGetExecutor(Introspector is, Class<?> clazz, String key) {
        super(clazz, discover(is, clazz, key));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj) throws IllegalAccessException, InvocationTargetException {
        return this.method != null ? this.method.invoke(obj, (Object[]) null) : null;
    }

    public Object tryExecute(Object obj, Object key) {
        if (obj == null || this.method == null || !this.property.equals(key) || !this.objectClass.equals(obj.getClass())) {
            return TRY_FAILED;
        }
        try {
            return this.method.invoke(obj, (Object[]) null);
        } catch (InvocationTargetException e) {
            return TRY_FAILED;
        } catch (IllegalAccessException e2) {
            return TRY_FAILED;
        }
    }

    static Method discover(Introspector is, Class<?> clazz, String property) {
        Method m = PropertyGetExecutor.discoverGet(is, "is", clazz, property);
        if (m != null) {
            if (m.getReturnType() == Boolean.TYPE) {
                return m;
            }
        }
        return null;
    }
}
