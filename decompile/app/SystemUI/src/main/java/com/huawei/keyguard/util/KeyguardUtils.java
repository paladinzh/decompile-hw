package com.huawei.keyguard.util;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.ContextThemeWrapper;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import java.io.File;
import java.util.Random;

public class KeyguardUtils {
    private static final Random sRandom = new Random();

    public static Context getHwThemeContext(Context context) {
        if (context == null) {
            HwLog.e("KeyguardUtils", "get HwTheme with invalidContext.", new NullPointerException());
            return null;
        }
        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeID != 0) {
            context = new ContextThemeWrapper(context, themeID);
        }
        return context;
    }

    public static int getTargetReceiverSize(Context context, Intent intent) {
        int i = 0;
        try {
            ParceledListSlice slice = AppGlobals.getPackageManager().queryIntentReceivers(intent, null, 0, 0);
            if (!(slice == null || slice.getList() == null)) {
                i = slice.getList().size();
            }
            return i;
        } catch (RemoteException e) {
            HwLog.w("KeyguardUtils", "query intent receivers fail. " + intent);
            return -1;
        }
    }

    public static int getTargetActivitySize(Context context, Intent intent) {
        int i = 0;
        try {
            ParceledListSlice slice = AppGlobals.getPackageManager().queryIntentActivities(intent, null, 0, 0);
            if (!(slice == null || slice.getList() == null)) {
                i = slice.getList().size();
            }
            return i;
        } catch (RemoteException e) {
            HwLog.w("KeyguardUtils", "query intent receivers fail. " + intent);
            return -1;
        }
    }

    public static boolean isNeedKeyguard(Context context) {
        boolean coverTrust = true;
        if (OsUtils.getGlobalInt(context, "disable_pwdonly_by_trustagent", 0) != 1) {
            coverTrust = false;
        }
        boolean isSecure = (coverTrust || !isScreenOn(context)) ? false : HwUnlockUtils.isSecure(context);
        if (KeyguardCfg.isDoubleLockOn(context) && isSecure) {
            HwKeyguardUpdateMonitor.getInstance(context).setAlternateUnlockEnabled(false);
        }
        return isSecure;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm != null) {
            return pm.isScreenOn();
        }
        HwLog.w("KeyguardUtils", "PowerManager is null");
        return false;
    }

    public static boolean isSdcardMount() {
        String path = Environment.getExternalStorageDirectory().getPath();
        if (path == null) {
            HwLog.w("KeyguardUtils", "External storage path is null!");
            return false;
        }
        boolean isExist = new File(path).exists();
        if (!isExist) {
            HwLog.w("KeyguardUtils", "The sdcard is not mounted now!");
        }
        return isExist;
    }

    public static float nextFloat() {
        float nextFloat;
        synchronized (sRandom) {
            nextFloat = sRandom.nextFloat();
        }
        return nextFloat;
    }

    public static int nextInt(int max) {
        int nextInt;
        synchronized (sRandom) {
            nextInt = sRandom.nextInt(max);
        }
        return nextInt;
    }
}
