package com.google.android.gms.location;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class ActivityRecognitionResult implements SafeParcelable {
    public static final ActivityRecognitionResultCreator CREATOR = new ActivityRecognitionResultCreator();
    Bundle extras;
    private final int mVersionCode;
    List<DetectedActivity> zzaNu;
    long zzaNv;
    long zzaNw;
    int zzaNx;

    public ActivityRecognitionResult(int versionCode, List<DetectedActivity> probableActivities, long timeMillis, long elapsedRealtimeMillis, int detectorInfoId, Bundle extras) {
        this.mVersionCode = versionCode;
        this.zzaNu = probableActivities;
        this.zzaNv = timeMillis;
        this.zzaNw = elapsedRealtimeMillis;
        this.zzaNx = detectorInfoId;
        this.extras = extras;
    }

    public ActivityRecognitionResult(DetectedActivity mostProbableActivity, long time, long elapsedRealtimeMillis) {
        this(mostProbableActivity, time, elapsedRealtimeMillis, 0, null);
    }

    public ActivityRecognitionResult(DetectedActivity mostProbableActivity, long time, long elapsedRealtimeMillis, int detectorInfoId, Bundle extra) {
        this(Collections.singletonList(mostProbableActivity), time, elapsedRealtimeMillis, detectorInfoId, extra);
    }

    public ActivityRecognitionResult(List<DetectedActivity> probableActivities, long time, long elapsedRealtimeMillis) {
        this((List) probableActivities, time, elapsedRealtimeMillis, 0, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ActivityRecognitionResult(List<DetectedActivity> probableActivities, long time, long elapsedRealtimeMillis, int detectorInfoId, Bundle extras) {
        boolean z = true;
        boolean z2 = probableActivities != null && probableActivities.size() > 0;
        zzx.zzb(z2, (Object) "Must have at least 1 detected activity");
        if (!(time <= 0)) {
        }
        z = false;
        zzx.zzb(z, (Object) "Must set times");
        this.mVersionCode = 2;
        this.zzaNu = probableActivities;
        this.zzaNv = time;
        this.zzaNw = elapsedRealtimeMillis;
        this.zzaNx = detectorInfoId;
        this.extras = extras;
    }

    public static ActivityRecognitionResult extractResult(Intent intent) {
        if (!hasResult(intent)) {
            return null;
        }
        Object obj = intent.getExtras().get("com.google.android.location.internal.EXTRA_ACTIVITY_RESULT");
        if (!(obj instanceof byte[])) {
            return !(obj instanceof ActivityRecognitionResult) ? null : (ActivityRecognitionResult) obj;
        } else {
            Parcel obtain = Parcel.obtain();
            obtain.unmarshall((byte[]) obj, 0, ((byte[]) obj).length);
            obtain.setDataPosition(0);
            return CREATOR.createFromParcel(obtain);
        }
    }

    public static boolean hasResult(Intent intent) {
        return intent != null ? intent.hasExtra("com.google.android.location.internal.EXTRA_ACTIVITY_RESULT") : false;
    }

    private static boolean zzc(Bundle bundle, Bundle bundle2) {
        if (bundle == null && bundle2 == null) {
            return true;
        }
        if (bundle != null || bundle2 == null) {
            if (bundle != null) {
                if (bundle2 != null) {
                }
            }
            if (bundle.size() != bundle2.size()) {
                return false;
            }
            for (String str : bundle.keySet()) {
                if (!bundle2.containsKey(str)) {
                    return false;
                }
                if (bundle.get(str) != null) {
                    if (bundle.get(str) instanceof Bundle) {
                        if (!zzc(bundle.getBundle(str), bundle2.getBundle(str))) {
                            return false;
                        }
                    } else if (!bundle.get(str).equals(bundle2.get(str))) {
                        return false;
                    }
                } else if (bundle2.get(str) != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityRecognitionResult activityRecognitionResult = (ActivityRecognitionResult) o;
        if (this.zzaNv == activityRecognitionResult.zzaNv && this.zzaNw == activityRecognitionResult.zzaNw && this.zzaNx == activityRecognitionResult.zzaNx && zzw.equal(this.zzaNu, activityRecognitionResult.zzaNu)) {
            if (!zzc(this.extras, activityRecognitionResult.extras)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int getActivityConfidence(int activityType) {
        for (DetectedActivity detectedActivity : this.zzaNu) {
            if (detectedActivity.getType() == activityType) {
                return detectedActivity.getConfidence();
            }
        }
        return 0;
    }

    public long getElapsedRealtimeMillis() {
        return this.zzaNw;
    }

    public DetectedActivity getMostProbableActivity() {
        return (DetectedActivity) this.zzaNu.get(0);
    }

    public List<DetectedActivity> getProbableActivities() {
        return this.zzaNu;
    }

    public long getTime() {
        return this.zzaNv;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(Long.valueOf(this.zzaNv), Long.valueOf(this.zzaNw), Integer.valueOf(this.zzaNx), this.zzaNu, this.extras);
    }

    public String toString() {
        return "ActivityRecognitionResult [probableActivities=" + this.zzaNu + ", timeMillis=" + this.zzaNv + ", elapsedRealtimeMillis=" + this.zzaNw + "]";
    }

    public void writeToParcel(Parcel out, int flags) {
        ActivityRecognitionResultCreator.zza(this, out, flags);
    }
}
