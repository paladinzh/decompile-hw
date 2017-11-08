package cn.com.xy.sms.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.log.LogManager;
import com.amap.api.services.district.DistrictSearchQuery;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class XyUtil {
    public static final String DEFAULT_IMEI = "360_DEFAULT_IMEI";
    private static String a = null;
    private static JSONObject b = null;

    private static String a(Map<String, Object> map, String str) {
        try {
            JSONArray jSONArray = new JSONArray();
            JSONArray jSONArray2 = new JSONArray(str);
            if (jSONArray2.length() > 0) {
                int length = jSONArray2.length();
                for (int i = 0; i < length; i++) {
                    JSONObject a = a((Map) map, jSONArray2.getJSONObject(i));
                    if (a != null) {
                        jSONArray.put(a);
                    }
                }
            }
            String jSONArray3 = jSONArray.toString();
            if (!StringUtils.isNull(jSONArray3)) {
                return jSONArray3;
            }
        } catch (Throwable th) {
        }
        return "";
    }

    private static String a(JSONObject jSONObject) {
        return jSONObject == null ? "" : StringUtils.encode(jSONObject.toString());
    }

    private static JSONObject a(Map<String, Object> map, JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                String str = (String) map.get("title_num");
                String str2 = (String) jSONObject.get("action");
                String str3;
                String str4;
                if ("reply_sms".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get("send_code");
                    str4 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "phone");
                    if (StringUtils.isNull(str4)) {
                        str4 = (String) JsonUtil.getValueWithMap(map, "phoneNum");
                    }
                    str = b(NumberInfo.TYPE_KEY, str2, "send_code", str3, "phone", str4, "titleNo", str);
                    jSONObject.remove("send_code");
                    jSONObject.remove("phone");
                    jSONObject.put("action_data", str);
                } else if ("send_sms".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get("send_code");
                    str4 = (String) jSONObject.get("phone");
                    str = b(NumberInfo.TYPE_KEY, str2, "send_code", str3, "phone", str4, "titleNo", str);
                    jSONObject.remove("send_code");
                    jSONObject.remove("phone");
                    jSONObject.put("action_data", str);
                } else if ("access_url".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get(Constant.URLS);
                    str = b(NumberInfo.TYPE_KEY, str2, Constant.URLS, str3, "titleNo", str);
                    jSONObject.remove(Constant.URLS);
                    jSONObject.put("action_data", str);
                } else if ("down_url".equalsIgnoreCase(str2) || "download".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get(Constant.URLS);
                    str = b(NumberInfo.TYPE_KEY, str2, Constant.URLS, str3, "titleNo", str);
                    jSONObject.remove(Constant.URLS);
                    jSONObject.put("action_data", str);
                } else if ("weibo_url".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get(Constant.URLS);
                    str = b(NumberInfo.TYPE_KEY, str2, Constant.URLS, str3, "titleNo", str);
                    jSONObject.remove(Constant.URLS);
                    jSONObject.put("action_data", str);
                } else if ("call_phone".equalsIgnoreCase(str2) || "call".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get("phone");
                    str = b(NumberInfo.TYPE_KEY, str2, "phone", str3, "titleNo", str);
                    jSONObject.remove("phone");
                    jSONObject.put("action_data", str);
                } else if ("map_site".equalsIgnoreCase(str2) || "open_map".equalsIgnoreCase(str2)) {
                    str2 = (String) jSONObject.get("address");
                    str = b(NumberInfo.TYPE_KEY, "WEB_MAP_SITE", "address", str2, "titleNo", str);
                    jSONObject.remove("address");
                    jSONObject.remove("action");
                    jSONObject.put("action", "WEB_MAP_SITE");
                    jSONObject.put("action_data", str);
                } else if ("recharge".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get("phone");
                    jSONObject.put("action_data", b(NumberInfo.TYPE_KEY, str2, "titleNo", str, "phone", str3));
                } else if ("copy_code".equalsIgnoreCase(str2)) {
                    str3 = (String) jSONObject.get("code");
                    str = b(NumberInfo.TYPE_KEY, str2, "titleNo", str, "code", str3);
                    jSONObject.remove("code");
                    jSONObject.put("action_data", str);
                } else if ("WEB_TRAFFIC_ORDER".equalsIgnoreCase(str2)) {
                    try {
                        r0 = a(NumberInfo.TYPE_KEY, str2, "titleNo", str);
                        r0.put("config", jSONObject.get("config"));
                        str = a(r0);
                        jSONObject.remove("config");
                        jSONObject.put("action_data", str);
                    } catch (Throwable th) {
                    }
                } else if ("WEB_INSTALMENT_PLAN".equalsIgnoreCase(str2)) {
                    try {
                        str4 = "";
                        Float valueOf = Float.valueOf(0.0f);
                        Integer valueOf2 = Integer.valueOf(0);
                        try {
                            valueOf2 = (Integer) jSONObject.get("amount");
                        } catch (Throwable th2) {
                        }
                        if (valueOf2.intValue() == 0) {
                            try {
                                valueOf = (Float) jSONObject.get("amount");
                            } catch (Throwable th3) {
                            }
                        }
                        str3 = valueOf2.intValue() == 0 ? str4 : String.valueOf(valueOf2);
                        if (valueOf.floatValue() != 0.0f) {
                            str3 = String.valueOf(valueOf);
                        }
                        r0 = a(NumberInfo.TYPE_KEY, str2, "titleNo", str, "amount", str3);
                        r0.put("config", jSONObject.get("config"));
                        r0.put("budgetSmsTemplate", JsonUtil.getValueFromJsonObject(jSONObject, "budgetSmsTemplate"));
                        r0.put("phone", JsonUtil.getValueFromJsonObject(jSONObject, "phone"));
                        str = a(r0);
                        jSONObject.remove("budgetSmsTemplate");
                        jSONObject.remove("phone");
                        jSONObject.remove("config");
                        jSONObject.put("action_data", str);
                    } catch (Throwable th4) {
                    }
                } else if ("WEB_QUERY_EXPRESS_FLOW".equalsIgnoreCase(str2)) {
                    try {
                        str3 = (String) jSONObject.get("express_name");
                        str4 = (String) jSONObject.get("express_no");
                        r0 = a(NumberInfo.TYPE_KEY, str2, "titleNo", str, "express_name", str3, "express_no", str4);
                        r0.put("postValue", a("express_name", str3, "express_no", str4));
                        str = a(r0);
                        jSONObject.remove("express_name");
                        jSONObject.remove("express_no");
                        jSONObject.put("action_data", str);
                    } catch (Throwable th5) {
                    }
                } else if ("WEB_QUERY_FLIGHT_TREND".equalsIgnoreCase(str2)) {
                    try {
                        str3 = (String) jSONObject.get("flight_num");
                        str4 = (String) jSONObject.get("flight_date");
                        String str5 = (String) jSONObject.get("flight_from");
                        String str6 = (String) jSONObject.get("flight_to");
                        r0 = a(NumberInfo.TYPE_KEY, str2, "titleNo", str, "flight_num", str3, "flight_date", str4, "flight_from", str5, "flight_to", str6);
                        r0.put("postValue", a("flight_num", str3, "flight_date", str4, "flight_from", str5, "flight_to", str6));
                        str = a(r0);
                        jSONObject.remove("flight_num");
                        jSONObject.remove("flight_date");
                        jSONObject.remove("flight_from");
                        jSONObject.remove("flight_to");
                        jSONObject.put("action_data", str);
                    } catch (Throwable th6) {
                    }
                }
                return jSONObject;
            } catch (Throwable th7) {
            }
        }
        return null;
    }

    private static JSONObject a(String... strArr) {
        if (strArr.length % 2 != 0) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        for (int i = 0; i < strArr.length; i += 2) {
            try {
                jSONObject.put(strArr[i], strArr[i + 1]);
            } catch (Throwable th) {
            }
        }
        return jSONObject;
    }

    private static boolean a(int i) {
        return i == 1;
    }

    private static String b(String... strArr) {
        try {
            JSONObject jSONObject = new JSONObject();
            if (strArr.length % 2 != 0) {
                return "";
            }
            for (int i = 0; i < strArr.length; i += 2) {
                jSONObject.put(strArr[i], strArr[i + 1]);
            }
            return StringUtils.encode(jSONObject.toString());
        } catch (Throwable th) {
            return "";
        }
    }

    private static boolean b(int i) {
        return i == 0 || i == 4 || i == 5 || i == 2 || i == 3;
    }

    public static Map<String, String> changeObjMapToStrMap(HashMap<String, Object> hashMap) {
        try {
            Object hashMap2 = new HashMap();
            if (!(hashMap == null || hashMap.isEmpty())) {
                for (Entry entry : hashMap.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        hashMap2.put((String) entry.getKey(), (String) entry.getValue());
                    }
                }
            }
            return hashMap2;
        } catch (Throwable th) {
            return null;
        }
    }

    public static int checkNetWork(Context context) {
        return checkNetWork(context, 1);
    }

    public static int checkNetWork(Context context, int i) {
        if (context == null) {
            return -1;
        }
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
            int type = activeNetworkInfo.getType();
            switch (i) {
                case 0:
                    return !b(type) ? 1 : 0;
                case 1:
                    return (DuoquUtils.getSdkDoAction().getWifiType(context) == 1 || !a(type)) ? 1 : 0;
                case 2:
                    return (a(type) || b(type)) ? 0 : 1;
                default:
                    break;
            }
        }
        return -1;
    }

    public static void chmod(String str, String str2) {
        try {
            a.e.execute(new H(str, str2));
        } catch (Throwable th) {
        }
    }

    public static void chmodSyn(String str, String str2) {
        try {
            if (!StringUtils.isNull(str2)) {
                String trim = str2.trim();
                if (trim.indexOf(" ") == -1) {
                    Runtime.getRuntime().exec("chmod " + str + " " + trim);
                }
            }
        } catch (Throwable th) {
        }
    }

    public static byte[] decompressBytes(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        byte[] bArr2 = new byte[0];
        Inflater inflater = new Inflater();
        inflater.reset();
        inflater.setInput(bArr);
        Closeable byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
        try {
            byte[] bArr3 = new byte[2560];
            long currentTimeMillis = System.currentTimeMillis();
            int i = 0;
            while (!inflater.finished()) {
                if (!inflater.needsInput()) {
                    int inflate = inflater.inflate(bArr3);
                    if (inflate > 0) {
                        byteArrayOutputStream.write(bArr3, 0, inflate);
                    }
                    if (i % SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE == 1) {
                        Thread.sleep(1);
                    }
                    if ((System.currentTimeMillis() - currentTimeMillis > 10000 ? 1 : 0) != 0) {
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            bArr2 = byteArrayOutputStream.toByteArray();
            try {
                inflater.reset();
                f.a(byteArrayOutputStream);
                inflater.end();
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
        return bArr2;
    }

    public static void doXycallBack(XyCallBack xyCallBack, String str) {
        if (xyCallBack != null) {
            try {
                xyCallBack.execute(str);
            } catch (Throwable th) {
            }
        }
    }

    public static void doXycallBackResult(XyCallBack xyCallBack, Object... objArr) {
        if (xyCallBack != null) {
            try {
                xyCallBack.execute(objArr);
            } catch (Throwable th) {
            }
        }
    }

    public static boolean getBoolean(Map<String, String> map, String str, boolean z) {
        if (!(map == null || map.isEmpty())) {
            try {
                z = Boolean.parseBoolean((String) map.get(str));
            } catch (Throwable th) {
                return z;
            }
        }
        return z;
    }

    public static String getIccid() {
        String iccidBySimIndex = DuoquUtils.getSdkDoAction().getIccidBySimIndex(0);
        if (StringUtils.isNull(iccidBySimIndex)) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) Constant.getContext().getSystemService("phone");
                if (!StringUtils.isNull(telephonyManager.getSimSerialNumber())) {
                    return telephonyManager.getSimSerialNumber();
                }
            } catch (Throwable th) {
            }
        }
        return iccidBySimIndex;
    }

    public static String getImei(Context context) {
        if (context != null) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (!(telephonyManager == null || telephonyManager.getDeviceId() == null)) {
                    return telephonyManager.getDeviceId();
                }
            } catch (Throwable th) {
            }
        }
        return DEFAULT_IMEI;
    }

    public static String getImeiAndXinghao(Context context) {
        return getImei(context) + ";" + getPhoneModel(context);
    }

    public static LineNumberReader getLineByCompressFile(String str) {
        try {
            return new LineNumberReader(new StringReader(new String(decompressBytes(f.e(str)), "UTF-8")));
        } catch (Throwable th) {
            return null;
        }
    }

    public static synchronized JSONObject getLoaction() {
        synchronized (XyUtil.class) {
            if (b == null) {
                try {
                    String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.LOACTION);
                    if (!StringUtils.isNull(stringParam)) {
                        b = new JSONObject(StringUtils.decode(stringParam));
                    }
                } catch (Throwable th) {
                }
                JSONObject jSONObject = b;
                return jSONObject;
            }
            jSONObject = b;
            return jSONObject;
        }
    }

    public static String getPhoneModel(Context context) {
        return Build.MODEL + "," + Build.BRAND;
    }

    public static String getPhoneNumber(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService("phone")).getLine1Number();
        } catch (Throwable th) {
            return "";
        }
    }

    public static String getSceneServiceAction(Map<String, Object> map) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("action", "display_scene_result");
            jSONObject.put("btn_name", new StringBuilder(String.valueOf((String) map.get("title_name"))).append("服务").toString());
            map.remove("is_return");
            map.remove("mactchs_id");
            map.remove(DistrictSearchQuery.KEYWORDS_PROVINCE);
            map.remove("popup_type");
            map.remove(NumberInfo.VERSION_KEY);
            map.remove(Constant.RECOGNIZE_LEVEL);
            map.remove("channel");
            map.remove("power");
            map.remove("smsCenterNum");
            map.remove("phoneNum");
            StringBuffer stringBuffer = new StringBuffer();
            for (Entry entry : map.entrySet()) {
                if (entry.getValue() instanceof String) {
                    stringBuffer.append(new StringBuilder(String.valueOf((String) entry.getKey())).append(": ").append(entry.getValue()).append("<br/>").toString());
                } else if (entry.getValue() instanceof String[]) {
                    String[] strArr = (String[]) entry.getValue();
                    if (strArr != null) {
                        stringBuffer.append(new StringBuilder(String.valueOf((String) entry.getKey())).append(":[").toString());
                        r2 = strArr.length;
                        for (int i = 0; i < r2; i++) {
                            stringBuffer.append(strArr[i]);
                            if (i != r2 - 1) {
                                stringBuffer.append(",");
                            }
                        }
                        stringBuffer.append("]<br/>");
                    }
                } else if (entry.getValue() instanceof List) {
                    List list = (List) entry.getValue();
                    if (list != null) {
                        stringBuffer.append(new StringBuilder(String.valueOf((String) entry.getKey())).append(":[").toString());
                        int size = list.size();
                        for (r2 = 0; r2 < size; r2++) {
                            Object obj = list.get(r2);
                            if (obj != null) {
                                if (obj instanceof String) {
                                    stringBuffer.append(obj.toString());
                                } else if (obj instanceof String[]) {
                                    String[] strArr2 = (String[]) obj;
                                    int length = strArr2.length;
                                    stringBuffer.append("[");
                                    for (int i2 = 0; i2 < length; i2++) {
                                        stringBuffer.append(strArr2[i2]);
                                        if (i2 != length - 1) {
                                            stringBuffer.append(",");
                                        }
                                    }
                                    stringBuffer.append("]");
                                } else {
                                    stringBuffer.append(obj.toString());
                                }
                                if (r2 != size - 1) {
                                    stringBuffer.append(",");
                                }
                            }
                        }
                        stringBuffer.append("]<br/>");
                    }
                }
            }
            jSONObject.put("action_data", b(NumberInfo.TYPE_KEY, "display_scene_result", "dataresult", stringBuffer.toString(), "titleNo", (String) map.get("title_num")));
            JSONArray jSONArray = new JSONArray();
            jSONArray.put(jSONObject);
            return jSONArray.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    public static int getSimIndex(Map<String, Object> map) {
        if (map == null) {
            return -1;
        }
        int intValue;
        String str = (String) map.get("simIndex");
        if (str != null) {
            try {
                intValue = Integer.valueOf(str).intValue();
            } catch (Throwable th) {
            }
            return intValue;
        }
        intValue = -1;
        return intValue;
    }

    public static String getXyValue() {
        if (a == null) {
            a = PopupUtil.getValue();
        }
        return a;
    }

    public static void handleMapAction(Map<String, Object> map) {
        if (map != null && !map.isEmpty()) {
            String str = (String) map.get("ADACTION");
            if (!StringUtils.isNull(str)) {
                str = a((Map) map, str);
                if (!StringUtils.isNull(str)) {
                    map.put("ADACTION", str);
                }
            }
        }
    }

    public static boolean isFlagOn(String str, Map map) {
        if (map != null) {
            try {
                if (Boolean.TRUE.toString().equalsIgnoreCase((String) map.get(str))) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static boolean isProvinceUsable(String str, String str2) {
        try {
            return !StringUtils.isNull(str) ? (!StringUtils.isNull(str) && str.equals("*")) ? true : (StringUtils.isNull(str2) || StringUtils.isNull(str) || str.replaceAll("，", ",").replaceAll("；", ";").replaceAll("市", "").indexOf(str2) == -1) ? false : true : true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static void removeAreaCodeInfo() {
        SysParamEntityManager.setParam("areaCode", "");
    }

    public static void removeLoactionInfo() {
        SysParamEntityManager.setParam(Constant.LOACTION, "");
    }

    public static void setLoaction(JSONObject jSONObject) {
        b = jSONObject;
    }

    public static void setLoactionInfo(double d, double d2) {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(Constant.LOACTION_LATITUDE, d);
        jSONObject.put(Constant.LOACTION_LONGITUDE, d2);
        jSONObject.put(Constant.LOACTION_TIME, System.currentTimeMillis());
        SysParamEntityManager.setParam(Constant.LOACTION, StringUtils.encode(jSONObject.toString()));
        setLoaction(jSONObject);
    }

    public static void unZip(InputStream inputStream, String str, String str2) {
        unZip(inputStream, str, str2, false, "", false);
    }

    public static void unZip(InputStream inputStream, String str, String str2, boolean z, String str3, boolean z2) {
        Closeable fileOutputStream;
        Throwable th;
        File file;
        Closeable closeable = null;
        try {
            File file2 = new File(new StringBuilder(String.valueOf(str2)).append(str).toString());
            try {
                if (!file2.exists()) {
                    file2.createNewFile();
                }
                fileOutputStream = new FileOutputStream(file2);
            } catch (Throwable th2) {
                th = th2;
                file = file2;
                file.delete();
                f.a(closeable);
                f.a((Closeable) inputStream);
                throw th;
            }
            try {
                byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
                upZipFile(file2, str2, z, str3, z2);
                file2.delete();
                if (file2.exists()) {
                    file2.delete();
                }
                f.a(fileOutputStream);
                f.a((Closeable) inputStream);
            } catch (Throwable th3) {
                th = th3;
                closeable = fileOutputStream;
                file = file2;
                file.delete();
                f.a(closeable);
                f.a((Closeable) inputStream);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            file = null;
            if (file != null && file.exists()) {
                file.delete();
            }
            f.a(closeable);
            f.a((Closeable) inputStream);
            throw th;
        }
    }

    public static void upZipFile(File file, String str) {
        upZipFile(file, str, false, "", false);
    }

    public static void upZipFile(File file, String str, boolean z, String str2, boolean z2) {
        ZipFile zipFile;
        Closeable closeable;
        Closeable closeable2;
        Object obj;
        Object obj2;
        ZipFile zipFile2;
        Throwable th;
        Closeable closeable3 = null;
        try {
            File file2 = new File(str);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            zipFile = new ZipFile(file);
            try {
                Enumeration entries = zipFile.entries();
                closeable = null;
                closeable2 = null;
                while (entries.hasMoreElements()) {
                    try {
                        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                        if (!StringUtils.isNull(zipEntry.getName())) {
                            String str3 = new String(zipEntry.getName().getBytes("8859_1"), "GB2312");
                            if (z2) {
                                if (!StringUtils.isNull(str3) && str3.endsWith("jar")) {
                                    String replace = str3.replace(".jar", "");
                                    h.a(replace, str2, h.c(replace));
                                }
                            }
                            if (z && !StringUtils.isNull(str3) && str3.endsWith("jar")) {
                                str3 = str3.replace(".jar", "_" + str2 + ".jar").replaceAll("\\.\\.", "");
                            }
                            str3 = new StringBuilder(String.valueOf(str)).append(File.separator).append(str3).toString();
                            boolean z3 = LogManager.debug;
                            File file3 = new File(str3);
                            if (file3.isDirectory()) {
                                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "upZipFile is dir: " + file3.getName(), null);
                            } else {
                                if (file3.exists()) {
                                    file3.delete();
                                } else {
                                    File parentFile = file3.getParentFile();
                                    if (!parentFile.exists()) {
                                        parentFile.mkdirs();
                                    }
                                    file3.createNewFile();
                                }
                                OutputStream fileOutputStream = new FileOutputStream(file3);
                                try {
                                    byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                                    if (inputStream != null) {
                                        while (true) {
                                            int read = inputStream.read(bArr);
                                            if (read <= 0) {
                                                break;
                                            }
                                            fileOutputStream.write(bArr, 0, read);
                                        }
                                        inputStream.close();
                                    }
                                    try {
                                        fileOutputStream.close();
                                        closeable = fileOutputStream;
                                        closeable2 = inputStream;
                                    } catch (Throwable th2) {
                                        Object obj3 = inputStream;
                                        th = th2;
                                        obj = fileOutputStream;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    obj = fileOutputStream;
                                }
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
                f.a(zipFile);
                f.a(closeable2);
                f.a(closeable);
            } catch (Throwable th22) {
                closeable2 = null;
                th = th22;
                closeable = null;
                f.a(zipFile);
                f.a(closeable2);
                f.a(closeable);
                throw th;
            }
        } catch (Throwable th222) {
            closeable2 = null;
            zipFile = null;
            Throwable th5 = th222;
            closeable = null;
            th = th5;
            f.a(zipFile);
            f.a(closeable2);
            f.a(closeable);
            throw th;
        }
    }

    public static boolean upZipFile(String str, String str2) {
        ZipFile zipFile;
        Closeable closeable;
        ZipFile zipFile2;
        Throwable th;
        Throwable th2;
        Closeable closeable2 = null;
        Closeable closeable3;
        try {
            File file = new File(str2);
            if (!file.exists()) {
                file.mkdirs();
            }
            zipFile = new ZipFile(new File(str));
            try {
                Enumeration entries = zipFile.entries();
                closeable = null;
                closeable3 = null;
                while (entries.hasMoreElements()) {
                    try {
                        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                        if (!StringUtils.isNull(zipEntry.getName())) {
                            closeable = zipFile.getInputStream(zipEntry);
                            File file2 = new File(new StringBuilder(String.valueOf(str2)).append(File.separator).append(new String(zipEntry.getName().getBytes("8859_1"), "GB2312").replace("\\.\\.", "")).toString());
                            if (file2.exists()) {
                                file2.delete();
                            } else {
                                File parentFile = file2.getParentFile();
                                if (!parentFile.exists()) {
                                    parentFile.mkdirs();
                                }
                                file2.createNewFile();
                            }
                            closeable2 = new FileOutputStream(file2);
                            try {
                                byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                                while (true) {
                                    int read = closeable.read(bArr);
                                    if (read <= 0) {
                                        break;
                                    }
                                    closeable2.write(bArr, 0, read);
                                }
                                f.a(closeable);
                                f.a(closeable2);
                                closeable3 = closeable2;
                            } catch (Throwable th3) {
                                th = th3;
                                closeable3 = closeable2;
                                th2 = th;
                            }
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                f.a(zipFile);
                f.a(closeable);
                f.a(closeable3);
                return true;
            } catch (Throwable th5) {
                closeable3 = null;
                th2 = th5;
                closeable = null;
                f.a(zipFile);
                f.a(closeable);
                f.a(closeable3);
                throw th2;
            }
        } catch (Throwable th52) {
            closeable3 = null;
            zipFile = null;
            th = th52;
            closeable = null;
            th2 = th;
            f.a(zipFile);
            f.a(closeable);
            f.a(closeable3);
            throw th2;
        }
    }
}
