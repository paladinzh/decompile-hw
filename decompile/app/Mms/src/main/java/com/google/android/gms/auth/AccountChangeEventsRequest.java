package com.google.android.gms.auth;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class AccountChangeEventsRequest implements SafeParcelable {
    public static final Creator<AccountChangeEventsRequest> CREATOR = new zzb();
    final int mVersion;
    Account zzTI;
    @Deprecated
    String zzVa;
    int zzVc;

    public AccountChangeEventsRequest() {
        this.mVersion = 1;
    }

    AccountChangeEventsRequest(int version, int eventIndex, String accountName, Account account) {
        this.mVersion = version;
        this.zzVc = eventIndex;
        this.zzVa = accountName;
        if (account == null && !TextUtils.isEmpty(accountName)) {
            this.zzTI = new Account(accountName, "com.google");
        } else {
            this.zzTI = account;
        }
    }

    public int describeContents() {
        return 0;
    }

    public Account getAccount() {
        return this.zzTI;
    }

    public String getAccountName() {
        return this.zzVa;
    }

    public int getEventIndex() {
        return this.zzVc;
    }

    public AccountChangeEventsRequest setAccount(Account account) {
        this.zzTI = account;
        return this;
    }

    public AccountChangeEventsRequest setAccountName(String accountName) {
        this.zzVa = accountName;
        return this;
    }

    public AccountChangeEventsRequest setEventIndex(int eventIndex) {
        this.zzVc = eventIndex;
        return this;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzb.zza(this, dest, flags);
    }
}
