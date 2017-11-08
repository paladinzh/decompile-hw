package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class ResolveAccountRequest implements SafeParcelable {
    public static final Creator<ResolveAccountRequest> CREATOR = new zzy();
    final int mVersionCode;
    private final Account zzTI;
    private final int zzamq;
    private final GoogleSignInAccount zzamr;

    ResolveAccountRequest(int versionCode, Account account, int sessionId, GoogleSignInAccount signInAccountHint) {
        this.mVersionCode = versionCode;
        this.zzTI = account;
        this.zzamq = sessionId;
        this.zzamr = signInAccountHint;
    }

    public ResolveAccountRequest(Account account, int sessionId, GoogleSignInAccount signInAccountHint) {
        this(2, account, sessionId, signInAccountHint);
    }

    public int describeContents() {
        return 0;
    }

    public Account getAccount() {
        return this.zzTI;
    }

    public int getSessionId() {
        return this.zzamq;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzy.zza(this, dest, flags);
    }

    @Nullable
    public GoogleSignInAccount zzqW() {
        return this.zzamr;
    }
}
