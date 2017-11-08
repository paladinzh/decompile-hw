package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class LogEvent implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    public final String tag;
    public final int versionCode;
    public final long zzbdA;
    public final long zzbdB;
    public final byte[] zzbdC;
    public final Bundle zzbdD;

    LogEvent(int versionCode, long eventTime, long eventUptime, String tag, byte[] sourceExtensionBytes, Bundle keyValuePairs) {
        this.versionCode = versionCode;
        this.zzbdA = eventTime;
        this.zzbdB = eventUptime;
        this.tag = tag;
        this.zzbdC = sourceExtensionBytes;
        this.zzbdD = keyValuePairs;
    }

    public LogEvent(long eventTime, long eventUptime, String tag, byte[] sourceExtensionBytes, String... extras) {
        this.versionCode = 1;
        this.zzbdA = eventTime;
        this.zzbdB = eventUptime;
        this.tag = tag;
        this.zzbdC = sourceExtensionBytes;
        this.zzbdD = zzd(extras);
    }

    private static Bundle zzd(String... strArr) {
        if (strArr == null) {
            return null;
        }
        if (strArr.length % 2 == 0) {
            int length = strArr.length / 2;
            if (length == 0) {
                return null;
            }
            Bundle bundle = new Bundle(length);
            for (int i = 0; i < length; i++) {
                bundle.putString(strArr[i * 2], strArr[(i * 2) + 1]);
            }
            return bundle;
        }
        throw new IllegalArgumentException("extras must have an even number of elements");
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag=").append(this.tag).append(",");
        stringBuilder.append("eventTime=").append(this.zzbdA).append(",");
        stringBuilder.append("eventUptime=").append(this.zzbdB).append(",");
        if (!(this.zzbdD == null || this.zzbdD.isEmpty())) {
            stringBuilder.append("keyValues=");
            for (String str : this.zzbdD.keySet()) {
                stringBuilder.append("(").append(str).append(",");
                stringBuilder.append(this.zzbdD.getString(str)).append(")");
                stringBuilder.append(HwCustPreloadContacts.EMPTY_STRING);
            }
        }
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }
}
