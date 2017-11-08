package com.android.systemui.power;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Slog;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.power.PowerUI.WarningsUI;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.io.PrintWriter;

public class PowerNotificationWarnings implements WarningsUI {
    private static final AudioAttributes AUDIO_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static final boolean DEBUG = PowerUI.DEBUG;
    private static final String[] SHOWING_STRINGS = new String[]{"SHOWING_NOTHING", "SHOWING_WARNING", "SHOWING_SAVER", "SHOWING_INVALID_CHARGER"};
    private int mBatteryLevel;
    private int mBucket;
    private long mBucketDroppedNegativeTimeMs;
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private boolean mInvalidCharger;
    private final NotificationManager mNoMan;
    private final Intent mOpenBatterySettings = settings("android.intent.action.POWER_USAGE_SUMMARY");
    private boolean mPlaySound;
    private final PowerManager mPowerMan;
    private final Receiver mReceiver = new Receiver();
    private SystemUIDialog mSaverConfirmation;
    private long mScreenOffTime;
    private int mShowing;
    private final OnClickListener mStartSaverMode = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    PowerNotificationWarnings.this.setSaverMode(true);
                }
            });
        }
    };
    private boolean mWarning;

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("PNW.batterySettings");
            filter.addAction("PNW.startSaver");
            filter.addAction("PNW.dismissedWarning");
            PowerNotificationWarnings.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, filter, "android.permission.STATUS_BAR_SERVICE", PowerNotificationWarnings.this.mHandler);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.i("PowerUI.Notification", "Received " + action);
            if (action.equals("PNW.batterySettings")) {
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
                PowerNotificationWarnings.this.mContext.startActivityAsUser(PowerNotificationWarnings.this.mOpenBatterySettings, UserHandle.CURRENT);
            } else if (action.equals("PNW.startSaver")) {
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
                PowerNotificationWarnings.this.showStartSaverConfirmation();
            } else if (action.equals("PNW.dismissedWarning")) {
                PowerNotificationWarnings.this.dismissLowBatteryWarning();
            }
        }
    }

    public PowerNotificationWarnings(Context context, PhoneStatusBar phoneStatusBar) {
        this.mContext = context;
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mPowerMan = (PowerManager) context.getSystemService("power");
        this.mReceiver.init();
    }

    public void dump(PrintWriter pw) {
        String str = null;
        pw.print("mWarning=");
        pw.println(this.mWarning);
        pw.print("mPlaySound=");
        pw.println(this.mPlaySound);
        pw.print("mInvalidCharger=");
        pw.println(this.mInvalidCharger);
        pw.print("mShowing=");
        pw.println(SHOWING_STRINGS[this.mShowing]);
        pw.print("mSaverConfirmation=");
        if (this.mSaverConfirmation != null) {
            str = "not null";
        }
        pw.println(str);
    }

    public void update(int batteryLevel, int bucket, long screenOffTime) {
        this.mBatteryLevel = batteryLevel;
        if (bucket >= 0) {
            this.mBucketDroppedNegativeTimeMs = 0;
        } else if (bucket < this.mBucket) {
            this.mBucketDroppedNegativeTimeMs = System.currentTimeMillis();
        }
        this.mBucket = bucket;
        this.mScreenOffTime = screenOffTime;
    }

    private void updateNotification() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "updateNotification mWarning=" + this.mWarning + " mPlaySound=" + this.mPlaySound + " mInvalidCharger=" + this.mInvalidCharger);
        }
        if (this.mInvalidCharger) {
            showInvalidChargerNotification();
            this.mShowing = 3;
        } else if (this.mWarning) {
            this.mShowing = 1;
        } else {
            this.mShowing = 0;
        }
    }

    private void showInvalidChargerNotification() {
        Notification.Builder nb = new Notification.Builder(this.mContext).setSmallIcon(R.drawable.ic_power_low).setWhen(0).setShowWhen(false).setOngoing(true).setContentTitle(this.mContext.getString(R.string.invalid_charger_title)).setContentText(this.mContext.getString(R.string.invalid_charger_text)).setPriority(2).setVisibility(1).setColor(this.mContext.getColor(17170519));
        SystemUI.overrideNotificationAppName(this.mContext, nb);
        this.mNoMan.notifyAsUser("low_battery", R.id.notification_power, nb.build(), UserHandle.ALL);
    }

    private static Intent settings(String action) {
        return new Intent(action).setFlags(1551892480);
    }

    public boolean isInvalidChargerWarningShowing() {
        return this.mInvalidCharger;
    }

    public void updateLowBatteryWarning() {
        updateNotification();
    }

    public void dismissLowBatteryWarning() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "dismissing low battery warning: level=" + this.mBatteryLevel);
        }
        dismissLowBatteryNotification();
    }

    private void dismissLowBatteryNotification() {
        if (this.mWarning) {
            Slog.i("PowerUI.Notification", "dismissing low battery notification");
        }
        this.mWarning = false;
        updateNotification();
    }

    public void showLowBatteryWarning(boolean playSound) {
        Slog.i("PowerUI.Notification", "show low battery warning: level=" + this.mBatteryLevel + " [" + this.mBucket + "] playSound=" + playSound);
        this.mPlaySound = playSound;
        this.mWarning = true;
        updateNotification();
    }

    public void dismissInvalidChargerWarning() {
        dismissInvalidChargerNotification();
    }

    private void dismissInvalidChargerNotification() {
        if (this.mInvalidCharger) {
            Slog.i("PowerUI.Notification", "dismissing invalid charger notification");
        }
        this.mInvalidCharger = false;
        updateNotification();
    }

    public void showInvalidChargerWarning() {
        this.mInvalidCharger = true;
        updateNotification();
    }

    public void userSwitched() {
        updateNotification();
    }

    private void showStartSaverConfirmation() {
        if (this.mSaverConfirmation == null) {
            SystemUIDialog d = new SystemUIDialog(this.mContext);
            d.setTitle(R.string.battery_saver_confirmation_title);
            d.setMessage(17040803);
            d.setNegativeButton(17039360, null);
            d.setPositiveButton(R.string.battery_saver_confirmation_ok, this.mStartSaverMode);
            d.setShowForAllUsers(true);
            d.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    PowerNotificationWarnings.this.mSaverConfirmation = null;
                }
            });
            d.show();
            this.mSaverConfirmation = d;
        }
    }

    private void setSaverMode(boolean mode) {
        this.mPowerMan.setPowerSaveMode(mode);
    }
}
