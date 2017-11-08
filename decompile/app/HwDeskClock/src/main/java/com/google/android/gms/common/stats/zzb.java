package com.google.android.gms.common.stats;

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
import com.google.android.gms.internal.zzll;
import com.google.android.gms.internal.zzlw;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class zzb {
    private static final Object zzadU = new Object();
    private static final ComponentName zzafB = new ComponentName("com.google.android.gms", "com.google.android.gms.common.stats.GmsCoreStatsService");
    private static Integer zzafD;
    private static zzb zzafw;
    private final List<String> zzafA;
    private zze zzafC;
    private final List<String> zzafx;
    private final List<String> zzafy;
    private final List<String> zzafz;

    private zzb() {
        if (getLogLevel() != zzd.LOG_LEVEL_OFF) {
            String str = (String) zza.zzafG.get();
            this.zzafx = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzafH.get();
            this.zzafy = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzafI.get();
            this.zzafz = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            str = (String) zza.zzafJ.get();
            this.zzafA = str != null ? Arrays.asList(str.split(",")) : Collections.EMPTY_LIST;
            this.zzafC = new zze(1024, ((Long) zza.zzafK.get()).longValue());
            return;
        }
        this.zzafx = Collections.EMPTY_LIST;
        this.zzafy = Collections.EMPTY_LIST;
        this.zzafz = Collections.EMPTY_LIST;
        this.zzafA = Collections.EMPTY_LIST;
    }

    private static int getLogLevel() {
        if (zzafD == null) {
            try {
                zzafD = Integer.valueOf(!zzll.zzjk() ? zzd.LOG_LEVEL_OFF : ((Integer) zza.zzafF.get()).intValue());
            } catch (SecurityException e) {
                zzafD = Integer.valueOf(zzd.LOG_LEVEL_OFF);
            }
        }
        return zzafD.intValue();
    }

    private void zza(Context context, ServiceConnection serviceConnection, String str, Intent intent, int i) {
        if (zzd.zzacG) {
            String zzb = zzb(serviceConnection);
            if (zza(context, str, intent, zzb, i)) {
                Parcelable connectionEvent;
                long currentTimeMillis = System.currentTimeMillis();
                String str2 = null;
                if ((getLogLevel() & zzd.zzafO) != 0) {
                    str2 = zzlw.zzm(3, 5);
                }
                long j = 0;
                if ((getLogLevel() & zzd.zzafQ) != 0) {
                    j = Debug.getNativeHeapAllocatedSize();
                }
                if (i == 1 || i == 4) {
                    connectionEvent = new ConnectionEvent(currentTimeMillis, i, null, null, null, null, str2, zzb, SystemClock.elapsedRealtime(), j);
                } else {
                    ServiceInfo zzc = zzc(context, intent);
                    connectionEvent = new ConnectionEvent(currentTimeMillis, i, zzlw.zzap(context), str, zzc.processName, zzc.name, str2, zzb, SystemClock.elapsedRealtime(), j);
                }
                context.startService(new Intent().setComponent(zzafB).putExtra("com.google.android.gms.common.stats.EXTRA_LOG_EVENT", connectionEvent));
            }
        }
    }

    private boolean zza(Context context, String str, Intent intent, String str2, int i) {
        int logLevel = getLogLevel();
        if (logLevel == zzd.LOG_LEVEL_OFF || this.zzafC == null) {
            return false;
        }
        if (i == 4 || i == 1) {
            return this.zzafC.zzcz(str2);
        } else {
            ServiceInfo zzc = zzc(context, intent);
            if (zzc != null) {
                String zzap = zzlw.zzap(context);
                String str3 = zzc.processName;
                String str4 = zzc.name;
                if (!(this.zzafx.contains(zzap) || this.zzafy.contains(str) || this.zzafz.contains(str3) || this.zzafA.contains(str4))) {
                    if (str3.equals(zzap)) {
                        if ((logLevel & zzd.zzafP) == 0) {
                        }
                    }
                    this.zzafC.zzcy(str2);
                    return true;
                }
                return false;
            }
            Log.w("ConnectionTracker", String.format("Client %s made an invalid request %s", new Object[]{str, intent.toUri(0)}));
            return false;
        }
    }

    private String zzb(ServiceConnection serviceConnection) {
        return String.valueOf((Process.myPid() << 0) | System.identityHashCode(serviceConnection));
    }

    private boolean zzb(Context context, Intent intent) {
        ComponentName component = intent.getComponent();
        if (component != null) {
            if (zzd.zzacG) {
                if (!"com.google.android.gms".equals(component.getPackageName())) {
                }
            }
            return zzll.zzi(context, component.getPackageName());
        }
        return false;
    }

    private static ServiceInfo zzc(Context context, Intent intent) {
        List queryIntentServices = context.getPackageManager().queryIntentServices(intent, 128);
        if (queryIntentServices == null || queryIntentServices.size() == 0) {
            Log.w("ConnectionTracker", String.format("There are no handler of this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zzlw.zzm(3, 20)}));
            return null;
        }
        if (queryIntentServices.size() > 1) {
            Log.w("ConnectionTracker", String.format("Multiple handlers found for this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zzlw.zzm(3, 20)}));
            Iterator it = queryIntentServices.iterator();
            if (it.hasNext()) {
                Log.w("ConnectionTracker", ((ResolveInfo) it.next()).serviceInfo.name);
                return null;
            }
        }
        return ((ResolveInfo) queryIntentServices.get(0)).serviceInfo;
    }

    public static zzb zzpF() {
        synchronized (zzadU) {
            if (zzafw == null) {
                zzafw = new zzb();
            }
        }
        return zzafw;
    }

    public void zza(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
        zza(context, serviceConnection, null, null, 1);
    }

    public void zza(Context context, ServiceConnection serviceConnection, String str, Intent intent) {
        zza(context, serviceConnection, str, intent, 3);
    }

    public boolean zza(Context context, String str, Intent intent, ServiceConnection serviceConnection, int i) {
        if (zzb(context, intent)) {
            Log.w("ConnectionTracker", "Attempted to bind to a service in a STOPPED package.");
            return false;
        }
        boolean bindService = context.bindService(intent, serviceConnection, i);
        if (bindService) {
            zza(context, serviceConnection, str, intent, 2);
        }
        return bindService;
    }

    public void zzb(Context context, ServiceConnection serviceConnection) {
        zza(context, serviceConnection, null, null, 4);
    }
}
