package com.huawei.hwid.core.d;

import com.huawei.hwid.core.d.b.e;

public class g {
    private static void a(Class cls, Class[] clsArr, Object[] objArr) throws Exception {
        if (cls == null) {
            throw new Exception("class is null in staticFun");
        } else if (clsArr != null) {
            if (objArr == null) {
                throw new Exception("paramsType or params should be same");
            } else if (clsArr.length != objArr.length) {
                throw new Exception("paramsType len:" + clsArr.length + " should equal params.len:" + objArr.length);
            }
        } else if (objArr != null) {
            throw new Exception("paramsType is null, but params is not null");
        }
    }

    public static Object a(Class cls, String str, Class[] clsArr, Object[] objArr) throws Exception {
        a(cls, clsArr, objArr);
        try {
            try {
                return cls.getMethod(str, clsArr).invoke(null, objArr);
            } catch (Throwable e) {
                e.e("HwInvoke", e.getMessage(), e);
                return null;
            } catch (Throwable e2) {
                e.e("HwInvoke", e2.getMessage(), e2);
                return null;
            } catch (Throwable e22) {
                e.e("HwInvoke", e22.getMessage(), e22);
                return null;
            }
        } catch (Throwable e222) {
            e.e("HwInvoke", e222.getMessage(), e222);
        } catch (Throwable e2222) {
            e.d("HwInvoke", e2222.getMessage(), e2222);
        }
    }

    public static Object a(String str, String str2, Class[] clsArr, Object[] objArr) {
        try {
            return a(Class.forName(str), str2, clsArr, objArr);
        } catch (Throwable e) {
            e.e("HwInvoke", e.getMessage(), e);
            return null;
        } catch (Throwable e2) {
            e.e("HwInvoke", e2.getMessage(), e2);
            return null;
        } catch (Throwable e22) {
            e.e("HwInvoke", e22.getMessage(), e22);
            return null;
        }
    }

    public static Object a(Class<?> cls, Object obj, String str, Class<?>[] clsArr, Object[] objArr) throws Exception {
        a(cls, clsArr, objArr);
        try {
            try {
                return cls.getMethod(str, clsArr).invoke(obj, objArr);
            } catch (Throwable e) {
                e.e("HwInvoke", e.getMessage(), e);
                return null;
            } catch (Throwable e2) {
                e.e("HwInvoke", e2.getMessage(), e2);
                return null;
            } catch (Throwable e22) {
                e.e("HwInvoke", e22.getMessage(), e22);
                return null;
            }
        } catch (NoSuchMethodException e3) {
            throw e3;
        } catch (Throwable e222) {
            e.d("HwInvoke", e222.getMessage(), e222);
        }
    }
}
