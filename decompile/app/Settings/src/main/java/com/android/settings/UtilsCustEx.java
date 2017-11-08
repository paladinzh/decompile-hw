package com.android.settings;

import android.content.Context;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.IMountService.Stub;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

public class UtilsCustEx {
    public static final boolean IS_SPRINT = SystemProperties.getBoolean("ro.config.sprint_dm_ext", false);

    public static String getUmsStoragePath(Context context) {
        for (StorageVolume storageVolume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (storageVolume.isRemovable() && !storageVolume.getPath().contains("usb")) {
                return storageVolume.getPath();
            }
        }
        Log.e("UtilsCustEx", "couldn't getUmsStoragePath");
        return null;
    }

    public static boolean isUmsStorageMounted(Context context) {
        try {
            if ("mounted".equals(Stub.asInterface(ServiceManager.getService("mount")).getVolumeState(getUmsStoragePath(context)))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("UtilsCustEx", "couldn't talk to MountService", e);
            return false;
        }
    }

    public static String getApnDisplayTitle(Context context, String name) {
        String title = name;
        if (!name.startsWith("@") || !name.contains("/")) {
            return title;
        }
        String[] parts = name.substring(1).split("/");
        int resId = 0;
        if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0) {
            resId = context.getResources().getIdentifier(parts[1], parts[0], context.getPackageName());
        }
        if (resId != 0) {
            return context.getString(resId);
        }
        return title;
    }
}
