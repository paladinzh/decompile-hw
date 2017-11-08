package cn.com.xy.sms.sdk.service.f;

import cn.com.xy.sms.sdk.db.TrainManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
final class d implements SdkCallBack {
    private /* synthetic */ c a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;
    private final /* synthetic */ SdkCallBack g;

    d(c cVar, String str, String str2, String str3, String str4, String str5, SdkCallBack sdkCallBack) {
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = str5;
        this.g = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        JSONObject jSONObject;
        Object obj;
        JSONObject jSONObject2;
        Throwable th;
        String str = null;
        if (!StringUtils.isNull(this.b)) {
            try {
                jSONObject = new JSONObject(this.b);
            } catch (JSONException e) {
            }
            if (objArr != null && objArr.length == 2 && objArr[1] != null && "offNetwork".equalsIgnoreCase(objArr[1].toString())) {
                String str2 = "offNetwork";
            } else {
                obj = null;
            }
            if (objArr != null) {
                if (!(objArr.length != 6 || objArr[0] == null || objArr[1] == null)) {
                    if (((Boolean) objArr[5]).booleanValue()) {
                        if (!a.a(JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString(), (String) objArr[3], (String) objArr[4])) {
                            try {
                                XyUtil.doXycallBackResult(this.g, null);
                                if (!"true".equalsIgnoreCase(this.c)) {
                                    try {
                                        jSONObject2 = null;
                                        jSONObject2.put("QueryTime", System.currentTimeMillis());
                                        jSONObject2 = null;
                                        jSONObject2.put("networkState", obj);
                                        if (objArr != null && objArr.length > 1) {
                                            str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                                        }
                                        if (!StringUtils.isNull(str)) {
                                            jSONObject2 = null;
                                            jSONObject2.put(TrainManager.STATION_LIST, str);
                                        }
                                        if (objArr != null && objArr.length > 0) {
                                            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], null, this.f);
                                            return;
                                        }
                                    } catch (Exception e2) {
                                    }
                                }
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                jSONObject = null;
                                if (jSONObject != null || !"true".equalsIgnoreCase(this.c)) {
                                    try {
                                        jSONObject.put("QueryTime", System.currentTimeMillis());
                                        jSONObject.put("networkState", obj);
                                        if (objArr != null) {
                                            if (objArr.length > 1) {
                                                str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                                            }
                                        }
                                        if (!StringUtils.isNull(str)) {
                                            jSONObject.put(TrainManager.STATION_LIST, str);
                                        }
                                        if (objArr != null && objArr.length > 0) {
                                            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
                                        }
                                    } catch (Exception e3) {
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    XyUtil.doXycallBackResult(this.g, objArr[0], objArr[1], objArr[2], objArr[3], objArr[4], objArr[5]);
                    if (jSONObject != null || !"true".equalsIgnoreCase(this.c)) {
                        try {
                            jSONObject.put("QueryTime", System.currentTimeMillis());
                            jSONObject.put("networkState", obj);
                            if (objArr != null) {
                                if (objArr.length > 1) {
                                    str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                                }
                            }
                            if (!StringUtils.isNull(str)) {
                                jSONObject.put(TrainManager.STATION_LIST, str);
                            }
                            if (objArr != null && objArr.length > 0) {
                                MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
                                return;
                            }
                        } catch (Exception e4) {
                        }
                    }
                    return;
                }
            }
            XyUtil.doXycallBackResult(this.g, obj);
            if (jSONObject != null || !"true".equalsIgnoreCase(this.c)) {
                try {
                    jSONObject.put("QueryTime", System.currentTimeMillis());
                    jSONObject.put("networkState", obj);
                    if (objArr != null) {
                        if (objArr.length > 1) {
                            str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                        }
                    }
                    if (!StringUtils.isNull(str)) {
                        jSONObject.put(TrainManager.STATION_LIST, str);
                    }
                    if (objArr != null && objArr.length > 0) {
                        MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
                    }
                } catch (Exception e5) {
                }
            }
            return;
        }
        jSONObject = null;
        if (objArr != null) {
            String str22 = "offNetwork";
            if (objArr != null) {
                if (((Boolean) objArr[5]).booleanValue()) {
                    if (a.a(JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString(), (String) objArr[3], (String) objArr[4])) {
                        XyUtil.doXycallBackResult(this.g, null);
                        if ("true".equalsIgnoreCase(this.c)) {
                            jSONObject2 = null;
                            jSONObject2.put("QueryTime", System.currentTimeMillis());
                            jSONObject2 = null;
                            jSONObject2.put("networkState", obj);
                            str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                            if (StringUtils.isNull(str)) {
                                jSONObject2 = null;
                                jSONObject2.put(TrainManager.STATION_LIST, str);
                            }
                            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], null, this.f);
                            return;
                        }
                        return;
                    }
                }
                XyUtil.doXycallBackResult(this.g, objArr[0], objArr[1], objArr[2], objArr[3], objArr[4], objArr[5]);
                if (jSONObject != null) {
                    return;
                }
                jSONObject.put("QueryTime", System.currentTimeMillis());
                jSONObject.put("networkState", obj);
                if (objArr != null) {
                    if (objArr.length > 1) {
                        str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                    }
                }
                if (StringUtils.isNull(str)) {
                    jSONObject.put(TrainManager.STATION_LIST, str);
                }
                MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
                return;
            }
            XyUtil.doXycallBackResult(this.g, obj);
            if (jSONObject != null) {
                return;
            }
            jSONObject.put("QueryTime", System.currentTimeMillis());
            jSONObject.put("networkState", obj);
            if (objArr != null) {
                if (objArr.length > 1) {
                    str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                }
            }
            if (StringUtils.isNull(str)) {
                jSONObject.put(TrainManager.STATION_LIST, str);
            }
            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
        }
        obj = null;
        if (objArr != null) {
            if (((Boolean) objArr[5]).booleanValue()) {
                if (a.a(JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString(), (String) objArr[3], (String) objArr[4])) {
                    XyUtil.doXycallBackResult(this.g, null);
                    if ("true".equalsIgnoreCase(this.c)) {
                        jSONObject2 = null;
                        jSONObject2.put("QueryTime", System.currentTimeMillis());
                        jSONObject2 = null;
                        jSONObject2.put("networkState", obj);
                        str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                        if (StringUtils.isNull(str)) {
                            jSONObject2 = null;
                            jSONObject2.put(TrainManager.STATION_LIST, str);
                        }
                        MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], null, this.f);
                        return;
                    }
                    return;
                }
            }
            XyUtil.doXycallBackResult(this.g, objArr[0], objArr[1], objArr[2], objArr[3], objArr[4], objArr[5]);
            if (jSONObject != null) {
                return;
            }
            jSONObject.put("QueryTime", System.currentTimeMillis());
            jSONObject.put("networkState", obj);
            if (objArr != null) {
                if (objArr.length > 1) {
                    str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                }
            }
            if (StringUtils.isNull(str)) {
                jSONObject.put(TrainManager.STATION_LIST, str);
            }
            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
            return;
        }
        try {
            XyUtil.doXycallBackResult(this.g, obj);
            if (jSONObject != null) {
                return;
            }
            jSONObject.put("QueryTime", System.currentTimeMillis());
            jSONObject.put("networkState", obj);
            if (objArr != null) {
                if (objArr.length > 1) {
                    str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                }
            }
            if (StringUtils.isNull(str)) {
                jSONObject.put(TrainManager.STATION_LIST, str);
            }
            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
        } catch (Throwable th22) {
            th = th22;
            if (jSONObject != null) {
                throw th;
            }
            jSONObject.put("QueryTime", System.currentTimeMillis());
            jSONObject.put("networkState", obj);
            if (objArr != null) {
                if (objArr.length > 1) {
                    str = JsonUtil.getValueFromJsonObject((JSONObject) objArr[1], TrainManager.STATION_LIST).toString();
                }
            }
            if (StringUtils.isNull(str)) {
                jSONObject.put(TrainManager.STATION_LIST, str);
            }
            MatchCacheManager.updateMatchCacheManager(this.d, this.e, (String) objArr[0], jSONObject, this.f);
            throw th;
        }
    }
}
