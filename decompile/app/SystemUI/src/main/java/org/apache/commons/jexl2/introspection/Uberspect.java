package org.apache.commons.jexl2.introspection;

import java.util.Iterator;
import org.apache.commons.jexl2.JexlInfo;

public interface Uberspect {
    JexlMethod getConstructorMethod(Object obj, Object[] objArr, JexlInfo jexlInfo);

    Iterator<?> getIterator(Object obj, JexlInfo jexlInfo);

    JexlMethod getMethod(Object obj, String str, Object[] objArr, JexlInfo jexlInfo);

    JexlPropertyGet getPropertyGet(Object obj, Object obj2, JexlInfo jexlInfo);

    JexlPropertySet getPropertySet(Object obj, Object obj2, Object obj3, JexlInfo jexlInfo);
}
