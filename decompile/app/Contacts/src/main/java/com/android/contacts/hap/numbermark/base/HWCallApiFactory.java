package com.android.contacts.hap.numbermark.base;

import android.content.Context;
import android.text.TextUtils;
import com.android.contacts.hap.util.SeparatedFeatureDelegate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HWCallApiFactory {
    private static HWCallApiFactory factory = new HWCallApiFactory();

    private static class TencentApiProxy {
        private static ISDKCallApi sTencentApiInstance;

        private TencentApiProxy() {
        }

        public static synchronized ISDKCallApi getInstance(Context context) {
            ISDKCallApi iSDKCallApi;
            synchronized (TencentApiProxy.class) {
                if (sTencentApiInstance == null) {
                    sTencentApiInstance = HWCallApiFactory.getReflectionApiInstance(context, "com.android.contacts.hap.numbermark.hwtencent.api.TencentSDKCallApi", "getInstance");
                }
                iSDKCallApi = sTencentApiInstance;
            }
            return iSDKCallApi;
        }
    }

    private static class TomsApiProxy {
        private static ISDKCallApi sTomsApiInstance;

        private TomsApiProxy() {
        }

        public static synchronized ISDKCallApi getInstance(Context context) {
            ISDKCallApi iSDKCallApi;
            synchronized (TomsApiProxy.class) {
                if (sTomsApiInstance == null) {
                    sTomsApiInstance = HWCallApiFactory.getReflectionApiInstance(context, "com.android.contacts.hap.numbermark.hwtoms.api.TomsSDKCallApi", "getInstance");
                }
                iSDKCallApi = sTomsApiInstance;
            }
            return iSDKCallApi;
        }
    }

    private static class W3ApiProxy {
        private static ISDKCallApi sW3ApiInstance;

        private W3ApiProxy() {
        }

        public static synchronized ISDKCallApi getInstance(Context context) {
            ISDKCallApi iSDKCallApi;
            synchronized (W3ApiProxy.class) {
                if (sW3ApiInstance == null) {
                    sW3ApiInstance = HWCallApiFactory.getReflectionApiInstance(context, "com.android.contacts.hap.numbermark.hww3.api.W3SDKCallApi", "getInstance");
                }
                iSDKCallApi = sW3ApiInstance;
            }
            return iSDKCallApi;
        }
    }

    private HWCallApiFactory() {
    }

    public static HWCallApiFactory getInstance() {
        return factory;
    }

    public ISDKCallApi getSDKCallApiByNum(String num, Context context) {
        if (num == null || !SeparatedFeatureDelegate.isInstalled(context)) {
            return null;
        }
        if ("tencent".equals(getSupplier(num))) {
            return TencentApiProxy.getInstance(context);
        }
        return TomsApiProxy.getInstance(context);
    }

    public ISDKCallApi getSDKCallApiByName(String name, Context context) {
        if (SeparatedFeatureDelegate.isInstalled(context)) {
            if ("w3".equals(name)) {
                return W3ApiProxy.getInstance(context);
            }
            if ("tencent".equals(name)) {
                return TencentApiProxy.getInstance(context);
            }
            if ("toms".equals(name)) {
                return TomsApiProxy.getInstance(context);
            }
        }
        return null;
    }

    public static String getSupplier(String num) {
        if (TextUtils.isEmpty(num)) {
            return null;
        }
        String subNum = "";
        if (num.length() > 11) {
            subNum = num.substring(num.length() - 11, num.length());
        }
        if (num.matches("1\\d{10}$") || subNum.matches("1\\d{10}$")) {
            return "tencent";
        }
        if (num.matches("\\d{10}|(\\d{3,4})?\\d{8}|(\\d{4})?\\d{7}|\\d{6}|\\d{5}")) {
            return "toms";
        }
        return "tencent";
    }

    private static ISDKCallApi getReflectionApiInstance(Context context, String classPath, String methodName) {
        if (context == null) {
            return null;
        }
        ISDKCallApi api = null;
        try {
            Method method = Class.forName(classPath).getMethod(methodName, new Class[]{Context.class});
            api = (ISDKCallApi) method.invoke(method, new Object[]{context});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        return api;
    }
}
