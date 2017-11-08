package cn.com.xy.sms.sdk.service.c;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.o;
import cn.com.xy.sms.sdk.db.entity.p;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a {
    public static JSONObject a(String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        int i = 1;
        if (StringUtils.isNull(str)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num is null");
            return null;
        }
        JSONObject a = o.a(str);
        if (a != null) {
            try {
                int i2;
                String optString = a.optString("updateInfoTime");
                JSONObject jSONObject = new JSONObject(a.optString("actions"));
                long parseLong = Long.parseLong(optString);
                if (!Constant.FIND_CMD_STATUS.equalsIgnoreCase(jSONObject.optString("status"))) {
                    if ((System.currentTimeMillis() <= 21600000 + parseLong ? 1 : 0) == 0) {
                        i2 = 1;
                        if (i2 == 0) {
                            if (System.currentTimeMillis() > parseLong + DexUtil.getUpdateCycleByType(27, Constant.month)) {
                                i = 0;
                            }
                            if (i != 0) {
                                XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                                return jSONObject;
                            }
                        }
                        if (XyUtil.checkNetWork(Constant.getContext()) == -1) {
                            d(str, str2, hashMap, sdkCallBack);
                        } else {
                            XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                            return jSONObject;
                        }
                    }
                }
                i2 = 0;
                if (i2 == 0) {
                    if (System.currentTimeMillis() > parseLong + DexUtil.getUpdateCycleByType(27, Constant.month)) {
                        i = 0;
                    }
                    if (i != 0) {
                        XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                        return jSONObject;
                    }
                }
                if (XyUtil.checkNetWork(Constant.getContext()) == -1) {
                    XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                    return jSONObject;
                }
                d(str, str2, hashMap, sdkCallBack);
            } catch (Throwable th) {
            }
        } else if (XyUtil.checkNetWork(Constant.getContext()) != -1) {
            d(str, str2, hashMap, sdkCallBack);
        } else {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "have no available net work");
        }
        return null;
    }

    private static boolean a(JSONObject jSONObject, long j) {
        if (!Constant.FIND_CMD_STATUS.equalsIgnoreCase(jSONObject.optString("status"))) {
            if (!(System.currentTimeMillis() <= 21600000 + j)) {
                return true;
            }
        }
        return false;
    }

    public static JSONObject b(String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        if (StringUtils.allValuesIsNotNull(str, str2)) {
            JSONObject a = p.a(str, str2);
            if (a != null) {
                try {
                    String optString = a.optString("updateInfoTime");
                    String optString2 = a.optString("result");
                    JSONObject jSONObject = new JSONObject(optString2);
                    long parseLong = Long.parseLong(optString);
                    if (Constant.EMPTY_JSON.equals(optString2)) {
                        if ((System.currentTimeMillis() > 21600000 + parseLong ? 1 : null) == null) {
                        }
                        if (XyUtil.checkNetWork(Constant.getContext()) == -1) {
                            e(str, str2, hashMap, sdkCallBack);
                        } else {
                            XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                            return jSONObject;
                        }
                    }
                    if ((System.currentTimeMillis() <= parseLong + DexUtil.getUpdateCycleByType(26, Constant.month) ? 1 : null) != null) {
                        XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                        return jSONObject;
                    }
                    if (XyUtil.checkNetWork(Constant.getContext()) == -1) {
                        XyUtil.doXycallBackResult(sdkCallBack, "0", jSONObject);
                        return jSONObject;
                    }
                    e(str, str2, hashMap, sdkCallBack);
                } catch (Throwable th) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "error:" + th.getMessage());
                }
            } else if (XyUtil.checkNetWork(Constant.getContext()) != -1) {
                e(str, str2, hashMap, sdkCallBack);
            } else {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "have no available net work");
            }
            return null;
        }
        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num or sms content is null");
        return null;
    }

    private static boolean b(Map map) {
        if (map == null || !map.containsKey("isNewThread")) {
            return true;
        }
        return "true".equalsIgnoreCase((String) map.get("isNewThread"));
    }

    public static JSONObject c(String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        if (StringUtils.allValuesIsNotNull(str, str2)) {
            return b(str, str2, hashMap, sdkCallBack);
        }
        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num or sms content is null");
        return null;
    }

    private static void d(String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num is null");
            return;
        }
        String a = j.a(str, str2, (Map) hashMap);
        if (StringUtils.isNull(a)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "request content error");
            return;
        }
        NetUtil.requestNewTokenIfNeed(hashMap);
        try {
            NetUtil.executeNewServiceHttpRequest(NetUtil.REQ_QUERY_OPERATOR, a, new b(a, hashMap, NetUtil.getToken(), sdkCallBack, str, str2), b(hashMap), false, true, hashMap);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "request error:" + th.getMessage());
        }
    }

    private static void e(String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        if (StringUtils.allValuesIsNotNull(str, str2)) {
            String b = j.b(str, str2, hashMap);
            if (StringUtils.isNull(b)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "request content error");
                return;
            }
            NetUtil.requestNewTokenIfNeed(hashMap);
            try {
                NetUtil.executeNewServiceHttpRequest(NetUtil.REQ_QUERY_OPERATOR_MSG, b, new c(b, hashMap, NetUtil.getToken(), sdkCallBack, str, str2), b(hashMap), false, true, hashMap);
                return;
            } catch (Throwable th) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "request error:" + th.getMessage());
                return;
            }
        }
        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num or sms content is null");
    }
}
