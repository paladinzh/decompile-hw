package com.android.mms.transaction;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import com.android.mms.HwCustMmsConfigImpl;
import com.huawei.cspcommon.MLog;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import java.lang.reflect.InvocationTargetException;
import org.apache.http.HttpRequest;

public class HwCustHttpUtilsImpl extends HwCustHttpUtils {
    public static final String AUTHORITY = "com.huawei.sprint.chameleon.provider";
    public static final String CHAMELEON_COLUMNS_VALUE = "value";
    private static final String CLASS_NAME_HWNVFUNCATION = "com.huawei.android.hwnv.HWNVFuncation";
    private static final String CLASS_NAME_MIPGENUSERPROFTYPE = "com.huawei.android.hwnv.MipGenUserProfType";
    private static final String CODE_FORMATE = "UTF-8";
    public static final Uri CONTENT_URI_CHAMELEON = Uri.parse("content://com.huawei.sprint.chameleon.provider/customization/chameleon");
    private static final String CUST_UA_PREFIX_HUAWEI = "HUAWEI ";
    private static final String DEFAULT_CARRIERID_SPRINT = "SPRINT";
    private static final String EMPTY_STRING = "";
    private static final String HDR_KEY_AUTHORIZATION = "Proxy-Authorization";
    private static final String HDR_KEY_CACHE_CONTROL = "Cache-control";
    private static final String HDR_VALUE_CACHE_CONTROL = "no-transform";
    private static final int HUAWEI_PREFIX_LEN = 7;
    public static final String INDEX = "_index";
    private static final String NODE_KEY = "name";
    private static final String NODE_VALUE = "value";
    private static final String[] PROJECTION = new String[]{"name", "value"};
    public static final int SYSPROP_BROWSER_UAPROF_URL = 14;
    private static final String TAG = "HwCustHttpUtilsImpl";
    public static final String WHERE_HTTP_HEADER_INDEX = "_index='647'";
    public static final String WHERE_INDEX = "_index = ?";
    private static final String idString = SystemProperties.get("ro.build.id", EMPTY_STRING);
    private static final boolean isShowATTDevice = SystemProperties.getBoolean("ro.config.show_deviceid", false);
    private static final String modelString = SystemProperties.get("ro.product.model", EMPTY_STRING);

    public void checkHttpHeaderUseCurrentLocale(StringBuilder buffer) {
        if (HwCustMmsConfigImpl.isHttpHeaderUseCurrentLocale()) {
            buffer.append(";q=0.7");
        }
    }

    public void addHeader(Context context, HttpRequest req, int method) {
        if (!(!isShowATTDevice || TextUtils.isEmpty(idString) || TextUtils.isEmpty(modelString))) {
            req.addHeader("x-att-deviceid", modelString + "/" + idString);
        }
        if (HwCustMmsConfigImpl.isHeaderSprintCustom()) {
            addHeaderProxyAuthorization(req);
            addHeaderXMdn(context, req);
            if (method == 1) {
                addHeaderCacheControl(req);
            }
        }
    }

    public String getCustomUserAgent(Context context, String userAgent) {
        if (HwCustMmsConfigImpl.isUserAgentSrpintCustom()) {
            return Build.PRODUCT + " " + getCarrierID(context);
        }
        String productStr = Build.MODEL;
        if (productStr.startsWith(CUST_UA_PREFIX_HUAWEI)) {
            productStr = productStr.substring(7);
        }
        if (HwCustMmsConfigImpl.isEnableUaPrefixHuawei()) {
            productStr = CUST_UA_PREFIX_HUAWEI + productStr;
        }
        String uaCustString = HwCustMmsConfigImpl.getUserAgentCustString();
        if (!TextUtils.isEmpty(uaCustString)) {
            return uaCustString.replace("#mn", productStr).replace("#pn", Build.PRODUCT);
        }
        if (TextUtils.isEmpty(userAgent) || !HwCustMmsConfigImpl.isShowUserAgentWithNoDash()) {
            return userAgent;
        }
        return userAgent.replace("-", EMPTY_STRING);
    }

    private String getCarrierID(Context context) {
        String CarrierID = getSyspropUaprof(context, WHERE_INDEX, new String[]{String.valueOf(420)});
        MLog.d(TAG, "getCarrierID CarrierID = " + CarrierID);
        if (CarrierID != null) {
            return CarrierID;
        }
        return DEFAULT_CARRIERID_SPRINT;
    }

    private void addHeaderProxyAuthorization(HttpRequest req) {
        Object mipUserProf = getClassInstance(CLASS_NAME_MIPGENUSERPROFTYPE);
        if (mipUserProf == null) {
            MLog.w(TAG, "addHeaderProxyAuthorization It cannot refect the class : com.huawei.android.hwnv.MipGenUserProfType");
            return;
        }
        String encodedProxyAuthorization = EMPTY_STRING;
        try {
            int activeIndex = Integer.parseInt(operateStaticFunction(CLASS_NAME_HWNVFUNCATION, "getMipActiveProfile").toString());
            if (activeIndex == -1) {
                MLog.w(TAG, "addHeaderProxyAuthorization It cannot get the activeIndex from the class : com.huawei.android.hwnv.HWNVFuncation");
            } else if (((Boolean) operateStaticFunction(CLASS_NAME_HWNVFUNCATION, "getMipGenUserProf", mipUserProf, activeIndex)).booleanValue()) {
                String userName = (String) excuteInstanceMethod(mipUserProf, "getUsername");
                if (userName != null) {
                    if (userName.contains("@modem.")) {
                        userName = userName.replace("@modem.", "@");
                    }
                    encodedProxyAuthorization = "Basic " + new String(Base64.encode((userName + ":pcs").getBytes(CODE_FORMATE), 2), CODE_FORMATE);
                }
                req.addHeader(HDR_KEY_AUTHORIZATION, encodedProxyAuthorization);
            } else {
                MLog.w(TAG, "addHeaderProxyAuthorization Can not read value from NV");
            }
        } catch (Exception e) {
            MLog.e(TAG, "addHeaderProxyAuthorization error create Proxy Authorization info:" + e.getMessage());
        }
    }

