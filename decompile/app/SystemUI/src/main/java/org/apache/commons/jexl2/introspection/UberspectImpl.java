package org.apache.commons.jexl2.introspection;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.JexlInfo;
import org.apache.commons.jexl2.internal.AbstractExecutor;
import org.apache.commons.jexl2.internal.ArrayIterator;
import org.apache.commons.jexl2.internal.EnumerationIterator;
import org.apache.commons.jexl2.internal.Introspector;
import org.apache.commons.jexl2.internal.introspection.MethodKey;
import org.apache.commons.logging.Log;

public class UberspectImpl extends Introspector implements Uberspect {
    public static final Object TRY_FAILED = AbstractExecutor.TRY_FAILED;

    private final class ConstructorMethod implements JexlMethod {
        private final Constructor<?> ctor;

        private ConstructorMethod(Constructor<?> theCtor) {
            this.ctor = theCtor;
        }

        public Object invoke(Object obj, Object[] params) throws Exception {
            Class<?> clazz;
            if (obj instanceof Class) {
                clazz = (Class) obj;
            } else if (obj == null) {
                clazz = this.ctor.getDeclaringClass();
            } else {
                clazz = UberspectImpl.this.getClassByName(obj.toString());
            }
            if (clazz.equals(this.ctor.getDeclaringClass())) {
                return this.ctor.newInstance(params);
            }
            throw new IntrospectionException("constructor resolution error");
        }

        public Object tryInvoke(String name, Object obj, Object[] params) {
            Class<?> clazz;
            if (obj instanceof Class) {
                clazz = (Class) obj;
            } else if (obj == null) {
                clazz = this.ctor.getDeclaringClass();
            } else {
                clazz = UberspectImpl.this.getClassByName(obj.toString());
            }
            if (clazz.equals(this.ctor.getDeclaringClass())) {
                if (name == null || name.equals(clazz.getName())) {
                    try {
                        return this.ctor.newInstance(params);
                    } catch (InstantiationException e) {
                        return UberspectImpl.TRY_FAILED;
                    } catch (IllegalAccessException e2) {
                        return UberspectImpl.TRY_FAILED;
                    } catch (IllegalArgumentException e3) {
                        return UberspectImpl.TRY_FAILED;
                    } catch (InvocationTargetException e4) {
                        return UberspectImpl.TRY_FAILED;
                    }
                }
            }
            return UberspectImpl.TRY_FAILED;
        }

        public boolean tryFailed(Object rval) {
            return rval == UberspectImpl.TRY_FAILED;
        }

        public boolean isCacheable() {
            return true;
        }

        public Class<?> getReturnType() {
            return this.ctor.getDeclaringClass();
        }
    }

    @Deprecated
    public static final class FieldPropertyGet implements JexlPropertyGet {
        private final Field field;

        public FieldPropertyGet(Field theField) {
            this.field = theField;
        }

        public Object invoke(Object obj) throws Exception {
            return this.field.get(obj);
        }

        public Object tryInvoke(Object obj, Object key) {
            if (!obj.getClass().equals(this.field.getDeclaringClass()) || !key.equals(this.field.getName())) {
                return UberspectImpl.TRY_FAILED;
            }
            try {
                return this.field.get(obj);
            } catch (IllegalAccessException e) {
                return UberspectImpl.TRY_FAILED;
            }
        }

        public boolean tryFailed(Object rval) {
            return rval == UberspectImpl.TRY_FAILED;
        }

        public boolean isCacheable() {
            return true;
        }
    }

    @Deprecated
    public static final class FieldPropertySet implements JexlPropertySet {
        private final Field field;

        public FieldPropertySet(Field theField) {
            this.field = theField;
        }

        public Object invoke(Object obj, Object arg) throws Exception {
            this.field.set(obj, arg);
            return arg;
        }

        public Object tryInvoke(Object obj, Object key, Object value) {
            if (obj.getClass().equals(this.field.getDeclaringClass()) && key.equals(this.field.getName())) {
                if (value == null || MethodKey.isInvocationConvertible(this.field.getType(), value.getClass(), false)) {
                    try {
                        this.field.set(obj, value);
                        return value;
                    } catch (IllegalAccessException e) {
                        return UberspectImpl.TRY_FAILED;
                    }
                }
            }
            return UberspectImpl.TRY_FAILED;
        }

        public boolean tryFailed(Object rval) {
            return rval == UberspectImpl.TRY_FAILED;
        }

