package com.google.android.gms.auth;

import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;
import java.util.List;

/* compiled from: Unknown */
public class TokenData implements SafeParcelable {
    public static final zze CREATOR = new zze();
    final int mVersionCode;
    private final String zzVo;
    private final Long zzVp;
    private final boolean zzVq;
    private final boolean zzVr;
    private final List<String> zzVs;

    TokenData(int versionCode, String token, Long expirationTimeSecs, boolean isCached, boolean isSnowballed, List<String> grantedScopes) {
        this.mVersionCode = versionCode;
        this.zzVo = zzx.zzcM(token);
        this.zzVp = expirationTimeSecs;
        this.zzVq = isCached;
        this.zzVr = isSnowballed;
        this.zzVs = grantedScopes;
    }

    @Nullable
    public static TokenData zzc(Bundle bundle, String str) {
        bundle.setClassLoader(TokenData.class.getClassLoader());
        Bundle bundle2 = bundle.getBundle(str);
        if (bundle2 == null) {
            return null;
        }
        bundle2.setClassLoader(TokenData.class.getClassLoader());
        return (TokenData) bundle2.getParcelable("TokenData");
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof TokenData)) {
            return false;
        }
        TokenData tokenData = (TokenData) o;
        if (TextUtils.equals(this.zzVo, tokenData.zzVo) && zzw.equal(this.zzVp, tokenData.zzVp) && this.zzVq == tokenData.zzVq && this.zzVr == tokenData.zzVr && zzw.equal(this.zzVs, tokenData.zzVs)) {
            z = true;
        }
        return z;
    }

    public String getToken() {
        return this.zzVo;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzVo, this.zzVp, Boolean.valueOf(this.zzVq), Boolean.valueOf(this.zzVr), this.zzVs);
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }

    @Nullable
    public Long zzmn() {
        return this.zzVp;
    }

    public boolean zzmo() {
        return this.zzVq;
    }

    public boolean zzmp() {
        return this.zzVr;
    }

    @Nullable
    public List<String> zzmq() {
        return this.zzVs;
    }
}
