package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class a extends d {
    public int a;
    public int b;
    public String c;
    public String d;

    a() {
    }

    static a a(JSONObject jSONObject) {
        d aVar = new a();
        d.a(jSONObject, aVar);
        try {
            aVar.a = jSONObject.optInt("ct", 0);
            aVar.b = jSONObject.optInt("lf", -1);
            if (!jSONObject.isNull("vs")) {
                aVar.c = jSONObject.getString("vs");
            }
            if (!jSONObject.isNull("ps")) {
                aVar.d = jSONObject.getString("ps");
            }
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting CameraEvent from JSON", e);
            }
        }
        return aVar;
    }

    public /* bridge */ /* synthetic */ String a() {
        return super.a();
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a > 0) {
            a(stringBuilder, "ct", (long) this.a);
        }
        if (this.b > -1) {
            a(stringBuilder, "lf", (long) this.b);
        }
        if (this.c != null) {
            a(stringBuilder, "vs", this.c);
        }
        if (this.d != null) {
            a(stringBuilder, "ps", this.d);
        }
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
