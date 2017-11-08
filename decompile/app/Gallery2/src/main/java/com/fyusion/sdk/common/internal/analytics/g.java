package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class g extends Event {
    public String a;
    public String b;
    public int c;
    public int d;

    g() {
    }

    public g(String str) {
        super("SHARE", str);
    }

    static g a(JSONObject jSONObject) {
        Event gVar = new g();
        Event.a(jSONObject, gVar);
        try {
            if (!jSONObject.isNull("sp")) {
                gVar.a = jSONObject.getString("sp");
            }
            if (!jSONObject.isNull("rid")) {
                gVar.b = jSONObject.getString("rid");
            }
            gVar.c = jSONObject.optInt(TMSDKContext.CON_LC, 0);
            gVar.d = jSONObject.optInt("res", 0);
        } catch (Throwable e) {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting ShareEvent from JSON", e);
            }
        }
        return gVar;
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a != null) {
            a(stringBuilder, "sp", this.a);
        }
        if (this.b != null) {
            a(stringBuilder, "rid", this.b);
        }
        a(stringBuilder, TMSDKContext.CON_LC, (long) this.c);
        if (this.d > 0) {
            a(stringBuilder, "res", (long) this.d);
        }
    }

    public /* bridge */ /* synthetic */ int compareTo(@NonNull Event event) {
        return super.compareTo(event);
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public /* bridge */ /* synthetic */ String getTimedEventKey() {
        return super.getTimedEventKey();
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
