package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;

public final class DuckGetExecutor extends Get {
    private final Object property;

    public DuckGetExecutor(Introspector is, Class<?> clazz, Object identifier) {
        super(clazz, discover(is, clazz, identifier));
        this.property = identifier;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object obj) throws IllegalAccessException, InvocationTargetException {
        Object[] args = new Object[]{this.property};
        if (this.method != null) {
            return this.method.invoke(obj, args);
        }
        return null;
    }

    public Object tryExecute(Object obj, Object key) {
        if (obj == null || this.method == null || !this.property.equals(key) || !this.objectClass.equals(obj.getClass())) {
            return TRY_FAILED;
        }
        try {
            return this.method.invoke(obj, new Object[]{this.property});
        } catch (InvocationTargetException e) {
            return TRY_FAILED;
        } catch (IllegalAccessException e2) {
            return TRY_FAILED;
        }
    }

    private static Method discover(Introspector is, Class<?> clazz, Object identifier) {
        return is.getMethod(clazz, "get", AbstractExecutor.makeArgs(identifier));
    }
}
