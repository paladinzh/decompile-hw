package com.google.android.gms.common.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.ResolveAccountResponse;
import com.google.android.gms.common.internal.zzp;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.signin.internal.AuthAccountResult;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

/* compiled from: Unknown */
public class zzg implements zzj {
    private final Context mContext;
    private com.google.android.gms.signin.zzd zzZA;
    private int zzZB;
    private boolean zzZC;
    private boolean zzZD;
    private zzp zzZE;
    private boolean zzZF;
    private boolean zzZG;
    private final com.google.android.gms.common.internal.zzf zzZH;
    private final Map<Api<?>, Integer> zzZI;
    private ArrayList<Future<?>> zzZJ = new ArrayList();
    private final GoogleApiAvailability zzZi;
    private final com.google.android.gms.common.api.Api.zza<? extends com.google.android.gms.signin.zzd, com.google.android.gms.signin.zze> zzZj;
    private final zzi zzZq;
    private final Lock zzZs;
    private ConnectionResult zzZt;
    private int zzZu;
    private int zzZv = 0;
    private boolean zzZw = false;
    private int zzZx;
    private final Bundle zzZy = new Bundle();
    private final Set<com.google.android.gms.common.api.Api.zzc> zzZz = new HashSet();

    /* compiled from: Unknown */
    private static class zza extends com.google.android.gms.signin.internal.zzb {
        private final WeakReference<zzg> zzZL;

        zza(zzg zzg) {
            this.zzZL = new WeakReference(zzg);
        }

        public void zza(final ConnectionResult connectionResult, AuthAccountResult authAccountResult) {
            final zzg zzg = (zzg) this.zzZL.get();
            if (zzg != null) {
                zzg.zzZq.zza(new zzb(this, zzg) {
                    final /* synthetic */ zza zzZO;

                    public void zzno() {
                        zzg.zzc(connectionResult);
                    }
                });
            }
        }
    }

    /* compiled from: Unknown */
    private static class zzb extends com.google.android.gms.common.internal.zzt.zza {
        private final WeakReference<zzg> zzZL;

        zzb(zzg zzg) {
            this.zzZL = new WeakReference(zzg);
        }

        public void zzb(final ResolveAccountResponse resolveAccountResponse) {
            final zzg zzg = (zzg) this.zzZL.get();
            if (zzg != null) {
                zzg.zzZq.zza(new zzb(this, zzg) {
                    final /* synthetic */ zzb zzZQ;

                    public void zzno() {
                        zzg.zza(resolveAccountResponse);
                    }
                });
            }
        }
    }

    /* compiled from: Unknown */
    private abstract class zzi implements Runnable {
        final /* synthetic */ zzg zzZK;

        private zzi(zzg zzg) {
            this.zzZK = zzg;
        }

        public void run() {
            this.zzZK.zzZs.lock();
            try {
                if (Thread.interrupted()) {
                    this.zzZK.zzZs.unlock();
                } else {
                    zzno();
                }
            } catch (RuntimeException e) {
                this.zzZK.zzZq.zza(e);
            } finally {
                this.zzZK.zzZs.unlock();
            }
        }

        protected abstract void zzno();
    }

    /* compiled from: Unknown */
    private class zzc extends zzi {
        final /* synthetic */ zzg zzZK;

        private zzc(zzg zzg) {
            this.zzZK = zzg;
            super();
        }

        public void zzno() {
            this.zzZK.zzZA.zza(this.zzZK.zzZE, this.zzZK.zzZq.zzaai, new zza(this.zzZK));
        }
    }

    /* compiled from: Unknown */
    private static class zzd implements com.google.android.gms.common.api.GoogleApiClient.zza {
        private final WeakReference<zzg> zzZL;
        private final Api<?> zzZR;
        private final int zzZS;

        public zzd(zzg zzg, Api<?> api, int i) {
            this.zzZL = new WeakReference(zzg);
            this.zzZR = api;
            this.zzZS = i;
        }

