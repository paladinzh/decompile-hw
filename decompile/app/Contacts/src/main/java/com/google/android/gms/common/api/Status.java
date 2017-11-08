package com.google.android.gms.common.api;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender.SendIntentException;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class Status implements Result, SafeParcelable {
    public static final Creator<Status> CREATOR = new zzc();
    public static final Status zzagC = new Status(0);
    public static final Status zzagD = new Status(14);
    public static final Status zzagE = new Status(8);
    public static final Status zzagF = new Status(15);
    public static final Status zzagG = new Status(16);
    private final PendingIntent mPendingIntent;
    private final int mVersionCode;
    private final int zzade;
    private final String zzafC;

    public Status(int statusCode) {
        this(statusCode, null);
    }

    Status(int versionCode, int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this.mVersionCode = versionCode;
        this.zzade = statusCode;
        this.zzafC = statusMessage;
        this.mPendingIntent = pendingIntent;
    }

    public Status(int statusCode, String statusMessage) {
        this(1, statusCode, statusMessage, null);
    }

    public Status(int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this(1, statusCode, statusMessage, pendingIntent);
    }

    private String zzpd() {
        return this.zzafC == null ? CommonStatusCodes.getStatusCodeString(this.zzade) : this.zzafC;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Status)) {
            return false;
        }
        Status status = (Status) obj;
        if (this.mVersionCode == status.mVersionCode && this.zzade == status.zzade && zzw.equal(this.zzafC, status.zzafC) && zzw.equal(this.mPendingIntent, status.mPendingIntent)) {
            z = true;
        }
        return z;
    }

    public PendingIntent getResolution() {
        return this.mPendingIntent;
    }

    public Status getStatus() {
        return this;
    }

    public int getStatusCode() {
        return this.zzade;
    }

    public String getStatusMessage() {
        return this.zzafC;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public boolean hasResolution() {
        return this.mPendingIntent != null;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.mVersionCode), Integer.valueOf(this.zzade), this.zzafC, this.mPendingIntent);
    }

    public boolean isCanceled() {
        return this.zzade == 16;
    }

    public boolean isInterrupted() {
        return this.zzade == 14;
    }

    public boolean isSuccess() {
        return this.zzade <= 0;
    }

    public void startResolutionForResult(Activity activity, int requestCode) throws SendIntentException {
        if (hasResolution()) {
            activity.startIntentSenderForResult(this.mPendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        }
    }

    public String toString() {
        return zzw.zzy(this).zzg("statusCode", zzpd()).zzg("resolution", this.mPendingIntent).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }

    PendingIntent zzpc() {
        return this.mPendingIntent;
    }
}
