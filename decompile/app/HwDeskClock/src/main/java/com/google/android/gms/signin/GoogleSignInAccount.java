package com.google.android.gms.signin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class GoogleSignInAccount implements SafeParcelable {
    public static final Creator<GoogleSignInAccount> CREATOR = new zza();
    final int versionCode;
    private String zzRn;
    private String zzaNZ;
    private Uri zzaOa;
    private String zzahj;
    private String zzwj;

    GoogleSignInAccount(int versionCode, String id, String idToken, String email, String displayName, Uri photoUrl) {
        this.versionCode = versionCode;
        this.zzwj = zzx.zzcs(id);
        this.zzRn = idToken;
        this.zzaNZ = email;
        this.zzahj = displayName;
        this.zzaOa = photoUrl;
    }

    public int describeContents() {
        return 0;
    }

    public String getDisplayName() {
        return this.zzahj;
    }

    public String getEmail() {
        return this.zzaNZ;
    }

    public String getId() {
        return this.zzwj;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzlv() {
        return this.zzRn;
    }

    public Uri zzzo() {
        return this.zzaOa;
    }
}
