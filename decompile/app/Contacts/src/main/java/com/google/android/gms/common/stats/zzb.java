package com.google.android.gms.common.stats;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Debug;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import com.google.android.gms.common.internal.zzd;
import com.google.android.gms.common.stats.zzc.zza;
import com.google.android.gms.internal.zzmp;
import com.google.android.gms.internal.zznf;
import com.google.android.gms.location.places.Place;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class zzb {
    private static final Object zzalX = new Object();
    private static zzb zzanp;
    private static Integer zzanv;
    private final List<String> zzanq;
    private final List<String> zzanr;
    private final List<String> zzans;
    private final List<String> zzant;
    private zze zzanu;
    private zze zzanw;

    private zzb() {
        if (getLogLevel() != zzd.LOG_LEVEL_OFF) {
            String str = (String) zza.zzanA.get();
            this.zzanq = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzanB.get();
            this.zzanr = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzanC.get();
            this.zzans = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzanD.get();
            this.zzant = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            this.zzanu = new zze(Place.TYPE_SUBLOCALITY_LEVEL_2, ((Long) zza.zzanE.get()).longValue());
            this.zzanw = new zze(Place.TYPE_SUBLOCALITY_LEVEL_2, ((Long) zza.zzanE.get()).longValue());
            return;
        }
        this.zzanq = Collections.EMPTY_LIST;
        this.zzanr = Collections.EMPTY_LIST;
        this.zzans = Collections.EMPTY_LIST;
        this.zzant = Collections.EMPTY_LIST;
    }

    private static int getLogLevel() {
        if (zzanv == null) {
            try {
                zzanv = Integer.valueOf(!zzmp.zzkr() ? zzd.LOG_LEVEL_OFF : ((Integer) zza.zzanz.get()).intValue());
            } catch (SecurityException e) {
                zzanv = Integer.valueOf(zzd.LOG_LEVEL_OFF);
            }
        }
        return zzanv.intValue();
    }

    private void zza(Context context, String str, int i, String str2, String str3, String str4, String str5) {
        Parcelable connectionEvent;
        long currentTimeMillis = System.currentTimeMillis();
        String str6 = null;
        if (!((getLogLevel() & zzd.zzanJ) == 0 || i == 13)) {
            str6 = zznf.zzn(3, 5);
        }
        long j = 0;
        if ((getLogLevel() & zzd.zzanL) != 0) {
            j = Debug.getNativeHeapAllocatedSize();
        }
        if (i == 1 || i == 4 || i == 14) {
            connectionEvent = new ConnectionEvent(currentTimeMillis, i, null, null, null, null, str6, str, SystemClock.elapsedRealtime(), j);
        } else {
            connectionEvent = new ConnectionEvent(currentTimeMillis, i, str2, str3, str4, str5, str6, str, SystemClock.elapsedRealtime(), j);
        }
        context.startService(new Intent().setComponent(zzd.zzanF).putExtra("com.google.android.gms.common.stats.EXTRA_LOG_EVENT", connectionEvent));
    }

    private void zza(Context context, String str, String str2, Intent intent, int i) {
        String str3 = null;
        if (zzrQ() && this.zzanu != null) {
            String str4;
            String str5;
            if (i != 4 && i != 1) {
                ServiceInfo zzd = zzd(context, intent);
                if (zzd != null) {
                    str4 = zzd.processName;
                    str5 = zzd.name;
                    str3 = zznf.zzaz(context);
                    if (zzb(str3, str2, str4, str5)) {
                        this.zzanu.zzcS(str);
                    } else {
                        return;
                    }
                }
                Log.w("ConnectionTracker", String.format("Client %s made an invalid request %s", new Object[]{str2, intent.toUri(0)}));
                return;
            } else if (this.zzanu.zzcT(str)) {
                str5 = null;
                str4 = null;
            } else {
                return;
            }
            zza(context, str, i, str3, str2, str4, str5);
        }
    }

    private String zzb(ServiceConnection serviceConnection) {
        return String.valueOf((((long) Process.myPid()) << 32) | ((long) System.identityHashCode(serviceConnection)));
    }

    private boolean zzb(String str, String str2, String str3, String str4) {
        int logLevel = getLogLevel();
        if (!(this.zzanq.contains(str) || this.zzanr.contains(str2) || this.zzans.contains(str3) || this.zzant.contains(str4))) {
            if (str3.equals(str)) {
                if ((logLevel & zzd.zzanK) == 0) {
                }
            }
            return true;
        }
        return false;
    }

    private boolean zzc(Context context, Intent intent) {
        ComponentName component = intent.getComponent();
        if (component != null) {
            if (zzd.zzakE) {
                if (!"com.google.android.gms".equals(component.getPackageName())) {
                }
            }
            return zzmp.zzk(context, component.getPackageName());
        }
        return false;
    }

    private static ServiceInfo zzd(Context context, Intent intent) {
        List queryIntentServices = context.getPackageManager().queryIntentServices(intent, 128);
        if (queryIntentServices == null || queryIntentServices.size() == 0) {
            Log.w("ConnectionTracker", String.format("There are no handler of this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zznf.zzn(3, 20)}));
            return null;
        }
        if (queryIntentServices.size() > 1) {
            Log.w("ConnectionTracker", String.format("Multiple handlers found for this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zznf.zzn(3, 20)}));
            Iterator it = queryIntentServices.iterator();
            if (it.hasNext()) {
                Log.w("ConnectionTracker", ((ResolveInfo) it.next()).serviceInfo.name);
                return null;
            }
        }
        return ((ResolveInfo) queryIntentServices.get(0)).serviceInfo;
    }

    public static zzb zzrP() {
        synchronized (zzalX) {
            if (zzanp == null) {
                zzanp = new zzb();
            }
        }
        return zzanp;
    }

    private boolean zzrQ() {
        return zzd.zzakE && getLogLevel() != zzd.LOG_LEVEL_OFF;
    }

    @SuppressLint({"UntrackedBindService"})
    public void zza(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
        zza(context, zzb(serviceConnection), null, null, 1);
    }

    public void zza(Context context, ServiceConnection serviceConnection, String str, Intent intent) {
        zza(context, zzb(serviceConnection), str, intent, 3);
    }

    public boolean zza(Context context, Intent intent, ServiceConnection serviceConnection, int i) {
        return zza(context, context.getClass().getName(), intent, serviceConnection, i);
    }

    @SuppressLint({"UntrackedBindService"})
    public boolean zza(Context context, String str, Intent intent, ServiceConnection serviceConnection, int i) {
        if (zzc(context, intent)) {
            Log.w("ConnectionTracker", "Attempted to bind to a service in a STOPPED package.");
            return false;
        }
        boolean bindService = context.bindService(intent, serviceConnection, i);
        if (bindService) {
            zza(context, zzb(serviceConnection), str, intent, 2);
        }
        return bindService;
    }

    public void zzb(Context context, ServiceConnection serviceConnection) {
        zza(context, zzb(serviceConnection), null, null, 4);
    }
}
