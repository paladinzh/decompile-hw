package com.huawei.hwid.core.c;

import com.huawei.hwid.core.c.b.a;

/* compiled from: HwInvoke */
public class j {
    public static int a(Class cls, String str, int i) {
        try {
            return cls.getField(str).getInt(null);
        } catch (IllegalArgumentException e) {
            a.e("HwInvoke", "getIntFiled(" + cls + " fiedName:" + str + ", def:" + i + ") err:" + e.toString());
            return i;
        } catch (IllegalAccessException e2) {
            a.e("HwInvoke", "getIntFiled(" + cls + " fiedName:" + str + ", def:" + i + ") err:" + e2.toString());
            return i;
        } catch (NoSuchFieldException e3) {
            a.e("HwInvoke", "getIntFiled(" + cls + " fiedName:" + str + ", def:" + i + ") err:" + e3.toString());
            return i;
        }
    }

    public static int a(String str, String str2, int i) {
        try {
            return a(Class.forName(str), str2, i);
        } catch (Exception e) {
            return i;
        }
    }

    public static Class a(String str) {
        try {
            return Class.forName(str);
        } catch (Throwable e) {
            a.e("HwInvoke", e.toString(), e);
            return null;
        } catch (Throwable e2) {
            a.e("HwInvoke", e2.toString(), e2);
            return null;
        } catch (Throwable e22) {
            a.e("HwInvoke", e22.toString(), e22);
            return null;
        }
    }

    public static Object a(Class cls, String str, Class[] clsArr, Object[] objArr) throws Exception {
        if (cls != null) {
            if (clsArr != null) {
                if (objArr == null) {
                    throw new Exception("paramsType or params should be same");
                } else if (clsArr.length != objArr.length) {
                    throw new Exception("paramsType len:" + clsArr.length + " should equal params.len:" + objArr.length);
                }
            } else if (objArr != null) {
                throw new Exception("paramsType is null, but params is not null");
            }
            try {
                try {
                    return cls.getMethod(str, clsArr).invoke(null, objArr);
                } catch (Throwable e) {
                    a.e("HwInvoke", e.toString(), e);
                    return null;
                } catch (Throwable e2) {
                    a.e("HwInvoke", e2.toString(), e2);
                    return null;
                } catch (Throwable e22) {
                    a.e("HwInvoke", e22.toString(), e22);
                    return null;
                }
            } catch (Throwable e222) {
                a.e("HwInvoke", e222.toString(), e222);
            } catch (Throwable e2222) {
                a.d("HwInvoke", e2222.toString(), e2222);
            }
        } else {
            throw new Exception("class is null in staticFun");
        }
    }

    public static Object a(String str, String str2, Class[] clsArr, Object[] objArr) {
        try {
            return a(Class.forName(str), str2, clsArr, objArr);
        } catch (Throwable e) {
            a.e("HwInvoke", e.toString(), e);
            return null;
        } catch (Throwable e2) {
            a.e("HwInvoke", e2.toString(), e2);
            return null;
        } catch (Throwable e22) {
            a.e("HwInvoke", e22.toString(), e22);
            return null;
        }
    }

    public static Object a(Class cls, Object obj, String str, Class[] clsArr, Object[] objArr) throws Exception {
        if (cls != null) {
            if (clsArr != null) {
                if (objArr == null) {
                    throw new Exception("paramsType or params should be same");
                } else if (clsArr.length != objArr.length) {
                    throw new Exception("paramsType len:" + clsArr.length + " should equal params.len:" + objArr.length);
                }
            } else if (objArr != null) {
                throw new Exception("paramsType is null, but params is not null");
            }
            try {
                try {
                    return cls.getMethod(str, clsArr).invoke(obj, objArr);
                } catch (Throwable e) {
                    a.e("HwInvoke", e.toString(), e);
                    return null;
                } catch (Throwable e2) {
                    a.e("HwInvoke", e2.toString(), e2);
                    return null;
                } catch (Throwable e22) {
                    a.e("HwInvoke", e22.toString(), e22);
                    return null;
                }
            } catch (NoSuchMethodException e3) {
                throw e3;
            } catch (Throwable e222) {
                a.d("HwInvoke", e222.toString(), e222);
            }
        } else {
            throw new Exception("class is null in staticFun");
        }
    }

    public static Object b(String str, String str2, Class[] clsArr, Object[] objArr) throws Exception {
        Class cls = Class.forName(str);
        return a(cls, cls.newInstance(), str2, clsArr, objArr);
    }
}
