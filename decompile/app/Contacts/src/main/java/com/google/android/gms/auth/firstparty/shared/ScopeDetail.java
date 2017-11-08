package com.google.android.gms.auth.firstparty.shared;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public class ScopeDetail implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    String description;
    final int version;
    List<String> zzYA;
    public FACLData zzYB;
    String zzYw;
    String zzYx;
    String zzYy;
    String zzYz;

    ScopeDetail(int version, String description, String detail, String iconBase64, String paclPickerDataBase64, String service, List<String> warnings, FACLData friendPickerData) {
        this.version = version;
        this.description = description;
        this.zzYw = detail;
        this.zzYx = iconBase64;
        this.zzYy = paclPickerDataBase64;
        this.zzYz = service;
        this.zzYA = warnings;
        this.zzYB = friendPickerData;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzc.zza(this, dest, flags);
    }
}
