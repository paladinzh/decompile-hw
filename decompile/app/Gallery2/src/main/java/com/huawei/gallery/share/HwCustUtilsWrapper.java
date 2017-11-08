package com.huawei.gallery.share;

import com.android.gallery3d.util.GalleryLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwCustUtilsWrapper {
    private static final String CREATE_OBJ = "createObj";
    private static final String TAG = "HwCustUtilsWrapper";
    private static Class<?> sClazz;
    private static Method[] sMethodList;

    static {
        try {
            GalleryLog.d(TAG, "start init HwCustUtils");
            sClazz = Class.forName("com.huawei.cust.HwCustUtils");
            sMethodList = sClazz.getMethods();
            GalleryLog.d(TAG, "init HwCustUtils success !!!");
        } catch (ClassNotFoundException e) {
            GalleryLog.d(TAG, "init HwCustUtils failed !!!", e);
        }
    }

    public static <T> T createObj(Class<T> clazz, Object... params) {
        if (sMethodList == null || sMethodList.length == 0) {
            GalleryLog.w(TAG, "can't find method ");
            return null;
        }
        Method createObj = null;
        for (Method method : sMethodList) {
            if (CREATE_OBJ.equals(method.getName()) && method.isVarArgs()) {
                Class<?>[] typeParameters = method.getParameterTypes();
                if (2 == typeParameters.length && typeParameters[0].isAssignableFrom(Class.class)) {
                    createObj = method;
                    break;
                }
            }
        }
        GalleryLog.d(TAG, "method find result " + createObj);
        if (createObj != null) {
            try {
                return createObj.invoke(null, new Object[]{clazz, params});
            } catch (IllegalArgumentException e) {
                GalleryLog.d(TAG, "call method " + createObj.getName() + " from HwCustUtils failed !!!" + e.getMessage());
            } catch (IllegalAccessException e2) {
                GalleryLog.d(TAG, "call method " + createObj.getName() + " from HwCustUtils failed !!!" + e2.getMessage());
            } catch (InvocationTargetException e3) {
                GalleryLog.d(TAG, "call method " + createObj.getName() + " from HwCustUtils failed !!!" + e3.getMessage());
            }
        }
        return null;
    }
}
