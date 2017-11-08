package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* compiled from: Unknown */
final class zzm extends zzl implements Callback {
    private final Handler mHandler;
    private final HashMap<zza, zzb> zzadW = new HashMap();
    private final com.google.android.gms.common.stats.zzb zzadX;
    private final long zzadY;
    private final Context zzqO;

    /* compiled from: Unknown */
    private static final class zza {
        private final String zzOj;
        private final String zzadZ;
        private final ComponentName zzaea = null;

        public zza(String str, String str2) {
            this.zzOj = zzx.zzcs(str);
            this.zzadZ = zzx.zzcs(str2);
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza zza = (zza) o;
            if (zzw.equal(this.zzOj, zza.zzOj)) {
                if (!zzw.equal(this.zzaea, zza.zzaea)) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            return zzw.hashCode(this.zzOj, this.zzaea);
        }

        public String toString() {
            return this.zzOj != null ? this.zzOj : this.zzaea.flattenToString();
        }

        public Intent zzoM() {
            return this.zzOj == null ? new Intent().setComponent(this.zzaea) : new Intent(this.zzOj).setPackage(this.zzadZ);
        }
    }

    /* compiled from: Unknown */
    private final class zzb {
        private int mState = 2;
        private IBinder zzacF;
        private ComponentName zzaea;
        private final zza zzaeb = new zza(this);
        private final Set<ServiceConnection> zzaec = new HashSet();
        private boolean zzaed;
        private final zza zzaee;
        final /* synthetic */ zzm zzaef;

        /* compiled from: Unknown */
        public class zza implements ServiceConnection {
            final /* synthetic */ zzb zzaeg;

            public zza(zzb zzb) {
                this.zzaeg = zzb;
            }

            public void onServiceConnected(ComponentName component, IBinder binder) {
                synchronized (this.zzaeg.zzaef.zzadW) {
                    this.zzaeg.zzacF = binder;
                    this.zzaeg.zzaea = component;
                    for (ServiceConnection onServiceConnected : this.zzaeg.zzaec) {
                        onServiceConnected.onServiceConnected(component, binder);
                    }
                    this.zzaeg.mState = 1;
                }
            }

            public void onServiceDisconnected(ComponentName component) {
                synchronized (this.zzaeg.zzaef.zzadW) {
                    this.zzaeg.zzacF = null;
                    this.zzaeg.zzaea = component;
                    for (ServiceConnection onServiceDisconnected : this.zzaeg.zzaec) {
                        onServiceDisconnected.onServiceDisconnected(component);
                    }
                    this.zzaeg.mState = 2;
                }
            }
        }

        public zzb(zzm zzm, zza zza) {
            this.zzaef = zzm;
            this.zzaee = zza;
        }

        public IBinder getBinder() {
            return this.zzacF;
        }

        public ComponentName getComponentName() {
            return this.zzaea;
        }

        public int getState() {
            return this.mState;
        }

        public boolean isBound() {
            return this.zzaed;
        }

        public void zza(ServiceConnection serviceConnection, String str) {
            this.zzaef.zzadX.zza(this.zzaef.zzqO, serviceConnection, str, this.zzaee.zzoM());
            this.zzaec.add(serviceConnection);
        }

        public boolean zza(ServiceConnection serviceConnection) {
            return this.zzaec.contains(serviceConnection);
        }

        public void zzb(ServiceConnection serviceConnection, String str) {
            this.zzaef.zzadX.zzb(this.zzaef.zzqO, serviceConnection);
            this.zzaec.remove(serviceConnection);
        }

        public void zzcl(String str) {
            this.zzaed = this.zzaef.zzadX.zza(this.zzaef.zzqO, str, this.zzaee.zzoM(), this.zzaeb, 129);
            if (this.zzaed) {
                this.mState = 3;
                return;
            }
            try {
                this.zzaef.zzadX.zza(this.zzaef.zzqO, this.zzaeb);
            } catch (IllegalArgumentException e) {
            }
        }

        public void zzcm(String str) {
            this.zzaef.zzadX.zza(this.zzaef.zzqO, this.zzaeb);
            this.zzaed = false;
            this.mState = 2;
        }

        public boolean zzoN() {
            return this.zzaec.isEmpty();
        }
    }

    zzm(Context context) {
        this.zzqO = context.getApplicationContext();
        this.mHandler = new Handler(context.getMainLooper(), this);
        this.zzadX = com.google.android.gms.common.stats.zzb.zzpF();
        this.zzadY = 5000;
    }

    private boolean zza(zza zza, ServiceConnection serviceConnection, String str) {
        boolean isBound;
        zzx.zzb((Object) serviceConnection, (Object) "ServiceConnection must not be null");
        synchronized (this.zzadW) {
            zzb zzb = (zzb) this.zzadW.get(zza);
            if (zzb != null) {
                this.mHandler.removeMessages(0, zzb);
                if (!zzb.zza(serviceConnection)) {
                    zzb.zza(serviceConnection, str);
                    switch (zzb.getState()) {
                        case 1:
                            serviceConnection.onServiceConnected(zzb.getComponentName(), zzb.getBinder());
                            break;
                        case 2:
                            zzb.zzcl(str);
                            break;
                    }
                }
                throw new IllegalStateException("Trying to bind a GmsServiceConnection that was already connected before.  config=" + zza);
            }
            zzb = new zzb(this, zza);
            zzb.zza(serviceConnection, str);
            zzb.zzcl(str);
            this.zzadW.put(zza, zzb);
            isBound = zzb.isBound();
        }
        return isBound;
    }

    private void zzb(zza zza, ServiceConnection serviceConnection, String str) {
        zzx.zzb((Object) serviceConnection, (Object) "ServiceConnection must not be null");
        synchronized (this.zzadW) {
            zzb zzb = (zzb) this.zzadW.get(zza);
            if (zzb == null) {
                throw new IllegalStateException("Nonexistent connection status for service config: " + zza);
            } else if (zzb.zza(serviceConnection)) {
                zzb.zzb(serviceConnection, str);
                if (zzb.zzoN()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, zzb), this.zzadY);
                }
            } else {
                throw new IllegalStateException("Trying to unbind a GmsServiceConnection  that was not bound before.  config=" + zza);
            }
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                zzb zzb = (zzb) msg.obj;
                synchronized (this.zzadW) {
                    if (zzb.zzoN()) {
                        if (zzb.isBound()) {
                            zzb.zzcm("GmsClientSupervisor");
                        }
                        this.zzadW.remove(zzb.zzaee);
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public boolean zza(String str, String str2, ServiceConnection serviceConnection, String str3) {
        return zza(new zza(str, str2), serviceConnection, str3);
    }

    public void zzb(String str, String str2, ServiceConnection serviceConnection, String str3) {
        zzb(new zza(str, str2), serviceConnection, str3);
    }
}
