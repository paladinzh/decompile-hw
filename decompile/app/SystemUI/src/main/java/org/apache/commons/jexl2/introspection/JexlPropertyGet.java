package org.apache.commons.jexl2.introspection;

public interface JexlPropertyGet {
    Object invoke(Object obj) throws Exception;

    boolean isCacheable();

    boolean tryFailed(Object obj);

    Object tryInvoke(Object obj, Object obj2);
}
