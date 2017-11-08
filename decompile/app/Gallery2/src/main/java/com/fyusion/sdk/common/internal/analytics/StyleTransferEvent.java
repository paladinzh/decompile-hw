package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONObject;

/* compiled from: Unknown */
public class StyleTransferEvent extends Event {
    public static final String EVENT_KEY = "ST";
    public static final String EVENT_KEY_CLOSE = "ST_CLOSE";
    public static final String EVENT_KEY_FRAME_COUNT = "fc";
    public static final String EVENT_KEY_LOAD_STYLE = "ST_LOAD_ST";
    public static final String EVENT_KEY_SF_APPLY_STYLE = "ST_SF_APS";
    public static final String EVENT_KEY_SF_OPEN = "ST_SF_OPEN";
    public static final String EVENT_KEY_STR_APPLY_STYLE = "ST_STR_APS";
    public static final String EVENT_KEY_STR_OPEN = "ST_STR_OPEN";
    public static final String EVENT_KEY_STYLE_NAME = "sn";
    public int frameCount;
    public String styleName;

    public StyleTransferEvent() {
        super(EVENT_KEY);
    }

    static StyleTransferEvent a(JSONObject jSONObject) {
        Event styleTransferEvent = new StyleTransferEvent();
        Event.a(jSONObject, styleTransferEvent);
        try {
            if (!jSONObject.isNull(EVENT_KEY_FRAME_COUNT)) {
                styleTransferEvent.frameCount = jSONObject.getInt(EVENT_KEY_FRAME_COUNT);
            }
            if (!jSONObject.isNull(EVENT_KEY_STYLE_NAME)) {
                styleTransferEvent.styleName = jSONObject.getString(EVENT_KEY_STYLE_NAME);
            }
        } catch (Throwable e) {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting StyleTransferEvent from JSON", e);
            }
        }
        return styleTransferEvent;
    }

    public static StyleTransferEvent makeStyleTransferEvent(String str, boolean z, String str2) {
        StyleTransferEvent styleTransferEvent = new StyleTransferEvent();
        styleTransferEvent.timestamp = Fyulytics.currentTimestampMs();
        styleTransferEvent.key = str2;
        styleTransferEvent.key = !z ? EVENT_KEY_STR_OPEN : EVENT_KEY_SF_OPEN;
        styleTransferEvent.styleName = str;
        return styleTransferEvent;
    }

    void a(@NonNull StringBuilder stringBuilder) {
        if (this.frameCount > 0) {
            a(stringBuilder, EVENT_KEY_FRAME_COUNT, (long) this.frameCount);
        }
        if (this.styleName != null) {
            a(stringBuilder, EVENT_KEY_STYLE_NAME, this.styleName);
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
