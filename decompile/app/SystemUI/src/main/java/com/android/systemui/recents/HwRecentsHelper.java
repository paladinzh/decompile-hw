package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hsm.MediaTransactWrapper;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.recents.model.Task;
import com.android.systemui.utils.HwLog;
import com.huawei.android.util.SystemInfo;
import fyusion.vislib.BuildConfig;
import java.util.Set;

public class HwRecentsHelper {
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static boolean isAllTaskRemovingAll;
    private static Set<Integer> musiclist = null;

    public static boolean getAllTaskRemovingAllFlag() {
        return isAllTaskRemovingAll;
    }

    public static void setAllTaskRemovingAllFlag(boolean isRemoveAll) {
        HwLog.i("HwRecentsHelper", "setAllTaskRemovingAllFlag to " + isRemoveAll);
        isAllTaskRemovingAll = isRemoveAll;
    }

    public static void setTotalMemorySize(Context context) {
        SharedPreferences mShared = context.getSharedPreferences("memoryInfos", 0);
        String deviceRam = SystemInfo.getDeviceRam();
        try {
            mShared.edit().putLong("TotalMemorySize", Long.parseLong(deviceRam)).commit();
        } catch (NumberFormatException e) {
            Log.d("HwRecentsHelper", "Can't  get MemorySize from " + deviceRam + " ,mInitial_memory will be 0");
        } catch (Exception e2) {
            Log.e("HwRecentsHelper", "Can't  get MemorySize", e2);
        }
    }

    public static long getTotalMemorySize(Context context) {
        return context.getSharedPreferences("memoryInfos", 0).getLong("TotalMemorySize", 675052);
    }

    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }

    public static void saveLastMemoryInfo(Context context, String memInfoText) {
        context.getSharedPreferences("memoryInfos", 0).edit().putString("OldMemInfoText", memInfoText).commit();
    }

    public static String getLastMemoryInfo(Context context) {
        return context.getSharedPreferences("memoryInfos", 0).getString("OldMemInfoText", BuildConfig.FLAVOR);
    }

    public static void refreshPlayingMusicUidSet() {
        musiclist = MediaTransactWrapper.playingMusicUidSet();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean getPlayingMusicUid(Context context, Task task) {
        if (context == null || task == null || musiclist == null || musiclist.isEmpty()) {
            return false;
        }
        try {
            if (!musiclist.contains(Integer.valueOf(context.getPackageManager().getPackageUid(task.packageName, task.key.userId)))) {
                return false;
            }
            Log.d("HwRecentsHelper", "PlayingMusic is " + task.packageName);
            return true;
        } catch (NameNotFoundException e) {
            Log.d("HwRecentsHelper", "Can not get packageUid return.");
            return false;
        }
    }
}
