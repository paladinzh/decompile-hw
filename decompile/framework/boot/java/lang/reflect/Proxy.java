package java.lang.reflect;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import libcore.util.EmptyArray;
import sun.reflect.CallerSensitive;

public class Proxy implements Serializable {
    private static final Comparator<Method> ORDER_BY_SIGNATURE_AND_SUBTYPE = new Comparator<Method>() {
        public int compare(Method a, Method b) {
            int comparison = Method.ORDER_BY_SIGNATURE.compare(a, b);
            if (comparison != 0) {
                return comparison;
            }
            Class<?> aClass = a.getDeclaringClass();
            Class<?> bClass = b.getDeclaringClass();
            if (aClass == bClass) {
                return 0;
            }
            if (aClass.isAssignableFrom(bClass)) {
                return 1;
            }
            if (bClass.isAssignableFrom(aClass)) {
                return -1;
            }
            return 0;
        }
    };
    private static final Class[] constructorParams = new Class[]{InvocationHandler.class};
    private static Map<ClassLoader, Map<List<String>, Object>> loaderToCache = new WeakHashMap();
    private static long nextUniqueNumber = 0;
    private static Object nextUniqueNumberLock = new Object();
    private static Object pendingGenerationMarker = new Object();
    private static final String proxyClassNamePrefix = "$Proxy";
    private static Map<Class<?>, Void> proxyClasses = Collections.synchronizedMap(new WeakHashMap());
    private static final long serialVersionUID = -2222568056686623797L;
    protected InvocationHandler h;

    private static native Class<?> generateProxy(String str, Class<?>[] clsArr, ClassLoader classLoader, Method[] methodArr, Class<?>[][] clsArr2);

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        this.h = h;
    }

    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException {
        return getProxyClass0(loader, interfaces);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }
        Class<?> proxyClass = null;
        String[] interfaceNames = new String[interfaces.length];
        Set<Class<?>> interfaceSet = new HashSet();
        int i = 0;
        while (i < interfaces.length) {
            String interfaceName = interfaces[i].getName();
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(interfaceName, false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != interfaces[i]) {
                throw new IllegalArgumentException(interfaces[i] + " is not visible from class loader");
            } else if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
            } else if (interfaceSet.contains(interfaceClass)) {
                throw new IllegalArgumentException("repeated interface: " + interfaceClass.getName());
            } else {
                interfaceSet.add(interfaceClass);
                interfaceNames[i] = interfaceName;
                i++;
            }
        }
        List<String> key = Arrays.asList(interfaceNames);
        synchronized (loaderToCache) {
            Map<List<String>, Object> cache = (Map) loaderToCache.get(loader);
            if (cache == null) {
                cache = new HashMap();
                loaderToCache.put(loader, cache);
            }
        }
        synchronized (cache) {
            while (true) {
                Object value = cache.get(key);
                if (value instanceof Reference) {
                    proxyClass = (Class) ((Reference) value).get();
                }
                if (proxyClass == null) {
                    if (value != pendingGenerationMarker) {
                        break;
                    }
                    try {
                        cache.wait();
                    } catch (InterruptedException e2) {
                    }
                } else {
                    return proxyClass;
                }
            }
            cache.put(key, pendingGenerationMarker);
        }
    }

    private static List<Class<?>[]> deduplicateAndGetExceptions(List<Method> methods) {
        List<Class<?>[]> exceptions = new ArrayList(methods.size());
        int i = 0;
        while (i < methods.size()) {
            Method method = (Method) methods.get(i);
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            if (i <= 0 || Method.ORDER_BY_SIGNATURE.compare(method, (Method) methods.get(i - 1)) != 0) {
                exceptions.add(exceptionTypes);
                i++;
            } else {
                exceptions.set(i - 1, intersectExceptions((Class[]) exceptions.get(i - 1), exceptionTypes));
                methods.remove(i);
            }
        }
        return exceptions;
    }

    private static Class<?>[] intersectExceptions(Class<?>[] aExceptions, Class<?>[] bExceptions) {
        if (aExceptions.length == 0 || bExceptions.length == 0) {
            return EmptyArray.CLASS;
        }
        if (Arrays.equals((Object[]) aExceptions, (Object[]) bExceptions)) {
            return aExceptions;
        }
        Set<Class<?>> intersection = new HashSet();
        for (Class<?> a : aExceptions) {
            for (Class<?> b : bExceptions) {
                if (a.isAssignableFrom(b)) {
                    intersection.add(b);
                } else if (b.isAssignableFrom(a)) {
                    intersection.add(a);
                }
            }
        }
        return (Class[]) intersection.toArray(new Class[intersection.size()]);
    }

    private static void validateReturnTypes(List<Method> methods) {
        Method vs = null;
        for (Object method : methods) {
            if (vs == null || !vs.equalNameAndParameters(method)) {
                vs = method;
            } else {
                Class<?> returnType = method.getReturnType();
                Class<?> vsReturnType = vs.getReturnType();
                if (!returnType.isInterface() || !vsReturnType.isInterface()) {
                    if (vsReturnType.isAssignableFrom(returnType)) {
                        vs = method;
                    } else if (!returnType.isAssignableFrom(vsReturnType)) {
                        throw new IllegalArgumentException("proxied interface methods have incompatible return types:\n  " + vs + "\n  " + method);
                    }
                }
            }
        }
    }

    private static List<Method> getMethods(Class<?>[] interfaces) {
        List<Method> result = new ArrayList();
        try {
            result.add(Object.class.getMethod("equals", Object.class));
            result.add(Object.class.getMethod("hashCode", EmptyArray.CLASS));
            result.add(Object.class.getMethod("toString", EmptyArray.CLASS));
            getMethodsRecursive(interfaces, result);
            return result;
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }
    }

    private static void getMethodsRecursive(Class<?>[] interfaces, List<Method> methods) {
        for (Class<?> i : interfaces) {
            getMethodsRecursive(i.getInterfaces(), methods);
            Collections.addAll(methods, i.getDeclaredMethods());
        }
    }

    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        if (h == null) {
            throw new NullPointerException();
        }
        try {
            return newInstance(getProxyClass0(loader, interfaces).getConstructor(constructorParams), h);
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        }
    }

    private static Object newInstance(Constructor<?> cons, InvocationHandler h) {
        try {
            return cons.newInstance(h);
        } catch (ReflectiveOperationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e2) {
            Throwable t = e2.getCause();
            if (t instanceof RuntimeException) {
                throw ((RuntimeException) t);
            }
            throw new InternalError(t.toString());
        }
    }

    public static boolean isProxyClass(Class<?> cl) {
        if (cl != null) {
            return proxyClasses.containsKey(cl);
        }
        throw new NullPointerException();
    }

    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        if (proxy instanceof Proxy) {
            return ((Proxy) proxy).h;
        }
        throw new IllegalArgumentException("not a proxy instance");
    }

    private static Object invoke(Proxy proxy, Method method, Object[] args) throws Throwable {
        return proxy.h.invoke(proxy, method, args);
    }

    private static void reserved1() {
    }

    private static void reserved2() {
    }
}