        public void zza(ConnectionResult connectionResult) {
            boolean z = false;
            zzg zzg = (zzg) this.zzZL.get();
            if (zzg != null) {
                if (Looper.myLooper() == zzg.zzZq.getLooper()) {
                    z = true;
                }
                zzx.zza(z, "onReportServiceBinding must be called on the GoogleApiClient handler thread");
                zzg.zzZs.lock();
                try {
                    if (zzg.zzbe(0)) {
                        if (!connectionResult.isSuccess()) {
                            zzg.zzb(connectionResult, this.zzZR, this.zzZS);
                        }
                        if (zzg.zznp()) {
                            zzg.zznq();
                        }
                        zzg.zzZs.unlock();
                        return;
                    }
                    zzg.zzZs.unlock();
                } catch (Throwable th) {
                    zzg.zzZs.unlock();
                }
            }
        }

        public void zzb(ConnectionResult connectionResult) {
            boolean z = false;
            zzg zzg = (zzg) this.zzZL.get();
            if (zzg != null) {
                if (Looper.myLooper() == zzg.zzZq.getLooper()) {
                    z = true;
                }
                zzx.zza(z, "onReportAccountValidation must be called on the GoogleApiClient handler thread");
                zzg.zzZs.lock();
                try {
                    if (zzg.zzbe(1)) {
                        if (!connectionResult.isSuccess()) {
                            zzg.zzb(connectionResult, this.zzZR, this.zzZS);
                        }
                        if (zzg.zznp()) {
                            zzg.zzns();
                        }
                        zzg.zzZs.unlock();
                        return;
                    }
                    zzg.zzZs.unlock();
                } catch (Throwable th) {
                    zzg.zzZs.unlock();
                }
            }
        }
    }

    /* compiled from: Unknown */
    private class zze extends zzi {
        final /* synthetic */ zzg zzZK;
        private final Map<com.google.android.gms.common.api.Api.zzb, zzd> zzZT;

        public zze(zzg zzg, Map<com.google.android.gms.common.api.Api.zzb, zzd> map) {
            this.zzZK = zzg;
            super();
            this.zzZT = map;
        }

        public void zzno() {
            int i = 1;
            int i2 = 0;
            int i3 = 1;
            int i4 = 0;
            for (com.google.android.gms.common.api.Api.zzb zzb : this.zzZT.keySet()) {
                int i5;
                if (zzb.zznf()) {
                    if (((zzd) this.zzZT.get(zzb)).zzZS == 0) {
                        i4 = 1;
                        break;
                    } else {
                        i5 = i3;
                        i3 = 1;
                    }
                } else {
                    i5 = 0;
                    i3 = i4;
                }
                i4 = i3;
                i3 = i5;
            }
            i = 0;
            if (i4 != 0) {
                i2 = this.zzZK.zzZi.isGooglePlayServicesAvailable(this.zzZK.mContext);
            }
            if (i2 != 0) {
                if (i != 0 || i3 != 0) {
                    final ConnectionResult connectionResult = new ConnectionResult(i2, null);
                    this.zzZK.zzZq.zza(new zzb(this, this.zzZK) {
                        final /* synthetic */ zze zzZV;

                        public void zzno() {
                            this.zzZV.zzZK.zzf(connectionResult);
                        }
                    });
                    return;
                }
            }
            if (this.zzZK.zzZC) {
                this.zzZK.zzZA.connect();
            }
            for (com.google.android.gms.common.api.Api.zzb zzb2 : this.zzZT.keySet()) {
                final com.google.android.gms.common.api.GoogleApiClient.zza zza = (com.google.android.gms.common.api.GoogleApiClient.zza) this.zzZT.get(zzb2);
                if (zzb2.zznf() && i2 != 0) {
                    this.zzZK.zzZq.zza(new zzb(this, this.zzZK) {
                        final /* synthetic */ zze zzZV;

                        public void zzno() {
                            zza.zza(new ConnectionResult(16, null));
                        }
                    });
                } else {
                    zzb2.zza(zza);
                }
            }
        }
    }

    /* compiled from: Unknown */
    private class zzf extends zzi {
        final /* synthetic */ zzg zzZK;
        private final ArrayList<com.google.android.gms.common.api.Api.zzb> zzZX;

        public zzf(zzg zzg, ArrayList<com.google.android.gms.common.api.Api.zzb> arrayList) {
            this.zzZK = zzg;
            super();
            this.zzZX = arrayList;
        }

        public void zzno() {
            Set set = this.zzZK.zzZq.zzaai;
            if (set.isEmpty()) {
                set = this.zzZK.zznx();
            }
            Set set2 = set;
            Iterator it = this.zzZX.iterator();
            while (it.hasNext()) {
                ((com.google.android.gms.common.api.Api.zzb) it.next()).zza(this.zzZK.zzZE, set2);
            }
        }
    }

