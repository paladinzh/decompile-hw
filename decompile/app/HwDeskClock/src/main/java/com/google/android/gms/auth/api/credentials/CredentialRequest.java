package com.google.android.gms.auth.api.credentials;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class CredentialRequest implements SafeParcelable {
    public static final Creator<CredentialRequest> CREATOR = new zzc();
    final int mVersionCode;
    private final boolean zzRj;
    private final String[] zzRk;
    private final CredentialPickerConfig zzRl;
    private final CredentialPickerConfig zzRm;

    /* compiled from: Unknown */
    public static final class Builder {
    }

    CredentialRequest(int version, boolean supportsPasswordLogin, String[] accountTypes, CredentialPickerConfig credentialPickerConfig, CredentialPickerConfig credentialHintPickerConfig) {
        this.mVersionCode = version;
        this.zzRj = supportsPasswordLogin;
        this.zzRk = (String[]) zzx.zzv(accountTypes);
        if (credentialPickerConfig == null) {
            credentialPickerConfig = new com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Builder().build();
        }
        this.zzRl = credentialPickerConfig;
        if (credentialHintPickerConfig == null) {
            credentialHintPickerConfig = new com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Builder().build();
        }
        this.zzRm = credentialHintPickerConfig;
    }

    public int describeContents() {
        return 0;
    }

    public String[] getAccountTypes() {
        return this.zzRk;
    }

    public CredentialPickerConfig getCredentialHintPickerConfig() {
        return this.zzRm;
    }

    public CredentialPickerConfig getCredentialPickerConfig() {
        return this.zzRl;
    }

    public boolean getSupportsPasswordLogin() {
        return this.zzRj;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }
}
