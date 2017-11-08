package com.huawei.gallery.util;

import com.android.gallery3d.util.GalleryLog;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public final class ReflectUtils {

    private static class GetObjectValueAction implements PrivilegedExceptionAction<Object> {
        private String mFieldName;
        private Object mObj;

        GetObjectValueAction(Object object, String field) {
            this.mObj = object;
            this.mFieldName = field;
        }

        public Object run() throws Exception {
            Field field = this.mObj.getClass().getDeclaredField(this.mFieldName);
            field.setAccessible(true);
            return field.get(this.mObj);
        }
    }

    private ReflectUtils() {
    }

    public static Object getFieldValue(Object object, String fieldName) {
        try {
            return AccessController.doPrivileged(new GetObjectValueAction(object, fieldName));
        } catch (Exception e) {
            GalleryLog.e("ReflectUtils", "find  fieldvalue got exception.");
            return null;
        }
    }
}
