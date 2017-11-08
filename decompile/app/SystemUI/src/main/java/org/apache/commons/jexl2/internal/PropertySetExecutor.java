package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Set;

public final class PropertySetExecutor extends Set {
    private final String property;

    public PropertySetExecutor(Introspector is, Class<?> clazz, String identifier, Object arg) {
        super(clazz, discover(is, clazz, identifier, arg));
        this.property = identifier;
    }

    public Object getTargetProperty() {
        return this.property;
    }

    public Object execute(Object o, Object arg) throws IllegalAccessException, InvocationTargetException {
        Object[] pargs = new Object[]{arg};
        if (this.method != null) {
            this.method.invoke(o, pargs);
        }
        return arg;
    }

    public Object tryExecute(Object o, Object identifier, Object arg) {
        if (o != null && this.method != null && this.property.equals(identifier) && this.objectClass.equals(o.getClass())) {
            if (arg == null || this.method.getParameterTypes()[0].equals(arg.getClass())) {
                try {
                    return execute(o, arg);
                } catch (InvocationTargetException e) {
                    return TRY_FAILED;
                } catch (IllegalAccessException e2) {
                    return TRY_FAILED;
                }
            }
        }
        return TRY_FAILED;
    }

    private static Method discover(Introspector is, Class<?> clazz, String property, Object arg) {
        Object[] params = new Object[]{arg};
        StringBuilder sb = new StringBuilder("set");
        sb.append(property);
        char c = sb.charAt(3);
        sb.setCharAt(3, Character.toUpperCase(c));
        Method method = is.getMethod(clazz, sb.toString(), params);
        if (method != null) {
            return method;
        }
        sb.setCharAt(3, Character.toLowerCase(c));
        return is.getMethod(clazz, sb.toString(), params);
    }
}
