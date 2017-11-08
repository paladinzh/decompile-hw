package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class a extends Event {
    public int a;
    public int b;
    public String c;
    public String d;

    a() {
    }

    public a(String str) {
        super("CAMERA", str);
    }

    public static Event a(Event event) {
        return Event.makeNewEvent(event, "RECORDING_START");
    }

    private static a a(int i, int i2, String str) {
        a aVar = new a(str);
        aVar.key = "RECORDING";
        aVar.timestamp = Fyulytics.currentTimestampMs();
        aVar.a = i;
        aVar.b = i2;
        return aVar;
    }

    private static a a(int i, int i2, boolean z) {
        a aVar = new a();
        aVar.timestamp = Fyulytics.currentTimestampMs();
        aVar.key = !z ? "CAMERA_CLOSE" : "CAMERA_OPEN";
        aVar.a = i;
        aVar.b = i2;
        return aVar;
    }

    public static a a(int i, String str) {
        return a(1, i, str);
    }

    public static a a(int i, boolean z) {
        return a(1, i, z);
    }

    static a a(JSONObject jSONObject) {
        Event aVar = new a();
        Event.a(jSONObject, aVar);
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
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting CameraEvent from JSON", e);
            }
        }
        return aVar;
    }

    public static a b(int i, String str) {
        return a(2, i, str);
    }

    public static a b(int i, boolean z) {
        return a(2, i, z);
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
