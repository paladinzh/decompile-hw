package cn.com.xy.sms.sdk.service.number;

import android.util.Pair;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.number.k;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a {
    private static Pair<Double, Double> a() {
        try {
            JSONObject loaction = XyUtil.getLoaction();
            if (loaction != null) {
                return new Pair(Double.valueOf(loaction.optDouble(Constant.LOACTION_LATITUDE, 0.0d)), Double.valueOf(loaction.optDouble(Constant.LOACTION_LONGITUDE, 0.0d)));
            }
        } catch (Throwable th) {
        }
        return null;
    }

    private static String a(Map<String, String> map, Map<String, String> map2) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("items", b((Map) map));
            Pair d = d(map2);
            if (d == null) {
                d = a();
            } else {
                a(d);
            }
            Pair pair = d;
            if (!(pair == null || ((Double) pair.first).doubleValue() == 0.0d || ((Double) pair.second).doubleValue() == 0.0d)) {
                jSONObject.put(Constant.LOACTION_LATITUDE, pair.first);
                jSONObject.put(Constant.LOACTION_LONGITUDE, pair.second);
            }
            return jSONObject.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    private static void a(Pair<Double, Double> pair) {
        try {
            XyUtil.setLoactionInfo(((Double) pair.first).doubleValue(), ((Double) pair.second).doubleValue());
            XyUtil.removeAreaCodeInfo();
        } catch (Throwable th) {
        }
    }

    public static void a(Map<String, String> map, Map<String, String> map2, XyCallBack xyCallBack) {
        Set set = null;
        if (map == null || map.isEmpty()) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), null, "no number");
            return;
        }
        if (map != null) {
            set = map.keySet();
        }
        String a = a(map, map2);
        if (StringUtils.isNull(a)) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), set, "reqeustContent == null");
            return;
        }
        try {
            NetUtil.executeServiceHttpRequest(NetUtil.REQ_QUERY_NUM, a, map2, new b(xyCallBack, set));
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), set, th.getMessage());
        }
    }

    private static void a(JSONObject jSONObject) {
        if (jSONObject != null) {
            String optString = jSONObject.optString("areaCode");
            if (!(StringUtils.isNull(optString) || optString.equals(SysParamEntityManager.getStringParam(Constant.getContext(), "areaCode")))) {
                SysParamEntityManager.setParam("areaCode", optString);
            }
        }
    }

    public static boolean a(Map<String, String> map) {
        return (map == null || StringUtils.isNull((String) map.get(Constant.LOACTION_LATITUDE)) || StringUtils.isNull((String) map.get(Constant.LOACTION_LONGITUDE))) ? false : true;
    }

    private static JSONArray b(Map<String, String> map) {
        JSONArray jSONArray = new JSONArray();
        for (Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            if (!StringUtils.isNull(str)) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put(NumberInfo.NUM_KEY, str);
                    String str2 = (String) entry.getValue();
                    if (!StringUtils.isNull(str2)) {
                        jSONObject.put(NumberInfo.VERSION_KEY, str2);
                    }
                    jSONArray.put(jSONObject);
                } catch (Throwable th) {
                }
            }
        }
        return jSONArray;
    }

    private static JSONArray b(JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() == 0) {
            return null;
        }
        JSONArray jSONArray2 = new JSONArray();
        long currentTimeMillis = System.currentTimeMillis();
        int length = jSONArray.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                String optString = jSONObject.optString(NumberInfo.NUM_KEY);
                if (!StringUtils.isNull(optString) && n.b(jSONObject)) {
                    Object obj;
                    new StringBuilder("ParseNumberService net numinfo:").append(jSONObject);
                    if (jSONObject.length() == 2 && jSONObject.has(NumberInfo.NUM_KEY) && jSONObject.has(NumberInfo.VERSION_KEY)) {
                        int i2 = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        if (jSONObject != null) {
                            String optString2 = jSONObject.optString("areaCode");
                            if (!(StringUtils.isNull(optString2) || optString2.equals(SysParamEntityManager.getStringParam(Constant.getContext(), "areaCode")))) {
                                SysParamEntityManager.setParam("areaCode", optString2);
                            }
                        }
                        if ((n.a(optString, jSONObject, jSONObject.optString(NumberInfo.VERSION_KEY), currentTimeMillis) <= 0 ? 1 : null) == null) {
                            k.a(optString, jSONObject);
                            k.d(optString);
                            jSONArray2.put(jSONObject);
                        }
                    } else {
                        n.a(optString, System.currentTimeMillis());
                    }
                }
            } catch (Throwable th) {
            }
        }
        return jSONArray2;
    }

    private static boolean b(JSONObject jSONObject) {
        return jSONObject.length() == 2 && jSONObject.has(NumberInfo.NUM_KEY) && jSONObject.has(NumberInfo.VERSION_KEY);
    }

    private static Pair<Double, Double> c(Map<String, String> map) {
        Pair<Double, Double> d = d(map);
        if (d == null) {
            return a();
        }
        try {
            XyUtil.setLoactionInfo(((Double) d.first).doubleValue(), ((Double) d.second).doubleValue());
            XyUtil.removeAreaCodeInfo();
        } catch (Throwable th) {
        }
        return d;
    }

    private static Pair<Double, Double> d(Map<String, String> map) {
        if (!a((Map) map)) {
            return null;
        }
        try {
            return new Pair(Double.valueOf(Double.parseDouble((String) map.get(Constant.LOACTION_LATITUDE))), Double.valueOf(Double.parseDouble((String) map.get(Constant.LOACTION_LONGITUDE))));
        } catch (Throwable th) {
            return null;
        }
    }
}
