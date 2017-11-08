package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class i extends Event {
    public String a;
    public int b;
    public int c;
    public String d;
    public String e;

    i() {
    }

    public i(String str) {
        super("VIEW", str);
    }

    public static Event a(Event event) {
        return Event.makeNewEvent(event, "VIEW_START");
    }

    static i a(JSONObject jSONObject) {
        Event iVar = new i();
        Event.a(jSONObject, iVar);
        try {
            if (!jSONObject.isNull("vs")) {
                iVar.a = jSONObject.getString("vs");
            }
            if (!jSONObject.isNull("ori")) {
                iVar.d = jSONObject.getString("ori");
            }
            if (!jSONObject.isNull("ord")) {
                iVar.e = jSONObject.getString("ord");
            }
            iVar.c = jSONObject.optInt("typ", 0);
            iVar.b = jSONObject.optInt("res", 0);
        } catch (Throwable e) {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting ViewEvent from JSON", e);
            }
        }
        return iVar;
    }

    public static String a(String str, String str2, int i) {
        return str + "_" + str2 + "_" + i;
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
        if (this.e != null) {
            a(stringBuilder, "ord", this.e);
        }
        a(stringBuilder, "typ", (long) this.c);
    }

    public /* bridge */ /* synthetic */ int compareTo(@NonNull Event event) {
        return super.compareTo(event);
    }

    public /* bridge */ /* synthetic */ boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String getTimedEventKey() {
        return a(this.key, this.uid, this.b);
    }

    public /* bridge */ /* synthetic */ int hashCode() {
        return super.hashCode();
    }
}
