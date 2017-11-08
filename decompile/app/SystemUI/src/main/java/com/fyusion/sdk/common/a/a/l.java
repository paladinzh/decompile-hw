package com.fyusion.sdk.common.a.a;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class l extends d {
    public String a;
    public int b;
    public int c;
    public String d;
    public String k;

    l() {
    }

    public l(String str) {
        super("VIEW", str);
    }

    static l a(JSONObject jSONObject) {
        d lVar = new l();
        d.a(jSONObject, lVar);
        try {
            if (!jSONObject.isNull("vs")) {
                lVar.a = jSONObject.getString("vs");
            }
            if (!jSONObject.isNull("ori")) {
                lVar.d = jSONObject.getString("ori");
            }
            if (!jSONObject.isNull("ord")) {
                lVar.k = jSONObject.getString("ord");
            }
            lVar.c = jSONObject.optInt("typ", 0);
            lVar.b = jSONObject.optInt("res", 0);
        } catch (Throwable e) {
            if (f.a().c()) {
                Log.w("Fyulytics", "Got exception converting ViewEvent from JSON", e);
            }
        }
        return lVar;
    }

    public static String a(String str, String str2, int i) {
        return str + "_" + str2 + "_" + i;
    }

    public static d b(d dVar) {
        return d.a(dVar, "VIEW_START");
    }

    public String a() {
        return a(this.e, this.g, this.b);
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a != null) {
            a(stringBuilder, "vs", this.a);
        }
        if (this.b > 0) {
            a(stringBuilder, "res", (long) this.b);
        }
        if (this.d != null) {
            a(stringBuilder, "ori", this.d);
        }
        if (this.k != null) {
            a(stringBuilder, "ord", this.k);
        }
        a(stringBuilder, "typ", (long) this.c);
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
