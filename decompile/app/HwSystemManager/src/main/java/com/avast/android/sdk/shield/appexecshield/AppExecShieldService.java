package com.avast.android.sdk.shield.appexecshield;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.UsageStatsPermissionHelper;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.at;
import com.avast.android.sdk.engine.obfuscated.au;
import com.huawei.permissionmanager.db.DBHelper;
import java.io.File;
import java.util.List;

/* compiled from: Unknown */
public abstract class AppExecShieldService extends Service {
    private Handler a;
    private c b;
    private b c;
    private com.avast.android.sdk.internal.e<at> d;
    private Handler e;

    /* compiled from: Unknown */
    private abstract class c implements Runnable {
        private ActivityManager a;
        final /* synthetic */ AppExecShieldService b;
        private Context c;

        public c(AppExecShieldService appExecShieldService, Context context) {
            this.b = appExecShieldService;
            this.a = (ActivityManager) context.getSystemService("activity");
            this.c = context;
        }

        public abstract void a(Context context, ActivityManager activityManager);

        public final void run() {
            try {
                a(this.c, this.a);
            } catch (Throwable e) {
                ao.b("Abstract method call threw an exception", e);
            }
            this.b.a.postDelayed(this, 200);
        }
    }

    /* compiled from: Unknown */
    private class a extends c {
        final /* synthetic */ AppExecShieldService a;
        private String c = "";
        private final UsageStatsManager d;

        public a(AppExecShieldService appExecShieldService, Context context) {
            this.a = appExecShieldService;
            super(appExecShieldService, context);
            this.d = (UsageStatsManager) appExecShieldService.getApplicationContext().getSystemService("usagestats");
        }

        @TargetApi(21)
        public void a(Context context, ActivityManager activityManager) {
            String str = null;
            long currentTimeMillis = System.currentTimeMillis();
            long j = currentTimeMillis - DBHelper.HISTORY_MAX_SIZE;
            if (!UsageStatsPermissionHelper.hasPermission(context)) {
                this.a.onUsageStatsPermissionDisabled();
                this.a.stopSelf();
            }
            UsageEvents queryEvents = this.d.queryEvents(j, currentTimeMillis);
            Event event = new Event();
            while (queryEvents.hasNextEvent()) {
                queryEvents.getNextEvent(event);
                if (event.getEventType() == 1) {
                    str = event.getPackageName();
                }
            }
            if (!(str == null || this.c.equals(str))) {
                this.c = str;
                at a = at.a(str, true);
                if (a != null) {
                    this.a.a(a);
                }
            }
        }
    }

    /* compiled from: Unknown */
    private final class b extends Thread {
        final /* synthetic */ AppExecShieldService a;
        private Context b;
        private au c;

        public b(AppExecShieldService appExecShieldService, Context context, Handler handler) {
            this.a = appExecShieldService;
            this.b = context;
            this.c = new au((ActivityManager) context.getSystemService("activity"), handler);
        }

        public void run() {
            super.run();
            while (true) {
                try {
                    at atVar = (at) this.a.d.a();
                    try {
                        PackageInfo packageInfo = this.b.getPackageManager().getPackageInfo(atVar.a(), 0);
                        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                        String str = packageInfo.packageName;
                        ao.a("Scanning: " + packageInfo.packageName);
                        this.a.onPreAppScan(str);
                        List scan = EngineInterface.scan(this.b, null, new File(applicationInfo.sourceDir), packageInfo, 36);
                        ao.a("Result:" + ((ScanResultStructure) scan.get(0)).result.toString());
                        if (this.a.onAppScanResult(this.b, str, scan)) {
                            this.c.a(this.b);
                            this.c.a(str);
                        }
                    } catch (Throwable e) {
                        ao.a("Package not found: " + atVar.a(), e);
                    }
                } catch (InterruptedException e2) {
                    return;
                }
            }
        }
    }

    /* compiled from: Unknown */
    private class d extends c {
        final /* synthetic */ AppExecShieldService a;
        private String c = "";

        public d(AppExecShieldService appExecShieldService, Context context) {
            this.a = appExecShieldService;
            super(appExecShieldService, context);
        }

