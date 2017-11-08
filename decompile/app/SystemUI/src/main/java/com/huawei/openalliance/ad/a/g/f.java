package com.huawei.openalliance.ad.a.g;

import com.huawei.openalliance.ad.a.a.a.a;
import com.huawei.openalliance.ad.a.a.b;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.j;
import fyusion.vislib.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class f {
    public static String a(String str, a aVar) {
        if (j.a(str)) {
            d.c("InterConfusion", "original string is null!");
            return BuildConfig.FLAVOR;
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (aVar instanceof com.huawei.openalliance.ad.a.a.a) {
                str = a(jSONObject);
            } else if (aVar instanceof b) {
                str = b(jSONObject);
            } else if (aVar instanceof com.huawei.openalliance.ad.a.a.f) {
                str = c(jSONObject);
            }
        } catch (JSONException e) {
            d.c("InterConfusion", "parse original string to json failed!");
        } catch (Exception e2) {
            d.c("InterConfusion", "parse original string to json failed!");
        }
        return str;
    }

    private static String a(JSONObject jSONObject) {
        if (jSONObject == null) {
            return BuildConfig.FLAVOR;
        }
        try {
            JSONObject jSONObject2;
            if (!jSONObject.isNull("device")) {
                jSONObject2 = jSONObject.getJSONObject("device");
                jSONObject2.put("androidid", "******");
                jSONObject2.put("imei", "******");
                jSONObject2.put("mac", "******");
                jSONObject2.put("userAccount", "******");
            }
            if (!jSONObject.isNull("network")) {
                jSONObject2 = jSONObject.getJSONObject("network");
                jSONObject2.put("cellInfo", "******");
                jSONObject2.put("wifiInfo", "******");
            }
        } catch (JSONException e) {
            jSONObject.remove("device");
            jSONObject.remove("network");
            d.c("InterConfusion", "fail to confuse adcontentreq");
        }
        return jSONObject.toString();
    }

    private static String b(JSONObject jSONObject) {
        if (jSONObject == null) {
            return BuildConfig.FLAVOR;
        }
        try {
            if (!jSONObject.isNull("multiad")) {
                JSONArray jSONArray = jSONObject.getJSONArray("multiad");
                for (int i = 0; i < jSONArray.length(); i++) {
                    if (!jSONArray.isNull(i)) {
                        JSONObject jSONObject2 = (JSONObject) jSONArray.get(i);
                        if (jSONObject2.isNull("content")) {
                            continue;
                        } else {
                            JSONArray jSONArray2 = jSONObject2.getJSONArray("content");
                            for (int i2 = 0; i2 < jSONArray2.length(); i2++) {
                                if (!jSONArray2.isNull(i2)) {
                                    jSONObject2 = (JSONObject) jSONArray2.get(i2);
                                    jSONObject2.put("paramfromserver", "******");
                                    if (!jSONObject2.isNull("impmonitorurl")) {
                                        jSONObject2.put("impmonitorurl", "******");
                                    }
                                    if (!jSONObject2.isNull("clickmonitorurl")) {
                                        jSONObject2.put("clickmonitorurl", "******");
                                    }
                                }
                            }
                            continue;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            d.c("InterConfusion", "fail to confuse adcontentrsp");
        } catch (Exception e2) {
            jSONObject.remove("multiad");
            d.c("InterConfusion", "fail to confuse adcontentrsp");
        }
        return jSONObject.toString();
    }

    private static String c(JSONObject jSONObject) {
        if (jSONObject == null) {
            return BuildConfig.FLAVOR;
        }
        try {
            if (!jSONObject.isNull("event")) {
                JSONArray jSONArray = jSONObject.getJSONArray("event");
                for (int i = 0; i < jSONArray.length(); i++) {
                    if (!jSONArray.isNull(i)) {
                        ((JSONObject) jSONArray.get(i)).put("paramfromserver", "******");
                    }
                }
            }
        } catch (JSONException e) {
            d.c("InterConfusion", "fail to confuse eventreq");
        } catch (Exception e2) {
            jSONObject.remove("event");
            d.c("InterConfusion", "fail to confuse eventreq");
        }
        return jSONObject.toString();
    }
}
