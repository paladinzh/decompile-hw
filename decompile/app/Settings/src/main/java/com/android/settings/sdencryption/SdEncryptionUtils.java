package com.android.settings.sdencryption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.view.LayoutInflater;
import android.view.View;
import com.android.settings.ShowAdminSupportDetailsDialog;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.android.app.admin.DevicePolicyManagerEx;
import com.huawei.android.os.HwSdCardCryptdEx;
import java.util.Calendar;
import java.util.List;

public class SdEncryptionUtils {
    private static String TAG = "SdEncryptionUtils";

    public static String getExternalSDcardVolId(Context context) {
        StorageVolume vol = getSdVolume(context);
        if (vol == null) {
            return null;
        }
        return vol.getId();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVolumeExternalSDcard(Context context, StorageVolume storageVolume) {
        if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (storageVolume.getUuid() == null) {
            return false;
        }
        VolumeInfo volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid());
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isSd();
        }
        return false;
    }

    public static StorageVolume getSdVolume(Context context) {
        for (StorageVolume storageVolume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (isVolumeExternalSDcard(context, storageVolume)) {
                return storageVolume;
            }
        }
        return null;
    }

    public static String getSdCryptionState(Context context) {
        StorageVolume vol = getSdVolume(context);
        if (vol == null) {
            SdLog.e(TAG, "the storageVolume to  get the sd card state is null");
            return "no_card";
        }
        String sdState = vol.getState();
        SdLog.i(TAG, "the volume id is " + vol.getId() + ", the sd card state is " + sdState);
        return adjustSdCardCryptionState(sdState);
    }

    public static String getSimpleCryptState(String state) {
        if (state.equals("disable") || state.equals("encrypting") || state.equals("half_encrypted")) {
            return "Encrypt";
        }
        if (state.equals("enable") || state.equals("decrypting") || state.equals("half_decrypted")) {
            return "Decrypt";
        }
        return "";
    }

    public static void startCryption(final boolean enable, Context context) {
        final String volID = getExternalSDcardVolId(context);
        new Thread(new Runnable() {
            public void run() {
                long beginTime = Calendar.getInstance().getTimeInMillis();
                SdLog.i(SdEncryptionUtils.TAG, "Start cryption " + enable);
                HwSdCardCryptdEx.setSdCardCryptdEnable(enable, volID);
                SdLog.i(SdEncryptionUtils.TAG, "Finish cryption " + enable + " takes " + (Calendar.getInstance().getTimeInMillis() - beginTime));
            }
        }).start();
    }

    public static void sendStateBroadcast(Context context, int statusCode, boolean enable) {
        Intent intent = new Intent("com.huawei.android.HWSDCRYPTD_NOTIFICATION");
        intent.putExtra("status", statusCode);
        intent.putExtra("enable", enable);
        context.sendBroadcast(intent, "com.huawei.hwSdCryptd.permission.RECV_HWSDCRYPTD_RESULT");
        SdLog.i(TAG, "Send broadcast. Code = " + statusCode);
    }

    public static int getCryptPercent() {
        int percent = SystemProperties.getInt("vold.cryptsd_progress", 0);
        if (percent != 100) {
            return percent;
        }
        String cryptState = getCryptState();
        if ("encrypting".equals(cryptState) || "decrypting".equals(cryptState)) {
            return percent - 1;
        }
        return percent;
    }

    public static String getCryptState() {
        return SystemProperties.get("vold.cryptsd.state");
    }

    public static AlertDialog getDisallowDialog(Activity context) {
        EnforcedAdmin admin = checkIfDecryptSdDisallowed(context);
        if (admin == null) {
            return null;
        }
        LayoutInflater inflater = context.getLayoutInflater();
        Builder builder = new Builder(context);
        View dialogView = inflater.inflate(2130968617, null);
        ShowAdminSupportDetailsDialog.setAdminSupportDetails(context, dialogView, admin, false);
        dialogView.setVisibility(0);
        builder.setPositiveButton(2131624573, null).setView(dialogView).setTitle(2131627106);
        return builder.create();
    }

    public static EnforcedAdmin checkIfDecryptSdDisallowed(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        int userId = UserHandle.myUserId();
        EnforcedAdmin enforcedAdmin = null;
        List<ComponentName> admins = dpm.getActiveAdminsAsUser(userId);
        if (admins == null) {
            return null;
        }
        for (ComponentName admin : admins) {
            if (DevicePolicyManagerEx.isSDCardDecryptionDisabled(admin)) {
                if (enforcedAdmin != null) {
                    return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
                }
                enforcedAdmin = new EnforcedAdmin(admin, userId);
            }
        }
        return enforcedAdmin;
    }

    public static String getSdCryptionState(VolumeInfo vol) {
        if (vol == null) {
            SdLog.e(TAG, "the volumeInfo to  get the sd card state is null");
            return "no_card";
        }
        DiskInfo disk = vol.getDisk();
        if (disk == null || !disk.isSd()) {
            return "no_card";
        }
        String sdState = VolumeInfo.getEnvironmentForState(vol.getState());
        SdLog.i(TAG, "the volume id is " + vol.getId() + ", the sd card state is = " + sdState);
        return adjustSdCardCryptionState(sdState);
    }

    private static String adjustSdCardCryptionState(String sdState) {
        if (sdState == null) {
            SdLog.i(TAG, "the Sd card state is null.");
            return "no_card";
        }
        String cryptionState = getCryptState();
        SdLog.i(TAG, "the property of cryption is " + cryptionState);
        if (cryptionState == null) {
            return "no_card";
        }
        if ("mismatch".equals(cryptionState)) {
            cryptionState = "disable";
        }
        if ("mounted".equals(sdState)) {
            if ("encrypting".equals(cryptionState)) {
                cryptionState = "half_encrypted";
            } else if ("decrypting".equals(cryptionState)) {
                cryptionState = "half_decrypted";
            }
        } else if (!("unmounted".equals(sdState) && ("encrypting".equals(cryptionState) || "decrypting".equals(cryptionState)))) {
            cryptionState = "no_card";
        }
        return cryptionState;
    }

    public static boolean isSdCardEncryptingOrDecrypting(VolumeInfo vol) {
        String sdEncryptionState = getSdCryptionState(vol);
        if ("encrypting".equals(sdEncryptionState)) {
            return true;
        }
        return "decrypting".equals(sdEncryptionState);
    }

    public static boolean isFeatureAvailable() {
        boolean isFeatureAvailable = SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true);
        SdLog.i(TAG, "isFeatureAvailable = " + isFeatureAvailable);
        return isFeatureAvailable;
    }

    public static void backupSecretKey() {
        if (isFeatureAvailable()) {
            SdLog.i(TAG, "BackupSecretKey " + HwSdCardCryptdEx.backupSecretkey());
        }
    }
}
