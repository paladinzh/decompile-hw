package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class j extends d {
    public String a;
    public int b;

    public j() {
        super("ST");
    }

    static j a(JSONObject jSONObject) {
        d jVar = new j();
        d.a(jSONObject, jVar);
        try {
            if (!jSONObject.isNull("fc")) {
                jVar.b = jSONObject.getInt("fc");
            }
            if (!jSONObject.isNull("sn")) {
                jVar.a = jSONObject.getString("sn");
            }
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting StyleTransferEvent from JSON", e);
            }
        }
        return jVar;
    }

    public /* bridge */ /* synthetic */ String a() {
        return super.a();
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.b > 0) {
            a(stringBuilder, "fc", (long) this.b);
        }
        if (this.a != null) {
            a(stringBuilder, "sn", this.a);
        }
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
