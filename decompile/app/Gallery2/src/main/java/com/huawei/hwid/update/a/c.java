package com.huawei.hwid.update.a;

import android.content.Context;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.update.j;
import org.json.JSONException;
import org.json.JSONObject;

public class c {
    private String a;
    private String b;
    private String c;
    private String d;

    public void a(String str) {
        this.a = str;
    }

    public void b(String str) {
        this.b = str;
    }

    public void c(String str) {
        this.c = str;
    }

    public void d(String str) {
        this.d = str;
    }

    public JSONObject a(Context context) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("operateType", this.a);
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("IMEI", j.a(l.c(context)));
            jSONObject2.put("versionID", this.b);
            jSONObject2.put("clientversion", this.c);
            jSONObject2.put("descinfo", this.d);
            jSONObject.putOpt("updateLog", jSONObject2);
            e.b("UpdateStatus", "updateLog");
            return jSONObject;
        } catch (JSONException e) {
            return jSONObject;
        }
    }
}