    /* compiled from: Unknown */
    private class zzg implements ConnectionCallbacks, OnConnectionFailedListener {
        final /* synthetic */ zzg zzZK;

        private zzg(zzg zzg) {
            this.zzZK = zzg;
        }

        public void onConnected(Bundle connectionHint) {
            this.zzZK.zzZA.zza(new zzb(this.zzZK));
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzZK.zzZs.lock();
            try {
                if (this.zzZK.zze(result)) {
                    this.zzZK.zznv();
                    this.zzZK.zznt();
                } else {
                    this.zzZK.zzf(result);
                }
                this.zzZK.zzZs.unlock();
            } catch (Throwable th) {
                this.zzZK.zzZs.unlock();
            }
        }

        public void onConnectionSuspended(int cause) {
        }
    }

    /* compiled from: Unknown */
    private class zzh extends zzi {
        final /* synthetic */ zzg zzZK;
        private final ArrayList<com.google.android.gms.common.api.Api.zzb> zzZX;

        public zzh(zzg zzg, ArrayList<com.google.android.gms.common.api.Api.zzb> arrayList) {
            this.zzZK = zzg;
            super();
            this.zzZX = arrayList;
        }

        public void zzno() {
            Iterator it = this.zzZX.iterator();
            while (it.hasNext()) {
                ((com.google.android.gms.common.api.Api.zzb) it.next()).zza(this.zzZK.zzZE);
            }
        }
    }

    public zzg(zzi zzi, com.google.android.gms.common.internal.zzf zzf, Map<Api<?>, Integer> map, GoogleApiAvailability googleApiAvailability, com.google.android.gms.common.api.Api.zza<? extends com.google.android.gms.signin.zzd, com.google.android.gms.signin.zze> zza, Lock lock, Context context) {
        this.zzZq = zzi;
        this.zzZH = zzf;
        this.zzZI = map;
        this.zzZi = googleApiAvailability;
        this.zzZj = zza;
        this.zzZs = lock;
        this.mContext = context;
    }

    private void zzX(boolean z) {
        if (this.zzZA != null) {
            if (this.zzZA.isConnected() && z) {
                this.zzZA.zzzp();
            }
            this.zzZA.disconnect();
            this.zzZE = null;
        }
    }

    private void zza(ResolveAccountResponse resolveAccountResponse) {
        if (zzbe(0)) {
            ConnectionResult zzoR = resolveAccountResponse.zzoR();
            if (zzoR.isSuccess()) {
                this.zzZE = resolveAccountResponse.zzoQ();
                this.zzZD = true;
                this.zzZF = resolveAccountResponse.zzoS();
                this.zzZG = resolveAccountResponse.zzoT();
                zznq();
            } else if (zze(zzoR)) {
                zznv();
                zznq();
            } else {
                zzf(zzoR);
            }
        }
    }

    private boolean zza(int i, int i2, ConnectionResult connectionResult) {
        boolean z = false;
        if (i2 == 1 && !zzd(connectionResult)) {
            return false;
        }
        if (this.zzZt == null || i < this.zzZu) {
            z = true;
        }
        return z;
    }

    private void zzb(ConnectionResult connectionResult, Api<?> api, int i) {
        if (i != 2) {
            int priority = api.zznb().getPriority();
            if (zza(priority, i, connectionResult)) {
                this.zzZt = connectionResult;
                this.zzZu = priority;
            }
        }
        this.zzZq.zzaah.put(api.zznd(), connectionResult);
    }

    private boolean zzbe(int i) {
        if (this.zzZv == i) {
            return true;
        }
        Log.wtf("GoogleApiClientConnecting", "GoogleApiClient connecting is in step " + zzbf(this.zzZv) + " but received callback for step " + zzbf(i));
        zzf(new ConnectionResult(8, null));
        return false;
    }

    private String zzbf(int i) {
        switch (i) {
            case 0:
                return "STEP_GETTING_SERVICE_BINDINGS";
            case 1:
                return "STEP_VALIDATING_ACCOUNT";
            case 2:
                return "STEP_AUTHENTICATING";
            case 3:
                return "STEP_GETTING_REMOTE_SERVICE";
            default:
                return "UNKNOWN";
        }
    }

