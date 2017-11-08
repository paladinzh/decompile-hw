package org.apache.commons.jexl2.internal.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl2.internal.introspection.MethodKey.AmbiguousException;
import org.apache.commons.logging.Log;

public class IntrospectorBase {
    private static final Constructor<?> CTOR_MISS = CacheMiss.class.getConstructors()[0];
    private final Map<Class<?>, ClassMap> classMethodMaps = new HashMap();
    private final Map<String, Class<?>> constructibleClasses = new HashMap();
    private final Map<MethodKey, Constructor<?>> constructorsMap = new HashMap();
    private ClassLoader loader;
    protected final Log rlog;

    private static class CacheMiss {
    }

    public IntrospectorBase(Log log) {
        this.rlog = log;
        this.loader = getClass().getClassLoader();
    }

    public Class<?> getClassByName(String className) {
        try {
            return Class.forName(className, false, this.loader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Method getMethod(Class<?> c, MethodKey key) {
        try {
            return getMap(c).findMethod(key);
        } catch (AmbiguousException xambiguous) {
            if (this.rlog != null && this.rlog.isInfoEnabled()) {
                this.rlog.info("ambiguous method invocation: " + c.getName() + "." + key.debugString(), xambiguous);
            }
            return null;
        }
    }

    public Field getField(Class<?> c, String key) {
        return getMap(c).findField(c, key);
    }

    public Method[] getMethods(Class<?> c, String methodName) {
        if (c != null) {
            return getMap(c).get(methodName);
        }
        return null;
    }

    public Constructor<?> getConstructor(Class<?> c, MethodKey key) {
        String cname;
        synchronized (this.constructorsMap) {
            Constructor<?> ctor = (Constructor) this.constructorsMap.get(key);
            if (CTOR_MISS.equals(ctor)) {
                return null;
            } else if (ctor == null) {
                cname = key.getMethod();
                Class<?> clazz = (Class) this.constructibleClasses.get(cname);
                if (clazz == null) {
                    if (c != null) {
                        if (c.getName().equals(key.getMethod())) {
                            clazz = c;
                            this.constructibleClasses.put(cname, clazz);
                        }
                    }
                    try {
                        clazz = this.loader.loadClass(cname);
                        this.constructibleClasses.put(cname, clazz);
                    } catch (ClassNotFoundException xnotfound) {
                        if (this.rlog != null) {
                            if (this.rlog.isInfoEnabled()) {
                                this.rlog.info("unable to find class: " + cname + "." + key.debugString(), xnotfound);
                            }
                        }
                        ctor = null;
                    } catch (AmbiguousException xambiguous) {
                        if (this.rlog != null && this.rlog.isInfoEnabled()) {
                            this.rlog.info("ambiguous constructor invocation: " + cname + "." + key.debugString(), xambiguous);
                        }
                        ctor = null;
                    }
                }
                List<Constructor<?>> l = new LinkedList();
                for (Constructor<?> ictor : clazz.getConstructors()) {
                    l.add(ictor);
                }
                ctor = key.getMostSpecificConstructor(l);
                if (ctor == null) {
                    this.constructorsMap.put(key, CTOR_MISS);
                } else {
                    this.constructorsMap.put(key, ctor);
                }
            }
        }
        return ctor;
    }

    private ClassMap getMap(Class<?> c) {
        ClassMap classMap;
        synchronized (this.classMethodMaps) {
            classMap = (ClassMap) this.classMethodMaps.get(c);
            if (classMap == null) {
                classMap = new ClassMap(c, this.rlog);
                this.classMethodMaps.put(c, classMap);
            }
        }
        return classMap;
    }
}
