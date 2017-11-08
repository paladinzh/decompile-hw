package com.huawei.hwid.update.a;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.update.j;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class a {
    private Context a;

    public a(Context context) {
        this.a = context;
    }

    public JSONObject a(int i) {
        JSONObject jSONObject = new JSONObject();
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("FingerPrint", a());
            jSONObject2.put("DeviceName", b());
            jSONObject2.put("FirmWare", c());
            jSONObject2.put("IMEI", j.a(l.c(this.a)));
            jSONObject2.put("Language", e());
            jSONObject2.put("OS", d());
            jSONObject.putOpt("rules", jSONObject2);
            JSONArray jSONArray = new JSONArray();
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("PackageName", "com.huawei.hwid");
            jSONObject3.put("PackageVersionCode", "20101000");
            if (49827 != i) {
                jSONObject3.put("PackageVersionName", "2.1.1.0_OVE");
            } else {
                jSONObject3.put("PackageVersionName", "2.1.1.0");
            }
            jSONObject3.put("componentID", Integer.toString(i));
            jSONArray.put(jSONObject3);
            jSONObject.put("components", jSONArray);
        } catch (JSONException e) {
            e.d("AppUpdateInfo", "to json error");
        }
        return jSONObject;
    }

    public String a() {
        return Build.FINGERPRINT;
    }

    public String b() {
        return Build.MODEL;
    }

    public String c() {
        return Build.DISPLAY;
    }

    public String d() {
        return "Android " + VERSION.RELEASE;
    }

    public String e() {
        Configuration configuration = this.a.getResources().getConfiguration();
        String language = configuration.locale.getLanguage();
        return (language + '-' + configuration.locale.getCountry()).toLowerCase(Locale.getDefault());
    }
}
