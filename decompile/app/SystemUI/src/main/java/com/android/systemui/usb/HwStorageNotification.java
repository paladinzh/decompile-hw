package com.android.systemui.usb;

import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.usb.StorageNotification.MoveInfo;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SharedPreferenceUtils;
import com.android.systemui.utils.UpdateNotificationInfos;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.HashMap;

public class HwStorageNotification extends StorageNotification {
    private static boolean mEncryptFailedFlag = false;
    private static int mSettingsStatusFlag = 0;
    private static int mVolumeState = -1;
    private Handler mAsyncEventHandler;
    private Handler mMainHandler;
    private int mProgress = 0;
    private Handler mProgressHandler;
    private VolumeEncryptionHandler mVolumeEncryptionHandler;
    private VolumeErrorHandler mVolumeErrorHandler;
    private VolumeUnlockHandler mVolumeUnlockHandler;

    private static class NotificationInfo {
        public int actionType;
        public int messageId;
        public int titleId;

        NotificationInfo(int titleId, int messageId, int actionType) {
            this.titleId = titleId;
            this.messageId = messageId;
            this.actionType = actionType;
        }
    }

    private class VolumeEncryptionHandler {
        private Bundle extras = new Bundle();
        private int mEncryptCode;
        private String mEncryptMsg;
        private String mEncryptStatus;
        private BroadcastReceiver mSDcardEncryptReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        HwLog.i("HwStorageNotification", "mSDcardEncryptReceiver action =" + action);
                        VolumeInfo vol = VolumeEncryptionHandler.this.getExternalSDcardVolInfo();
                        if (vol != null) {
                            if (action.equals("com.huawei.android.HWSDCRYPTD_NOTIFICATION")) {
                                HwStorageNotification.mSettingsStatusFlag = intent.getIntExtra("status", 0);
                                if (HwStorageNotification.mSettingsStatusFlag == 2) {
                                    HwLog.i("HwStorageNotification", " settings status == " + HwStorageNotification.mSettingsStatusFlag);
                                    HwLog.i("HwStorageNotification", " SD card mount =  " + VolumeEncryptionHandler.this.isSDCardMounted());
                                    HwStorageNotification.mVolumeState = vol.getState();
                                    VolumeEncryptionHandler.this.initEncryptionProgress(vol);
                                } else if (HwStorageNotification.mSettingsStatusFlag == 1) {
                                    VolumeEncryptionHandler.this.cancelNotification(vol);
                                }
                            } else if (action.equals("com.huawei.android.HWSDCRYPTD_STATE")) {
                                VolumeEncryptionHandler.this.mEncryptCode = intent.getIntExtra("code", 0);
                                VolumeEncryptionHandler.this.mEncryptStatus = intent.getStringExtra("enable");
                                VolumeEncryptionHandler.this.mEncryptMsg = intent.getStringExtra("message");
                                HwLog.i("HwStorageNotification", "mEncryptFailedFlag  =  " + HwStorageNotification.mEncryptFailedFlag);
                                if (VolumeEncryptionHandler.this.mEncryptCode == 906) {
                                    HwLog.i("HwStorageNotification", " mEncryptFailedFlag = TRUE ");
                                    HwStorageNotification.mEncryptFailedFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        };
        private Runnable runnable;

        public VolumeEncryptionHandler() {
            registerBroadcastReceiver();
        }

        private void registerBroadcastReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.android.HWSDCRYPTD_NOTIFICATION");
            filter.addAction("com.huawei.android.HWSDCRYPTD_STATE");
            HwStorageNotification.this.mContext.registerReceiver(this.mSDcardEncryptReceiver, filter, "com.huawei.hwSdCryptd.permission.RECV_HWSDCRYPTD_RESULT", HwStorageNotification.this.mAsyncEventHandler);
        }

        private Bundle getNotificationAppName() {
            this.extras.putString("android.substName", HwStorageNotification.this.mContext.getString(R.string.sdcard_label));
            return this.extras;
        }

        private String processBroadcastMessage(String msg) {
            if (msg.contains("Cryptsd failed in Encrypt")) {
                return "Encrypt";
            }
            if (msg.contains("Cryptsd failed in Decrypt")) {
                return "Decrypt";
            }
            return null;
        }

        protected void onPublicVolumeStateChangedInternal(VolumeInfo vol) {
            DiskInfo disk = vol.getDisk();
            HwLog.i("HwStorageNotification", " SDcard status = " + vol.getState());
            if (disk != null && disk.isSd()) {
                if (vol.getState() == 2) {
                    volumeMounted(vol);
                } else if (vol.getState() == 7) {
                    volumeRemoved(vol);
                } else if (vol.getState() == 5) {
                    HwStorageNotification.mEncryptFailedFlag = false;
                    cancelNotification(vol);
                }
            }
        }

        private void volumeMounted(VolumeInfo vol) {
            DiskInfo disk = vol.getDisk();
            HwStorageNotification.mVolumeState = 2;
            HwLog.i("HwStorageNotification", "volumeMounted : Volume State is " + HwStorageNotification.mVolumeState);
            if (isEncryptedOnHwPhone()) {
                HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3004, super.buildNotificationBuilder(vol, HwStorageNotification.this.mContext.getString(R.string.sdcard_encrypted_on_another_device_notification_title, new Object[]{disk.getDescription()}), HwStorageNotification.this.mContext.getString(R.string.sdcard_encrypted_on_another_device_notification_message, new Object[]{disk.getDescription()})).setCategory("progress").setPriority(-1).setDefaults(-1).setAutoCancel(true).setShowWhen(true).setExtras(getNotificationAppName()).build(), UserHandle.ALL);
            }
        }

