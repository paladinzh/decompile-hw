package com.google.android.gms.auth.api.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.auth.api.signin.EmailSignInConfig;
import com.google.android.gms.auth.api.signin.FacebookSignInConfig;
import com.google.android.gms.auth.api.signin.GoogleSignInConfig;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class SignInConfiguration implements SafeParcelable {
    public static final Creator<SignInConfiguration> CREATOR = new zze();
    final int versionCode;
    private final String zzRT;
    private String zzRU;
    private EmailSignInConfig zzRV;
    private GoogleSignInConfig zzRW;
    private FacebookSignInConfig zzRX;
    private String zzRY;

    SignInConfiguration(int versionCode, String consumerPkgName, String serverClientId, EmailSignInConfig emailConfig, GoogleSignInConfig googleConfig, FacebookSignInConfig facebookConfig, String apiKey) {
        this.versionCode = versionCode;
        this.zzRT = zzx.zzcs(consumerPkgName);
        this.zzRU = serverClientId;
        this.zzRV = emailConfig;
        this.zzRW = googleConfig;
        this.zzRX = facebookConfig;
        this.zzRY = apiKey;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }

    public String zzlF() {
        return this.zzRT;
    }

    public String zzlG() {
        return this.zzRU;
    }

    public EmailSignInConfig zzlH() {
        return this.zzRV;
    }

    public GoogleSignInConfig zzlI() {
        return this.zzRW;
    }

    public FacebookSignInConfig zzlJ() {
        return this.zzRX;
    }

    public String zzlK() {
        return this.zzRY;
    }
}
