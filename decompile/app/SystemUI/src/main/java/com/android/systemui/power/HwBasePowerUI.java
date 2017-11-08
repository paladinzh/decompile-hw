package com.android.systemui.power;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import java.util.Locale;

public class HwBasePowerUI extends SystemUI {
    private static boolean mIsfactory = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private int mAlertDialogThemeID;
    private Handler mHandler = new Handler();
    private boolean mIsShutdownWarningShown = false;
    private MediaPlayer mMediaPlayer;
    private boolean mShowTemperatureWarning = true;
    Runnable mShutDownRunnable = new Runnable() {
        public void run() {
            if (HwBasePowerUI.this.mShutdownDialog != null) {
                HwBasePowerUI.this.mShutdownDialog.dismiss();
            }
            HwLog.i("HwPowerUI", "device is shutdown automatically because its battery is low!");
            Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
            shutdown.setFlags(268435456);
            try {
                HwBasePowerUI.this.mContext.startActivity(shutdown);
            } catch (ActivityNotFoundException e) {
                HwLog.e("HwPowerUI", "mShutDownRunnable start Intent.ACTION_REQUEST_SHUTDOWN Activity not found!");
            }
        }
    };
    private AlertDialog mShutdownDialog = null;
    Runnable mTemperatureTimer = new Runnable() {
        public void run() {
            HwBasePowerUI.this.mShowTemperatureWarning = true;
        }
    };
    private OnDismissListener mTemperatureWarningDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            HwBasePowerUI.this.mHandler.postDelayed(HwBasePowerUI.this.mTemperatureTimer, 30000);
        }
    };
    private WakeLock mWakeLock = null;

    public void start() {
        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeID != 0) {
            this.mContext.setTheme(themeID);
        }
        this.mAlertDialogThemeID = this.mContext.getResources().getIdentifier("androidhwext.R.style.Theme_Emui_Dialog_Alert", null, null);
    }

    protected static boolean isIgnore(int batteryLevel) {
        if (!"true".equals(SystemProperties.get("ro.config.hw_quickpoweron"))) {
            return false;
        }
        String strPower = SystemProperties.get("persist.sys.quickpoweron", "0");
        if ((!"startshutdown".equalsIgnoreCase(strPower) && !"shutdown".equalsIgnoreCase(strPower)) || batteryLevel <= 2) {
            return false;
        }
        HwLog.i("HwPowerUI", "ignore Intent.ACTION_BATTERY_CHANGED when fake shut down");
        return true;
    }

    protected void processHuaweiBatteryStatusChange(boolean plugged, int batteryLevel, Intent intent) {
        HwLog.i("HwPowerUI", "processLowerBatteryLevel plugged:" + plugged + " batteryLevel:" + batteryLevel + " intent:" + Proguard.get(intent));
        processShutdownDialog(plugged, batteryLevel);
        doTemperatueWarning(intent);
    }

    private void processShutdownDialog(boolean plugged, int batteryLevel) {
        if (plugged || batteryLevel > 2) {
            releaseWakeLock();
            this.mHandler.removeCallbacks(this.mShutDownRunnable);
            if (this.mShutdownDialog != null) {
                this.mShutdownDialog.dismiss();
            }
            this.mIsShutdownWarningShown = false;
        } else if (!this.mIsShutdownWarningShown) {
            this.mHandler.postDelayed(this.mShutDownRunnable, 30000);
            acquireWakeLock();
            showShutdownWarning();
        }
    }

    private void acquireWakeLock() {
        if (this.mWakeLock == null) {
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            if (pm != null) {
                this.mWakeLock = pm.newWakeLock(536870913, getClass().getCanonicalName());
                if (this.mWakeLock != null) {
                    this.mWakeLock.acquire();
                }
            }
        }
    }

    private void showShutdownWarning() {
        Builder b2 = new Builder(this.mContext, this.mAlertDialogThemeID);
        b2.setTitle(17039380);
        b2.setIcon(17301543);
        b2.setMessage(String.format(Locale.getDefault(), this.mContext.getString(R.string.battery_shutdown_title_ex), new Object[]{Integer.valueOf(30)}));
        AlertDialog d2 = b2.create();
        d2.getWindow().setType(2009);
        d2.getWindow().addFlags(128);
        d2.show();
        this.mShutdownDialog = d2;
        String LOW_BATTERY_MEDIA_PATH = "/system/media/audio/ui/LowBattery.ogg";
        try {
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            this.mMediaPlayer.setDataSource(LOW_BATTERY_MEDIA_PATH);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();
        } catch (Exception e) {
            HwLog.e("HwPowerUI", "error: " + e.getMessage(), e);
        }
        this.mIsShutdownWarningShown = true;
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void doTemperatueWarning(Intent intent) {
        if (mIsfactory) {
            HwLog.i("HwPowerUI", "doTemperatueWarning mIsfactory is true ,and return!");
        } else if (this.mShowTemperatureWarning) {
            int health = intent.getIntExtra("health", 1);
            HwLog.i("HwPowerUI", "doTemperatueWarning, health:" + health);
            if (7 == health) {
                showTemperatureWarning(R.string.temperature_low_warning);
            } else if (3 == health) {
                showTemperatureWarning(R.string.temperature_high_warning);
            }
        } else {
            HwLog.e("HwPowerUI", "doTemperatueWarning ,do not show warning dialog now.");
        }
    }

    private void showTemperatureWarning(int msgId) {
        Builder b3 = new Builder(this.mContext, this.mAlertDialogThemeID);
        b3.setTitle(17039380);
        b3.setIcon(17301543);
        b3.setMessage(msgId);
        b3.setPositiveButton(17039370, null);
        AlertDialog d3 = b3.create();
        d3.setOnDismissListener(this.mTemperatureWarningDismissListener);
        d3.getWindow().setType(2003);
        d3.show();
        this.mShowTemperatureWarning = false;
    }
}
