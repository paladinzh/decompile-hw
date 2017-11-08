package com.avast.android.sdk.engine.obfuscated;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Process;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import java.util.List;

/* compiled from: Unknown */
public class au {
    private ActivityManager a;
    private Handler b;
    private int c = 0;

    public au(ActivityManager activityManager, Handler handler) {
        this.a = activityManager;
        this.b = handler;
    }

    public void a(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        context.startActivity(intent);
    }

    public void a(String str) {
        b(str);
        this.c++;
        ao.a("Killed: " + str + SqlMarker.COMMA_SEPARATE + this.c + " times.");
        if (this.c < 5) {
            ao.a("Killing in 1 second: " + str);
            if (this.b != null) {
                this.b.postDelayed(new av(this, str), 400);
            }
        }
    }

    @SuppressLint({"NewApi"})
    public void b(String str) {
        try {
            List<RunningAppProcessInfo> runningAppProcesses = this.a.getRunningAppProcesses();
            if (runningAppProcesses != null) {
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (runningAppProcessInfo.pkgList != null) {
                        for (Object equals : runningAppProcessInfo.pkgList) {
                            if (str.equals(equals)) {
                                int i = runningAppProcessInfo.pid;
                                Process.sendSignal(i, 9);
                                Process.killProcess(i);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        if (VERSION.SDK_INT >= 8) {
            this.a.killBackgroundProcesses(str);
        }
    }
}
