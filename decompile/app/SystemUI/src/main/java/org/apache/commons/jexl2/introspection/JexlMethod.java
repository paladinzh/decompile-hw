package org.apache.commons.jexl2.introspection;

public interface JexlMethod {
    Class<?> getReturnType();

    Object invoke(Object obj, Object[] objArr) throws Exception;

    boolean isCacheable();

    boolean tryFailed(Object obj);

    Object tryInvoke(String str, Object obj, Object[] objArr);
}
