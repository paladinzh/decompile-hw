package com.google.android.gms.auth.api.signin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Patterns;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class EmailSignInConfig implements SafeParcelable {
    public static final Creator<EmailSignInConfig> CREATOR = new zza();
    final int versionCode;
    private final Uri zzRO;
    private String zzRP;
    private Uri zzRQ;

    EmailSignInConfig(int versionCode, Uri serverWidgetUrl, String modeQueryName, Uri tosUrl) {
        zzx.zzb((Object) serverWidgetUrl, (Object) "Server widget url cannot be null in order to use email/password sign in.");
        zzx.zzh(serverWidgetUrl.toString(), "Server widget url cannot be null in order to use email/password sign in.");
        zzx.zzb(Patterns.WEB_URL.matcher(serverWidgetUrl.toString()).matches(), (Object) "Invalid server widget url");
        this.versionCode = versionCode;
        this.zzRO = serverWidgetUrl;
        this.zzRP = modeQueryName;
        this.zzRQ = tosUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public Uri zzlA() {
        return this.zzRO;
    }

    public Uri zzlB() {
        return this.zzRQ;
    }

    public String zzlC() {
        return this.zzRP;
    }
}
