package com.google.android.gms.wearable.internal;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.wearable.internal.zzaw.zza;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
final class zzax<T> {
    private final Map<T, zzbo<T>> zzaov = new HashMap();

    zzax() {
    }

    public void zzb(zzbn zzbn) {
        synchronized (this.zzaov) {
            zzau zzbm_zzo = new zzbm$zzo();
            for (Entry entry : this.zzaov.entrySet()) {
                zzbo zzbo = (zzbo) entry.getValue();
                if (zzbo != null) {
                    zzbo.clear();
                    if (zzbn.isConnected()) {
                        try {
                            ((zzaw) zzbn.zzoC()).zza(zzbm_zzo, new RemoveListenerRequest(zzbo));
                            if (Log.isLoggable("WearableClient", 2)) {
                                Log.d("WearableClient", "disconnect: removed: " + entry.getKey() + "/" + zzbo);
                            }
                        } catch (RemoteException e) {
                            Log.w("WearableClient", "disconnect: Didn't remove: " + entry.getKey() + "/" + zzbo);
                        }
                    } else {
                        continue;
                    }
                }
            }
            this.zzaov.clear();
        }
    }

    public void zzeb(IBinder iBinder) {
        synchronized (this.zzaov) {
            zzaw zzea = zza.zzea(iBinder);
            zzau zzbm_zzo = new zzbm$zzo();
            for (Entry entry : this.zzaov.entrySet()) {
                zzbo zzbo = (zzbo) entry.getValue();
                try {
                    zzea.zza(zzbm_zzo, new AddListenerRequest(zzbo));
                    if (Log.isLoggable("WearableClient", 2)) {
                        Log.d("WearableClient", "onPostInitHandler: added: " + entry.getKey() + "/" + zzbo);
                    }
                } catch (RemoteException e) {
                    Log.d("WearableClient", "onPostInitHandler: Didn't add: " + entry.getKey() + "/" + zzbo);
                }
            }
        }
    }
}
