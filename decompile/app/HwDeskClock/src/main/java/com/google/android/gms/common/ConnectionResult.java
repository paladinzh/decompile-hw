package com.google.android.gms.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender.SendIntentException;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class ConnectionResult implements SafeParcelable {
    public static final Creator<ConnectionResult> CREATOR = new zzb();
    public static final ConnectionResult zzYi = new ConnectionResult(0, null);
    private final PendingIntent mPendingIntent;
    final int mVersionCode;
    private final int zzWu;

    ConnectionResult(int versionCode, int statusCode, PendingIntent pendingIntent) {
        this.mVersionCode = versionCode;
        this.zzWu = statusCode;
        this.mPendingIntent = pendingIntent;
    }

    public ConnectionResult(int statusCode, PendingIntent pendingIntent) {
        this(1, statusCode, pendingIntent);
    }

    static String getStatusString(int statusCode) {
        switch (statusCode) {
            case 0:
                return "SUCCESS";
            case 1:
                return "SERVICE_MISSING";
            case 2:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case 3:
                return "SERVICE_DISABLED";
            case MetaballPath.POINT_NUM /*4*/:
                return "SIGN_IN_REQUIRED";
            case 5:
                return "INVALID_ACCOUNT";
            case 6:
                return "RESOLUTION_REQUIRED";
            case 7:
                return "NETWORK_ERROR";
            case 8:
                return "INTERNAL_ERROR";
            case 9:
                return "SERVICE_INVALID";
            case 10:
                return "DEVELOPER_ERROR";
            case 11:
                return "LICENSE_CHECK_FAILED";
            case 13:
                return "CANCELED";
            case 14:
                return "TIMEOUT";
            case 15:
                return "INTERRUPTED";
            case 16:
                return "API_UNAVAILABLE";
            case 17:
                return "SIGN_IN_FAILED";
            case 18:
                return "SERVICE_UPDATING";
            default:
                return "UNKNOWN_ERROR_CODE(" + statusCode + ")";
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof ConnectionResult)) {
            return false;
        }
        ConnectionResult connectionResult = (ConnectionResult) o;
        if (this.zzWu == connectionResult.zzWu) {
            if (!zzw.equal(this.mPendingIntent, connectionResult.mPendingIntent)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int getErrorCode() {
        return this.zzWu;
    }

    public PendingIntent getResolution() {
        return this.mPendingIntent;
    }

    public boolean hasResolution() {
        return (this.zzWu == 0 || this.mPendingIntent == null) ? false : true;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.zzWu), this.mPendingIntent);
    }

    public boolean isSuccess() {
        return this.zzWu == 0;
    }

    public void startResolutionForResult(Activity activity, int requestCode) throws SendIntentException {
        if (hasResolution()) {
            activity.startIntentSenderForResult(this.mPendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        }
    }

    public String toString() {
        return zzw.zzu(this).zzg("statusCode", getStatusString(this.zzWu)).zzg("resolution", this.mPendingIntent).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }
}
