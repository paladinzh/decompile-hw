package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public abstract class zzj<T extends IInterface> implements com.google.android.gms.common.api.Api.zzb, com.google.android.gms.common.internal.zzk.zza {
    public static final String[] zzadG = new String[]{"service_esmobile", "service_googleme"};
    private final Context mContext;
    final Handler mHandler;
    private final Account zzOY;
    private final Looper zzYV;
    private final zzf zzZH;
    private final GoogleApiAvailability zzZi;
    private final Set<Scope> zzZp;
    private zze zzadA;
    private int zzadB;
    private final ConnectionCallbacks zzadC;
    private final OnConnectionFailedListener zzadD;
    private final int zzadE;
    protected AtomicInteger zzadF;
    private final zzl zzadv;
    private zzs zzadw;
    private com.google.android.gms.common.api.GoogleApiClient.zza zzadx;
    private T zzady;
    private final ArrayList<zzc<?>> zzadz;
    private final Object zzpc;

    /* compiled from: Unknown */
    protected abstract class zzc<TListener> {
        private TListener mListener;
        final /* synthetic */ zzj zzadI;
        private boolean zzadJ = false;

        public zzc(zzj zzj, TListener tListener) {
            this.zzadI = zzj;
            this.mListener = tListener;
        }

        public void unregister() {
            zzoI();
            synchronized (this.zzadI.zzadz) {
                this.zzadI.zzadz.remove(this);
            }
        }

        protected abstract void zzoG();

        public void zzoH() {
            synchronized (this) {
                Object obj = this.mListener;
                if (this.zzadJ) {
                    Log.w("GmsClient", "Callback proxy " + this + " being reused. This is not safe.");
                }
            }
            if (obj == null) {
                zzoG();
            } else {
                try {
                    zzs(obj);
                } catch (RuntimeException e) {
                    zzoG();
                    throw e;
                }
            }
            synchronized (this) {
                this.zzadJ = true;
            }
            unregister();
        }

        public void zzoI() {
            synchronized (this) {
                this.mListener = null;
            }
        }

        protected abstract void zzs(TListener tListener);
    }

    /* compiled from: Unknown */
    private abstract class zza extends zzc<Boolean> {
        public final int statusCode;
        public final Bundle zzadH;
        final /* synthetic */ zzj zzadI;

        protected zza(zzj zzj, int i, Bundle bundle) {
            this.zzadI = zzj;
            super(zzj, Boolean.valueOf(true));
            this.statusCode = i;
            this.zzadH = bundle;
        }

        protected void zzc(Boolean bool) {
            PendingIntent pendingIntent = null;
            if (bool != null) {
                switch (this.statusCode) {
                    case 0:
                        if (!zzoF()) {
                            this.zzadI.zzb(1, null);
                            zzi(new ConnectionResult(8, null));
                            break;
                        }
                        break;
                    case 10:
                        this.zzadI.zzb(1, null);
                        throw new IllegalStateException("A fatal developer error has occurred. Check the logs for further information.");
                    default:
                        this.zzadI.zzb(1, null);
                        if (this.zzadH != null) {
                            pendingIntent = (PendingIntent) this.zzadH.getParcelable("pendingIntent");
                        }
                        zzi(new ConnectionResult(this.statusCode, pendingIntent));
                        break;
                }
                return;
            }
            this.zzadI.zzb(1, null);
        }

        protected abstract void zzi(ConnectionResult connectionResult);

        protected abstract boolean zzoF();

        protected void zzoG() {
        }

        protected /* synthetic */ void zzs(Object obj) {
            zzc((Boolean) obj);
        }
    }

    /* compiled from: Unknown */
    final class zzb extends Handler {
        final /* synthetic */ zzj zzadI;

        public zzb(zzj zzj, Looper looper) {
            this.zzadI = zzj;
            super(looper);
        }

        private void zza(Message message) {
            zzc zzc = (zzc) message.obj;
            zzc.zzoG();
            zzc.unregister();
        }

        private boolean zzb(Message message) {
            return message.what == 2 || message.what == 1 || message.what == 5 || message.what == 6;
        }

        public void handleMessage(Message msg) {
            PendingIntent pendingIntent = null;
            if (this.zzadI.zzadF.get() == msg.arg1) {
                if (msg.what == 1 || msg.what == 5 || msg.what == 6) {
                    if (!this.zzadI.isConnecting()) {
                        zza(msg);
                        return;
                    }
                }
                if (msg.what == 3) {
                    if (msg.obj instanceof PendingIntent) {
                        pendingIntent = (PendingIntent) msg.obj;
                    }
                    ConnectionResult connectionResult = new ConnectionResult(msg.arg2, pendingIntent);
                    this.zzadI.zzadx.zza(connectionResult);
                    this.zzadI.onConnectionFailed(connectionResult);
                    return;
                } else if (msg.what == 4) {
                    this.zzadI.zzb(4, null);
                    if (this.zzadI.zzadC != null) {
                        this.zzadI.zzadC.onConnectionSuspended(msg.arg2);
                    }
                    this.zzadI.onConnectionSuspended(msg.arg2);
                    this.zzadI.zza(4, 1, null);
                    return;
                } else if (msg.what == 2 && !this.zzadI.isConnected()) {
                    zza(msg);
                    return;
                } else if (zzb(msg)) {
                    ((zzc) msg.obj).zzoH();
                    return;
                } else {
                    Log.wtf("GmsClient", "Don't know how to handle this message.");
                    return;
                }
            }
            if (zzb(msg)) {
                zza(msg);
            }
        }
    }

    /* compiled from: Unknown */
    public static final class zzd extends com.google.android.gms.common.internal.zzr.zza {
        private zzj zzadK;
        private final int zzadL;

        public zzd(zzj zzj, int i) {
            this.zzadK = zzj;
            this.zzadL = i;
        }

        private void zzoJ() {
            this.zzadK = null;
        }

        public void zza(int i, IBinder iBinder, Bundle bundle) {
            zzx.zzb(this.zzadK, (Object) "onPostInitComplete can be called only once per call to getRemoteService");
            this.zzadK.zza(i, iBinder, bundle, this.zzadL);
            zzoJ();
        }

        public void zzb(int i, Bundle bundle) {
            zzx.zzb(this.zzadK, (Object) "onAccountValidationComplete can be called only once per call to validateAccount");
            this.zzadK.zza(i, bundle, this.zzadL);
            zzoJ();
        }
    }

    /* compiled from: Unknown */
    public final class zze implements ServiceConnection {
        final /* synthetic */ zzj zzadI;
        private final int zzadL;

        public zze(zzj zzj, int i) {
            this.zzadI = zzj;
            this.zzadL = i;
        }

        public void onServiceConnected(ComponentName component, IBinder binder) {
            zzx.zzb((Object) binder, (Object) "Expecting a valid IBinder");
            this.zzadI.zzadw = com.google.android.gms.common.internal.zzs.zza.zzaK(binder);
            this.zzadI.zzbA(this.zzadL);
        }

        public void onServiceDisconnected(ComponentName component) {
            this.zzadI.mHandler.sendMessage(this.zzadI.mHandler.obtainMessage(4, this.zzadL, 1));
        }
    }

    /* compiled from: Unknown */
    protected class zzf implements com.google.android.gms.common.api.GoogleApiClient.zza {
        final /* synthetic */ zzj zzadI;

        public zzf(zzj zzj) {
            this.zzadI = zzj;
        }

        public void zza(ConnectionResult connectionResult) {
            if (connectionResult.isSuccess()) {
                this.zzadI.zza(null, this.zzadI.zzZp);
            } else if (this.zzadI.zzadD != null) {
                this.zzadI.zzadD.onConnectionFailed(connectionResult);
            }
        }

        public void zzb(ConnectionResult connectionResult) {
            throw new IllegalStateException("Legacy GmsClient received onReportAccountValidation callback.");
        }
    }

    /* compiled from: Unknown */
    protected final class zzg extends zza {
        final /* synthetic */ zzj zzadI;
        public final IBinder zzadM;

        public zzg(zzj zzj, int i, IBinder iBinder, Bundle bundle) {
            this.zzadI = zzj;
            super(zzj, i, bundle);
            this.zzadM = iBinder;
        }

        protected void zzi(ConnectionResult connectionResult) {
            if (this.zzadI.zzadD != null) {
                this.zzadI.zzadD.onConnectionFailed(connectionResult);
            }
            this.zzadI.onConnectionFailed(connectionResult);
        }

        protected boolean zzoF() {
            try {
                String interfaceDescriptor = this.zzadM.getInterfaceDescriptor();
                if (this.zzadI.zzfB().equals(interfaceDescriptor)) {
                    IInterface zzV = this.zzadI.zzV(this.zzadM);
                    if (zzV == null || !this.zzadI.zza(2, 3, zzV)) {
                        return false;
                    }
                    Bundle zzmw = this.zzadI.zzmw();
                    if (this.zzadI.zzadC != null) {
                        this.zzadI.zzadC.onConnected(zzmw);
                    }
                    return true;
                }
                Log.e("GmsClient", "service descriptor mismatch: " + this.zzadI.zzfB() + " vs. " + interfaceDescriptor);
                return false;
            } catch (RemoteException e) {
                Log.w("GmsClient", "service probably died");
                return false;
            }
        }
    }

    /* compiled from: Unknown */
    protected final class zzh extends zza {
        final /* synthetic */ zzj zzadI;

        public zzh(zzj zzj) {
            this.zzadI = zzj;
            super(zzj, 0, null);
        }

        protected void zzi(ConnectionResult connectionResult) {
            this.zzadI.zzadx.zza(connectionResult);
            this.zzadI.onConnectionFailed(connectionResult);
        }

        protected boolean zzoF() {
            this.zzadI.zzadx.zza(ConnectionResult.zzYi);
            return true;
        }
    }

    /* compiled from: Unknown */
    protected final class zzi extends zza {
        final /* synthetic */ zzj zzadI;

        public zzi(zzj zzj, int i, Bundle bundle) {
            this.zzadI = zzj;
            super(zzj, i, bundle);
        }

        protected void zzi(ConnectionResult connectionResult) {
            this.zzadI.zzadx.zzb(connectionResult);
            this.zzadI.onConnectionFailed(connectionResult);
        }

        protected boolean zzoF() {
            this.zzadI.zzadx.zzb(ConnectionResult.zzYi);
            return true;
        }
    }

    protected zzj(Context context, Looper looper, int i, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        this(context, looper, zzl.zzak(context), GoogleApiAvailability.getInstance(), i, zzf, (ConnectionCallbacks) zzx.zzv(connectionCallbacks), (OnConnectionFailedListener) zzx.zzv(onConnectionFailedListener));
    }

    protected zzj(Context context, Looper looper, zzl zzl, GoogleApiAvailability googleApiAvailability, int i, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        this.zzpc = new Object();
        this.zzadz = new ArrayList();
        this.zzadB = 1;
        this.zzadF = new AtomicInteger(0);
        this.mContext = (Context) zzx.zzb((Object) context, (Object) "Context must not be null");
        this.zzYV = (Looper) zzx.zzb((Object) looper, (Object) "Looper must not be null");
        this.zzadv = (zzl) zzx.zzb((Object) zzl, (Object) "Supervisor must not be null");
        this.zzZi = (GoogleApiAvailability) zzx.zzb((Object) googleApiAvailability, (Object) "API availability must not be null");
        this.mHandler = new zzb(this, looper);
        this.zzadE = i;
        this.zzZH = (zzf) zzx.zzv(zzf);
        this.zzOY = zzf.getAccount();
        this.zzZp = zzb(zzf.zzok());
        this.zzadC = connectionCallbacks;
        this.zzadD = onConnectionFailedListener;
    }

    private boolean zza(int i, int i2, T t) {
        synchronized (this.zzpc) {
            if (this.zzadB == i) {
                zzb(i2, t);
                return true;
            }
            return false;
        }
    }

    private Set<Scope> zzb(Set<Scope> set) {
        Set<Scope> zza = zza((Set) set);
        if (zza == null) {
            return zza;
        }
        for (Scope contains : zza) {
            if (!set.contains(contains)) {
                throw new IllegalStateException("Expanding scopes is not permitted, use implied scopes instead");
            }
        }
        return zza;
    }

    private void zzb(int i, T t) {
        boolean z = false;
        if ((i == 3) == (t != null)) {
            z = true;
        }
        zzx.zzZ(z);
        synchronized (this.zzpc) {
            this.zzadB = i;
            this.zzady = t;
            zzc(i, t);
            switch (i) {
                case 1:
                    zzoy();
                    break;
                case 2:
                    zzox();
                    break;
                case 3:
                    zzow();
                    break;
            }
        }
    }

    private void zzox() {
        if (this.zzadA != null) {
            Log.e("GmsClient", "Calling connect() while still connected, missing disconnect() for " + zzfA() + " on " + zzou());
            this.zzadv.zzb(zzfA(), zzou(), this.zzadA, zzov());
            this.zzadF.incrementAndGet();
        }
        this.zzadA = new zze(this, this.zzadF.get());
        if (!this.zzadv.zza(zzfA(), zzou(), this.zzadA, zzov())) {
            Log.e("GmsClient", "unable to connect to service: " + zzfA() + " on " + zzou());
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, this.zzadF.get(), 9));
        }
    }

    private void zzoy() {
        if (this.zzadA != null) {
            this.zzadv.zzb(zzfA(), zzou(), this.zzadA, zzov());
            this.zzadA = null;
        }
    }

    public void disconnect() {
        this.zzadF.incrementAndGet();
        synchronized (this.zzadz) {
            int size = this.zzadz.size();
            for (int i = 0; i < size; i++) {
                ((zzc) this.zzadz.get(i)).zzoI();
            }
            this.zzadz.clear();
        }
        zzb(1, null);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        synchronized (this.zzpc) {
            int i = this.zzadB;
            IInterface iInterface = this.zzady;
        }
        writer.append(prefix).append("mConnectState=");
        switch (i) {
            case 1:
                writer.print("DISCONNECTED");
                break;
            case 2:
                writer.print("CONNECTING");
                break;
            case 3:
                writer.print("CONNECTED");
                break;
            case MetaballPath.POINT_NUM /*4*/:
                writer.print("DISCONNECTING");
                break;
            default:
                writer.print("UNKNOWN");
                break;
        }
        writer.append(" mService=");
        if (iInterface != null) {
            writer.append(zzfB()).append("@").println(Integer.toHexString(System.identityHashCode(iInterface.asBinder())));
        } else {
            writer.println("null");
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.zzpc) {
            z = this.zzadB == 3;
        }
        return z;
    }

    public boolean isConnecting() {
        boolean z;
        synchronized (this.zzpc) {
            z = this.zzadB == 2;
        }
        return z;
    }

    protected void onConnectionFailed(ConnectionResult result) {
    }

    protected void onConnectionSuspended(int cause) {
    }

    protected abstract T zzV(IBinder iBinder);

    protected Set<Scope> zza(Set<Scope> set) {
        return set;
    }

    protected void zza(int i, Bundle bundle, int i2) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, i2, -1, new zzi(this, i, bundle)));
    }

    protected void zza(int i, IBinder iBinder, Bundle bundle, int i2) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, i2, -1, new zzg(this, i, iBinder, bundle)));
    }

    public void zza(com.google.android.gms.common.api.GoogleApiClient.zza zza) {
        this.zzadx = (com.google.android.gms.common.api.GoogleApiClient.zza) zzx.zzb((Object) zza, (Object) "Connection progress callbacks cannot be null.");
        zzb(2, null);
    }

    public void zza(com.google.android.gms.common.api.GoogleApiClient.zza zza, ConnectionResult connectionResult) {
        this.zzadx = (com.google.android.gms.common.api.GoogleApiClient.zza) zzx.zzb((Object) zza, (Object) "Connection progress callbacks cannot be null.");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, this.zzadF.get(), connectionResult.getErrorCode(), connectionResult.getResolution()));
    }

    public void zza(zzp zzp) {
        try {
            this.zzadw.zza(new zzd(this, this.zzadF.get()), new ValidateAccountRequest(zzp, (Scope[]) this.zzZp.toArray(new Scope[this.zzZp.size()]), this.mContext.getPackageName(), zzoD()));
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "service died");
            zzbz(1);
        } catch (Throwable e2) {
            Log.w("GmsClient", "Remote exception occurred", e2);
        }
    }

    public void zza(zzp zzp, Set<Scope> set) {
        try {
            GetServiceRequest zzg = new GetServiceRequest(this.zzadE).zzck(this.mContext.getPackageName()).zzg(zzli());
            if (set != null) {
                zzg.zzd(set);
            }
            if (zzlm()) {
                zzg.zzb(zzoh()).zzc(zzp);
            } else if (zzoE()) {
                zzg.zzb(this.zzOY);
            }
            this.zzadw.zza(new zzd(this, this.zzadF.get()), zzg);
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "service died");
            zzbz(1);
        } catch (Throwable e2) {
            Log.w("GmsClient", "Remote exception occurred", e2);
        }
    }

    protected void zzbA(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6, i, -1, new zzh(this)));
    }

    public void zzbz(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, this.zzadF.get(), i));
    }

    protected void zzc(int i, T t) {
    }

    protected abstract String zzfA();

    protected abstract String zzfB();

    protected Bundle zzli() {
        return new Bundle();
    }

    public boolean zzlm() {
        return false;
    }

    public Bundle zzmw() {
        return null;
    }

    public boolean zznf() {
        return true;
    }

    protected final zzf zzoA() {
        return this.zzZH;
    }

    protected final void zzoB() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and wait for onConnected() to be called.");
        }
    }

    public final T zzoC() throws DeadObjectException {
        T t;
        synchronized (this.zzpc) {
            if (this.zzadB != 4) {
                zzoB();
                zzx.zza(this.zzady != null, "Client is connected but service is null");
                t = this.zzady;
            } else {
                throw new DeadObjectException();
            }
        }
        return t;
    }

    protected Bundle zzoD() {
        return null;
    }

    public boolean zzoE() {
        return false;
    }

    public final Account zzoh() {
        return this.zzOY == null ? new Account("<<default account>>", "com.google") : this.zzOY;
    }

    protected String zzou() {
        return "com.google.android.gms";
    }

    protected final String zzov() {
        return this.zzZH.zzon();
    }

    protected void zzow() {
    }
}
