package com.huawei.keyguard.monitor;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.huawei.keyguard.util.HwLog;

public class RadarUtil {
    static Context mContext;
    private static long sLastAutoUploadTime = 0;

    public static void uploadPlayMusicError(Context context, String userData) {
        uploadRadarScene(context, 100, 1002, userData);
    }

    public static void uploadUploadLockscreenFail(Context context, String userData) {
        uploadRadarScene(context, 100, 1003, userData);
    }

    public static void uploadLockscreenUnableAutochanged(Context context, String userData) {
        uploadRadarScene(context, 100, 1005, userData);
    }

    public static void uploadUploadLockscreenOOM(Context context, String userData) {
        uploadRadarScene(context, 100, 1006, userData);
    }

    public static void uploadLoadingAlbumImageOOM(Context context, String userData) {
        uploadRadarScene(context, 100, 1007, userData);
    }

    public static void uploadLoadingAlbumImageUnfittable(Context context, String userData) {
        uploadRadarScene(context, 100, 1008, userData);
    }

    public static void uploadsendPresentBroadcastException(Context context, String userData) {
        uploadRadarScene(context, 100, 1100, userData);
    }

    private static void uploadRadarScene(Context Context, int errorCode, int sceneType, String userData) {
        mContext = Context;
        autoUpload(errorCode, sceneType, userData);
    }

    public static void uploadUnlockScreenTypeMismatch(Context context, String userData) {
        uploadRadarScene(context, 100, 1009, userData);
    }

    private static String getApkVersion() {
        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (pi != null) {
                return pi.versionName;
            }
            return "4.0.0.0";
        } catch (Exception e) {
            return "4.0.0.0";
        }
    }

    private static synchronized void autoUpload(int bugType, int sceneType, String msg) {
        synchronized (RadarUtil.class) {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Package:").append("com.android.keyguard").append("\n");
            sb.append("APK version:").append(getApkVersion()).append("\n");
            sb.append("Bug type:").append(bugType).append("\n");
            sb.append("Scene def:").append(sceneType).append("\n");
            HwLog.i("KeyguardRadar", "autoUpload->bugType:" + bugType + "; sceneDef:" + "; msg:" + msg + ";");
            long currentTime = System.currentTimeMillis();
            if (currentTime - sLastAutoUploadTime < 60000) {
                HwLog.w("KeyguardRadar", "autoUpload->trigger auto upload frequently, return directly.");
                return;
            }
            sLastAutoUploadTime = currentTime;
            try {
                autoUpload("keyguard", 66, sb.toString(), msg);
            } catch (Exception ex) {
                HwLog.e("KeyguardRadar", "autoUpload->LogException.msg() ex:", ex);
            }
        }
    }

    private static void autoUpload(String appId, int level, String header, String msg) {
        try {
            HwLog.i("KeyguardRadar", "autoupload");
            Class<?> clazz = Class.forName("android.util.HwLogException");
            clazz.getMethod("msg", new Class[]{String.class, Integer.TYPE, String.class, String.class}).invoke(clazz.newInstance(), new Object[]{appId, Integer.valueOf(level), header, msg});
        } catch (ClassNotFoundException ex) {
            HwLog.e("KeyguardRadar", "autoUpload->HwLogException.msg() ClassNotFoundException, ex:", ex);
        } catch (NoSuchMethodException ex2) {
            HwLog.e("KeyguardRadar", "autoUpload->HwLogException.msg() NoSuchMethodException, ex:", ex2);
        } catch (Exception ex3) {
            HwLog.e("KeyguardRadar", "autoUpload->HwLogException.msg() Exception, ex:", ex3);
        }
    }
}
