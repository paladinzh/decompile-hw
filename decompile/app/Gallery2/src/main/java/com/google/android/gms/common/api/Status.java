package com.google.android.gms.common.api;

import android.app.PendingIntent;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ep;

/* compiled from: Unknown */
public final class Status implements SafeParcelable {
    public static final StatusCreator CREATOR = new StatusCreator();
    public static final Status zQ = new Status(0, null, null);
    public static final Status zR = new Status(14, null, null);
    public static final Status zS = new Status(15, null, null);
    private final PendingIntent mPendingIntent;
    private final int wj;
    private final int yJ;
    private final String zT;

    Status(int versionCode, int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this.wj = versionCode;
        this.yJ = statusCode;
        this.zT = statusMessage;
        this.mPendingIntent = pendingIntent;
    }

    public Status(int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this(1, statusCode, statusMessage, pendingIntent);
    }

    private String dn() {
        return this.zT == null ? CommonStatusCodes.getStatusCodeString(this.yJ) : this.zT;
    }

    PendingIntent dE() {
        return this.mPendingIntent;
    }

    String dF() {
        return this.zT;
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
        if (this.wj == status.wj && this.yJ == status.yJ && ep.equal(this.zT, status.zT) && ep.equal(this.mPendingIntent, status.mPendingIntent)) {
            z = true;
        }
        return z;
    }

    public int getStatusCode() {
        return this.yJ;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return ep.hashCode(Integer.valueOf(this.wj), Integer.valueOf(this.yJ), this.zT, this.mPendingIntent);
    }

    public String toString() {
        return ep.e(this).a("statusCode", dn()).a("resolution", this.mPendingIntent).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        StatusCreator.a(this, out, flags);
    }
}
