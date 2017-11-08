package com.google.android.gms.location;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public class ActivityRecognitionResult implements SafeParcelable {
    public static final ActivityRecognitionResultCreator CREATOR = new ActivityRecognitionResultCreator();
    List<DetectedActivity> KP;
    long KQ;
    long KR;
    private final int wj = 1;

    public ActivityRecognitionResult(int versionCode, List<DetectedActivity> probableActivities, long timeMillis, long elapsedRealtimeMillis) {
        this.KP = probableActivities;
        this.KQ = timeMillis;
        this.KR = elapsedRealtimeMillis;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public String toString() {
        return "ActivityRecognitionResult [probableActivities=" + this.KP + ", timeMillis=" + this.KQ + ", elapsedRealtimeMillis=" + this.KR + "]";
    }

    public void writeToParcel(Parcel out, int flags) {
        ActivityRecognitionResultCreator.a(this, out, flags);
    }
}
