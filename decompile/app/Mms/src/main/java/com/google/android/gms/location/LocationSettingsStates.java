package com.google.android.gms.location;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.safeparcel.zzc;

/* compiled from: Unknown */
public final class LocationSettingsStates implements SafeParcelable {
    public static final Creator<LocationSettingsStates> CREATOR = new zzh();
    private final int mVersionCode;
    private final boolean zzaOj;
    private final boolean zzaOk;
    private final boolean zzaOl;
    private final boolean zzaOm;
    private final boolean zzaOn;
    private final boolean zzaOo;

    LocationSettingsStates(int version, boolean gpsUsable, boolean nlpUsable, boolean bleUsable, boolean gpsPresent, boolean nlpPresent, boolean blePresent) {
        this.mVersionCode = version;
        this.zzaOj = gpsUsable;
        this.zzaOk = nlpUsable;
        this.zzaOl = bleUsable;
        this.zzaOm = gpsPresent;
        this.zzaOn = nlpPresent;
        this.zzaOo = blePresent;
    }

    public static LocationSettingsStates fromIntent(Intent intent) {
        return (LocationSettingsStates) zzc.zza(intent, "com.google.android.gms.location.LOCATION_SETTINGS_STATES", CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public boolean isBlePresent() {
        return this.zzaOo;
    }

    public boolean isBleUsable() {
        return this.zzaOl;
    }

    public boolean isGpsPresent() {
        return this.zzaOm;
    }

    public boolean isGpsUsable() {
        return this.zzaOj;
    }

    public boolean isLocationPresent() {
        return this.zzaOm || this.zzaOn;
    }

    public boolean isLocationUsable() {
        return this.zzaOj || this.zzaOk;
    }

    public boolean isNetworkLocationPresent() {
        return this.zzaOn;
    }

    public boolean isNetworkLocationUsable() {
        return this.zzaOk;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzh.zza(this, dest, flags);
    }
}