        public void a(Context context, ActivityManager activityManager) {
            String str;
            at a;
            List runningAppProcesses = activityManager.getRunningAppProcesses();
            if (!(runningAppProcesses == null || runningAppProcesses.isEmpty())) {
                RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) runningAppProcesses.get(0);
                if (runningAppProcessInfo.pkgList != null && runningAppProcessInfo.pkgList.length > 0) {
                    str = runningAppProcessInfo.pkgList[0];
                    if (!(str == null || this.c.equals(str))) {
                        this.c = str;
                        a = at.a(str, true);
                        if (a != null) {
                            this.a.a(a);
                        }
                    }
                }
            }
            str = null;
            this.c = str;
            a = at.a(str, true);
            if (a != null) {
                this.a.a(a);
            }
        }
    }

    /* compiled from: Unknown */
    private class e extends c {
        final /* synthetic */ AppExecShieldService a;
        private String c = "";

        public e(AppExecShieldService appExecShieldService, Context context) {
            this.a = appExecShieldService;
            super(appExecShieldService, context);
        }

        public void a(Context context, ActivityManager activityManager) {
            RunningTaskInfo runningTaskInfo = (RunningTaskInfo) activityManager.getRunningTasks(1).get(0);
            if (!this.c.equals(runningTaskInfo.topActivity.getPackageName())) {
                at a;
                String packageName = runningTaskInfo.topActivity.getPackageName();
                this.c = packageName;
                String replace = runningTaskInfo.baseActivity.getClassName().replace(packageName, "");
                at a2 = at.a(packageName, true);
                a2.a(replace, null);
                packageName = runningTaskInfo.topActivity.getClassName().replace(packageName, "");
                if (!runningTaskInfo.topActivity.getPackageName().equals(runningTaskInfo.baseActivity.getPackageName())) {
                    a2.a(false);
                }
                if (".UninstallerActivity".equals(packageName)) {
                    a = at.a("com.android.packageinstaller", false);
                    a.a(false);
                    a.a(".UninstallerActivity", "android.intent.action.DELETE");
                } else {
                    a = a2;
                }
                if (".AppWidgetPickActivity".equals(packageName)) {
                    a.a(packageName, null);
                }
                if ("com.google.android.finsky.billing.lightpurchase.IabV2Activity".equals(packageName) || "com.google.android.finsky.billing.lightpurchase.IabV3Activity".equals(packageName)) {
                    a.a(false);
                }
                this.a.a(a);
            }
        }
    }

    private void a(at atVar) {
        if (atVar.b() != null && atVar.b().contains(".AppWidgetPickActivity")) {
            ao.a("Skip blocking adding app widget.");
            return;
        }
        if (onAppStartDetected(atVar.a())) {
            ao.a("Realtime shield enabled, starting scan...");
            if (!this.d.offerFirst(atVar)) {
                this.d.removeLast();
                this.d.offerFirst(atVar);
            }
        }
    }

    public abstract boolean onAppScanResult(Context context, String str, List<ScanResultStructure> list);

    public abstract boolean onAppStartDetected(String str);

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        c dVar;
        int i = 0;
        super.onCreate();
        this.d = new com.avast.android.sdk.internal.e();
        this.e = new Handler();
        this.c = new b(this, this, this.e);
        this.c.setPriority(1);
        this.c.start();
        if (VERSION.SDK_INT < 21) {
            if (getPackageManager().checkPermission("android.permission.GET_TASKS", getPackageName()) == 0) {
                i = 1;
            }
            dVar = i == 0 ? new d(this, this) : new e(this, this);
        } else {
            dVar = !UsageStatsPermissionHelper.hasUsageStatsStrippedOut(this) ? new a(this, this) : new d(this, this);
        }
        this.b = dVar;
    }

    public void onDestroy() {
        if (!(this.a == null || this.b == null)) {
            this.a.removeCallbacks(this.b);
        }
        if (this.c != null) {
            this.c.interrupt();
            this.c = null;
        }
        super.onDestroy();
    }

    public abstract void onPreAppScan(String str);

    public int onStartCommand(Intent intent, int i, int i2) {
        if (!(this.a == null || this.b == null)) {
            this.a.removeCallbacks(this.b);
        }
        if (this.a == null) {
            this.a = new Handler();
        }
        this.a.post(this.b);
        return 1;
    }

    public abstract void onUsageStatsPermissionDisabled();
}
