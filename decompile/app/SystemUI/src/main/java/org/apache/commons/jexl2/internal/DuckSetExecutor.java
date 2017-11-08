package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Set;

public final class DuckSetExecutor extends Set {
    private final Object property;

    public DuckSetExecutor(Introspector is, Class<?> clazz, Object key, Object value) {
        super(clazz, discover(is, clazz, key, value));
        this.property = key;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj, Object value) throws IllegalAccessException, InvocationTargetException {
        Object[] pargs = new Object[]{this.property, value};
        if (this.method != null) {
            this.method.invoke(obj, pargs);
        }
        return value;
    }

    public Object tryExecute(Object obj, Object key, Object value) {
        if (obj == null || this.method == null || !this.property.equals(key) || !this.objectClass.equals(obj.getClass())) {
            return TRY_FAILED;
        }
        try {
            this.method.invoke(obj, new Object[]{this.property, value});
            return value;
        } catch (InvocationTargetException e) {
            return TRY_FAILED;
        } catch (IllegalAccessException e2) {
            return TRY_FAILED;
        }
    }

    private static Method discover(Introspector is, Class<?> clazz, Object key, Object value) {
        return is.getMethod(clazz, "set", AbstractExecutor.makeArgs(key, value));
    }
}
