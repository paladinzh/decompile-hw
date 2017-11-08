package com.fyusion.sdk.common.internal.analytics;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Objects;
import org.json.JSONObject;

/* compiled from: Unknown */
class Event implements Cloneable, Comparable<Event> {
    public long dur;
    public String key;
    public String message;
    public String status;
    public long timestamp;
    public String uid;

    Event() {
    }

    public Event(String str) {
        this.key = str;
        this.timestamp = Fyulytics.currentTimestampMs();
    }

    public Event(String str, String str2) {
        this.key = str;
        this.uid = str2;
        this.timestamp = Fyulytics.currentTimestampMs();
    }

    static Event a(JSONObject jSONObject, Event event) {
        try {
            if (!jSONObject.isNull("n")) {
                event.key = jSONObject.getString("n");
            }
            event.timestamp = jSONObject.optLong("ts");
            if (!jSONObject.isNull("uid")) {
                event.uid = jSONObject.getString("uid");
            }
            event.dur = jSONObject.optLong("dr", 0);
            if (!jSONObject.isNull("st")) {
                event.status = jSONObject.getString("st");
            }
            if (!jSONObject.isNull("m")) {
                event.message = jSONObject.getString("m");
            }
            Event event2 = event;
        } catch (Throwable e) {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting JSON to an Event", e);
            }
            event2 = null;
        }
        if (!(event2 == null || event2.key == null)) {
            if (event2.key.length() > 0) {
                return event2;
            }
        }
        return null;
    }

    static Event b(JSONObject jSONObject) {
        try {
            String string = jSONObject.isNull("n") ? null : jSONObject.getString("n");
            if (string != null) {
                Object obj = -1;
                switch (string.hashCode()) {
                    case -2014348979:
                        if (string.equals("RECORDING_END")) {
                            obj = 10;
                            break;
                        }
                        break;
                    case -1667346370:
                        if (string.equals("CAMERA_CLOSE")) {
                            obj = 7;
                            break;
                        }
                        break;
                    case -1515159428:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_LOAD_STYLE)) {
                            obj = 14;
                            break;
                        }
                        break;
                    case -1350744454:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_CLOSE)) {
                            obj = 19;
                            break;
                        }
                        break;
                    case -514814511:
                        if (string.equals("RECORDING")) {
                            obj = 8;
                            break;
                        }
                        break;
                    case -118222296:
                        if (string.equals("VIEW_START")) {
                            obj = 2;
                            break;
                        }
                        break;
                    case -53424348:
                        if (string.equals("CAMERA_OPEN")) {
                            obj = 6;
                            break;
                        }
                        break;
                    case 2657:
                        if (string.equals(StyleTransferEvent.EVENT_KEY)) {
                            obj = 12;
                            break;
                        }
                        break;
                    case 3675:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_STYLE_NAME)) {
                            obj = 13;
                            break;
                        }
                        break;
                    case 2575037:
                        if (string.equals("TILT")) {
                            obj = null;
                            break;
                        }
                        break;
                    case 2634405:
                        if (string.equals("VIEW")) {
                            obj = 1;
                            break;
                        }
                        break;
                    case 78862271:
                        if (string.equals("SHARE")) {
                            obj = 20;
                            break;
                        }
                        break;
                    case 172755160:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_SF_OPEN)) {
                            obj = 17;
                            break;
                        }
                        break;
                    case 243170262:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_STR_OPEN)) {
                            obj = 18;
                            break;
                        }
                        break;
                    case 562020088:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_STR_APPLY_STYLE)) {
                            obj = 16;
                            break;
                        }
                        break;
                    case 1120794199:
                        if (string.equals("VIEW_LOOP_CLOSE")) {
                            obj = 4;
                            break;
                        }
                        break;
                    case 1253989460:
                        if (string.equals("RECORDING_START")) {
                            obj = 9;
                            break;
                        }
                        break;
                    case 1529579958:
                        if (string.equals(StyleTransferEvent.EVENT_KEY_SF_APPLY_STYLE)) {
                            obj = 15;
                            break;
                        }
                        break;
                    case 1691835506:
                        if (string.equals("PROCESSOR")) {
                            obj = 11;
                            break;
                        }
                        break;
                    case 1979749409:
                        if (string.equals("VIEW_END")) {
                            obj = 3;
                            break;
                        }
                        break;
                    case 1980544805:
                        if (string.equals("CAMERA")) {
                            obj = 5;
                            break;
                        }
                        break;
                }
                switch (obj) {
                    case null:
                        return h.a(jSONObject);
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        return i.a(jSONObject);
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        return a.a(jSONObject);
                    case 11:
                        return f.a(jSONObject);
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                        return StyleTransferEvent.a(jSONObject);
                    case 20:
                        return g.a(jSONObject);
                    default:
                        break;
                }
            }
            return a(jSONObject, new Event());
        } catch (Throwable e) {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception converting JSON to an Event", e);
            }
            return null;
        }
    }

    public static Event makeNewEvent(Event event, String str) {
        CloneNotSupportedException cloneNotSupportedException;
        Event event2 = null;
        try {
            Event event3 = (Event) event.clone();
            try {
                event3.key = str;
                return event3;
            } catch (CloneNotSupportedException e) {
                CloneNotSupportedException cloneNotSupportedException2 = e;
                event2 = event3;
                cloneNotSupportedException = cloneNotSupportedException2;
            }
        } catch (CloneNotSupportedException e2) {
            cloneNotSupportedException = e2;
            cloneNotSupportedException.printStackTrace();
            return event2;
        }
    }

    void a(@NonNull StringBuilder stringBuilder) {
    }

    void a(@NonNull StringBuilder stringBuilder, @NonNull String str, long j) {
        b(stringBuilder.append(','), str, j);
    }

    void a(@NonNull StringBuilder stringBuilder, @NonNull String str, @NonNull String str2) {
        b(stringBuilder.append(','), str, str2);
    }

    void b(@NonNull StringBuilder stringBuilder) {
        stringBuilder.append('{');
        b(stringBuilder, "n", this.key);
        a(stringBuilder, "ts", this.timestamp);
        if (this.uid != null) {
            a(stringBuilder, "uid", this.uid);
        }
        if (this.status != null) {
            a(stringBuilder, "st", this.status);
        }
        if (this.message != null) {
            a(stringBuilder, "m", this.message);
        }
        if ((this.dur <= 0 ? 1 : null) == null) {
            a(stringBuilder, "dr", this.dur);
        }
        a(stringBuilder);
        stringBuilder.append('}');
    }

    void b(@NonNull StringBuilder stringBuilder, @NonNull String str, long j) {
        stringBuilder.append('\"').append(str).append("\":").append(j);
    }

    void b(@NonNull StringBuilder stringBuilder, @NonNull String str, @NonNull String str2) {
        stringBuilder.append('\"').append(str).append("\":\"").append(str2).append('\"');
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int compareTo(@NonNull Event event) {
        int compare = Long.compare(this.timestamp, event.timestamp);
        if (compare != 0) {
            return compare;
        }
        if (!(this.uid == null || event.uid == null)) {
            compare = this.uid.compareTo(event.uid);
            if (compare != 0) {
                return compare;
            }
        }
        if (!(this.key == null || event.key == null)) {
            compare = this.key.compareTo(event.key);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Event event = (Event) obj;
        if (this.timestamp == event.timestamp && Objects.equals(this.key, event.key)) {
            if (!Objects.equals(this.uid, event.uid)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String getTimedEventKey() {
        return Fyulytics.makeTimedEventKey(this.key, this.uid);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.key, Long.valueOf(this.timestamp), this.uid});
    }
}
