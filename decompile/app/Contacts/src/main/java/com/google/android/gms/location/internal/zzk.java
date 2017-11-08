package com.google.android.gms.location.internal;

import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.zzd;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class zzk {
    private final Context mContext;
    private ContentProviderClient zzaOG = null;
    private boolean zzaOH = false;
    private Map<LocationCallback, zza> zzaOI = new HashMap();
    private final zzp<zzi> zzaOt;
    private Map<LocationListener, zzc> zzaxd = new HashMap();

    /* compiled from: Unknown */
    private static class zza extends com.google.android.gms.location.zzc.zza {
        private Handler zzaOJ;

        zza(final LocationCallback locationCallback, Looper looper) {
            if (looper == null) {
                looper = Looper.myLooper();
                zzx.zza(looper != null, (Object) "Can't create handler inside thread that has not called Looper.prepare()");
            }
            this.zzaOJ = new Handler(this, looper) {
                final /* synthetic */ zza zzaOK;

                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            locationCallback.onLocationResult((LocationResult) msg.obj);
                            return;
                        case 1:
                            locationCallback.onLocationAvailability((LocationAvailability) msg.obj);
                            return;
                        default:
                            return;
                    }
                }
            };
        }

        private void zzb(int i, Object obj) {
            if (this.zzaOJ != null) {
                Message obtain = Message.obtain();
                obtain.what = i;
                obtain.obj = obj;
                this.zzaOJ.sendMessage(obtain);
                return;
            }
            Log.e("LocationClientHelper", "Received a data in client after calling removeLocationUpdates.");
        }

        public void onLocationAvailability(LocationAvailability state) {
            zzb(1, state);
        }

        public void onLocationResult(LocationResult locationResult) {
            zzb(0, locationResult);
        }

        public void release() {
            this.zzaOJ = null;
        }
    }

    /* compiled from: Unknown */
    private static class zzb extends Handler {
        private final LocationListener zzaOL;

        public zzb(LocationListener locationListener) {
            this.zzaOL = locationListener;
        }

        public zzb(LocationListener locationListener, Looper looper) {
            super(looper);
            this.zzaOL = locationListener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.zzaOL.onLocationChanged(new Location((Location) msg.obj));
                    return;
                default:
                    Log.e("LocationClientHelper", "unknown message in LocationHandler.handleMessage");
                    return;
            }
        }
    }

    /* compiled from: Unknown */
    private static class zzc extends com.google.android.gms.location.zzd.zza {
        private Handler zzaOJ;

        zzc(LocationListener locationListener, Looper looper) {
            if (looper == null) {
                zzx.zza(Looper.myLooper() != null, (Object) "Can't create handler inside thread that has not called Looper.prepare()");
            }
            this.zzaOJ = looper != null ? new zzb(locationListener, looper) : new zzb(locationListener);
        }

        public void onLocationChanged(Location location) {
            if (this.zzaOJ != null) {
                Message obtain = Message.obtain();
                obtain.what = 1;
                obtain.obj = location;
                this.zzaOJ.sendMessage(obtain);
                return;
            }
            Log.e("LocationClientHelper", "Received a location in client after calling removeLocationUpdates.");
        }

        public void release() {
            this.zzaOJ = null;
        }
    }

    public zzk(Context context, zzp<zzi> zzp) {
        this.mContext = context;
        this.zzaOt = zzp;
    }

    private zza zza(LocationCallback locationCallback, Looper looper) {
        zza zza;
        synchronized (this.zzaOI) {
            zza = (zza) this.zzaOI.get(locationCallback);
            if (zza == null) {
                zza = new zza(locationCallback, looper);
            }
            this.zzaOI.put(locationCallback, zza);
        }
        return zza;
    }

    private zzc zza(LocationListener locationListener, Looper looper) {
        zzc zzc;
        synchronized (this.zzaxd) {
            zzc = (zzc) this.zzaxd.get(locationListener);
            if (zzc == null) {
                zzc = new zzc(locationListener, looper);
            }
            this.zzaxd.put(locationListener, zzc);
        }
        return zzc;
    }

    public Location getLastLocation() {
        this.zzaOt.zzqI();
        try {
            return ((zzi) this.zzaOt.zzqJ()).zzei(this.mContext.getPackageName());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeAllListeners() {
        try {
            synchronized (this.zzaxd) {
                for (zzd zzd : this.zzaxd.values()) {
                    if (zzd != null) {
                        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(zzd, null));
                    }
                }
                this.zzaxd.clear();
            }
            synchronized (this.zzaOI) {
                for (com.google.android.gms.location.zzc zzc : this.zzaOI.values()) {
                    if (zzc != null) {
                        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(zzc, null));
                    }
                }
                this.zzaOI.clear();
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void zza(PendingIntent pendingIntent, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zzb(pendingIntent, zzg));
    }

    public void zza(LocationCallback locationCallback, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        zzx.zzb((Object) locationCallback, (Object) "Invalid null callback");
        synchronized (this.zzaOI) {
            com.google.android.gms.location.zzc zzc = (zza) this.zzaOI.remove(locationCallback);
            if (zzc != null) {
                zzc.release();
                ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(zzc, zzg));
            }
        }
    }

    public void zza(LocationListener locationListener, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        zzx.zzb((Object) locationListener, (Object) "Invalid null listener");
        synchronized (this.zzaxd) {
            zzd zzd = (zzc) this.zzaxd.remove(locationListener);
            if (this.zzaOG != null && this.zzaxd.isEmpty()) {
                this.zzaOG.release();
                this.zzaOG = null;
            }
            if (zzd != null) {
                zzd.release();
                ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(zzd, zzg));
            }
        }
    }

    public void zza(LocationRequest locationRequest, PendingIntent pendingIntent, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(LocationRequestInternal.zzb(locationRequest), pendingIntent, zzg));
    }

    public void zza(LocationRequest locationRequest, LocationListener locationListener, Looper looper, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(LocationRequestInternal.zzb(locationRequest), zza(locationListener, looper), zzg));
    }

    public void zza(LocationRequestInternal locationRequestInternal, LocationCallback locationCallback, Looper looper, zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zza(LocationRequestUpdateData.zza(locationRequestInternal, zza(locationCallback, looper), zzg));
    }

    public void zza(zzg zzg) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zza(zzg);
    }

    public void zzam(boolean z) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zzam(z);
        this.zzaOH = z;
    }

    public void zzc(Location location) throws RemoteException {
        this.zzaOt.zzqI();
        ((zzi) this.zzaOt.zzqJ()).zzc(location);
    }

    public LocationAvailability zzyO() {
        this.zzaOt.zzqI();
        try {
            return ((zzi) this.zzaOt.zzqJ()).zzej(this.mContext.getPackageName());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void zzyP() {
        if (this.zzaOH) {
            try {
                zzam(false);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
