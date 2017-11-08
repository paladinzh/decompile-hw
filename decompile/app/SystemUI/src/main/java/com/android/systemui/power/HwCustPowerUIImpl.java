package com.android.systemui.power;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.android.systemui.R;

public class HwCustPowerUIImpl extends HwCustPowerUI {
    private static final boolean IS_FULL_POWER_NOTIFY = SystemProperties.getBoolean("ro.config.hw_fullpowernotify", false);
    private static final boolean SHOW_CHARGE_DIALOG_ENABLE = SystemProperties.getBoolean("ro.config.show_charge_dialog", false);
    private static final boolean USB_NOTIFICATION_SOUND_ENABLE = SystemProperties.getBoolean("ro.config.usbnotifnsnd", false);
    private final int BATTERY_SCALE = 100;
    private String TAG = "HwCustPowerUIImpl";
    private boolean isShowNotifiyFlag = false;
    private int mAlertDialogThemeID = 0;
    private int mBatteryLevel = 100;
    private AlertDialog mChargerDialog;
    private AlertDialog mDisChargerDialog;
    private Notification mFullBatteryNotification = null;
    private NotificationManager mNotificationManager = null;
    Ringtone mRingTone = null;
    Uri mToneUri = null;
    private Vibrator mVibrator;

    public HwCustPowerUIImpl(Context context) {
        super(context);
        init();
    }

    public void addMoreRegAction(IntentFilter filter) {
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
    }

    public void handleCustIntent(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
            boolean plugged = intent.getIntExtra("plugged", 1) != 0;
            int batteryLevel = intent.getIntExtra("level", 100);
            this.mBatteryLevel = batteryLevel;
            if (IS_FULL_POWER_NOTIFY) {
                int maxLevel = intent.getIntExtra("scale", 100);
                Log.d(this.TAG, "FULL_POWER_NOTIFY: batteryLevel is " + batteryLevel + " plugged is " + plugged + " maxLevel is " + maxLevel);
                if (!this.isShowNotifiyFlag && plugged && batteryLevel >= maxLevel) {
                    this.mNotificationManager.notify(R.string.full_battery_notofication_title, this.mFullBatteryNotification);
                    this.isShowNotifiyFlag = true;
                } else if (!this.isShowNotifiyFlag) {
                } else {
                    if (!plugged || batteryLevel < maxLevel) {
                        this.mNotificationManager.cancel(R.string.full_battery_notofication_title);
                        this.isShowNotifiyFlag = false;
                    }
                }
            }
        } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
            dismissDisconnectDialog();
            if (SHOW_CHARGE_DIALOG_ENABLE) {
                try {
                    if (Secure.getInt(this.mContext.getContentResolver(), "accessibility_enabled") == 1) {
                        showChargerConnectedDialog();
                    }
                } catch (SettingNotFoundException e) {
                    Log.e(this.TAG, "error: " + e.getMessage(), e);
                }
            }
        } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
            if (USB_NOTIFICATION_SOUND_ENABLE) {
                playUsbNotificationSound();
            }
            dismissConnectDialog();
            if (SHOW_CHARGE_DIALOG_ENABLE) {
                try {
                    if (Secure.getInt(this.mContext.getContentResolver(), "accessibility_enabled") == 1) {
                        showChargerDisconnectedDialog();
                    }
                } catch (SettingNotFoundException e2) {
                    Log.e(this.TAG, "error: " + e2.getMessage(), e2);
                }
            }
        }
    }

    private void playUsbNotificationSound() {
        this.mToneUri = Uri.parse("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.powerconnected);
        if (this.mToneUri != null) {
            this.mRingTone = RingtoneManager.getRingtone(this.mContext, this.mToneUri);
            if (this.mRingTone != null) {
                this.mRingTone.play();
            }
        }
    }

    private void init() {
        if (IS_FULL_POWER_NOTIFY) {
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            Bitmap bmp = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.ic_charge_done_notification_large);
            Builder builder = new Builder(this.mContext);
            builder.setSmallIcon(R.drawable.ic_charge_done_notification).setContentTitle(this.mContext.getText(R.string.full_battery_notofication_title)).setTicker(this.mContext.getText(R.string.full_battery_notofication_title)).setContentText(this.mContext.getText(R.string.full_battery_notofication_content)).setSound(RingtoneManager.getActualDefaultRingtoneUri(this.mContext, 2)).setPriority(1).setLargeIcon(bmp);
            this.mFullBatteryNotification = builder.build();
            this.mFullBatteryNotification.flags = 8;
            Notification notification = this.mFullBatteryNotification;
            notification.defaults |= 2;
        }
    }

    private void showChargerConnectedDialog() {
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mVibrator.vibrate(100);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, this.mAlertDialogThemeID);
        builder.setCancelable(true);
        builder.setMessage(R.string.charger_connected);
        builder.setIconAttribute(16843605);
        builder.setPositiveButton(17039370, null);
        final Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
        intent.setFlags(1484783616);
        if (intent.resolveActivity(this.mContext.getPackageManager()) != null) {
            builder.setNegativeButton(33685629, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HwCustPowerUIImpl.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwCustPowerUIImpl.this.mChargerDialog = null;
            }
        });
        dialog.getWindow().setType(2003);
        LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.privateFlags |= 16;
        dialog.show();
        this.mChargerDialog = dialog;
    }

    private void showChargerDisconnectedDialog() {
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mVibrator.vibrate(100);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, this.mAlertDialogThemeID);
        builder.setCancelable(true);
        builder.setMessage(this.mContext.getString(R.string.charger_disconnected, new Object[]{Integer.valueOf(this.mBatteryLevel)}));
        builder.setIconAttribute(16843605);
        builder.setPositiveButton(17039370, null);
        final Intent intent = new Intent("android.intent.action.POWER_USAGE_SUMMARY");
        intent.setFlags(1484783616);
        if (intent.resolveActivity(this.mContext.getPackageManager()) != null) {
            builder.setNegativeButton(33685629, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    HwCustPowerUIImpl.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwCustPowerUIImpl.this.mDisChargerDialog = null;
            }
        });
        dialog.getWindow().setType(2003);
        LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.privateFlags |= 16;
        dialog.show();
        this.mDisChargerDialog = dialog;
    }

    private void dismissConnectDialog() {
        if (this.mChargerDialog != null) {
            this.mChargerDialog.dismiss();
        }
    }

    private void dismissDisconnectDialog() {
        if (this.mDisChargerDialog != null) {
            this.mDisChargerDialog.dismiss();
        }
    }
}
