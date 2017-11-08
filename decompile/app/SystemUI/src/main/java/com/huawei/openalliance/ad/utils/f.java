package com.huawei.openalliance.ad.utils;

import com.huawei.openalliance.ad.utils.b.d;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class f {
    public static Class a(Field field) {
        return !Map.class.isAssignableFrom(field.getType()) ? !List.class.isAssignableFrom(field.getType()) ? null : a(field, 0) : a(field, 1);
    }

    private static Class a(Field field, int i) {
        int i2 = 0;
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > i) {
                try {
                    if (actualTypeArguments[i] instanceof Class) {
                        return (Class) actualTypeArguments[i];
                    }
                    String obj = actualTypeArguments[i].toString();
                    int indexOf = obj.indexOf("class ");
                    if (indexOf >= 0) {
                        i2 = indexOf;
                    }
                    indexOf = obj.indexOf("<");
                    if (indexOf < 0) {
                        indexOf = obj.length();
                    }
                    return Class.forName(obj.substring(i2, indexOf));
                } catch (Throwable e) {
                    d.a("ReflectAPI", "Exception", e);
                }
            }
        }
        return null;
    }

    public static Field[] a(Class cls) {
        Object obj = null;
        if (cls.getSuperclass() != null) {
            obj = a(cls.getSuperclass());
        }
        Object declaredFields = cls.getDeclaredFields();
        if (obj == null || obj.length <= 0) {
            return declaredFields;
        }
        Object obj2 = new Field[(declaredFields.length + obj.length)];
        System.arraycopy(obj, 0, obj2, 0, obj.length);
        System.arraycopy(declaredFields, 0, obj2, obj.length, declaredFields.length);
        return obj2;
    }
}
