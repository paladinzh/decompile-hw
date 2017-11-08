package com.google.android.gms.ads.identifier;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.stats.zzb;
import com.google.android.gms.common.zzc;
import com.google.android.gms.internal.zzat;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class AdvertisingIdClient {
    private final Context mContext;
    com.google.android.gms.common.zza zzoS;
    zzat zzoT;
    boolean zzoU;
    Object zzoV;
    zza zzoW;
    final long zzoX;

    /* compiled from: Unknown */
    public static final class Info {
        private final String zzpc;
        private final boolean zzpd;

        public Info(String advertisingId, boolean limitAdTrackingEnabled) {
            this.zzpc = advertisingId;
            this.zzpd = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.zzpc;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.zzpd;
        }

        public String toString() {
            return "{" + this.zzpc + "}" + this.zzpd;
        }
    }

    /* compiled from: Unknown */
    static class zza extends Thread {
        private WeakReference<AdvertisingIdClient> zzoY;
        private long zzoZ;
        CountDownLatch zzpa = new CountDownLatch(1);
        boolean zzpb = false;

        public zza(AdvertisingIdClient advertisingIdClient, long j) {
            this.zzoY = new WeakReference(advertisingIdClient);
            this.zzoZ = j;
            start();
        }

        private void disconnect() {
            AdvertisingIdClient advertisingIdClient = (AdvertisingIdClient) this.zzoY.get();
            if (advertisingIdClient != null) {
                advertisingIdClient.finish();
                this.zzpb = true;
            }
        }

        public void cancel() {
            this.zzpa.countDown();
        }

        public void run() {
            try {
                if (!this.zzpa.await(this.zzoZ, TimeUnit.MILLISECONDS)) {
                    disconnect();
                }
            } catch (InterruptedException e) {
                disconnect();
            }
        }

        public boolean zzaK() {
            return this.zzpb;
        }
    }

    public AdvertisingIdClient(Context context) {
        this(context, 30000);
    }

    public AdvertisingIdClient(Context context, long timeoutInMillis) {
        this.zzoV = new Object();
        zzx.zzz(context);
        this.mContext = context;
        this.zzoU = false;
        this.zzoX = timeoutInMillis;
    }

    public static Info getAdvertisingIdInfo(Context context) throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        AdvertisingIdClient advertisingIdClient = new AdvertisingIdClient(context, -1);
        try {
            advertisingIdClient.zzb(false);
            Info info = advertisingIdClient.getInfo();
            return info;
        } finally {
            advertisingIdClient.finish();
        }
    }

    public static void setShouldSkipGmsCoreVersionCheck(boolean shouldSkipGmsCoreVersionCheck) {
    }

    static zzat zza(Context context, com.google.android.gms.common.zza zza) throws IOException {
        try {
            return com.google.android.gms.internal.zzat.zza.zzb(zza.zzoJ());
        } catch (InterruptedException e) {
            throw new IOException("Interrupted exception");
        } catch (Throwable th) {
            IOException iOException = new IOException(th);
        }
    }

    private void zzaJ() {
        synchronized (this.zzoV) {
            if (this.zzoW != null) {
                this.zzoW.cancel();
                try {
                    this.zzoW.join();
                } catch (InterruptedException e) {
                }
            }
            if ((this.zzoX <= 0 ? 1 : null) == null) {
                this.zzoW = new zza(this, this.zzoX);
            }
        }
    }

    static com.google.android.gms.common.zza zzp(Context context) throws IOException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        try {
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            switch (zzc.zzoK().isGooglePlayServicesAvailable(context)) {
                case 0:
                case 2:
                    ServiceConnection zza = new com.google.android.gms.common.zza();
                    Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
                    intent.setPackage("com.google.android.gms");
                    try {
                        if (zzb.zzrP().zza(context, intent, zza, 1)) {
                            return zza;
                        }
                        throw new IOException("Connection failure");
                    } catch (Throwable th) {
                        IOException iOException = new IOException(th);
                    }
                default:
                    throw new IOException("Google Play services not available");
            }
        } catch (NameNotFoundException e) {
            throw new GooglePlayServicesNotAvailableException(9);
        }
    }

    protected void finalize() throws Throwable {
        finish();
        super.finalize();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void finish() {
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        synchronized (this) {
            if (!(this.mContext == null || this.zzoS == null)) {
                try {
                    if (this.zzoU) {
                        zzb.zzrP().zza(this.mContext, this.zzoS);
                    }
                } catch (Throwable e) {
                    Log.i("AdvertisingIdClient", "AdvertisingIdClient unbindService failed.", e);
                }
                this.zzoU = false;
                this.zzoT = null;
                this.zzoS = null;
            }
        }
    }

    public Info getInfo() throws IOException {
        Info info;
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        synchronized (this) {
            if (!this.zzoU) {
                synchronized (this.zzoV) {
                    if (this.zzoW != null) {
                        if (this.zzoW.zzaK()) {
                        }
                    }
                    throw new IOException("AdvertisingIdClient is not connected.");
                }
                try {
                    zzb(false);
                    if (!this.zzoU) {
                        throw new IOException("AdvertisingIdClient cannot reconnect.");
                    }
                } catch (Throwable e) {
                    Log.i("AdvertisingIdClient", "GMS remote exception ", e);
                    throw new IOException("Remote exception");
                } catch (Throwable e2) {
                    throw new IOException("AdvertisingIdClient cannot reconnect.", e2);
                }
            }
            zzx.zzz(this.zzoS);
            zzx.zzz(this.zzoT);
            info = new Info(this.zzoT.getId(), this.zzoT.zzc(true));
        }
        zzaJ();
        return info;
    }

    public void start() throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        zzb(true);
    }

    protected void zzb(boolean z) throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        zzx.zzcE("Calling this from your main thread can lead to deadlock");
        synchronized (this) {
            if (this.zzoU) {
                finish();
            }
            this.zzoS = zzp(this.mContext);
            this.zzoT = zza(this.mContext, this.zzoS);
            this.zzoU = true;
            if (z) {
                zzaJ();
            }
        }
    }
}
