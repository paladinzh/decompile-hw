package org.apache.commons.jexl2.internal.introspection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl2.internal.introspection.MethodKey.AmbiguousException;

final class MethodMap {
    private final Map<String, List<Method>> methodByNameMap = new HashMap();

    MethodMap() {
    }

    public synchronized void add(Method method) {
        String methodName = method.getName();
        List<Method> l = (List) this.methodByNameMap.get(methodName);
        if (l == null) {
            l = new ArrayList();
            this.methodByNameMap.put(methodName, l);
        }
        l.add(method);
    }

    public synchronized List<Method> get(String key) {
        return (List) this.methodByNameMap.get(key);
    }

    Method find(MethodKey methodKey) throws AmbiguousException {
        List<Method> methodList = get(methodKey.getMethod());
        if (methodList != null) {
            return methodKey.getMostSpecificMethod(methodList);
        }
        return null;
    }
}
