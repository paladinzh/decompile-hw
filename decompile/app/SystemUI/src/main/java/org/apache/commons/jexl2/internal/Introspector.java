package org.apache.commons.jexl2.internal;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.apache.commons.jexl2.internal.AbstractExecutor.Get;
import org.apache.commons.jexl2.internal.AbstractExecutor.Set;
import org.apache.commons.jexl2.internal.introspection.IntrospectorBase;
import org.apache.commons.jexl2.internal.introspection.MethodKey;
import org.apache.commons.logging.Log;

public class Introspector {
    private volatile SoftReference<IntrospectorBase> ref = new SoftReference(null);
    protected final Log rlog;

    protected Introspector(Log log) {
        this.rlog = log;
    }

    protected Integer toInteger(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Number) {
            return Integer.valueOf(((Number) arg).intValue());
        }
        try {
            return Integer.valueOf(arg.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected String toString(Object arg) {
        return arg != null ? arg.toString() : null;
    }

    protected final IntrospectorBase base() {
        Throwable th;
        IntrospectorBase intro = (IntrospectorBase) this.ref.get();
        if (intro == null) {
            synchronized (this) {
                try {
                    intro = (IntrospectorBase) this.ref.get();
                    if (intro == null) {
                        IntrospectorBase intro2 = new IntrospectorBase(this.rlog);
                        try {
                            this.ref = new SoftReference(intro2);
                            intro = intro2;
                        } catch (Throwable th2) {
                            th = th2;
                            intro = intro2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        return intro;
    }

    public Class<?> getClassByName(String className) {
        return base().getClassByName(className);
    }

    public final Field getField(Class<?> c, String key) {
        return base().getField(c, key);
    }

    public final Method getMethod(Class<?> c, String name, Object[] params) {
        return base().getMethod(c, new MethodKey(name, params));
    }

    public final Method getMethod(Class<?> c, MethodKey key) {
        return base().getMethod(c, key);
    }

    public final Method[] getMethods(Class<?> c, String methodName) {
        return base().getMethods(c, methodName);
    }

    public final Constructor<?> getConstructor(Object ctorHandle, Object[] args) {
        String className;
        Class<?> clazz = null;
        if (ctorHandle instanceof Class) {
            clazz = (Class) ctorHandle;
            className = clazz.getName();
        } else if (ctorHandle == null) {
            return null;
        } else {
            className = ctorHandle.toString();
        }
        return base().getConstructor(clazz, new MethodKey(className, args));
    }

    public final AbstractExecutor.Method getMethodExecutor(Object obj, String name, Object[] args) {
        AbstractExecutor.Method me = new MethodExecutor(this, obj, name, args);
        return !me.isAlive() ? null : me;
    }

    public final Get getGetExecutor(Object obj, Object identifier) {
        Get executor;
        Class<?> claz = obj.getClass();
        String property = toString(identifier);
        if (property != null) {
            executor = new PropertyGetExecutor(this, claz, property);
            if (executor.isAlive()) {
                return executor;
            }
            executor = new BooleanGetExecutor(this, claz, property);
            if (executor.isAlive()) {
                return executor;
            }
        }
        executor = new MapGetExecutor(this, claz, identifier);
        if (executor.isAlive()) {
            return executor;
        }
        Integer index = toInteger(identifier);
        if (index != null) {
            executor = new ListGetExecutor(this, claz, index);
            if (executor.isAlive()) {
                return executor;
            }
        }
        executor = new DuckGetExecutor(this, claz, identifier);
        if (executor.isAlive()) {
            return executor;
        }
        executor = new DuckGetExecutor(this, claz, property);
        if (executor.isAlive()) {
            return executor;
        }
        return null;
    }

    public final Set getSetExecutor(Object obj, Object identifier, Object arg) {
        Set executor;
        Class<?> claz = obj.getClass();
        String property = toString(identifier);
        if (property != null) {
            executor = new PropertySetExecutor(this, claz, property, arg);
            if (executor.isAlive()) {
                return executor;
            }
        }
        executor = new MapSetExecutor(this, claz, identifier, arg);
        if (executor.isAlive()) {
            return executor;
        }
        Integer index = toInteger(identifier);
        if (index != null) {
            executor = new ListSetExecutor(this, claz, index, arg);
            if (executor.isAlive()) {
                return executor;
            }
        }
        executor = new DuckSetExecutor(this, claz, identifier, arg);
        if (executor.isAlive()) {
            return executor;
        }
        executor = new DuckSetExecutor(this, claz, property, arg);
        if (executor.isAlive()) {
            return executor;
        }
        return null;
    }
}
