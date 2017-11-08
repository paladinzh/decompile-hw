package com.google.android.gms.common.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class SignInButtonConfig implements SafeParcelable {
    public static final Creator<SignInButtonConfig> CREATOR = new zzaa();
    final int mVersionCode;
    private final Scope[] zzafT;
    private final int zzamu;
    private final int zzamv;

    SignInButtonConfig(int versionCode, int buttonSize, int colorScheme, Scope[] scopes) {
        this.mVersionCode = versionCode;
        this.zzamu = buttonSize;
        this.zzamv = colorScheme;
        this.zzafT = scopes;
    }

    public SignInButtonConfig(int buttonSize, int colorScheme, Scope[] scopes) {
        this(1, buttonSize, colorScheme, scopes);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzaa.zza(this, dest, flags);
    }

    public int zzrb() {
        return this.zzamu;
    }

    public int zzrc() {
        return this.zzamv;
    }

    public Scope[] zzrd() {
        return this.zzafT;
    }
}