    private void zzc(ConnectionResult connectionResult) {
        if (zzbe(2)) {
            if (connectionResult.isSuccess()) {
                zznt();
            } else if (zze(connectionResult)) {
                zznv();
                zznt();
            } else {
                zzf(connectionResult);
            }
        }
    }

    private boolean zzd(ConnectionResult connectionResult) {
        boolean z = false;
        if (connectionResult.hasResolution()) {
            return true;
        }
        if (this.zzZi.zzbb(connectionResult.getErrorCode()) != null) {
            z = true;
        }
        return z;
    }

    private boolean zze(ConnectionResult connectionResult) {
        if (this.zzZB != 2) {
            if (this.zzZB != 1) {
                return false;
            }
            if (connectionResult.hasResolution()) {
                return false;
            }
        }
        return true;
    }

    private void zzf(ConnectionResult connectionResult) {
        boolean z = false;
        this.zzZw = false;
        zznw();
        if (!connectionResult.hasResolution()) {
            z = true;
        }
        zzX(z);
        this.zzZq.zzaah.clear();
        this.zzZq.zzg(connectionResult);
        if (!this.zzZq.zznC() || !this.zzZi.zzd(this.mContext, connectionResult.getErrorCode())) {
            this.zzZq.zznF();
            this.zzZq.zzZZ.zzj(connectionResult);
        }
        this.zzZq.zzZZ.zzoK();
    }

    private boolean zznp() {
        this.zzZx--;
        if (this.zzZx > 0) {
            return false;
        }
        if (this.zzZx < 0) {
            Log.wtf("GoogleApiClientConnecting", "GoogleApiClient received too many callbacks for the given step. Clients may be in an unexpected state; GoogleApiClient will now disconnect.");
            zzf(new ConnectionResult(8, null));
            return false;
        } else if (this.zzZt == null) {
            return true;
        } else {
            zzf(this.zzZt);
            return false;
        }
    }

    private void zznq() {
        if (this.zzZx == 0) {
            if (!this.zzZC) {
                zznt();
            } else if (this.zzZD) {
                zznr();
            }
        }
    }

    private void zznr() {
        ArrayList arrayList = new ArrayList();
        this.zzZv = 1;
        this.zzZx = this.zzZq.zzaag.size();
        for (com.google.android.gms.common.api.Api.zzc zzc : this.zzZq.zzaag.keySet()) {
            if (!this.zzZq.zzaah.containsKey(zzc)) {
                arrayList.add(this.zzZq.zzaag.get(zzc));
            } else if (zznp()) {
                zzns();
            }
        }
        if (!arrayList.isEmpty()) {
            this.zzZJ.add(zzk.zznG().submit(new zzh(this, arrayList)));
        }
    }

    private void zzns() {
        this.zzZv = 2;
        this.zzZq.zzaai = zznx();
        this.zzZJ.add(zzk.zznG().submit(new zzc()));
    }

    private void zznt() {
        ArrayList arrayList = new ArrayList();
        this.zzZv = 3;
        this.zzZx = this.zzZq.zzaag.size();
        for (com.google.android.gms.common.api.Api.zzc zzc : this.zzZq.zzaag.keySet()) {
            if (!this.zzZq.zzaah.containsKey(zzc)) {
                arrayList.add(this.zzZq.zzaag.get(zzc));
            } else if (zznp()) {
                zznu();
            }
        }
        if (!arrayList.isEmpty()) {
            this.zzZJ.add(zzk.zznG().submit(new zzf(this, arrayList)));
        }
    }

    private void zznu() {
        this.zzZq.zznB();
        zzk.zznG().execute(new Runnable(this) {
            final /* synthetic */ zzg zzZK;

            {
                this.zzZK = r1;
            }

            public void run() {
                this.zzZK.zzZi.zzac(this.zzZK.mContext);
            }
        });
        if (this.zzZA != null) {
            if (this.zzZF) {
                this.zzZA.zza(this.zzZE, this.zzZG);
            }
            zzX(false);
        }
        for (com.google.android.gms.common.api.Api.zzc zzc : this.zzZq.zzaah.keySet()) {
            ((com.google.android.gms.common.api.Api.zzb) this.zzZq.zzaag.get(zzc)).disconnect();
        }
        if (this.zzZw) {
            this.zzZw = false;
            disconnect();
            return;
        }
        this.zzZq.zzZZ.zzh(!this.zzZy.isEmpty() ? this.zzZy : null);
    }

