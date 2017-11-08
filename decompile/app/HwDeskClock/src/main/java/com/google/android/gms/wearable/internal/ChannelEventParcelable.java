package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.ChannelApi.ChannelListener;

/* compiled from: Unknown */
public final class ChannelEventParcelable implements SafeParcelable {
    public static final Creator<ChannelEventParcelable> CREATOR = new zzm();
    final int mVersionCode;
    final int type;
    final int zzaZS;
    final int zzaZT;
    final ChannelImpl zzaZU;

    ChannelEventParcelable(int versionCode, ChannelImpl channel, int type, int closeReason, int appErrorCode) {
        this.mVersionCode = versionCode;
        this.zzaZU = channel;
        this.type = type;
        this.zzaZS = closeReason;
        this.zzaZT = appErrorCode;
    }

    private static String zzkB(int i) {
        switch (i) {
            case 1:
                return "CHANNEL_OPENED";
            case 2:
                return "CHANNEL_CLOSED";
            case 3:
                return "INPUT_CLOSED";
            case MetaballPath.POINT_NUM /*4*/:
                return "OUTPUT_CLOSED";
            default:
                return Integer.toString(i);
        }
    }

    private static String zzkC(int i) {
        switch (i) {
            case 0:
                return "CLOSE_REASON_NORMAL";
            case 1:
                return "CLOSE_REASON_DISCONNECTED";
            case 2:
                return "CLOSE_REASON_REMOTE_CLOSE";
            case 3:
                return "CLOSE_REASON_LOCAL_CLOSE";
            default:
                return Integer.toString(i);
        }
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "ChannelEventParcelable[versionCode=" + this.mVersionCode + ", channel=" + this.zzaZU + ", type=" + zzkB(this.type) + ", closeReason=" + zzkC(this.zzaZS) + ", appErrorCode=" + this.zzaZT + "]";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzm.zza(this, dest, flags);
    }

    public void zza(ChannelListener channelListener) {
        switch (this.type) {
            case 1:
                channelListener.onChannelOpened(this.zzaZU);
                return;
            case 2:
                channelListener.onChannelClosed(this.zzaZU, this.zzaZS, this.zzaZT);
                return;
            case 3:
                channelListener.onInputClosed(this.zzaZU, this.zzaZS, this.zzaZT);
                return;
            case MetaballPath.POINT_NUM /*4*/:
                channelListener.onOutputClosed(this.zzaZU, this.zzaZS, this.zzaZT);
                return;
            default:
                Log.w("ChannelEventParcelable", "Unknown type: " + this.type);
                return;
        }
    }
}
