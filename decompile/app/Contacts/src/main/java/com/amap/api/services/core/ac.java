package com.amap.api.services.core;

import android.content.Context;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: ShareUrlSearchHandler */
public class ac extends a<String, String> {
    private String h;

    protected /* synthetic */ Object a(String str) throws AMapException {
        return b(str);
    }

    public ac(Context context, String str) {
        super(context, str);
        this.h = str;
    }

    public Map<String, String> b() {
        byte[] a;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("channel=open_api").append("&flag=1").append("&address=" + URLEncoder.encode(this.h));
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("open_api").append(CallInterceptDetails.BRANDED_STATE).append(this.h).append("@").append("8UbJH6N2szojnTHONAWzB6K7N1kaj7Y0iUMarxac");
        stringBuilder.append("&sign=").append(ap.a(stringBuffer.toString()).toUpperCase(Locale.US));
        stringBuilder.append("&output=json");
        try {
            a = ah.a(stringBuilder.toString().getBytes("utf-8"), "Yaynpa84IKOfasFx".substring(0, 16).getBytes("utf-8"));
        } catch (Throwable e) {
            i.a(e, "ShareUrlSearchHandler", "getParams");
            a = null;
        }
        Map<String, String> hashMap = new HashMap();
        hashMap.put("ent", CallInterceptDetails.UNBRANDED_STATE);
        hashMap.put("in", ao.b(a));
        hashMap.put("keyt", "openapi");
        return hashMap;
    }

    protected String b(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            String a = n.a(jSONObject, "code");
            if (CallInterceptDetails.BRANDED_STATE.equals(a)) {
                return n.a(jSONObject, "transfer_url");
            }
            if ("0".equals(a)) {
                throw new AMapException(AMapException.AMAP_SERVICE_UNKNOWN_ERROR);
            } else if (CallInterceptDetails.UNBRANDED_STATE.equals(a)) {
                throw new AMapException(AMapException.AMAP_SHARE_FAILURE);
            } else if ("3".equals(a)) {
                throw new AMapException(AMapException.AMAP_SERVICE_INVALID_PARAMS);
            } else if ("4".equals(a)) {
                throw new AMapException(AMapException.AMAP_SIGNATURE_ERROR);
            } else {
                if ("5".equals(a)) {
                    throw new AMapException(AMapException.AMAP_SHARE_LICENSE_IS_EXPIRED);
                }
                return null;
            }
        } catch (Throwable e) {
            i.a(e, "ShareUrlSearchHandler", "paseJSON");
        }
    }

    public String g() {
        return "http://m5.amap.com/ws/mapapi/shortaddress/transform";
    }

    protected byte[] a(int i, ci ciVar, cj cjVar) throws ai {
        if (i == 1) {
            return ciVar.d(cjVar);
        }
        if (i != 2) {
            return null;
        }
        return ciVar.e(cjVar);
    }
}
