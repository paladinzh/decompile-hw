package org.apache.commons.jexl2.internal.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl2.internal.introspection.MethodKey.AmbiguousException;
import org.apache.commons.logging.Log;

final class ClassMap {
    private final Map<String, Field> fieldCache;
    private final MethodCache methodCache;

    static final class MethodCache {
        private static final Method CACHE_MISS = cacheMiss();
        private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPES = new HashMap(13);
        private final MethodMap methodMap = new MethodMap();
        private final Map<MethodKey, Method> methods = new HashMap();

        MethodCache() {
        }

        public static Method cacheMiss() {
            try {
                return MethodCache.class.getMethod("cacheMiss", new Class[0]);
            } catch (Exception e) {
                return null;
            }
        }

        static {
            PRIMITIVE_TYPES.put(Boolean.TYPE, Boolean.class);
            PRIMITIVE_TYPES.put(Byte.TYPE, Byte.class);
            PRIMITIVE_TYPES.put(Character.TYPE, Character.class);
            PRIMITIVE_TYPES.put(Double.TYPE, Double.class);
            PRIMITIVE_TYPES.put(Float.TYPE, Float.class);
            PRIMITIVE_TYPES.put(Integer.TYPE, Integer.class);
            PRIMITIVE_TYPES.put(Long.TYPE, Long.class);
            PRIMITIVE_TYPES.put(Short.TYPE, Short.class);
        }

        static Class<?> primitiveClass(Class<?> parm) {
            Class<?> prim = (Class) PRIMITIVE_TYPES.get(parm);
            return prim != null ? prim : parm;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Method get(MethodKey methodKey) throws AmbiguousException {
            synchronized (this.methodMap) {
                Method cacheEntry = (Method) this.methods.get(methodKey);
                if (cacheEntry == CACHE_MISS) {
                    return null;
                } else if (cacheEntry == null) {
                    try {
                        cacheEntry = this.methodMap.find(methodKey);
                        if (cacheEntry == null) {
                            this.methods.put(methodKey, CACHE_MISS);
                        } else {
                            this.methods.put(methodKey, cacheEntry);
                        }
                    } catch (AmbiguousException ae) {
                        this.methods.put(methodKey, CACHE_MISS);
                        throw ae;
                    }
                }
            }
        }

        void put(Method method) {
            synchronized (this.methodMap) {
                MethodKey methodKey = new MethodKey(method);
                if (this.methods.get(methodKey) == null) {
                    this.methods.put(methodKey, method);
                    this.methodMap.add(method);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Method[] get(String methodName) {
            synchronized (this.methodMap) {
                List<Method> lm = this.methodMap.get(methodName);
                if (!(lm == null || lm.isEmpty())) {
                    Method[] methodArr = (Method[]) lm.toArray(new Method[lm.size()]);
                    return methodArr;
                }
            }
        }
    }

    ClassMap(Class<?> aClass, Log log) {
        this.methodCache = createMethodCache(aClass, log);
        this.fieldCache = createFieldCache(aClass);
    }

    Field findField(Class<?> cls, String fname) {
        return (Field) this.fieldCache.get(fname);
    }

    private static Map<String, Field> createFieldCache(Class<?> clazz) {
        Field[] fields = clazz.getFields();
        if (fields.length <= 0) {
            return Collections.emptyMap();
        }
        Map<String, Field> cache = new HashMap();
        for (Field field : fields) {
            cache.put(field.getName(), field);
        }
        return cache;
    }

    Method[] get(String methodName) {
        return this.methodCache.get(methodName);
    }

    Method findMethod(MethodKey key) throws AmbiguousException {
        return this.methodCache.get(key);
    }

    private static MethodCache createMethodCache(Class<?> classToReflect, Log log) {
        MethodCache cache = new MethodCache();
        while (classToReflect != null) {
            if (Modifier.isPublic(classToReflect.getModifiers())) {
                populateMethodCacheWith(cache, classToReflect, log);
            }
            Class<?>[] interfaces = classToReflect.getInterfaces();
            for (Class populateMethodCacheWithInterface : interfaces) {
                populateMethodCacheWithInterface(cache, populateMethodCacheWithInterface, log);
            }
            classToReflect = classToReflect.getSuperclass();
        }
        return cache;
    }

    private static void populateMethodCacheWithInterface(MethodCache cache, Class<?> iface, Log log) {
        if (Modifier.isPublic(iface.getModifiers())) {
            populateMethodCacheWith(cache, iface, log);
        }
        Class<?>[] supers = iface.getInterfaces();
        for (Class populateMethodCacheWithInterface : supers) {
            populateMethodCacheWithInterface(cache, populateMethodCacheWithInterface, log);
        }
    }

    private static void populateMethodCacheWith(MethodCache cache, Class<?> clazz, Log log) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isPublic(methods[i].getModifiers())) {
                    cache.put(methods[i]);
                }
            }
        } catch (SecurityException se) {
            if (log.isDebugEnabled()) {
                log.debug("While accessing methods of " + clazz + ": ", se);
            }
        }
    }
}