        public boolean isCacheable() {
            return true;
        }
    }

    public static final class IndexedContainer {
        private final Object object;
        private final IndexedType type;

        private IndexedContainer(IndexedType theType, Object theObject) {
            this.type = theType;
            this.object = theObject;
        }
    }

    private static final class IndexedType implements JexlPropertyGet {
        private final Class<?> clazz;
        private final String container;
        private final Method[] getters;
        private final Method[] setters;

        IndexedType(String name, Class<?> c, Method[] gets, Method[] sets) {
            this.container = name;
            this.clazz = c;
            this.getters = gets;
            this.setters = sets;
        }

        public Object invoke(Object obj) throws Exception {
            if (obj != null && this.clazz.equals(obj.getClass())) {
                return new IndexedContainer(this, obj);
            }
            throw new IntrospectionException("property resolution error");
        }

        public Object tryInvoke(Object obj, Object key) {
            if (obj != null && key != null && this.clazz.equals(obj.getClass()) && this.container.equals(key.toString())) {
                return new IndexedContainer(this, obj);
            }
            return UberspectImpl.TRY_FAILED;
        }

        public boolean tryFailed(Object rval) {
            return rval == UberspectImpl.TRY_FAILED;
        }

        public boolean isCacheable() {
            return true;
        }
    }

    public UberspectImpl(Log runtimeLogger) {
        super(runtimeLogger);
    }

    public Iterator<?> getIterator(Object obj, JexlInfo info) {
        if (obj instanceof Iterator) {
            return (Iterator) obj;
        }
        if (obj.getClass().isArray()) {
            return new ArrayIterator(obj);
        }
        if (obj instanceof Map) {
            return ((Map) obj).values().iterator();
        }
        if (obj instanceof Enumeration) {
            return new EnumerationIterator((Enumeration) obj);
        }
        if (obj instanceof Iterable) {
            return ((Iterable) obj).iterator();
        }
        try {
            AbstractExecutor.Method it = getMethodExecutor(obj, "iterator", null);
            if (it != null) {
                if (Iterator.class.isAssignableFrom(it.getReturnType())) {
                    return (Iterator) it.execute(obj, null);
                }
            }
            return null;
        } catch (Throwable xany) {
            throw new JexlException(info, "unable to generate iterator()", xany);
        }
    }

    public JexlMethod getMethod(Object obj, String method, Object[] args, JexlInfo info) {
        return getMethodExecutor(obj, method, args);
    }

    public JexlMethod getConstructorMethod(Object ctorHandle, Object[] args, JexlInfo info) {
        Constructor<?> ctor = getConstructor(ctorHandle, args);
        if (ctor == null) {
            return null;
        }
        return new ConstructorMethod(ctor);
    }

    public JexlPropertyGet getPropertyGet(Object obj, Object identifier, JexlInfo info) {
        JexlPropertyGet get = getGetExecutor(obj, identifier);
        if (!(get != null || obj == null || identifier == null)) {
            get = getIndexedGet(obj, identifier.toString());
            if (get == null) {
                Field field = getField(obj, identifier.toString(), info);
                if (field != null) {
                    return new FieldPropertyGet(field);
                }
            }
        }
        return get;
    }

    public JexlPropertySet getPropertySet(Object obj, Object identifier, Object arg, JexlInfo info) {
        JexlPropertySet set = getSetExecutor(obj, identifier, arg);
        if (!(set != null || obj == null || identifier == null)) {
            Field field = getField(obj, identifier.toString(), info);
            if (!(field == null || Modifier.isFinal(field.getModifiers()))) {
                if (arg == null || MethodKey.isInvocationConvertible(field.getType(), arg.getClass(), false)) {
                    return new FieldPropertySet(field);
                }
            }
        }
        return set;
    }

    public Field getField(Object obj, String name, JexlInfo info) {
        return getField(!(obj instanceof Class) ? obj.getClass() : (Class) obj, name);
    }

    protected JexlPropertyGet getIndexedGet(Object object, String name) {
        if (!(object == null || name == null)) {
            String base = name.substring(0, 1).toUpperCase() + name.substring(1);
            String container = name;
            Class<?> clazz = object.getClass();
            Method[] getters = getMethods(object.getClass(), "get" + base);
            Method[] setters = getMethods(object.getClass(), "set" + base);
            if (getters != null) {
                return new IndexedType(container, clazz, getters, setters);
            }
        }
        return null;
    }
}
