package com.google.android.gms.wearable.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.zzc.zzb;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.common.zzd;
import com.google.android.gms.wearable.CapabilityApi.CapabilityListener;
import com.google.android.gms.wearable.ChannelApi.ChannelListener;
import com.google.android.gms.wearable.DataApi.DataListener;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.NodeApi.NodeListener;
import com.google.android.gms.wearable.zzc.zza;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public class zzbn extends zzj<zzaw> {
    private final ExecutorService zzaRx = Executors.newCachedThreadPool();
    private final zzax<zza> zzbaX = new zzax();
    private final zzax<com.google.android.gms.wearable.zza.zza> zzbaY = new zzax();
    private final zzax<ChannelListener> zzbaZ = new zzax();
    private final zzax<DataListener> zzbba = new zzax();
    private final zzax<MessageListener> zzbbb = new zzax();
    private final zzax<NodeListener> zzbbc = new zzax();
    private final zzax<NodeApi.zza> zzbbd = new zzax();
    private final Map<String, zzax<CapabilityListener>> zzbbe = new HashMap();

    public zzbn(Context context, Looper looper, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, zzf zzf) {
        super(context, looper, 14, zzf, connectionCallbacks, onConnectionFailedListener);
    }

    public static Intent zzaT(Context context) {
        Intent intent = new Intent("com.google.android.wearable.app.cn.UPDATE_ANDROID_WEAR").setPackage("com.google.android.wearable.app.cn");
        if (context.getPackageManager().resolveActivity(intent, 65536) != null) {
            return intent;
        }
        return new Intent("android.intent.action.VIEW", Uri.parse("market://details").buildUpon().appendQueryParameter("id", "com.google.android.wearable.app.cn").build());
    }

    public void disconnect() {
        this.zzbaX.zzb(this);
        this.zzbaY.zzb(this);
        this.zzbba.zzb(this);
        this.zzbbb.zzb(this);
        this.zzbbc.zzb(this);
        this.zzbbd.zzb(this);
        synchronized (this.zzbbe) {
            for (zzax zzb : this.zzbbe.values()) {
                zzb.zzb(this);
            }
        }
        super.disconnect();
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzec(iBinder);
    }

    protected void zza(int i, IBinder iBinder, Bundle bundle, int i2) {
        if (Log.isLoggable("WearableClient", 2)) {
            Log.d("WearableClient", "onPostInitHandler: statusCode " + i);
        }
        if (i == 0) {
            this.zzbaX.zzeb(iBinder);
            this.zzbaY.zzeb(iBinder);
            this.zzbba.zzeb(iBinder);
            this.zzbbb.zzeb(iBinder);
            this.zzbbc.zzeb(iBinder);
            this.zzbbd.zzeb(iBinder);
            synchronized (this.zzbbe) {
                for (zzax zzeb : this.zzbbe.values()) {
                    zzeb.zzeb(iBinder);
                }
            }
        }
        super.zza(i, iBinder, bundle, i2);
    }

    public void zza(GoogleApiClient.zza zza) {
        int i = 7887000;
        if (!zznf()) {
            try {
                Bundle bundle = getContext().getPackageManager().getApplicationInfo("com.google.android.wearable.app.cn", 128).metaData;
                if (bundle != null) {
                    i = bundle.getInt("com.google.android.wearable.api.version", 7887000);
                }
                if (i < GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE) {
                    Log.w("WearableClient", "Android Wear out of date. Requires API version " + GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE + " but found " + i);
                    zza(zza, new ConnectionResult(6, PendingIntent.getActivity(getContext(), 0, zzaT(getContext()), 0)));
                    return;
                }
            } catch (NameNotFoundException e) {
                zza(zza, new ConnectionResult(16, null));
                return;
            }
        }
        super.zza(zza);
    }

    public void zza(zzb<SendMessageResult> zzb, String str, String str2, byte[] bArr) throws RemoteException {
        ((zzaw) zzoC()).zza(new zzbm$zzt(zzb), str, str2, bArr);
    }

    protected zzaw zzec(IBinder iBinder) {
        return zzaw.zza.zzea(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.wearable.BIND";
    }

    protected String zzfB() {
        return "com.google.android.gms.wearable.internal.IWearableService";
    }

    public boolean zznf() {
        return !zzd.zzmY().zzb(getContext().getPackageManager(), "com.google.android.wearable.app.cn");
    }

    protected String zzou() {
        return !zzd.zzmY().zzb(getContext().getPackageManager(), "com.google.android.wearable.app.cn") ? "com.google.android.gms" : "com.google.android.wearable.app.cn";
    }

    public void zzp(zzb<GetConnectedNodesResult> zzb) throws RemoteException {
        ((zzaw) zzoC()).zzd(new zzbm$zzj(zzb));
    }
}
