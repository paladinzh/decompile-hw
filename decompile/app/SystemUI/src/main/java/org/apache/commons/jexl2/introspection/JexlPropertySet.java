package org.apache.commons.jexl2.introspection;

public interface JexlPropertySet {
    Object invoke(Object obj, Object obj2) throws Exception;

    boolean isCacheable();

    boolean tryFailed(Object obj);

    Object tryInvoke(Object obj, Object obj2, Object obj3);
}
