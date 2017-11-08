package com.google.android.gms.common.api;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class Status implements Result, SafeParcelable {
    public static final Creator<Status> CREATOR = new zzn();
    public static final Status zzaaE = new Status(0);
    public static final Status zzaaF = new Status(14);
    public static final Status zzaaG = new Status(8);
    public static final Status zzaaH = new Status(15);
    public static final Status zzaaI = new Status(16);
    private final PendingIntent mPendingIntent;
    private final int mVersionCode;
    private final int zzWu;
    private final String zzaaJ;

    public Status(int statusCode) {
        this(statusCode, null);
    }

    Status(int versionCode, int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this.mVersionCode = versionCode;
        this.zzWu = statusCode;
        this.zzaaJ = statusMessage;
        this.mPendingIntent = pendingIntent;
    }

    public Status(int statusCode, String statusMessage) {
        this(1, statusCode, statusMessage, null);
    }

    public Status(int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this(1, statusCode, statusMessage, pendingIntent);
    }

    private String zznJ() {
        return this.zzaaJ == null ? CommonStatusCodes.getStatusCodeString(this.zzWu) : this.zzaaJ;
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
        if (this.mVersionCode == status.mVersionCode && this.zzWu == status.zzWu && zzw.equal(this.zzaaJ, status.zzaaJ) && zzw.equal(this.mPendingIntent, status.mPendingIntent)) {
            z = true;
        }
        return z;
    }

    public Status getStatus() {
        return this;
    }

    public int getStatusCode() {
        return this.zzWu;
    }

    public String getStatusMessage() {
        return this.zzaaJ;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.mVersionCode), Integer.valueOf(this.zzWu), this.zzaaJ, this.mPendingIntent);
    }

    public boolean isSuccess() {
        return this.zzWu <= 0;
    }

    public String toString() {
        return zzw.zzu(this).zzg("statusCode", zznJ()).zzg("resolution", this.mPendingIntent).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzn.zza(this, out, flags);
    }

    PendingIntent zznI() {
        return this.mPendingIntent;
    }
}
