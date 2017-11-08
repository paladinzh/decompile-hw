package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.view.View;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.signin.zze;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
public final class zzf {
    private final Account zzOY;
    private final String zzQl;
    private final Set<Scope> zzYY;
    private final int zzYZ;
    private final View zzZa;
    private final String zzZb;
    private final Set<Scope> zzadd;
    private final Map<Api<?>, zza> zzade;
    private final zze zzadf;
    private Integer zzadg;

    /* compiled from: Unknown */
    public static final class zza {
        public final Set<Scope> zzZp;
        public final boolean zzadh;
    }

    public zzf(Account account, Set<Scope> set, Map<Api<?>, zza> map, int i, View view, String str, String str2, zze zze) {
        this.zzOY = account;
        this.zzYY = set != null ? Collections.unmodifiableSet(set) : Collections.EMPTY_SET;
        if (map == null) {
            map = Collections.EMPTY_MAP;
        }
        this.zzade = map;
        this.zzZa = view;
        this.zzYZ = i;
        this.zzQl = str;
        this.zzZb = str2;
        this.zzadf = zze;
        Set hashSet = new HashSet(this.zzYY);
        for (zza zza : this.zzade.values()) {
            hashSet.addAll(zza.zzZp);
        }
        this.zzadd = Collections.unmodifiableSet(hashSet);
    }

    public Account getAccount() {
        return this.zzOY;
    }

    @Deprecated
    public String getAccountName() {
        return this.zzOY == null ? null : this.zzOY.name;
    }

    public void zza(Integer num) {
        this.zzadg = num;
    }

    public Set<Scope> zzb(Api<?> api) {
        zza zza = (zza) this.zzade.get(api);
        if (zza == null || zza.zzZp.isEmpty()) {
            return this.zzYY;
        }
        Set<Scope> hashSet = new HashSet(this.zzYY);
        hashSet.addAll(zza.zzZp);
        return hashSet;
    }

    public Account zzoh() {
        return this.zzOY == null ? new Account("<<default account>>", "com.google") : this.zzOY;
    }

    public Set<Scope> zzoj() {
        return this.zzYY;
    }

    public Set<Scope> zzok() {
        return this.zzadd;
    }

    public Map<Api<?>, zza> zzol() {
        return this.zzade;
    }

    public String zzom() {
        return this.zzQl;
    }

    public String zzon() {
        return this.zzZb;
    }

    public zze zzop() {
        return this.zzadf;
    }

    public Integer zzoq() {
        return this.zzadg;
    }
}
