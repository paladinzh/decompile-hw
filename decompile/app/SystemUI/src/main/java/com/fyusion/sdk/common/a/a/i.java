package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class i extends d {
    public String a;
    public String b;
    public int c;
    public int d;

    i() {
    }

    static i a(JSONObject jSONObject) {
        d iVar = new i();
        d.a(jSONObject, iVar);
        try {
            if (!jSONObject.isNull("sp")) {
                iVar.a = jSONObject.getString("sp");
            }
            if (!jSONObject.isNull("rid")) {
                iVar.b = jSONObject.getString("rid");
            }
            iVar.c = jSONObject.optInt("lc", 0);
            iVar.d = jSONObject.optInt("res", 0);
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting ShareEvent from JSON", e);
            }
        }
        return iVar;
    }

    public /* bridge */ /* synthetic */ String a() {
        return super.a();
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a != null) {
            a(stringBuilder, "sp", this.a);
        }
        if (this.b != null) {
            a(stringBuilder, "rid", this.b);
        }
        a(stringBuilder, "lc", (long) this.c);
        if (this.d > 0) {
            a(stringBuilder, "res", (long) this.d);
        }
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
