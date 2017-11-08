package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.preference.Preference;
import com.android.settings.UtilsCustEx;
import java.io.File;
import java.util.Locale;

public class HwCustStorageVolumePreferenceCategoryImpl extends HwCustStorageVolumePreferenceCategory {
    public static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";
    public static final String DMPROPERTY_SDCARD = "sd_card.disable";

    public void custStorageDisable(Preference sdCardPreference, Context context, String volumePath) {
        if (UtilsCustEx.IS_SPRINT && isSDCard(volumePath, context) && isSDCardRestricted()) {
            sdCardPreference.setEnabled(false);
        }
    }

    private boolean isSDCardRestricted() {
        if (new File("/data/OtaSave/Extensions/sd_card.disable").exists()) {
            return true;
        }
        return false;
    }

    public boolean isSDCard(String path, Context context) {
        for (StorageVolume volume : StorageManager.from(context).getVolumeList()) {
            if (isVolumeExternalSDcard(volume) && path != null && path.equalsIgnoreCase(volume.getPath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVolumeExternalSDcard(StorageVolume volume) {
        return (volume == null || isVolumeUsb(volume) || volume.isEmulated()) ? false : true;
    }

    public static boolean isVolumeUsb(StorageVolume storageVolume) {
        String USB_STORAGE = "usb";
        if (storageVolume == null || storageVolume.getPath() == null) {
            return false;
        }
        return storageVolume.getPath().toLowerCase(Locale.US).contains("usb");
    }
}
