package org.apache.commons.jexl2.internal.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class MethodKey {
    private static final Parameters<Constructor<?>> CONSTRUCTORS = new Parameters<Constructor<?>>() {
        protected Class<?>[] getParameterTypes(Constructor<?> app) {
            return app.getParameterTypes();
        }
    };
    private static final Parameters<Method> METHODS = new Parameters<Method>() {
        protected Class<?>[] getParameterTypes(Method app) {
            return app.getParameterTypes();
        }
    };
    private static final Class<?>[] NOARGS = new Class[0];
    private final int hashCode;
    private final String method;
    private final Class<?>[] params;

    private static abstract class Parameters<T> {
        protected abstract Class<?>[] getParameterTypes(T t);

        private Parameters() {
        }

        private T getMostSpecific(List<T> methods, Class<?>[] classes) {
            LinkedList<T> applicables = getApplicables(methods, classes);
            if (applicables.isEmpty()) {
                return null;
            }
            if (applicables.size() == 1) {
                return applicables.getFirst();
            }
            LinkedList<T> maximals = new LinkedList();
            Iterator<T> applicable = applicables.iterator();
            while (applicable.hasNext()) {
                T app = applicable.next();
                Class<?>[] appArgs = getParameterTypes(app);
                boolean lessSpecific = false;
                Iterator<T> maximal = maximals.iterator();
                while (!lessSpecific && maximal.hasNext()) {
                    switch (moreSpecific(appArgs, getParameterTypes(maximal.next()))) {
                        case 0:
                            maximal.remove();
                            break;
                        case 1:
                            lessSpecific = true;
                            break;
                        default:
                            break;
                    }
                }
                if (!lessSpecific) {
                    maximals.addLast(app);
                }
            }
            if (maximals.size() <= 1) {
                return maximals.getFirst();
            }
            throw new AmbiguousException();
        }

        private int moreSpecific(Class<?>[] c1, Class<?>[] c2) {
            boolean c1MoreSpecific = false;
            boolean c2MoreSpecific = false;
            if (c1.length > c2.length) {
                return 0;
            }
            if (c2.length > c1.length) {
                return 1;
            }
            for (int i = 0; i < c1.length; i++) {
                if (c1[i] != c2[i]) {
                    boolean last;
                    if (i != c1.length - 1) {
                        last = false;
                    } else {
                        last = true;
                    }
                    c1MoreSpecific = c1MoreSpecific || isStrictConvertible(c2[i], c1[i], last);
                    c2MoreSpecific = c2MoreSpecific || isStrictConvertible(c1[i], c2[i], last);
                }
            }
            if (c1MoreSpecific) {
                return !c2MoreSpecific ? 0 : 2;
            } else {
                if (c2MoreSpecific) {
                    return 1;
                }
                int primDiff = 0;
                for (int c = 0; c < c1.length; c++) {
                    if (c1[c].isPrimitive()) {
                        primDiff += 1 << c;
                    }
                    if (c2[c].isPrimitive()) {
                        primDiff -= 1 << c;
                    }
                }
                if (primDiff <= 0) {
                    return primDiff >= 0 ? 2 : 1;
                } else {
                    return 0;
                }
            }
        }

        private LinkedList<T> getApplicables(List<T> methods, Class<?>[] classes) {
            LinkedList<T> list = new LinkedList();
            for (T method : methods) {
                if (isApplicable(method, classes)) {
                    list.add(method);
                }
            }
            return list;
        }

        private boolean isApplicable(T method, Class<?>[] classes) {
            int i;
            Class<?>[] methodArgs = getParameterTypes(method);
            if (methodArgs.length != classes.length) {
                if (methodArgs.length == classes.length + 1) {
                    if (!methodArgs[methodArgs.length - 1].isArray()) {
                    }
                }
                if (methodArgs.length <= 0) {
                    return false;
                }
                Class<?> lastarg = methodArgs[methodArgs.length - 1];
                if (!lastarg.isArray()) {
                    return false;
                }
                for (i = 0; i < methodArgs.length - 1; i++) {
                    if (!isConvertible(methodArgs[i], classes[i], false)) {
                        return false;
                    }
                }
                Class<?> vararg = lastarg.getComponentType();
                for (i = methodArgs.length - 1; i < classes.length; i++) {
                    if (!isConvertible(vararg, classes[i], false)) {
                        return false;
                    }
                }
                return true;
            }
            i = 0;
            while (i < classes.length) {
                if (isConvertible(methodArgs[i], classes[i], false)) {
                    i++;
                } else if (i == classes.length - 1 && methodArgs[i].isArray()) {
                    return isConvertible(methodArgs[i], classes[i], true);
                } else {
                    return false;
                }
            }
            return true;
        }

        private boolean isConvertible(Class<?> formal, Class<?> actual, boolean possibleVarArg) {
            if (actual.equals(Void.class)) {
                actual = null;
            }
            return MethodKey.isInvocationConvertible(formal, actual, possibleVarArg);
        }

        private boolean isStrictConvertible(Class<?> formal, Class<?> actual, boolean possibleVarArg) {
            if (actual.equals(Void.class)) {
                actual = null;
            }
            return MethodKey.isStrictInvocationConvertible(formal, actual, possibleVarArg);
        }
    }

    public static class AmbiguousException extends RuntimeException {
        private static final long serialVersionUID = -2314636505414551664L;
    }

    public MethodKey(String aMethod, Object[] args) {
        this.method = aMethod;
        int hash = this.method.hashCode();
        if (args != null) {
            int size = args.length;
            if (size > 0) {
                this.params = new Class[size];
                for (int p = 0; p < size; p++) {
                    Class<?> parm;
                    Object arg = args[p];
                    if (arg != null) {
                        parm = arg.getClass();
                    } else {
                        parm = Void.class;
                    }
                    hash = (hash * 37) + parm.hashCode();
                    this.params[p] = parm;
                }
                this.hashCode = hash;
            }
        }
        this.params = NOARGS;
        this.hashCode = hash;
    }

    MethodKey(Method aMethod) {
        this(aMethod.getName(), aMethod.getParameterTypes());
    }

    MethodKey(String aMethod, Class<?>[] args) {
        this.method = aMethod.intern();
        int hash = this.method.hashCode();
        if (args != null) {
            int size = args.length;
            if (size > 0) {
                this.params = new Class[size];
                for (int p = 0; p < size; p++) {
                    Class<?> parm = MethodCache.primitiveClass(args[p]);
                    hash = (hash * 37) + parm.hashCode();
                    this.params[p] = parm;
                }
                this.hashCode = hash;
            }
        }
        this.params = NOARGS;
        this.hashCode = hash;
    }

    String getMethod() {
        return this.method;
    }

    public int hashCode() {
        return this.hashCode;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey key = (MethodKey) obj;
        if (this.method.equals(key.method) && Arrays.equals(this.params, key.params)) {
            z = true;
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(this.method);
        for (Class<?> c : this.params) {
            String name;
            if (c != Void.class) {
                name = c.getName();
            } else {
                name = "null";
            }
            builder.append(name);
        }
        return builder.toString();
    }

    public String debugString() {
        StringBuilder builder = new StringBuilder(this.method);
        builder.append('(');
        for (int i = 0; i < this.params.length; i++) {
            String name;
            if (i > 0) {
                builder.append(", ");
            }
            if (Void.class != this.params[i]) {
                name = this.params[i].getName();
            } else {
                name = "null";
            }
            builder.append(name);
        }
        builder.append(')');
        return builder.toString();
    }

    public Method getMostSpecificMethod(List<Method> methods) {
        return (Method) METHODS.getMostSpecific(methods, this.params);
    }

    public Constructor<?> getMostSpecificConstructor(List<Constructor<?>> methods) {
        return (Constructor) CONSTRUCTORS.getMostSpecific(methods, this.params);
    }

    public static boolean isInvocationConvertible(Class<?> formal, Class<?> actual, boolean possibleVarArg) {
        if (actual == null && !formal.isPrimitive()) {
            return true;
        }
        if (actual != null && formal.isAssignableFrom(actual)) {
            return true;
        }
        if (formal.isPrimitive()) {
            if (formal == Boolean.TYPE && actual == Boolean.class) {
                return true;
            }
            if (formal == Character.TYPE && actual == Character.class) {
                return true;
            }
            if (formal == Byte.TYPE && actual == Byte.class) {
                return true;
            }
            if (formal == Short.TYPE) {
                if (actual == Short.class || actual == Byte.class) {
                    return true;
                }
            }
            if (formal == Integer.TYPE) {
                if (actual == Integer.class || actual == Short.class || actual == Byte.class) {
                    return true;
                }
            }
            if (formal == Long.TYPE) {
                if (actual == Long.class || actual == Integer.class || actual == Short.class || actual == Byte.class) {
                    return true;
                }
            }
            if (formal == Float.TYPE) {
                if (actual == Float.class || actual == Long.class || actual == Integer.class || actual == Short.class || actual == Byte.class) {
                    return true;
                }
            }
            if (formal == Double.TYPE) {
                if (actual == Double.class || actual == Float.class || actual == Long.class || actual == Integer.class || actual == Short.class || actual == Byte.class) {
                    return true;
                }
            }
        }
        if (!possibleVarArg || !formal.isArray()) {
            return false;
        }
        if (actual != null && actual.isArray()) {
            actual = actual.getComponentType();
        }
        return isInvocationConvertible(formal.getComponentType(), actual, false);
    }

    public static boolean isStrictInvocationConvertible(Class<?> formal, Class<?> actual, boolean possibleVarArg) {
        if ((actual == null && !formal.isPrimitive()) || formal.isAssignableFrom(actual)) {
            return true;
        }
        if (formal.isPrimitive()) {
            if (formal == Short.TYPE && actual == Byte.TYPE) {
                return true;
            }
            if (formal == Integer.TYPE) {
                if (actual == Short.TYPE || actual == Byte.TYPE) {
                    return true;
                }
            }
            if (formal == Long.TYPE) {
                if (actual == Integer.TYPE || actual == Short.TYPE || actual == Byte.TYPE) {
                    return true;
                }
            }
            if (formal == Float.TYPE) {
                if (actual == Long.TYPE || actual == Integer.TYPE || actual == Short.TYPE || actual == Byte.TYPE) {
                    return true;
                }
            }
            if (formal == Double.TYPE) {
                if (actual == Float.TYPE || actual == Long.TYPE || actual == Integer.TYPE || actual == Short.TYPE || actual == Byte.TYPE) {
                    return true;
                }
            }
        }
        if (!possibleVarArg || !formal.isArray()) {
            return false;
        }
        if (actual != null && actual.isArray()) {
            actual = actual.getComponentType();
        }
        return isStrictInvocationConvertible(formal.getComponentType(), actual, false);
    }
}
