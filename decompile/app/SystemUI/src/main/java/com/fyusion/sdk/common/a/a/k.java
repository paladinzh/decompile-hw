package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import org.json.JSONObject;

/* compiled from: Unknown */
public class k extends d {
    public int a;

    k() {
    }

    public k(String str, int i) {
        super("TILT", str);
        this.a = i;
    }

    static k a(JSONObject jSONObject) {
        d kVar = new k();
        d.a(jSONObject, kVar);
        kVar.a = jSONObject.optInt("tc", 0);
        return kVar;
    }

    public /* bridge */ /* synthetic */ String a() {
        return super.a();
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a > 0) {
            a(stringBuilder, "tc", (long) this.a);
        }
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
