package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.zzd;

/* compiled from: Unknown */
public class AncsNotificationParcelable implements SafeParcelable, zzd {
    public static final Creator<AncsNotificationParcelable> CREATOR = new zzg();
    private int mId;
    final int mVersionCode;
    private final String zzTZ;
    private final String zzaIu;
    private byte zzaZA;
    private byte zzaZB;
    private byte zzaZC;
    private byte zzaZD;
    private final String zzaZz;
    private final String zzagW;
    private String zzahj;
    private final String zzass;

    AncsNotificationParcelable(int versionCode, int id, String appId, String dateTime, String notificationText, String title, String subtitle, String displayName, byte eventId, byte eventFlags, byte categoryId, byte categoryCount) {
        this.mId = id;
        this.mVersionCode = versionCode;
        this.zzaIu = appId;
        this.zzaZz = dateTime;
        this.zzTZ = notificationText;
        this.zzagW = title;
        this.zzass = subtitle;
        this.zzahj = displayName;
        this.zzaZA = (byte) eventId;
        this.zzaZB = (byte) eventFlags;
        this.zzaZC = (byte) categoryId;
        this.zzaZD = (byte) categoryCount;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AncsNotificationParcelable ancsNotificationParcelable = (AncsNotificationParcelable) o;
        return (this.zzaZD == ancsNotificationParcelable.zzaZD && this.zzaZC == ancsNotificationParcelable.zzaZC && this.zzaZB == ancsNotificationParcelable.zzaZB && this.zzaZA == ancsNotificationParcelable.zzaZA && this.mId == ancsNotificationParcelable.mId && this.mVersionCode == ancsNotificationParcelable.mVersionCode && this.zzaIu.equals(ancsNotificationParcelable.zzaIu)) ? (this.zzaZz != null ? this.zzaZz.equals(ancsNotificationParcelable.zzaZz) : ancsNotificationParcelable.zzaZz == null) ? this.zzahj.equals(ancsNotificationParcelable.zzahj) && this.zzTZ.equals(ancsNotificationParcelable.zzTZ) && this.zzass.equals(ancsNotificationParcelable.zzass) && this.zzagW.equals(ancsNotificationParcelable.zzagW) : false : false;
    }

    public String getDisplayName() {
        return this.zzahj != null ? this.zzahj : this.zzaIu;
    }

    public int getId() {
        return this.mId;
    }

    public String getTitle() {
        return this.zzagW;
    }

    public int hashCode() {
        return (((((((((((((((((this.zzaZz == null ? 0 : this.zzaZz.hashCode()) + (((((this.mVersionCode * 31) + this.mId) * 31) + this.zzaIu.hashCode()) * 31)) * 31) + this.zzTZ.hashCode()) * 31) + this.zzagW.hashCode()) * 31) + this.zzass.hashCode()) * 31) + this.zzahj.hashCode()) * 31) + this.zzaZA) * 31) + this.zzaZB) * 31) + this.zzaZC) * 31) + this.zzaZD;
    }

    public String toString() {
        return "AncsNotificationParcelable{mVersionCode=" + this.mVersionCode + ", mId=" + this.mId + ", mAppId='" + this.zzaIu + '\'' + ", mDateTime='" + this.zzaZz + '\'' + ", mNotificationText='" + this.zzTZ + '\'' + ", mTitle='" + this.zzagW + '\'' + ", mSubtitle='" + this.zzass + '\'' + ", mDisplayName='" + this.zzahj + '\'' + ", mEventId=" + this.zzaZA + ", mEventFlags=" + this.zzaZB + ", mCategoryId=" + this.zzaZC + ", mCategoryCount=" + this.zzaZD + '}';
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzg.zza(this, dest, flags);
    }

    public byte zzCA() {
        return this.zzaZA;
    }

    public byte zzCB() {
        return this.zzaZB;
    }

    public byte zzCC() {
        return this.zzaZC;
    }

    public byte zzCD() {
        return this.zzaZD;
    }

    public String zzCy() {
        return this.zzaZz;
    }

    public String zzCz() {
        return this.zzTZ;
    }

    public String zztY() {
        return this.zzaIu;
    }

    public String zztp() {
        return this.zzass;
    }
}
