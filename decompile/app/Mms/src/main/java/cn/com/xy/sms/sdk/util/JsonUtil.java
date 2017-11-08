package cn.com.xy.sms.sdk.util;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.google.android.gms.common.Scopes;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class JsonUtil {
    public static void JSONCombine(JSONObject jSONObject, JSONObject jSONObject2) {
        if (jSONObject != null) {
            try {
                Iterator keys = jSONObject2.keys();
                while (keys.hasNext()) {
                    String str = (String) keys.next();
                    jSONObject.put(str, jSONObject2.get(str));
                }
            } catch (Throwable th) {
            }
        }
    }

    public static JSONObject changeMapToJason(Map<String, Object> map) {
        if (map != null) {
            try {
                if (!map.isEmpty()) {
                    return new JSONObject(map);
                }
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    public static JSONObject getJsonObject(JSONObject jSONObject, String... strArr) {
        int i = 0;
        if (strArr == null || jSONObject == null) {
            return null;
        }
        int length = strArr.length;
        if (length == 0 || length % 2 != 0) {
            return null;
        }
        while (i < length) {
            try {
                if (!(strArr[i] == null || strArr[i + 1] == null)) {
                    jSONObject.put(strArr[i], strArr[i + 1]);
                }
                i += 2;
            } catch (JSONException e) {
                return null;
            }
        }
        return jSONObject;
    }

    public static JSONObject getJsonObject(String... strArr) {
        int i = 0;
        if (strArr == null) {
            return null;
        }
        int length = strArr.length;
        if (length == 0 || length % 2 != 0) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            while (i < length) {
                if (!(strArr[i] == null || strArr[i + 1] == null)) {
                    jSONObject.put(strArr[i], strArr[i + 1]);
                }
                i += 2;
            }
            return jSONObject;
        } catch (JSONException e) {
            return null;
        }
    }

    public static long getLongValueFromJsonObject(JSONObject jSONObject, String str) {
        long j = 0;
        try {
            String stringValueFromJsonObject = getStringValueFromJsonObject(jSONObject, str);
            if (!StringUtils.isNull(stringValueFromJsonObject)) {
                j = Long.valueOf(stringValueFromJsonObject).longValue();
            }
        } catch (Throwable th) {
        }
        return j;
    }

    public static String getStringValueFromJsonObject(JSONObject jSONObject, String str) {
        Object valueFromJsonObject = getValueFromJsonObject(jSONObject, str);
        return valueFromJsonObject != null ? valueFromJsonObject.toString() : "";
    }

    public static Object getValFromJsonObject(JSONObject jSONObject, String str) {
        if (!(str == null || jSONObject == null)) {
            try {
                if (jSONObject.has(str)) {
                    return jSONObject.get(str);
                }
            } catch (Throwable th) {
            }
        }
        return "";
    }

    public static String getValueFromJson(JSONObject jSONObject, String str, String str2) {
        if (jSONObject != null) {
            try {
                String optString = jSONObject.optString(str);
                return !StringUtils.isNull(optString) ? optString : str2;
            } catch (Throwable th) {
            }
        }
        return str2;
    }

    public static Object getValueFromJsonObject(JSONObject jSONObject, String str) {
        if (!(str == null || jSONObject == null)) {
            try {
                if (jSONObject.has(str)) {
                    return jSONObject.get(str);
                }
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public static Object getValueWithMap(Map<String, Object> map, String str) {
        if (map != null) {
            try {
                if (!(map.isEmpty() || StringUtils.isNull(str) || !map.containsKey(str))) {
                    return map.get(str);
                }
            } catch (Throwable th) {
            }
        }
        return "";
    }

    public static String jsonObjectToString(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                return jSONObject.toString();
            } catch (Exception e) {
            }
        }
        return "";
    }

    public static Map<String, String> parseJSON2Map(String str) {
        Map<String, String> hashMap = new HashMap();
        try {
            JSONObject jSONObject = new JSONObject(str);
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String obj = keys.next().toString();
                hashMap.put(obj, jSONObject.optString(obj));
            }
        } catch (Throwable th) {
        }
        return hashMap;
    }

    public static JSONObject parseObjectToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            Class cls = obj.getClass();
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                jSONObject.put(field.getName(), field.get(obj));
            }
            jSONObject.put("objectToJson", true);
            jSONObject.put("className", cls.getName());
            return jSONObject;
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONArray parseStrToJsonArray(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        try {
            return new JSONArray(str);
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONObject parseStrToJsonObject(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        try {
            return new JSONObject(str.trim());
        } catch (Throwable th) {
            return null;
        }
    }

    public static String pubInfoToJson(JSONObject jSONObject) {
        return pubInfoToJson(jSONObject, null, null);
    }

    public static String pubInfoToJson(JSONObject jSONObject, String str, String str2) {
        if (jSONObject == null) {
            return "";
        }
        try {
            int length;
            JSONArray jSONArray;
            String str3;
            int i;
            JSONObject jSONObject2;
            JSONObject jSONObject3;
            String optString;
            String optString2;
            jSONObject.put("id", jSONObject.optString("pubId"));
            jSONObject.put("name", jSONObject.optString("pubName"));
            jSONObject.put("classifyName", jSONObject.optString("pubType"));
            jSONObject.put("weiboName", jSONObject.optString("weiBoName"));
            jSONObject.put("weiboUrl", jSONObject.optString("weiBoUrl"));
            jSONObject.put("weixin", jSONObject.optString("weiXin"));
            jSONObject.put(NumberInfo.LOGO_KEY, jSONObject.optString("rectLogoName"));
            jSONObject.put("logoc", jSONObject.optString("circleLogoName"));
            jSONObject.put("website", jSONObject.optString("webSite"));
            JSONArray optJSONArray = jSONObject.optJSONArray("pubNumInfolist");
            if (!StringUtils.isNull(str)) {
                if (!StringUtils.isNull(str2)) {
                    int i2 = 1;
                    if (optJSONArray != null && optJSONArray.length() > 0) {
                        length = optJSONArray.length();
                        jSONArray = new JSONArray();
                        str3 = null;
                        for (i = 0; i < length; i++) {
                            jSONObject2 = (JSONObject) optJSONArray.get(i);
                            jSONObject3 = new JSONObject();
                            optString = jSONObject2.optString(IccidInfoManager.NUM);
                            optString2 = jSONObject2.optString("areaCode");
                            if (r3 != null && optString2.contains(str2)) {
                                if (!str.equals(optString)) {
                                    if (!optString.contains("*")) {
                                        if (!str.startsWith(optString.replace("*", ""))) {
                                        }
                                    }
                                }
                                str3 = jSONObject2.optString("purpose");
                            }
                            jSONObject3.put("purpose", jSONObject2.optString("purpose"));
                            jSONObject3.put(IccidInfoManager.NUM, optString);
                            jSONObject3.put("areaCode", optString2);
                            jSONObject3.put("extend", jSONObject2.optString("extend"));
                            jSONArray.put(jSONObject3);
                        }
                        if (!StringUtils.isNull(str3)) {
                            jSONObject.put("purpose", str3);
                        }
                        jSONObject.put(NumberInfo.NUM_KEY, jSONArray);
                    } else {
                        jSONObject.put(NumberInfo.NUM_KEY, "");
                    }
                    jSONObject.remove("pubId");
                    jSONObject.remove("pubName");
                    jSONObject.remove("pubType");
                    jSONObject.remove("pubTypeCode");
                    jSONObject.remove("weiXin");
                    jSONObject.remove("weiBoName");
                    jSONObject.remove("weiBoUrl");
                    jSONObject.remove("introduce");
                    jSONObject.remove("address");
                    jSONObject.remove("faxNum");
                    jSONObject.remove("webSite");
                    jSONObject.remove("versionCode");
                    jSONObject.remove(Scopes.EMAIL);
                    jSONObject.remove("parentPubId");
                    jSONObject.remove("slogan");
                    jSONObject.remove("rectLogoName");
                    jSONObject.remove("circleLogoName");
                    jSONObject.remove("extend");
                    jSONObject.remove("pubNumInfolist");
                    jSONObject.remove("loadMenuTime");
                    jSONObject.remove("updateInfoTime");
                    jSONObject.remove("hasmenu");
                    return jSONObject.toString();
                }
            }
            Object obj = null;
            if (optJSONArray != null) {
                length = optJSONArray.length();
                jSONArray = new JSONArray();
                str3 = null;
                for (i = 0; i < length; i++) {
                    jSONObject2 = (JSONObject) optJSONArray.get(i);
                    jSONObject3 = new JSONObject();
                    optString = jSONObject2.optString(IccidInfoManager.NUM);
                    optString2 = jSONObject2.optString("areaCode");
                    if (str.equals(optString)) {
                        if (!optString.contains("*")) {
                            if (str.startsWith(optString.replace("*", ""))) {
                            }
                        }
                        jSONObject3.put("purpose", jSONObject2.optString("purpose"));
                        jSONObject3.put(IccidInfoManager.NUM, optString);
                        jSONObject3.put("areaCode", optString2);
                        jSONObject3.put("extend", jSONObject2.optString("extend"));
                        jSONArray.put(jSONObject3);
                    }
                    str3 = jSONObject2.optString("purpose");
                    jSONObject3.put("purpose", jSONObject2.optString("purpose"));
                    jSONObject3.put(IccidInfoManager.NUM, optString);
                    jSONObject3.put("areaCode", optString2);
                    jSONObject3.put("extend", jSONObject2.optString("extend"));
                    jSONArray.put(jSONObject3);
                }
                if (StringUtils.isNull(str3)) {
                    jSONObject.put("purpose", str3);
                }
                jSONObject.put(NumberInfo.NUM_KEY, jSONArray);
                jSONObject.remove("pubId");
                jSONObject.remove("pubName");
                jSONObject.remove("pubType");
                jSONObject.remove("pubTypeCode");
                jSONObject.remove("weiXin");
                jSONObject.remove("weiBoName");
                jSONObject.remove("weiBoUrl");
                jSONObject.remove("introduce");
                jSONObject.remove("address");
                jSONObject.remove("faxNum");
                jSONObject.remove("webSite");
                jSONObject.remove("versionCode");
                jSONObject.remove(Scopes.EMAIL);
                jSONObject.remove("parentPubId");
                jSONObject.remove("slogan");
                jSONObject.remove("rectLogoName");
                jSONObject.remove("circleLogoName");
                jSONObject.remove("extend");
                jSONObject.remove("pubNumInfolist");
                jSONObject.remove("loadMenuTime");
                jSONObject.remove("updateInfoTime");
                jSONObject.remove("hasmenu");
                return jSONObject.toString();
            }
            jSONObject.put(NumberInfo.NUM_KEY, "");
            jSONObject.remove("pubId");
            jSONObject.remove("pubName");
            jSONObject.remove("pubType");
            jSONObject.remove("pubTypeCode");
            jSONObject.remove("weiXin");
            jSONObject.remove("weiBoName");
            jSONObject.remove("weiBoUrl");
            jSONObject.remove("introduce");
            jSONObject.remove("address");
            jSONObject.remove("faxNum");
            jSONObject.remove("webSite");
            jSONObject.remove("versionCode");
            jSONObject.remove(Scopes.EMAIL);
            jSONObject.remove("parentPubId");
            jSONObject.remove("slogan");
            jSONObject.remove("rectLogoName");
            jSONObject.remove("circleLogoName");
            jSONObject.remove("extend");
            jSONObject.remove("pubNumInfolist");
            jSONObject.remove("loadMenuTime");
            jSONObject.remove("updateInfoTime");
            jSONObject.remove("hasmenu");
            return jSONObject.toString();
        } catch (Throwable th) {
            return "";
        }
    }

    public static void putJsonToConV(ContentValues contentValues, JSONObject jSONObject, String str, String str2) {
        String optString = jSONObject.optString(str2);
        if (StringUtils.isNull(optString)) {
            contentValues.remove(str);
        } else {
            contentValues.put(str, optString);
        }
    }

    public static void putJsonToMap(JSONObject jSONObject, Map<String, String> map) {
        if (!(jSONObject == null || map == null)) {
            try {
                Iterator keys = jSONObject.keys();
                while (keys.hasNext()) {
                    String str = (String) keys.next();
                    map.put(str, jSONObject.getString(str));
                }
            } catch (Throwable th) {
            }
        }
    }
}
