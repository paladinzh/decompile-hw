package cn.com.xy.sms.sdk.util;

import android.app.Activity;
import android.content.Context;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.a;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.util.w;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class DuoquUtils {
    private static AbsSdkDoAction a = null;
    private static String b = "DuoquUtils";
    public static n logSdkDoAction = null;
    public static AbsSdkDoAction sdkAction = null;

    public static boolean doAction(Activity activity, String str, Map<String, String> map) {
        try {
            getSdkDoAction().doAction(activity, str, map);
            logAction(activity, str, map);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean doActionContext(Context context, String str, Map<String, String> map) {
        try {
            getSdkDoAction().doAction(context, str, map);
            logAction(context, str, map);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean doCustomAction(Activity activity, String str, HashMap<String, Object> hashMap) {
        try {
            boolean z;
            String str2;
            Object obj;
            if ("call".equalsIgnoreCase(str)) {
                str2 = (String) hashMap.get("phone");
                obj = hashMap.get("simIndex");
                if (!StringUtils.isNull(str2)) {
                    if (obj == null) {
                        obj = Integer.valueOf(-1);
                    }
                    getSdkDoAction().callPhone(activity, str2, Integer.valueOf(obj.toString()).intValue());
                    z = true;
                    activity.finish();
                    if (hashMap != null) {
                        return z;
                    }
                    hashMap.clear();
                    return z;
                }
            } else if ("open_sms".equalsIgnoreCase(str) || "open_tongxunlu".equalsIgnoreCase(str) || "reply_sms_open".equalsIgnoreCase(str)) {
                str2 = (String) hashMap.get("phoneNum");
                if (StringUtils.isNull(str2)) {
                    str2 = (String) hashMap.get("phone");
                }
                r2 = str2;
                if (!StringUtils.isNull(r2)) {
                    Map map;
                    str2 = (String) hashMap.get("msgId");
                    if (StringUtils.isNull(str2)) {
                        map = null;
                    } else {
                        Map hashMap2 = new HashMap();
                        hashMap2.put("msgId", str2);
                        map = hashMap2;
                    }
                    getSdkDoAction().openSms(activity, r2, map);
                }
            } else if ("send_sms".equalsIgnoreCase(str)) {
                r2 = (String) hashMap.get("phoneNum");
                String str3 = (String) hashMap.get("smsCode");
                if (!(StringUtils.isNull(r2) || StringUtils.isNull(str3))) {
                    Object obj2 = hashMap.get("simIndex");
                    if (obj2 == null) {
                        obj2 = Integer.valueOf(-1);
                    }
                    obj = obj2;
                    getSdkDoAction().sendSms(activity, r2, str3, Integer.valueOf(obj.toString()).intValue(), null);
                    z = true;
                    if (z && hashMap != null) {
                        if (!(hashMap.containsKey("keepActivity") || activity == null || activity.isFinishing())) {
                            activity.finish();
                        }
                    }
                    if (hashMap != null) {
                        return z;
                    }
                    hashMap.clear();
                    return z;
                }
            } else if ("openApp".equalsIgnoreCase(str)) {
                str2 = String.valueOf(hashMap.get("appName"));
                if (StringUtils.isNull(str2)) {
                    str2 = String.valueOf(hashMap.get("exthend"));
                }
                String valueOf = String.valueOf(hashMap.get("appDownUrl"));
                if (StringUtils.isNull(str2)) {
                    str2 = String.valueOf(hashMap.get(Constant.URLS));
                    if (!StringUtils.isNull(str2)) {
                        getSdkDoAction().openAppByUrl(activity, str2, valueOf);
                    }
                } else {
                    getSdkDoAction().openAppByAppName(activity, str2, valueOf);
                }
            } else if ("toService".equalsIgnoreCase(str)) {
                JSONObject jSONObject = new JSONObject(hashMap);
                if (jSONObject.has("actionType")) {
                    str = jSONObject.getString("actionType");
                }
                getSdkDoAction().toService(activity, str, jSONObject);
            } else if ("download".equalsIgnoreCase(str)) {
                getSdkDoAction().downLoadApp(activity, String.valueOf(hashMap.get("appName")), String.valueOf(hashMap.get(Constant.URLS)), null);
            } else if (hashMap != null) {
                getSdkDoAction().exectueAction(activity, new JSONObject(hashMap), hashMap);
            }
            z = false;
            try {
                activity.finish();
            } catch (Throwable th) {
            }
            if (hashMap != null) {
                return z;
            }
            hashMap.clear();
            return z;
        } catch (Throwable th2) {
            if (hashMap != null) {
                hashMap.clear();
            }
        }
    }

    public static String getAI() {
        return m.a(w.d());
    }

    public static String getCode(int i) {
        String str = null;
        switch (i) {
            case 0:
                try {
                    str = f.b();
                    break;
                } catch (Throwable th) {
                    return "";
                }
            case 1:
                str = "3531333036463338";
                break;
            case 2:
                str = Constant.getXCode3();
                break;
            case 3:
                str = ViewUtil.getXCode4();
                break;
        }
        return str != null ? str : "";
    }

    public static Map<String, Object> getLogMap(Map<String, Object> map) {
        Map hashMap = new HashMap();
        if (map != null) {
            try {
                if (map.containsKey("logkey")) {
                    hashMap.put("logkey", map.get("logkey"));
                }
            } catch (Throwable th) {
            }
        }
        return hashMap;
    }

    public static n getLogSdkDoAction() {
        return logSdkDoAction == null ? null : logSdkDoAction;
    }

    public static String getNI() {
        return m.a(w.c());
    }

    public static String getPid() {
        String deviceId = a.getDeviceId(true);
        if (!StringUtils.isNull(deviceId)) {
            return deviceId;
        }
        deviceId = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.UNIQUE_CODE);
        return deviceId == null ? "" : deviceId;
    }

    public static AbsSdkDoAction getSdkDoAction() {
        if (sdkAction != null) {
            return sdkAction;
        }
        if (a == null) {
            a = new e();
        }
        String str = b;
        return a;
    }

    public static String getXid() {
        return j.b();
    }

    public static void logAction(Context context, String str, Map<String, String> map) {
        try {
            String str2 = ThemeUtil.SET_NULL_STR;
            JSONObject jSONObject = new JSONObject(StringUtils.decode(str));
            String optString = jSONObject.optString("action_type_id");
            if (StringUtils.isNull(optString)) {
                optString = (String) map.get("action");
                if (!StringUtils.isNull(optString)) {
                    optString = String.valueOf(DexUtil.getActionCode(optString));
                    str2 = optString;
                }
            } else {
                optString = optString.trim();
                str2 = optString;
            }
            optString = "";
            if (jSONObject.has("titleNo")) {
                optString = jSONObject.getString("titleNo");
            }
            if (StringUtils.isNull(optString)) {
                optString = "00000000";
            }
            if (!ThemeUtil.SET_NULL_STR.equals(str2)) {
                i.a(new k(5, "titleNo", optString, NumberInfo.TYPE_KEY, str2));
            }
            getSdkDoAction().statisticAction(optString, str2, null);
        } catch (Throwable th) {
        }
    }
}
