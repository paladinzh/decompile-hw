package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;

public final class PropertyGetExecutor extends Get {
    private static final Object[] EMPTY_PARAMS = new Object[0];
    private final String property;

    public PropertyGetExecutor(Introspector is, Class<?> clazz, String identifier) {
        super(clazz, discover(is, clazz, identifier));
        this.property = identifier;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object o) throws IllegalAccessException, InvocationTargetException {
        return this.method != null ? this.method.invoke(o, (Object[]) null) : null;
    }

    public Object tryExecute(Object o, Object identifier) {
        if (o == null || this.method == null || !this.property.equals(identifier) || !this.objectClass.equals(o.getClass())) {
            return TRY_FAILED;
        }
        try {
            return this.method.invoke(o, (Object[]) null);
        } catch (InvocationTargetException e) {
            return TRY_FAILED;
        } catch (IllegalAccessException e2) {
            return TRY_FAILED;
        }
    }

    static Method discover(Introspector is, Class<?> clazz, String property) {
        return discoverGet(is, "get", clazz, property);
    }

    static Method discoverGet(Introspector is, String which, Class<?> clazz, String property) {
        int start = which.length();
        StringBuilder sb = new StringBuilder(which);
        sb.append(property);
        char c = sb.charAt(start);
        sb.setCharAt(start, Character.toUpperCase(c));
        Method method = is.getMethod(clazz, sb.toString(), EMPTY_PARAMS);
        if (method != null) {
            return method;
        }
        sb.setCharAt(start, Character.toLowerCase(c));
        return is.getMethod(clazz, sb.toString(), EMPTY_PARAMS);
    }
}
