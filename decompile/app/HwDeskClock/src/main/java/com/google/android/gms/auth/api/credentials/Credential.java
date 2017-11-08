package com.google.android.gms.auth.api.credentials;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class Credential implements SafeParcelable {
    public static final Creator<Credential> CREATOR = new zza();
    private final String mName;
    final int mVersionCode;
    private final String zzRa;
    private final String zzRb;
    private final Uri zzRc;
    private final List<IdToken> zzRd;
    private final String zzRe;
    private final String zzRf;
    private final String zzRg;
    private final String zzRh;
    private final String zzwj;

    /* compiled from: Unknown */
    public static class Builder {
    }

    Credential(int version, String internalCredentialId, String internalServerContext, String id, String name, Uri profilePictureUri, List<IdToken> idTokens, String password, String accountType, String generatedPassword, String generatedHintId) {
        this.mVersionCode = version;
        this.zzRa = internalCredentialId;
        this.zzRb = internalServerContext;
        this.zzwj = (String) zzx.zzv(id);
        this.mName = name;
        this.zzRc = profilePictureUri;
        this.zzRd = idTokens != null ? Collections.unmodifiableList(idTokens) : Collections.emptyList();
        this.zzRe = password;
        this.zzRf = accountType;
        this.zzRg = generatedPassword;
        this.zzRh = generatedHintId;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof Credential)) {
            return false;
        }
        Credential credential = (Credential) other;
        if (TextUtils.equals(this.zzwj, credential.zzwj) && TextUtils.equals(this.mName, credential.mName) && zzw.equal(this.zzRc, credential.zzRc) && TextUtils.equals(this.zzRe, credential.zzRe) && TextUtils.equals(this.zzRf, credential.zzRf)) {
            if (!TextUtils.equals(this.zzRg, credential.zzRg)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String getAccountType() {
        return this.zzRf;
    }

    public String getGeneratedPassword() {
        return this.zzRg;
    }

    public String getId() {
        return this.zzwj;
    }

    public String getName() {
        return this.mName;
    }

    public String getPassword() {
        return this.zzRe;
    }

    public Uri getProfilePictureUri() {
        return this.zzRc;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzwj, this.mName, this.zzRc, this.zzRe, this.zzRf, this.zzRg);
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzlr() {
        return this.zzRa;
    }

    public String zzls() {
        return this.zzRb;
    }

    public List<IdToken> zzlt() {
        return this.zzRd;
    }

    public String zzlu() {
        return this.zzRh;
    }
}
