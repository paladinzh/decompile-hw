package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import org.json.JSONObject;

/* compiled from: Unknown */
public class h extends d {
    public int a;
    public int b;

    h() {
    }

    public h(String str) {
        super("PROCESSOR", str);
    }

    static h a(JSONObject jSONObject) {
        d hVar = new h();
        d.a(jSONObject, hVar);
        hVar.a = jSONObject.optInt("fc", 0);
        hVar.b = jSONObject.optInt("lc", 0);
        return hVar;
    }

    public /* bridge */ /* synthetic */ String a() {
        return super.a();
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a > 0) {
            a(stringBuilder, "fc", (long) this.a);
        }
        a(stringBuilder, "lc", (long) this.b);
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
