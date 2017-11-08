package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import org.json.JSONObject;

/* compiled from: Unknown */
public class h extends Event {
    public int a;

    h() {
    }

    public h(String str, int i) {
        super("TILT", str);
        this.a = i;
    }

    static h a(JSONObject jSONObject) {
        Event hVar = new h();
        Event.a(jSONObject, hVar);
        hVar.a = jSONObject.optInt("tc", 0);
        return hVar;
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a > 0) {
            a(stringBuilder, "tc", (long) this.a);
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