    private void addHeaderXMdn(Context context, HttpRequest req) {
        String xMDNvalue = TelephonyManager.getDefault().getLine1Number();
        if (!TextUtils.isEmpty(xMDNvalue)) {
            String chameleonMMSHttpHeaderKey = getHTTPHeaderKeyFromChameleon(context);
            if (TextUtils.isEmpty(chameleonMMSHttpHeaderKey) || chameleonMMSHttpHeaderKey.equalsIgnoreCase("null")) {
                req.addHeader("X-MDN", xMDNvalue);
                return;
            }
            req.addHeader(chameleonMMSHttpHeaderKey, xMDNvalue);
        }
    }

    private void addHeaderCacheControl(HttpRequest req) {
        req.addHeader(HDR_KEY_CACHE_CONTROL, HDR_VALUE_CACHE_CONTROL);
    }

    private Object getClassInstance(String className) {
        Object reflectInstance = null;
        try {
            reflectInstance = Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            MLog.e(TAG, "getClassInstance->e:", (Throwable) e);
        } catch (InstantiationException e2) {
            MLog.e(TAG, "getClassInstance->e:", (Throwable) e2);
        } catch (IllegalAccessException e3) {
            MLog.e(TAG, "getClassInstance->e:", (Throwable) e3);
        } catch (RuntimeException e4) {
            MLog.e(TAG, "getClassInstance->e:", (Throwable) e4);
        }
        return reflectInstance;
    }

    private Object excuteInstanceMethod(Object instance, String methodName) {
        Object result = null;
        try {
            result = Class.forName(instance.getClass().getName()).getMethod(methodName, new Class[0]).invoke(instance, new Object[0]);
        } catch (ClassNotFoundException e) {
            MLog.e(TAG, "excuteInstanceMethod->e:", (Throwable) e);
        } catch (NoSuchMethodException e2) {
            MLog.e(TAG, "excuteInstanceMethod->e:", (Throwable) e2);
        } catch (IllegalAccessException e3) {
            MLog.e(TAG, "excuteInstanceMethod->e:", (Throwable) e3);
        } catch (InvocationTargetException e4) {
            MLog.e(TAG, "excuteInstanceMethod->e:", (Throwable) e4);
        } catch (RuntimeException e5) {
            MLog.e(TAG, "excuteInstanceMethod->e:", (Throwable) e5);
        }
        return result;
    }

    private Object operateStaticFunction(String className, String methodName) {
        Object result = Integer.valueOf(-1);
        try {
            Class<?> reflectClass = Class.forName(className);
            result = reflectClass.getMethod(methodName, new Class[0]).invoke(reflectClass.newInstance(), new Object[0]);
        } catch (ClassNotFoundException e) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e);
        } catch (InstantiationException e2) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e2);
        } catch (IllegalAccessException e3) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e3);
        } catch (NoSuchMethodException e4) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e4);
        } catch (InvocationTargetException e5) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e5);
        } catch (RuntimeException e6) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e6);
        }
        return result;
    }

    private Object operateStaticFunction(String className, String methodName, Object instance, int index) {
        Object result = Boolean.valueOf(false);
        try {
            Class<?> reflectClass = Class.forName(className);
            Object reflectInstance = reflectClass.newInstance();
            result = reflectClass.getMethod(methodName, new Class[]{instance.getClass(), Integer.TYPE}).invoke(reflectInstance, new Object[]{instance, Integer.valueOf(index)});
        } catch (ClassNotFoundException e) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e);
        } catch (InstantiationException e2) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e2);
        } catch (IllegalAccessException e3) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e3);
        } catch (NoSuchMethodException e4) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e4);
        } catch (InvocationTargetException e5) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e5);
        } catch (RuntimeException e6) {
            MLog.e(TAG, "operateStaticFunction->e:", (Throwable) e6);
        }
        return result;
    }

    private String getHTTPHeaderKeyFromChameleon(Context context) {
        String str = null;
        Cursor chameleonCursor = context.getContentResolver().query(ChameleonContract.CONTENT_URI_CHAMELEON, PROJECTION, WHERE_HTTP_HEADER_INDEX, null, null);
        if (chameleonCursor != null) {
            while (chameleonCursor.moveToNext()) {
                try {
                    str = chameleonCursor.getString(1);
                    if (TextUtils.isEmpty(str)) {
                        str = EMPTY_STRING;
                    }
                } catch (SQLException e) {
                    MLog.e(TAG, "getHTTPHeaderKeyFromChameleon Exception while retriving chameleon MMSHttpHeader");
                } finally {
                    chameleonCursor.close();
                }
            }
        }
        return str;
    }

    public String getChameleonUAprof(Context context) {
        if (HwCustMmsConfigImpl.isChameleon()) {
            return SystemProperties.get("ro.device.wapprofile.url");
        }
        return null;
    }

    public String getSyspropUaprof(Context context, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String sysprop_Value = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI_CHAMELEON, null, selection, selectionArgs, null);
            int value_index = cursor.getColumnIndexOrThrow("value");
            cursor.moveToFirst();
            sysprop_Value = cursor.getString(value_index);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sysprop_Value;
    }
}
