package cn.com.xy.sms.sdk.service.f;

import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
final class b implements SdkCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;
    private final /* synthetic */ SdkCallBack g;
    private final /* synthetic */ String h;
    private final /* synthetic */ String i;
    private final /* synthetic */ String j;
    private final /* synthetic */ String k;
    private final /* synthetic */ String l;

    b(String str, String str2, String str3, String str4, String str5, String str6, SdkCallBack sdkCallBack, String str7, String str8, String str9, String str10, String str11) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = str5;
        this.f = str6;
        this.g = sdkCallBack;
        this.h = str7;
        this.i = str8;
        this.j = str9;
        this.k = str10;
        this.l = str11;
    }

    public final void execute(Object... objArr) {
        JSONObject jSONObject;
        JSONArray jSONArray;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        int i;
        JSONObject jSONObject2;
        String str11;
        JSONObject jSONObject3 = null;
        if (!StringUtils.isNull(this.a)) {
            try {
                jSONObject = new JSONObject(this.a);
            } catch (JSONException e) {
            }
            if (objArr != null) {
                if (objArr.length >= 2 && objArr[1].toString().indexOf("[") == 0) {
                    jSONArray = new JSONArray((String) objArr[1]);
                    str = "";
                    str2 = "";
                    str3 = "";
                    str4 = "";
                    str5 = "";
                    str6 = "";
                    str7 = "";
                    str8 = "";
                    str9 = "";
                    str10 = "";
                    i = 0;
                    while (i < jSONArray.length()) {
                        jSONObject2 = (JSONObject) jSONArray.get(i);
                        str10 = jSONObject2.getString("FlightCompany");
                        str9 = jSONObject2.getString("FlightDeptimePlanDate");
                        str8 = jSONObject2.getString("FlightArrtimePlanDate");
                        str7 = jSONObject2.getString("FlightHTerminal");
                        str6 = jSONObject2.getString("FlightDep");
                        str5 = jSONObject2.getString("FlightArr");
                        str4 = jSONObject2.getString("FlightDepAirport");
                        str3 = jSONObject2.getString("FlightArrAirport");
                        str2 = jSONObject2.getString("FlightState");
                        str = jSONObject2.getString("FlightTerminal");
                        if (StringUtils.isNull(this.h)) {
                            if (StringUtils.isNull(this.i) && StringUtils.isNull(this.j) && StringUtils.isNull(this.k)) {
                                break;
                            }
                        }
                        if (!StringUtils.isNull(this.h)) {
                            if (!(StringUtils.isNull(this.i) || this.h.indexOf(str6) == -1 || this.i.indexOf(str5) == -1)) {
                                break;
                            }
                        }
                        if (!StringUtils.isNull(this.h) && StringUtils.isNull(this.i) && this.h.equals(str6)) {
                            str5 = "";
                            str8 = "";
                            str3 = "";
                            break;
                        } else if (!StringUtils.isNull(this.i) && StringUtils.isNull(this.h) && this.i.equals(str5)) {
                            str6 = "";
                            str9 = "";
                            str4 = "";
                            break;
                        } else if (StringUtils.isNull(this.h) && StringUtils.isNull(this.i) && !StringUtils.isNull(this.j) && !StringUtils.isNull(this.k) && str4.contains(this.j) && str3.contains(this.k)) {
                            break;
                        } else if (StringUtils.isNull(this.h) && StringUtils.isNull(this.i) && StringUtils.isNull(this.k) && !StringUtils.isNull(this.j) && str4.contains(this.j)) {
                            break;
                        } else if (StringUtils.isNull(this.h) && StringUtils.isNull(this.i) && StringUtils.isNull(this.j) && !StringUtils.isNull(this.k) && str3.contains(this.k)) {
                            break;
                        } else {
                            i++;
                            str10 = "";
                        }
                    }
                    str11 = str10;
                    str10 = str9;
                    str9 = str8;
                    str8 = str7;
                    str7 = str6;
                    str6 = str5;
                    str5 = str4;
                    str4 = str3;
                    str3 = str2;
                    str2 = str;
                    if (StringUtils.isNull(str11)) {
                        jSONObject2 = new JSONObject();
                        try {
                            jSONObject2.put("FlightCompany", str11);
                            jSONObject2.put("FlightDeptimePlanDate", str10);
                            jSONObject2.put("FlightArrtimePlanDate", str9);
                            jSONObject2.put("FlightHTerminal", str8);
                            jSONObject2.put("FlightDep", str7);
                            jSONObject2.put("FlightArr", str6);
                            jSONObject2.put("FlightQueryTime", System.currentTimeMillis());
                            jSONObject2.put("FlightDepAirport", str5);
                            jSONObject2.put("FlightArrAirport", str4);
                            jSONObject2.put("FlightState", str3);
                            jSONObject2.put("FlightTerminal", str2);
                            XyUtil.doXycallBackResult(this.g, this.l, jSONObject2);
                            if (!(jSONObject == null && "true".equalsIgnoreCase(this.b))) {
                                try {
                                    jSONObject.put("QueryTime", System.currentTimeMillis());
                                    JsonUtil.JSONCombine(jSONObject, jSONObject2);
                                    MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
                                    return;
                                } catch (Exception e2) {
                                }
                            }
                            return;
                        } catch (JSONException e3) {
                            try {
                                XyUtil.doXycallBackResult(this.g, null);
                                if (!(jSONObject == null && "true".equalsIgnoreCase(this.b))) {
                                    try {
                                        jSONObject.put("QueryTime", System.currentTimeMillis());
                                        if (jSONObject2 != null) {
                                            JsonUtil.JSONCombine(jSONObject, jSONObject2);
                                        }
                                        MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
                                        return;
                                    } catch (Exception e4) {
                                        return;
                                    }
                                }
                                return;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                jSONObject3 = jSONObject2;
                                Throwable th3 = th2;
                                if (jSONObject != null || !"true".equalsIgnoreCase(this.b)) {
                                    try {
                                        jSONObject.put("QueryTime", System.currentTimeMillis());
                                        if (jSONObject3 != null) {
                                            JsonUtil.JSONCombine(jSONObject, jSONObject3);
                                        }
                                        MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
                                    } catch (Exception e5) {
                                    }
                                }
                                throw th3;
                            }
                        }
                    }
                    XyUtil.doXycallBackResult(this.g, null);
                    if (jSONObject != null || !"true".equalsIgnoreCase(this.b)) {
                        try {
                            jSONObject.put("QueryTime", System.currentTimeMillis());
                            MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
                            return;
                        } catch (Exception e6) {
                        }
                    } else {
                        return;
                    }
                }
            }
            XyUtil.doXycallBackResult(this.g, null);
            if (!(jSONObject == null && "true".equalsIgnoreCase(this.b))) {
                jSONObject.put("QueryTime", System.currentTimeMillis());
                MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
            }
            return;
        }
        jSONObject = null;
        if (objArr != null) {
            jSONArray = new JSONArray((String) objArr[1]);
            str = "";
            str2 = "";
            str3 = "";
            str4 = "";
            str5 = "";
            str6 = "";
            str7 = "";
            str8 = "";
            str9 = "";
            str10 = "";
            i = 0;
            while (i < jSONArray.length()) {
                jSONObject2 = (JSONObject) jSONArray.get(i);
                str10 = jSONObject2.getString("FlightCompany");
                str9 = jSONObject2.getString("FlightDeptimePlanDate");
                str8 = jSONObject2.getString("FlightArrtimePlanDate");
                str7 = jSONObject2.getString("FlightHTerminal");
                str6 = jSONObject2.getString("FlightDep");
                str5 = jSONObject2.getString("FlightArr");
                str4 = jSONObject2.getString("FlightDepAirport");
                str3 = jSONObject2.getString("FlightArrAirport");
                str2 = jSONObject2.getString("FlightState");
                str = jSONObject2.getString("FlightTerminal");
                if (StringUtils.isNull(this.h)) {
                    break;
                }
                if (StringUtils.isNull(this.h)) {
                    break;
                }
                if (StringUtils.isNull(this.h)) {
                    str5 = "";
                    str8 = "";
                    str3 = "";
                    break;
                }
                if (StringUtils.isNull(this.i)) {
                    str6 = "";
                    str9 = "";
                    str4 = "";
                    break;
                }
                if (StringUtils.isNull(this.h)) {
                    break;
                }
                if (StringUtils.isNull(this.h)) {
                    break;
                }
                if (StringUtils.isNull(this.h)) {
                    break;
                }
                i++;
                str10 = "";
            }
            str11 = str10;
            str10 = str9;
            str9 = str8;
            str8 = str7;
            str7 = str6;
            str6 = str5;
            str5 = str4;
            str4 = str3;
            str3 = str2;
            str2 = str;
            if (StringUtils.isNull(str11)) {
                jSONObject2 = new JSONObject();
                jSONObject2.put("FlightCompany", str11);
                jSONObject2.put("FlightDeptimePlanDate", str10);
                jSONObject2.put("FlightArrtimePlanDate", str9);
                jSONObject2.put("FlightHTerminal", str8);
                jSONObject2.put("FlightDep", str7);
                jSONObject2.put("FlightArr", str6);
                jSONObject2.put("FlightQueryTime", System.currentTimeMillis());
                jSONObject2.put("FlightDepAirport", str5);
                jSONObject2.put("FlightArrAirport", str4);
                jSONObject2.put("FlightState", str3);
                jSONObject2.put("FlightTerminal", str2);
                XyUtil.doXycallBackResult(this.g, this.l, jSONObject2);
                jSONObject.put("QueryTime", System.currentTimeMillis());
                JsonUtil.JSONCombine(jSONObject, jSONObject2);
                MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
                return;
            }
            XyUtil.doXycallBackResult(this.g, null);
            if (jSONObject != null) {
                return;
            }
            jSONObject.put("QueryTime", System.currentTimeMillis());
            MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
            return;
        }
        try {
            XyUtil.doXycallBackResult(this.g, null);
            try {
                jSONObject.put("QueryTime", System.currentTimeMillis());
                MatchCacheManager.updateMatchCacheManager(this.c, this.d, this.e, jSONObject, this.f);
            } catch (Exception e7) {
            }
        } catch (JSONException e8) {
            jSONObject2 = null;
        } catch (Throwable th4) {
            th3 = th4;
        }
    }
}
