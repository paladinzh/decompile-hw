package org.apache.commons.jexl2.internal;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.jexl2.internal.introspection.MethodKey;
import org.apache.commons.jexl2.introspection.JexlMethod;
import org.apache.commons.jexl2.introspection.JexlPropertyGet;
import org.apache.commons.jexl2.introspection.JexlPropertySet;

public abstract class AbstractExecutor {
    public static final Object TRY_FAILED = new Object() {
        public String toString() {
            return "tryExecute failed";
        }
    };
    protected final java.lang.reflect.Method method;
    protected final Class<?> objectClass;

    public static abstract class Get extends AbstractExecutor implements JexlPropertyGet {
        public abstract Object execute(Object obj) throws IllegalAccessException, InvocationTargetException;

        protected Get(Class<?> theClass, java.lang.reflect.Method theMethod) {
            super(theClass, theMethod);
        }

        public final Object invoke(Object obj) throws Exception {
            return execute(obj);
        }

        public final Object tryInvoke(Object obj, Object key) {
            return tryExecute(obj, key);
        }

        public Object tryExecute(Object obj, Object key) {
            return TRY_FAILED;
        }
    }

    public static abstract class Method extends AbstractExecutor implements JexlMethod {
        protected final MethodKey key;

        protected static final class Parameter {
            private final MethodKey key;
            private final java.lang.reflect.Method method;

            public Parameter(java.lang.reflect.Method m, MethodKey k) {
                this.method = m;
                this.key = k;
            }
        }

        public abstract Object execute(Object obj, Object[] objArr) throws IllegalAccessException, InvocationTargetException;

        protected Method(Class<?> c, Parameter km) {
            super(c, km.method);
            this.key = km.key;
        }

        public final Object invoke(Object obj, Object[] params) throws Exception {
            return execute(obj, params);
        }

        public final Object tryInvoke(String name, Object obj, Object[] params) {
            return tryExecute(name, obj, params);
        }

        public Object getTargetProperty() {
            return this.key;
        }

        public final Class<?> getReturnType() {
            return this.method.getReturnType();
        }

        public Object tryExecute(String name, Object obj, Object[] args) {
            return TRY_FAILED;
        }
    }

    public static abstract class Set extends AbstractExecutor implements JexlPropertySet {
        public abstract Object execute(Object obj, Object obj2) throws IllegalAccessException, InvocationTargetException;

        protected Set(Class<?> theClass, java.lang.reflect.Method theMethod) {
            super(theClass, theMethod);
        }

        public final Object invoke(Object obj, Object arg) throws Exception {
            return execute(obj, arg);
        }

        public final Object tryInvoke(Object obj, Object key, Object value) {
            return tryExecute(obj, key, value);
        }

        public Object tryExecute(Object obj, Object key, Object value) {
            return TRY_FAILED;
        }
    }

    static java.lang.reflect.Method initMarker(Class<?> clazz, String name, Class<?>... parms) {
        try {
            return clazz.getMethod(name, parms);
        } catch (Exception xnever) {
            throw new Error(xnever);
        }
    }

    static Object[] makeArgs(Object... args) {
        return args;
    }

    protected AbstractExecutor(Class<?> theClass, java.lang.reflect.Method theMethod) {
        this.objectClass = theClass;
        this.method = theMethod;
    }

    public boolean equals(Object obj) {
        if (this != obj) {
            if (!(obj instanceof AbstractExecutor)) {
                return false;
            }
            if (!equals((AbstractExecutor) obj)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return this.method.hashCode();
    }

    public boolean equals(AbstractExecutor arg) {
        if (!getClass().equals(arg.getClass()) || !getMethod().equals(arg.getMethod()) || !getTargetClass().equals(arg.getTargetClass())) {
            return false;
        }
        Object lhsp = getTargetProperty();
        Object rhsp = arg.getTargetProperty();
        if (lhsp == null && rhsp == null) {
            return true;
        }
        if (lhsp == null || rhsp == null) {
            return false;
        }
        return lhsp.equals(rhsp);
    }

    public final boolean isAlive() {
        return this.method != null;
    }

    public boolean isCacheable() {
        return this.method != null;
    }

    public final java.lang.reflect.Method getMethod() {
        return this.method;
    }

    public final Class<?> getTargetClass() {
        return this.objectClass;
    }

    public Object getTargetProperty() {
        return null;
    }

    public final boolean tryFailed(Object exec) {
        return exec == TRY_FAILED;
    }
}
