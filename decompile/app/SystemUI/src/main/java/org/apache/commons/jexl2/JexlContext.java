package org.apache.commons.jexl2;

public interface JexlContext {
    Object get(String str);

    boolean has(String str);

    void set(String str, Object obj);
}
