package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class f extends Event {
    public int a;
    public int b;

    f() {
    }

    public f(String str) {
        super("PROCESSOR", str);
    }

    static f a(JSONObject jSONObject) {
        Event fVar = new f();
        Event.a(jSONObject, fVar);
        fVar.a = jSONObject.optInt(StyleTransferEvent.EVENT_KEY_FRAME_COUNT, 0);
        fVar.b = jSONObject.optInt(TMSDKContext.CON_LC, 0);
        return fVar;
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.a > 0) {
            a(stringBuilder, StyleTransferEvent.EVENT_KEY_FRAME_COUNT, (long) this.a);
        }
        a(stringBuilder, TMSDKContext.CON_LC, (long) this.b);
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