    private void zznv() {
        this.zzZC = false;
        this.zzZq.zzaai = Collections.emptySet();
        for (com.google.android.gms.common.api.Api.zzc zzc : this.zzZz) {
            if (!this.zzZq.zzaah.containsKey(zzc)) {
                this.zzZq.zzaah.put(zzc, new ConnectionResult(17, null));
            }
        }
    }

    private void zznw() {
        Iterator it = this.zzZJ.iterator();
        while (it.hasNext()) {
            ((Future) it.next()).cancel(true);
        }
        this.zzZJ.clear();
    }

    private Set<Scope> zznx() {
        Set<Scope> hashSet = new HashSet(this.zzZH.zzoj());
        Map zzol = this.zzZH.zzol();
        for (Api api : zzol.keySet()) {
            if (!this.zzZq.zzaah.containsKey(api.zznd())) {
                hashSet.addAll(((com.google.android.gms.common.internal.zzf.zza) zzol.get(api)).zzZp);
            }
        }
        return hashSet;
    }

    public void begin() {
        this.zzZq.zzZZ.zzoL();
        this.zzZq.zzaah.clear();
        this.zzZw = false;
        this.zzZC = false;
        this.zzZt = null;
        this.zzZv = 0;
        this.zzZB = 2;
        this.zzZD = false;
        this.zzZF = false;
        Map hashMap = new HashMap();
        int i = 0;
        for (Api api : this.zzZI.keySet()) {
            com.google.android.gms.common.api.Api.zzb zzb = (com.google.android.gms.common.api.Api.zzb) this.zzZq.zzaag.get(api.zznd());
            int intValue = ((Integer) this.zzZI.get(api)).intValue();
            int i2 = (api.zznb().getPriority() != 1 ? 0 : 1) | i;
            if (zzb.zzlm()) {
                this.zzZC = true;
                if (intValue < this.zzZB) {
                    this.zzZB = intValue;
                }
                if (intValue != 0) {
                    this.zzZz.add(api.zznd());
                }
            }
            hashMap.put(zzb, new zzd(this, api, intValue));
            i = i2;
        }
        if (i != 0) {
            this.zzZC = false;
        }
        if (this.zzZC) {
            this.zzZH.zza(Integer.valueOf(this.zzZq.getSessionId()));
            ConnectionCallbacks zzg = new zzg();
            this.zzZA = (com.google.android.gms.signin.zzd) this.zzZj.zza(this.mContext, this.zzZq.getLooper(), this.zzZH, this.zzZH.zzop(), zzg, zzg);
        }
        this.zzZx = this.zzZq.zzaag.size();
        this.zzZJ.add(zzk.zznG().submit(new zze(this, hashMap)));
    }

    public void connect() {
        this.zzZw = false;
    }

    public void disconnect() {
        Iterator it = this.zzZq.zzaaa.iterator();
        while (it.hasNext()) {
            zze zze = (zze) it.next();
            if (zze.zznh() != 1) {
                zze.cancel();
                it.remove();
            }
        }
        this.zzZq.zzny();
        if (this.zzZt == null && !this.zzZq.zzaaa.isEmpty()) {
            this.zzZw = true;
            return;
        }
        zznw();
        zzX(true);
        this.zzZq.zzaah.clear();
        this.zzZq.zzg(null);
        this.zzZq.zzZZ.zzoK();
    }

    public String getName() {
        return "CONNECTING";
    }

    public void onConnected(Bundle connectionHint) {
        if (zzbe(3)) {
            if (connectionHint != null) {
                this.zzZy.putAll(connectionHint);
            }
            if (zznp()) {
                zznu();
            }
        }
    }

    public void onConnectionSuspended(int cause) {
        zzf(new ConnectionResult(8, null));
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, R extends Result, T extends com.google.android.gms.common.api.zzc.zza<R, A>> T zza(T t) {
        this.zzZq.zzaaa.add(t);
        return t;
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
        if (zzbe(3)) {
            zzb(connectionResult, api, i);
            if (zznp()) {
                zznu();
            }
        }
    }
}
