package org.apache.commons.jexl2.internal;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.jexl2.internal.AbstractExecutor.Method;
import org.apache.commons.jexl2.internal.introspection.MethodKey;

public final class MethodExecutor extends Method {
    private final boolean isVarArgs;

    public MethodExecutor(Introspector is, Object obj, String name, Object[] args) {
        boolean z = false;
        super(obj.getClass(), discover(is, obj, name, args));
        if (this.method != null && isVarArgMethod(this.method)) {
            z = true;
        }
        this.isVarArgs = z;
    }

    public Object execute(Object o, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (this.isVarArgs) {
            Class<?>[] formal = this.method.getParameterTypes();
            int index = formal.length - 1;
            Class<?> type = formal[index].getComponentType();
            if (args.length >= index) {
                args = handleVarArg(type, index, args);
            }
        }
        if (this.method.getDeclaringClass() == ArrayListWrapper.class && o.getClass().isArray()) {
            return this.method.invoke(new ArrayListWrapper(o), args);
        }
        return this.method.invoke(o, args);
    }

    public Object tryExecute(String name, Object obj, Object[] args) {
        MethodKey tkey = new MethodKey(name, args);
        if (!this.objectClass.equals(obj.getClass()) || !tkey.equals(this.key)) {
            return TRY_FAILED;
        }
        try {
            return execute(obj, args);
        } catch (InvocationTargetException e) {
            return TRY_FAILED;
        } catch (IllegalAccessException e2) {
            return TRY_FAILED;
        }
    }

    private static Parameter discover(Introspector is, Object obj, String method, Object[] args) {
        Class<?> clazz = obj.getClass();
        MethodKey key = new MethodKey(method, args);
        java.lang.reflect.Method m = is.getMethod(clazz, key);
        if (m == null && clazz.isArray()) {
            m = is.getMethod(ArrayListWrapper.class, key);
        }
        if (m == null && (obj instanceof Class)) {
            m = is.getMethod((Class) obj, key);
        }
        return new Parameter(m, key);
    }

    protected Object[] handleVarArg(Class<?> type, int index, Object[] actual) {
        int size = actual.length - index;
        Object lastActual;
        if (size != 1) {
            lastActual = Array.newInstance(type, size);
            for (int i = 0; i < size; i++) {
                Array.set(lastActual, i, actual[index + i]);
            }
            Object[] newActual = new Object[(index + 1)];
            System.arraycopy(actual, 0, newActual, 0, index);
            newActual[index] = lastActual;
            return newActual;
        } else if (actual[index] == null) {
            return actual;
        } else {
            Class<?> aclazz = actual[index].getClass();
            if (aclazz.isArray() && aclazz.getComponentType().equals(type)) {
                return actual;
            }
            lastActual = Array.newInstance(type, 1);
            Array.set(lastActual, 0, actual[index]);
            actual[index] = lastActual;
            return actual;
        }
    }

    private static boolean isVarArgMethod(java.lang.reflect.Method m) {
        Class<?>[] formal = m.getParameterTypes();
        if (formal == null || formal.length == 0) {
            return false;
        }
        return formal[formal.length - 1].isArray();
    }
}