        private void volumeRemoved(VolumeInfo vol) {
            if (vol != null) {
                HwStorageNotification.mVolumeState = 7;
                HwLog.i("HwStorageNotification", "volumeRemoved : Volume State is " + HwStorageNotification.mVolumeState);
                HwStorageNotification.mSettingsStatusFlag = 2;
                DiskInfo disk = vol.getDisk();
                CharSequence title = null;
                if (isEncrypting()) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_removed_during_encryption_notification_title, new Object[]{disk.getDescription()});
                }
                if (isDecrypting()) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_removed_during_decryption_notification_title, new Object[]{disk.getDescription()});
                }
                buildRemovedNotification(title, vol);
            }
        }

        private void buildRemovedNotification(CharSequence title, VolumeInfo vol) {
            if (title != null) {
                DiskInfo disk = vol.getDisk();
                HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3005, super.buildNotificationBuilder(vol, title, HwStorageNotification.this.mContext.getString(R.string.sdcard_removed_during_encryption_notification_message, new Object[]{disk.getDescription()})).setCategory("progress").setPriority(1).setDefaults(-1).setAutoCancel(true).setShowWhen(true).setExtras(getNotificationAppName()).build(), UserHandle.ALL);
            }
        }

        private void initEncryptionProgress(final VolumeInfo vol) {
            if (UserManager.get(HwStorageNotification.this.mContext).isPrimaryUser()) {
                this.runnable = new Runnable() {
                    public void run() {
                        if (HwStorageNotification.mVolumeState == 7 || HwStorageNotification.mSettingsStatusFlag == 1 || HwStorageNotification.mEncryptFailedFlag) {
                            HwStorageNotification.this.mNotificationManager.cancelAsUser(vol.getId(), 3004, UserHandle.ALL);
                            if (HwStorageNotification.mEncryptFailedFlag) {
                                HwStorageNotification.mEncryptFailedFlag = false;
                                VolumeEncryptionHandler.this.encryptionExceptionNotification(vol, VolumeEncryptionHandler.this.mEncryptCode, VolumeEncryptionHandler.this.mEncryptStatus, VolumeEncryptionHandler.this.mEncryptMsg);
                            }
                            return;
                        }
                        if (HwStorageNotification.this.mProgress > 99) {
                            HwStorageNotification.this.mProgress = 0;
                            HwStorageNotification.this.mNotificationManager.cancelAsUser(vol.getId(), 3004, UserHandle.ALL);
                        } else {
                            VolumeEncryptionHandler.this.sendEncryptionProgessNotification(vol);
                            HwStorageNotification.this.mProgress = VolumeEncryptionHandler.this.getEnctyptionProgress();
                            HwStorageNotification.this.mProgressHandler.postDelayed(VolumeEncryptionHandler.this.runnable, 250);
                        }
                    }
                };
                HwStorageNotification.this.mProgressHandler.post(this.runnable);
                return;
            }
            HwLog.i("HwStorageNotification", "Not Primary User!");
        }

        private void sendEncryptionProgessNotification(VolumeInfo vol) {
            DiskInfo disk = vol.getDisk();
            HwStorageNotification.this.mProgress = getEnctyptionProgress();
            new StringBuilder().append(HwStorageNotification.this.mProgress).append("%");
            if (disk.isSd()) {
                CharSequence title = null;
                if (isEncrypting() || isEncryptChecking()) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_is_being_encrypted_notification_title, new Object[]{disk.getDescription()});
                }
                if (isDecrypting() || isDecryptChecking()) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_is_being_decrypted_notification_title, new Object[]{disk.getDescription()});
                }
                if (HwStorageNotification.this.mProgress == 0) {
                    buildDetectingNotification(vol);
                } else {
                    HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3004, super.buildNotificationBuilder(vol, title, null).setCategory("progress").setPriority(-1).setAutoCancel(true).setOngoing(true).setSubText(HwStorageNotification.this.mContext.getString(R.string.encryption_progress, new Object[]{encryptionProgress})).setContentIntent(buildSettingsPendingIntent(vol)).setContentTitle(title).setProgress(100, HwStorageNotification.this.mProgress, false).setExtras(getNotificationAppName()).build(), UserHandle.SYSTEM);
                }
                if (title == null) {
                    HwLog.e("HwStorageNotification", "send encryption progess notification failed");
                }
            }
        }

        private void buildDetectingNotification(VolumeInfo vol) {
            DiskInfo disk = vol.getDisk();
            CharSequence title = HwStorageNotification.this.mContext.getString(R.string.sdcard_detecting, new Object[]{disk.getDescription()});
            HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3004, super.buildNotificationBuilder(vol, title, null).setCategory("progress").setPriority(-1).setAutoCancel(true).setOngoing(true).setContentTitle(title).setContentIntent(buildSettingsPendingIntent(vol)).setExtras(getNotificationAppName()).build(), UserHandle.SYSTEM);
        }

        private void encryptionExceptionNotification(VolumeInfo vol, int encryptCode, String status, String msg) {
            DiskInfo disk = vol.getDisk();
            String SDcardStatus = processBroadcastMessage(msg);
            HwLog.e("HwStorageNotification", "encryption exception SDcardStatus : " + SDcardStatus);
            if (disk.isSd()) {
                HwLog.e("HwStorageNotification", "Encryption exception : " + msg);
                CharSequence title = null;
                if ("Encrypt".equals(SDcardStatus)) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_encrypt_failed_title, new Object[]{disk.getDescription()});
                } else if ("Decrypt".equals(SDcardStatus)) {
                    title = HwStorageNotification.this.mContext.getString(R.string.sdcard_decrypt_failed_title, new Object[]{disk.getDescription()});
                }
                HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3004, super.buildNotificationBuilder(vol, title, HwStorageNotification.this.mContext.getString(R.string.sdcard_encrypt_failed_message, new Object[]{disk.getDescription()})).setCategory("progress").setPriority(1).setAutoCancel(true).setContentIntent(buildEncryptExceptionPendingIntent(vol, encryptCode, status, msg)).setShowWhen(true).setExtras(getNotificationAppName()).build(), UserHandle.SYSTEM);
            }
        }

        private void cancelNotification(VolumeInfo vol) {
            HwStorageNotification.this.mNotificationManager.cancelAsUser(vol.getId(), 3004, UserHandle.ALL);
        }

        private boolean isEncrypting() {
            return "encrypting".equals(SystemProperties.get("vold.cryptsd.state"));
        }

        private boolean isDecrypting() {
            return "decrypting".equals(SystemProperties.get("vold.cryptsd.state"));
        }

        private boolean isEncryptedOnHwPhone() {
            HwLog.d("HwStorageNotification", "mismatch" + SystemProperties.get("vold.cryptsd.state"));
            return "mismatch".equals(SystemProperties.get("vold.cryptsd.state"));
        }

        private boolean isEnable() {
            return "enable".equals(SystemProperties.get("vold.cryptsd.state"));
        }

        private boolean isDisable() {
            return "disable".equals(SystemProperties.get("vold.cryptsd.state"));
        }

        private boolean isDecryptChecking() {
            HwLog.d("HwStorageNotification", "decryption checking property = " + SystemProperties.get("vold.cryptsd.state"));
            if (getEnctyptionProgress() != 0) {
                return false;
            }
            if ((isEnable() || isDecrypting()) && !isSDCardMounted()) {
                return true;
            }
            return false;
        }

        private boolean isEncryptChecking() {
            HwLog.d("HwStorageNotification", "encryption checking property = " + SystemProperties.get("vold.cryptsd.state"));
            if (getEnctyptionProgress() != 0) {
                return false;
            }
            if ((isDisable() || isEncrypting()) && !isSDCardMounted()) {
                return true;
            }
            return false;
        }

        private int getEnctyptionProgress() {
            int progress = SystemProperties.getInt("vold.cryptsd_progress", -1);
            if (progress != 100) {
                return progress;
            }
            if (isEncrypting() || isDecrypting()) {
                return progress - 1;
            }
            return progress;
        }

        private boolean isSDCardMounted() {
            if (HwStorageNotification.this.mStorageManager != null) {
                for (StorageVolume storageVolume : HwStorageNotification.this.mStorageManager.getVolumeList()) {
                    if (storageVolume.isRemovable() && !storageVolume.getPath().contains("usb")) {
                        if ("mounted".equals(HwStorageNotification.this.mStorageManager.getVolumeState(storageVolume.getPath()))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private PendingIntent buildSettingsPendingIntent(VolumeInfo vol) {
            Intent intent = new Intent("android.app.action.START_SD_ENCRYPTION");
            intent.setPackage("com.android.settings");
            intent.setFlags(603979776);
            return PendingIntent.getActivityAsUser(HwStorageNotification.this.mContext, vol.getId().hashCode(), intent, 134217728, null, UserHandle.CURRENT);
        }

        private PendingIntent buildEncryptExceptionPendingIntent(VolumeInfo vol, int encryptCode, String status, String msg) {
            Intent intent = new Intent("android.app.action.START_SD_ENCRYPTION");
            intent.setPackage("com.android.settings");
            int requestKey = vol.getId().hashCode();
            intent.putExtra("message", msg);
            intent.putExtra("code", encryptCode);
            intent.putExtra("enable", status);
            return PendingIntent.getActivityAsUser(HwStorageNotification.this.mContext, requestKey, intent, 134217728, null, UserHandle.CURRENT);
        }

        private VolumeInfo getExternalSDcardVolInfo() {
            for (StorageVolume storageVolume : HwStorageNotification.this.mStorageManager.getVolumeList()) {
                if (isVolumeExternalSDcard(HwStorageNotification.this.mContext, storageVolume)) {
                    return HwStorageNotification.this.mStorageManager.findVolumeByUuid(storageVolume.getUuid());
                }
            }
            return null;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean isVolumeExternalSDcard(Context context, StorageVolume storageVolume) {
            if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable() || storageVolume.getUuid() == null) {
                return false;
            }
            VolumeInfo volumeInfo = HwStorageNotification.this.mStorageManager.findVolumeByUuid(storageVolume.getUuid());
            if (volumeInfo == null) {
                return false;
            }
            DiskInfo diskInfo = volumeInfo.getDisk();
            if (diskInfo != null) {
                return diskInfo.isSd();
            }
            return false;
        }
    }

    private class VolumeErrorHandler {
        private HashMap<Integer, NotificationInfo> mNotificationInfoMap = new HashMap();
        private BroadcastReceiver onStateChangeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwLog.i("HwStorageNotification", "onReceive:" + intent);
                if ("android.intent.action.MEDIA_ABNORMAL_SD".equals(intent.getAction())) {
                    VolumeInfo vol = (VolumeInfo) intent.getExtra("android.os.storage.extra.STORAGE_VOLUME");
                    int newState = intent.getIntExtra("android.os.storage.extra.VOLUME_NEW_STATE", 0);
                    int oldState = intent.getIntExtra("android.os.storage.extra.VOLUME_OLD_STATE", 0);
                    if (vol != null) {
                        String uuid = HwSdCardLockUtils.getCid();
                        HwLog.i("HwStorageNotification", "vol:" + vol.getId() + " uuid:" + uuid + ", newState=" + newState + ", oldState=" + oldState);
                        if (VolumeErrorHandler.this.isNeedToNoti(context, uuid, newState)) {
                            VolumeErrorHandler.this.onVolumeError(vol, newState);
                        }
                    }
                }
            }
        };

        public VolumeErrorHandler() {
            initNotificationMap();
            HwStorageNotification.this.mContext.registerReceiver(this.onStateChangeReceiver, new IntentFilter("android.intent.action.MEDIA_ABNORMAL_SD"), null, HwStorageNotification.this.mAsyncEventHandler);
        }

        private void initNotificationMap() {
            this.mNotificationInfoMap.put(Integer.valueOf(21), new NotificationInfo(R.string.state_volume_readerror_notification_title, R.string.state_volume_readerror_notification_message_change, 2));
            this.mNotificationInfoMap.put(Integer.valueOf(22), new NotificationInfo(R.string.state_volume_writeerror_notification_title, R.string.state_volume_writeerror_notification_message, 0));
            this.mNotificationInfoMap.put(Integer.valueOf(23), new NotificationInfo(R.string.state_volume_ro_notification_title, R.string.state_volume_ro_notification_message, 0));
            this.mNotificationInfoMap.put(Integer.valueOf(24), new NotificationInfo(R.string.state_volume_lowspeed_sd_title, R.string.state_volume_lowspeed_sd_message, 0));
            this.mNotificationInfoMap.put(Integer.valueOf(25), new NotificationInfo(R.string.state_volume_filesystem_error_notitication_title, R.string.state_volume_bad_sd_notification_message_change, 2));
            this.mNotificationInfoMap.put(Integer.valueOf(26), new NotificationInfo(R.string.state_volume_filesystem_error_notitication_title, R.string.state_volume_filesystem_error_notification_message_change, 1));
            this.mNotificationInfoMap.put(Integer.valueOf(27), new NotificationInfo(R.string.state_volume_lowspeed_sd_ex_title, R.string.state_volume_lowspeed_sd_ex_message, 2));
        }

        private void onVolumeError(VolumeInfo vol, int errorCode) {
            if (vol != null) {
                HwLog.i("HwStorageNotification", "onVolumeError:" + vol.getState() + ", " + errorCode);
                if (errorCode == 0) {
                    HwStorageNotification.this.mNotificationManager.cancelAsUser(vol.getId(), 3003, UserHandle.ALL);
                    return;
                }
                DiskInfo disk = vol.getDisk();
                if (disk != null) {
                    NotificationInfo info = (NotificationInfo) this.mNotificationInfoMap.get(Integer.valueOf(errorCode));
                    int titleId = info != null ? info.titleId : R.string.sdcard_error_notification_title;
                    int messageId = info != null ? info.messageId : R.string.sdcard_error_notification_message;
                    int actionType = info != null ? info.actionType : 0;
                    CharSequence title = HwStorageNotification.this.mContext.getString(titleId, new Object[]{disk.getDescription()});
                    CharSequence text = HwStorageNotification.this.mContext.getString(messageId, new Object[]{disk.getDescription()});
                    String sdTitle = disk.getDescription();
                    Intent intent = new Intent();
                    intent.setClass(HwStorageNotification.this.mContext, HwSDCardGuiderActivity.class);
                    intent.putExtra("SD_ACTION_TYPE", actionType);
                    intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
                    intent.putExtra("android.os.storage.extra.DISK_ID", disk.getId());
                    intent.putExtra("storage_title", sdTitle);
                    intent.putExtra("error_code", errorCode);
                    HwStorageNotification.this.mNotificationManager.notifyAsUser(vol.getId(), 3003, HwStorageNotification.this.buildNotificationBuilder(vol, title, text).setCategory("err").setContentIntent(PendingIntent.getActivityAsUser(HwStorageNotification.this.mContext, disk.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT)).build(), UserHandle.ALL);
                    BDReporter.e(HwStorageNotification.this.mContext, 70, "{" + errorCode + ":" + sdTitle + "}");
                }
            }
        }

        protected void onPublicVolumeStateChangedInternal(VolumeInfo vol) {
            if (vol.getState() == 8 || vol.getState() == 7) {
                clearNotification(vol);
            }
        }

        public void clearNotification(VolumeInfo vol) {
            HwLog.i("HwStorageNotification", "clear notification: 3003");
            HwStorageNotification.this.mNotificationManager.cancel(vol.getId(), 3003);
        }

        private boolean isNeedToNoti(Context context, String uuid, int state) {
            if (27 != state) {
                return true;
            }
            if (TextUtils.isEmpty(uuid) || uuid.equals(SharedPreferenceUtils.getString(context, "sd_notify", "sd_uuid"))) {
                return false;
            }
            SharedPreferenceUtils.writeString(context, "sd_notify", "sd_uuid", uuid);
            return true;
        }
    }

    private class VolumeUnlockHandler {
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwLog.i("HwStorageNotification", "onReceive:" + intent);
                String action = intent.getAction();
                if (action == null) {
                    HwLog.e("HwStorageNotification", "mReceiver action == null");
                    return;
                }
                if (action.equals("android.intent.action.HWSDLOCK_AUTO_UNLOCK_FAILED")) {
                    VolumeUnlockHandler.this.checkSDcardPassword("checking", true);
                } else if (action.equals("android.intent.action.HWSDLOCK_UNLOCK_COMPLETED")) {
                    VolumeUnlockHandler.this.mSDcardStatusUnlocked = true;
                } else if (action.equals("android.intent.action.HWSDLOCK_FORCE_ERASE_COMPLETED")) {
                    VolumeUnlockHandler.this.mSDcardStatusUnlocked = false;
                    HwSdCardLockUtils.deleteSDcardID(HwStorageNotification.this.mContext);
                } else if (action.equals("android.intent.action.HWSDLOCK_CLEAR_COMPLETED")) {
                    VolumeUnlockHandler.this.mSDcardStatusUnlocked = false;
                    HwSdCardLockUtils.deleteSDcardID(HwStorageNotification.this.mContext);
                } else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    VolumeUnlockHandler.this.mShutDown = true;
                } else if (action.equals("android.intent.action.HWSDLOCK_SET_PWD_COMPLETED")) {
                    Log.d("HwStorageNotification", "cancel notification: 3003");
                    HwStorageNotification.this.mNotificationManager.cancel(3003);
                    VolumeUnlockHandler.this.mSDcardStatusUnlocked = true;
                    HwSdCardLockUtils.deleteSDcardID(HwStorageNotification.this.mContext);
                }
            }
        };
        private boolean mSDcardStatusUnlocked = false;
        private boolean mShutDown = false;

        public VolumeUnlockHandler() {
            registerBraodcastReceiver();
        }

        private void registerBraodcastReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.HWSDLOCK_SET_PWD_COMPLETED");
            filter.addAction("android.intent.action.HWSDLOCK_UNLOCK_COMPLETED");
            filter.addAction("android.intent.action.HWSDLOCK_AUTO_UNLOCK_FAILED");
            filter.addAction("android.intent.action.HWSDLOCK_FORCE_ERASE_COMPLETED");
            filter.addAction("android.intent.action.HWSDLOCK_CLEAR_COMPLETED");
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            HwStorageNotification.this.mContext.registerReceiver(this.mReceiver, filter, null, HwStorageNotification.this.mAsyncEventHandler);
        }

        private void checkSDcardPassword(String status, boolean unmounted) {
            HwLog.i("HwStorageNotification", "checkSDcardPassword:" + status + "," + unmounted + "," + this.mSDcardStatusUnlocked + "," + this.mShutDown);
            boolean startdialog = false;
            if (status.equals("checking")) {
                if (HwSdCardLockUtils.isSdCardEncrpyted(HwStorageNotification.this.mContext) || HwSdCardLockUtils.iscontains(HwStorageNotification.this.mContext)) {
                    startdialog = true;
                } else {
                    startdialog = false;
                    HwSdCardLockUtils.insertSDcardID(HwStorageNotification.this.mContext);
                }
            } else if (status.equals("bad_removal") && this.mSDcardStatusUnlocked && !this.mShutDown) {
                startdialog = true;
            }
            if (startdialog && unmounted) {
                Intent it = new Intent(HwStorageNotification.this.mContext, HwSDcardPasswordDialogActivity.class);
                it.setFlags(335544320);
                it.putExtra("which", status);
                HwStorageNotification.this.mContext.startActivity(it);
            }
        }

        protected void onPublicVolumeStateChangedInternal(VolumeInfo vol) {
            DiskInfo disk = vol.getDisk();
            if (disk != null && disk.isSd()) {
                if (vol.getState() == 1) {
                    checkSDcardPassword("checking", false);
                } else if (vol.getState() == 2 || vol.getState() == 3) {
                    HwLog.i("HwStorageNotification", "sendBroadcast com.android.systemui.action.SD_MOUNTED");
                    Intent intent = new Intent("com.android.systemui.action.SD_MOUNTED");
                    intent.setPackage("com.android.systemui");
                    HwStorageNotification.this.mContext.sendBroadcast(intent);
                } else if (vol.getState() == 6) {
                    this.mSDcardStatusUnlocked = false;
                } else if (vol.getState() == 7) {
                    this.mSDcardStatusUnlocked = false;
                } else if (vol.getState() == 8) {
                    checkSDcardPassword("bad_removal", true);
                    this.mSDcardStatusUnlocked = false;
                }
            }
        }
    }

    public void start() {
        this.mMainHandler = new Handler();
        HandlerThread thr = new HandlerThread("SystemUI StorageNotification");
        thr.start();
        this.mAsyncEventHandler = new Handler(thr.getLooper());
        this.mProgressHandler = new Handler(thr.getLooper());
        this.mVolumeErrorHandler = new VolumeErrorHandler();
        this.mVolumeUnlockHandler = new VolumeUnlockHandler();
        this.mVolumeEncryptionHandler = new VolumeEncryptionHandler();
        super.start();
    }

    protected void onVolumeStateChangedInternal(final VolumeInfo vol) {
        this.mAsyncEventHandler.post(new Runnable() {
            public void run() {
                super.onVolumeStateChangedInternal(vol);
            }
        });
    }

    protected void onDiskScannedInternal(final DiskInfo disk, final int volumeCount) {
        this.mAsyncEventHandler.post(new Runnable() {
            public void run() {
                super.onDiskScannedInternal(disk, volumeCount);
            }
        });
    }

    protected void onDiskDestroyedInternal(final DiskInfo disk) {
        this.mAsyncEventHandler.post(new Runnable() {
            public void run() {
                super.onDiskDestroyedInternal(disk);
            }
        });
    }

    protected void onMoveProgress(MoveInfo move, int status, long estMillis) {
        final MoveInfo moveInfo = move;
        final int i = status;
        final long j = estMillis;
        this.mAsyncEventHandler.post(new Runnable() {
            public void run() {
                super.onMoveProgress(moveInfo, i, j);
            }
        });
    }

    protected void onMoveFinished(final MoveInfo move, final int status) {
        this.mAsyncEventHandler.post(new Runnable() {
            public void run() {
                super.onMoveFinished(move, status);
            }
        });
    }

    protected void onPublicVolumeStateChangedInternal(VolumeInfo vol) {
        DiskInfo disk = vol.getDisk();
        HwLog.i("HwStorageNotification", "onPublicVolumeStateChangedInternal:vol=" + vol.getId() + ", state=" + vol.getState() + ", disk=" + (disk != null ? disk.getId() : "null"));
        super.onPublicVolumeStateChangedInternal(vol);
        if (!(this.mVolumeErrorHandler == null || this.mVolumeUnlockHandler == null)) {
            this.mVolumeErrorHandler.onPublicVolumeStateChangedInternal(vol);
            this.mVolumeUnlockHandler.onPublicVolumeStateChangedInternal(vol);
        }
        if (this.mVolumeEncryptionHandler != null) {
            this.mVolumeEncryptionHandler.onPublicVolumeStateChangedInternal(vol);
        }
    }

    protected Builder buildNotificationBuilder(VolumeInfo vol, CharSequence title, CharSequence text) {
        return super.buildNotificationBuilder(vol, title, text).setExtras(UpdateNotificationInfos.getNotificationThemeData(getSmallThemeIcon(vol.getDisk(), vol.getState()), -1, getThemeIconBackgroundId(vol.getDisk(), vol.getState()), 15)).setTicker(title);
    }

    protected PendingIntent buildInitPendingIntent(DiskInfo disk) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardFormatConfirm");
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.getId());
        return PendingIntent.getActivityAsUser(this.mContext, disk.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    protected PendingIntent buildInitPendingIntent(VolumeInfo vol) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardFormatConfirm");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
        return PendingIntent.getActivityAsUser(this.mContext, vol.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    protected int getSmallIcon(DiskInfo disk, int state) {
        if (disk.isSd()) {
            switch (state) {
                case 1:
                    return R.drawable.stat_notify_sdcard_prepare;
                case 8:
                    return R.drawable.stat_sys_warning;
                default:
                    return R.drawable.stat_notify_sdcard;
            }
        } else if (!disk.isUsb()) {
            return R.drawable.stat_notify_sdcard;
        } else {
            switch (state) {
                case 1:
                    return R.drawable.stat_notify_sdcard_prepare;
                case 8:
                    return R.drawable.stat_sys_warning;
                default:
                    return R.drawable.stat_notify_sdcard_usb;
            }
        }
    }

    private int getSmallThemeIcon(DiskInfo disk, int state) {
        if (disk.isSd()) {
            switch (state) {
                case 1:
                    return R.drawable.stat_notify_sdcard_prepare_theme;
                case 8:
                    return R.drawable.stat_sys_warning_theme;
                default:
                    return R.drawable.stat_notify_sdcard_theme;
            }
        } else if (!disk.isUsb()) {
            return R.drawable.stat_notify_sdcard_theme;
        } else {
            switch (state) {
                case 1:
                    return R.drawable.stat_notify_sdcard_prepare_theme;
                case 8:
                    return R.drawable.stat_sys_warning_theme;
                default:
                    return R.drawable.stat_notify_sdcard_usb_theme;
            }
        }
    }

    private int getThemeIconBackgroundId(DiskInfo disk, int state) {
        if (disk.isSd()) {
            switch (state) {
                case 1:
                    return 5;
                case 8:
                    return 1;
                default:
                    return 5;
            }
        } else if (!disk.isUsb()) {
            return 5;
        } else {
            switch (state) {
                case 1:
                    return 5;
                case 8:
                    return 1;
                default:
                    return 5;
            }
        }
    }
}
