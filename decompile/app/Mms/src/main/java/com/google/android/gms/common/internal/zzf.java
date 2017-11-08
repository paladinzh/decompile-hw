package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.content.Context;
import android.view.View;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.internal.zzro;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
public final class zzf {
    private final Account zzTI;
    private final String zzUW;
    private final Set<Scope> zzagh;
    private final int zzagj;
    private final View zzagk;
    private final String zzagl;
    private final Set<Scope> zzalb;
    private final Map<Api<?>, zza> zzalc;
    private final zzro zzald;
    private Integer zzale;

    /* compiled from: Unknown */
    public static final class zza {
        public final Set<Scope> zzXf;
        public final boolean zzalf;

        public zza(Set<Scope> set, boolean z) {
            zzx.zzz(set);
            this.zzXf = Collections.unmodifiableSet(set);
            this.zzalf = z;
        }
    }

    public zzf(Account account, Set<Scope> set, Map<Api<?>, zza> map, int i, View view, String str, String str2, zzro zzro) {
        this.zzTI = account;
        this.zzagh = set != null ? Collections.unmodifiableSet(set) : Collections.EMPTY_SET;
        if (map == null) {
            map = Collections.EMPTY_MAP;
        }
        this.zzalc = map;
        this.zzagk = view;
        this.zzagj = i;
        this.zzUW = str;
        this.zzagl = str2;
        this.zzald = zzro;
        Set hashSet = new HashSet(this.zzagh);
        for (zza zza : this.zzalc.values()) {
            hashSet.addAll(zza.zzXf);
        }
        this.zzalb = Collections.unmodifiableSet(hashSet);
    }

    public static zzf zzat(Context context) {
        return new Builder(context).zzoY();
    }

    public Account getAccount() {
        return this.zzTI;
    }

    @Deprecated
    public String getAccountName() {
        return this.zzTI == null ? null : this.zzTI.name;
    }

    public void zza(Integer num) {
        this.zzale = num;
    }

    public Set<Scope> zzb(Api<?> api) {
        zza zza = (zza) this.zzalc.get(api);
        if (zza == null || zza.zzXf.isEmpty()) {
            return this.zzagh;
        }
        Set<Scope> hashSet = new HashSet(this.zzagh);
        hashSet.addAll(zza.zzXf);
        return hashSet;
    }

    public Account zzqq() {
        return this.zzTI == null ? new Account("<<default account>>", "com.google") : this.zzTI;
    }

    public int zzqr() {
        return this.zzagj;
    }

    public Set<Scope> zzqs() {
        return this.zzagh;
    }

    public Set<Scope> zzqt() {
        return this.zzalb;
    }

    public Map<Api<?>, zza> zzqu() {
        return this.zzalc;
    }

    public String zzqv() {
        return this.zzUW;
    }

    public String zzqw() {
        return this.zzagl;
    }

    public View zzqx() {
        return this.zzagk;
    }

    public zzro zzqy() {
        return this.zzald;
    }

    public Integer zzqz() {
        return this.zzale;
    }
}
