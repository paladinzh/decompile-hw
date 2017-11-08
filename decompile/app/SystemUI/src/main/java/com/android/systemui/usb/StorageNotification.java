package com.android.systemui.usb;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;

public class StorageNotification extends SystemUI {
    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mNotificationManager.cancelAsUser(null, 1397575510, UserHandle.ALL);
        }
    };
    private final H mHandler = new H();
    private final StorageEventListener mListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            StorageNotification.this.onVolumeStateChangedInternal(vol);
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            VolumeInfo vol = StorageNotification.this.mStorageManager.findVolumeByUuid(rec.getFsUuid());
            if (vol != null && vol.isMountedReadable()) {
                StorageNotification.this.onVolumeStateChangedInternal(vol);
            }
        }

        public void onVolumeForgotten(String fsUuid) {
            StorageNotification.this.mNotificationManager.cancelAsUser(fsUuid, 1397772886, UserHandle.ALL);
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            StorageNotification.this.onDiskScannedInternal(disk, volumeCount);
        }

        public void onDiskDestroyed(DiskInfo disk) {
            StorageNotification.this.onDiskDestroyedInternal(disk);
        }
    };
    private final MoveCallback mMoveCallback = new MoveCallback() {
        public void onCreated(int moveId, Bundle extras) {
            MoveInfo move = new MoveInfo();
            move.moveId = moveId;
            move.extras = extras;
            if (extras != null) {
                move.packageName = extras.getString("android.intent.extra.PACKAGE_NAME");
                move.label = extras.getString("android.intent.extra.TITLE");
                move.volumeUuid = extras.getString("android.os.storage.extra.FS_UUID");
            }
            StorageNotification.this.mMoves.put(moveId, move);
        }

        public void onStatusChanged(int moveId, int status, long estMillis) {
            MoveInfo move = (MoveInfo) StorageNotification.this.mMoves.get(moveId);
            if (move == null) {
                Log.w("StorageNotification", "Ignoring unknown move " + moveId);
                return;
            }
            if (PackageManager.isMoveStatusFinished(status)) {
                StorageNotification.this.onMoveFinished(move, status);
            } else {
                StorageNotification.this.onMoveProgress(move, status, estMillis);
            }
        }
    };
    private final SparseArray<MoveInfo> mMoves = new SparseArray();
    protected NotificationManager mNotificationManager;
    private final BroadcastReceiver mSnoozeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            StorageNotification.this.mStorageManager.setVolumeSnoozed(intent.getStringExtra("android.os.storage.extra.FS_UUID"), true);
        }
    };
    protected StorageManager mStorageManager;

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i("StorageNotification", "cancelAsUser volId :" + msg.obj);
                StorageNotification.this.mNotificationManager.cancelAsUser((String) msg.obj, 1397773634, UserHandle.ALL);
            }
        }
    }

    public static class MoveInfo {
        public Bundle extras;
        public String label;
        public int moveId;
        public String packageName;
        public String volumeUuid;
    }

    public void start() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        this.mStorageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        this.mStorageManager.registerListener(this.mListener);
        this.mContext.registerReceiver(this.mSnoozeReceiver, new IntentFilter("com.android.systemui.action.SNOOZE_VOLUME"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mContext.registerReceiver(this.mFinishReceiver, new IntentFilter("com.android.systemui.action.FINISH_WIZARD"), "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        for (DiskInfo disk : this.mStorageManager.getDisks()) {
            onDiskScannedInternal(disk, disk.volumeCount);
        }
        for (VolumeInfo vol : this.mStorageManager.getVolumes()) {
            onVolumeStateChangedInternal(vol);
        }
        this.mContext.getPackageManager().registerMoveCallback(this.mMoveCallback, new Handler());
        updateMissingPrivateVolumes();
    }

    private void updateMissingPrivateVolumes() {
        for (VolumeRecord rec : this.mStorageManager.getVolumeRecords()) {
            if (rec.getType() == 1) {
                String fsUuid = rec.getFsUuid();
                VolumeInfo info = this.mStorageManager.findVolumeByUuid(fsUuid);
                if ((info == null || !info.isMountedWritable()) && !rec.isSnoozed()) {
                    CharSequence title = this.mContext.getString(17040425, new Object[]{rec.getNickname()});
                    CharSequence text = this.mContext.getString(17040426);
                    Builder builder = new Builder(this.mContext).setSmallIcon(BadgedIconHelper.getBitampIcon(this.mContext, 17302547)).setContentTitle(title).setContentText(text).setContentIntent(buildForgetPendingIntent(rec)).setStyle(new BigTextStyle().bigText(text)).setVisibility(1).setLocalOnly(true).setCategory("sys").setDeleteIntent(buildSnoozeIntent(fsUuid));
                    SystemUI.overrideNotificationAppName(this.mContext, builder);
                    this.mNotificationManager.notifyAsUser(fsUuid, 1397772886, builder.build(), UserHandle.ALL);
                } else {
                    this.mNotificationManager.cancelAsUser(fsUuid, 1397772886, UserHandle.ALL);
                }
            }
        }
    }

    protected void onDiskScannedInternal(DiskInfo disk, int volumeCount) {
        if (volumeCount != 0 || disk.size <= 0) {
            this.mNotificationManager.cancelAsUser(disk.getId(), 1396986699, UserHandle.ALL);
            return;
        }
        CharSequence title = this.mContext.getString(17040414, new Object[]{disk.getDescription()});
        CharSequence text = this.mContext.getString(17040415, new Object[]{disk.getDescription()});
        Builder builder = new Builder(this.mContext).setSmallIcon(BadgedIconHelper.getBitampIcon(this.mContext, getSmallIcon(disk, 6))).setContentTitle(title).setContentText(text).setContentIntent(buildInitPendingIntent(disk)).setStyle(new BigTextStyle().bigText(text)).setVisibility(1).setLocalOnly(true).setCategory("err");
        SystemUI.overrideNotificationAppName(this.mContext, builder);
        this.mNotificationManager.notifyAsUser(disk.getId(), 1396986699, builder.build(), UserHandle.ALL);
    }

    protected void onDiskDestroyedInternal(DiskInfo disk) {
        this.mNotificationManager.cancelAsUser(disk.getId(), 1396986699, UserHandle.ALL);
    }

    protected void onVolumeStateChangedInternal(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
                onPublicVolumeStateChangedInternal(vol);
                return;
            case 1:
                onPrivateVolumeStateChangedInternal(vol);
                return;
            default:
                return;
        }
    }

    private void onPrivateVolumeStateChangedInternal(VolumeInfo vol) {
        Log.d("StorageNotification", "Notifying about private volume: " + vol.toString());
        updateMissingPrivateVolumes();
    }

    protected void onPublicVolumeStateChangedInternal(VolumeInfo vol) {
        Notification notif;
        switch (vol.getState()) {
            case 0:
                notif = onVolumeUnmounted(vol);
                break;
            case 1:
                if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                    notif = onVolumeChecking(vol);
                    break;
                }
                return;
            case 2:
            case 3:
                notif = onVolumeMounted(vol);
                break;
            case 4:
                notif = onVolumeFormatting(vol);
                break;
            case 5:
                notif = onVolumeEjecting(vol);
                break;
            case 6:
                notif = onVolumeUnmountable(vol);
                break;
            case 7:
                notif = onVolumeRemoved(vol);
                break;
            case 8:
                notif = onVolumeBadRemoval(vol);
                break;
            default:
                notif = null;
                break;
        }
        if (notif != null) {
            this.mNotificationManager.notifyAsUser(vol.getId(), 1397773634, notif, UserHandle.ALL);
        } else {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, 0, 0, vol.getId()), 2500);
        }
    }

    private Notification onVolumeUnmounted(VolumeInfo vol) {
        return null;
    }

    private Notification onVolumeChecking(VolumeInfo vol) {
        int i;
        Log.i("StorageNotification", "onVolumeChecking:" + vol.getId() + "," + vol.getDisk());
        DiskInfo disk = vol.getDisk();
        Context context = this.mContext;
        if (disk.isSd()) {
            i = 17040408;
        } else {
            i = R.string.ext_media_checking_notification_title_usb;
        }
        return buildNotificationBuilder(vol, context.getString(i, new Object[]{disk.getDescription()}), this.mContext.getString(17040409, new Object[]{disk.getDescription()})).setCategory("progress").setPriority(-1).setOngoing(true).build();
    }

    protected Notification onVolumeMounted(VolumeInfo vol) {
        Log.i("StorageNotification", "onVolumeMounted:" + vol.getId() + "," + vol.getDisk());
        if (SystemProperties.getBoolean("ro.config.usb_notif", true)) {
            VolumeRecord rec = this.mStorageManager.findRecordByUuid(vol.getFsUuid());
            DiskInfo disk = vol.getDisk();
            if (rec == null || disk == null) {
                Log.e("StorageNotification", "fail to get VolumeRecord or DiskInfo");
                return null;
            } else if (disk.isUsb()) {
                CharSequence title = disk.getDescription();
                CharSequence text = this.mContext.getString(17040411, new Object[]{disk.getDescription()});
                PendingIntent browseIntent = buildBrowsePendingIntent(vol);
                Builder builder = buildNotificationBuilder(vol, title, text);
                if (browseIntent != null) {
                    builder.addAction(new Action(17302338, this.mContext.getString(R.string.label_view), browseIntent));
                    builder.setContentIntent(browseIntent);
                }
                builder.addAction(new Action(17302323, this.mContext.getString(17040423), buildUnmountPendingIntent(vol))).setCategory("sys").setWhen(0).setPriority(2);
                builder.setDeleteIntent(buildSnoozeIntent(vol.getFsUuid()));
                this.mHandler.removeMessages(1);
                return builder.build();
            } else {
                Log.e("StorageNotification", "volume is not usb");
                return null;
            }
        }
        Log.e("StorageNotification", "ro.config.usb_notif is false");
        return null;
    }

    private Notification onVolumeFormatting(VolumeInfo vol) {
        return null;
    }

    private Notification onVolumeEjecting(VolumeInfo vol) {
        Log.i("StorageNotification", "onVolumeEjecting:" + vol.getId() + "," + vol.getDisk());
        DiskInfo disk = vol.getDisk();
        return buildNotificationBuilder(vol, this.mContext.getString(R.string.ext_media_unmounting_notification_title, new Object[]{disk.getDescription()}), this.mContext.getString(17040421, new Object[]{disk.getDescription()})).setCategory("progress").setPriority(-1).setOngoing(true).build();
    }

    private Notification onVolumeUnmountable(VolumeInfo vol) {
        int i;
        Log.i("StorageNotification", "onVolumeUnmountable:" + vol.getId() + "," + vol.getDisk());
        DiskInfo disk = vol.getDisk();
        Context context = this.mContext;
        if (disk.isSd()) {
            i = 17040412;
        } else {
            i = R.string.ext_media_unmountable_notification_title_usb;
        }
        CharSequence title = context.getString(i, new Object[]{disk.getDescription()});
        context = this.mContext;
        if (disk.isSd()) {
            i = 17040413;
        } else {
            i = R.string.ext_media_unmountable_notification_message_usb;
        }
        return buildNotificationBuilder(vol, title, context.getString(i, new Object[]{disk.getDescription()})).setContentIntent(buildInitPendingIntent(vol)).setCategory("err").build();
    }

    private Notification onVolumeRemoved(VolumeInfo vol) {
        Log.i("StorageNotification", "onVolumeRemoved:" + vol.getId() + "," + vol.getDisk());
        if (!vol.isPrimary()) {
            return null;
        }
        int i;
        DiskInfo disk = vol.getDisk();
        Context context = this.mContext;
        if (disk.isSd()) {
            i = 17040418;
        } else {
            i = R.string.ext_media_nomedia_notification_title_usb;
        }
        CharSequence title = context.getString(i, new Object[]{disk.getDescription()});
        context = this.mContext;
        if (disk.isSd()) {
            i = 17040419;
        } else {
            i = R.string.ext_media_nomedia_notification_message_usb;
        }
        return buildNotificationBuilder(vol, title, context.getString(i, new Object[]{disk.getDescription()})).setCategory("err").build();
    }

    private Notification onVolumeBadRemoval(VolumeInfo vol) {
        Log.i("StorageNotification", "onVolumeBadRemoval:" + vol.getId() + "," + vol.getDisk());
        if (!vol.isPrimary()) {
            return null;
        }
        int i;
        DiskInfo disk = vol.getDisk();
        Context context = this.mContext;
        if (disk.isSd()) {
            i = 17040416;
        } else {
            i = R.string.ext_media_badremoval_notification_title_usb;
        }
        CharSequence title = context.getString(i, new Object[]{disk.getDescription()});
        context = this.mContext;
        if (disk.isSd()) {
            i = 17040417;
        } else {
            i = R.string.ext_media_badremoval_notification_message_usb;
        }
        return buildNotificationBuilder(vol, title, context.getString(i, new Object[]{disk.getDescription()})).setCategory("err").build();
    }

    protected void onMoveProgress(MoveInfo move, int status, long estMillis) {
        CharSequence title;
        CharSequence charSequence;
        PendingIntent intent;
        if (TextUtils.isEmpty(move.label)) {
            title = this.mContext.getString(17040428);
        } else {
            title = this.mContext.getString(17040427, new Object[]{move.label});
        }
        if (estMillis < 0) {
            charSequence = null;
        } else {
            charSequence = DateUtils.formatDuration(estMillis);
        }
        if (move.packageName != null) {
            intent = buildWizardMovePendingIntent(move);
        } else {
            intent = buildWizardMigratePendingIntent(move);
        }
        Builder builder = new Builder(this.mContext).setSmallIcon(BadgedIconHelper.getBitampIcon(this.mContext, 17302547)).setContentTitle(title).setContentText(charSequence).setContentIntent(intent).setStyle(new BigTextStyle().bigText(charSequence)).setVisibility(1).setLocalOnly(true).setCategory("progress").setPriority(-1).setProgress(100, status, false).setOngoing(true);
        SystemUI.overrideNotificationAppName(this.mContext, builder);
        this.mNotificationManager.notifyAsUser(move.packageName, 1397575510, builder.build(), UserHandle.ALL);
    }

    protected void onMoveFinished(MoveInfo move, int status) {
        if (move.packageName != null) {
            this.mNotificationManager.cancelAsUser(move.packageName, 1397575510, UserHandle.ALL);
            return;
        }
        CharSequence title;
        CharSequence text;
        PendingIntent buildWizardReadyPendingIntent;
        VolumeInfo privateVol = this.mContext.getPackageManager().getPrimaryStorageCurrentVolume();
        String descrip = this.mStorageManager.getBestVolumeDescription(privateVol);
        if (status == -100) {
            title = this.mContext.getString(17040429);
            text = this.mContext.getString(17040430, new Object[]{descrip});
        } else {
            title = this.mContext.getString(17040431);
            text = this.mContext.getString(17040432);
        }
        if (privateVol != null && privateVol.getDisk() != null) {
            buildWizardReadyPendingIntent = buildWizardReadyPendingIntent(privateVol.getDisk());
        } else if (privateVol != null) {
            buildWizardReadyPendingIntent = buildVolumeSettingsPendingIntent(privateVol);
        } else {
            buildWizardReadyPendingIntent = null;
        }
        Builder builder = new Builder(this.mContext).setSmallIcon(BadgedIconHelper.getBitampIcon(this.mContext, 17302547)).setContentTitle(title).setContentText(text).setContentIntent(buildWizardReadyPendingIntent).setStyle(new BigTextStyle().bigText(text)).setVisibility(1).setLocalOnly(true).setCategory("sys").setPriority(-1).setAutoCancel(true);
        SystemUI.overrideNotificationAppName(this.mContext, builder);
        this.mNotificationManager.notifyAsUser(move.packageName, 1397575510, builder.build(), UserHandle.ALL);
    }

    protected int getSmallIcon(DiskInfo disk, int state) {
        if (disk.isSd()) {
            switch (state) {
                case 1:
                case 5:
                    return 17302547;
                default:
                    return 17302547;
            }
        } else if (disk.isUsb()) {
            return 17302569;
        } else {
            return 17302547;
        }
    }

    protected Builder buildNotificationBuilder(VolumeInfo vol, CharSequence title, CharSequence text) {
        Builder builder = new Builder(this.mContext).setSmallIcon(BadgedIconHelper.getBitampIcon(this.mContext, getSmallIcon(vol.getDisk(), vol.getState()))).setContentTitle(title).setContentText(text).setStyle(new BigTextStyle().bigText(text)).setVisibility(1).setLocalOnly(true);
        SystemUI.overrideNotificationAppName(this.mContext, builder);
        return builder;
    }

    protected PendingIntent buildInitPendingIntent(DiskInfo disk) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.getId());
        return PendingIntent.getActivityAsUser(this.mContext, disk.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    protected PendingIntent buildInitPendingIntent(VolumeInfo vol) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardInit");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
        return PendingIntent.getActivityAsUser(this.mContext, vol.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildUnmountPendingIntent(VolumeInfo vol) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageUnmountReceiver");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
        return PendingIntent.getBroadcastAsUser(this.mContext, vol.getId().hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildBrowsePendingIntent(VolumeInfo vol) {
        if (vol == null || vol.getPath() == null) {
            Log.e("StorageNotification", "fail to get volume path");
            return null;
        }
        Intent intent = buildFileBrowseIntent(vol.getPath().getAbsolutePath());
        if (intent == null) {
            Log.e("StorageNotification", "file browse intent is null");
            return null;
        }
        return PendingIntent.getActivityAsUser(this.mContext, vol.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private Intent buildFileBrowseIntent(String dir) {
        if (assertPackageExist("com.huawei.hidisk")) {
            Intent startIntent = new Intent();
            startIntent.setAction("android.intent.action.VIEW");
            startIntent.addCategory("android.intent.category.DEFAULT");
            startIntent.setType("filemanager.dir/*");
            startIntent.putExtra("curr_dir", dir);
            startIntent.setFlags(32768);
            return startIntent;
        }
        Log.e("StorageNotification", "hidisk does not exist");
        return null;
    }

    private boolean assertPackageExist(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private PendingIntent buildVolumeSettingsPendingIntent(VolumeInfo vol) {
        Intent intent = new Intent();
        switch (vol.getType()) {
            case 0:
                intent.setClassName("com.android.settings", "com.android.settings.Settings$PublicVolumeSettingsActivity");
                break;
            case 1:
                intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeSettingsActivity");
                break;
            default:
                return null;
        }
        intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
        return PendingIntent.getActivityAsUser(this.mContext, vol.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildSnoozeIntent(String fsUuid) {
        Intent intent = new Intent("com.android.systemui.action.SNOOZE_VOLUME");
        intent.putExtra("android.os.storage.extra.FS_UUID", fsUuid);
        return PendingIntent.getBroadcastAsUser(this.mContext, fsUuid.hashCode(), intent, 268435456, UserHandle.CURRENT);
    }

    private PendingIntent buildForgetPendingIntent(VolumeRecord rec) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$PrivateVolumeForgetActivity");
        intent.putExtra("android.os.storage.extra.FS_UUID", rec.getFsUuid());
        return PendingIntent.getActivityAsUser(this.mContext, rec.getFsUuid().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMigratePendingIntent(MoveInfo move) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMigrateProgress");
        intent.putExtra("android.content.pm.extra.MOVE_ID", move.moveId);
        VolumeInfo vol = this.mStorageManager.findVolumeByQualifiedUuid(move.volumeUuid);
        if (vol != null) {
            intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.getId());
        }
        return PendingIntent.getActivityAsUser(this.mContext, move.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardMovePendingIntent(MoveInfo move) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardMoveProgress");
        intent.putExtra("android.content.pm.extra.MOVE_ID", move.moveId);
        return PendingIntent.getActivityAsUser(this.mContext, move.moveId, intent, 268435456, null, UserHandle.CURRENT);
    }

    private PendingIntent buildWizardReadyPendingIntent(DiskInfo disk) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardReady");
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.getId());
        return PendingIntent.getActivityAsUser(this.mContext, disk.getId().hashCode(), intent, 268435456, null, UserHandle.CURRENT);
    }
}
