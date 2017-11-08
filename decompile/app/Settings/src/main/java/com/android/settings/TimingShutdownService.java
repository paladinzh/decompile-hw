package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

public class TimingShutdownService extends Service {
    private static final boolean SHOW_SHUTDOWN_ANIM = SystemProperties.getBoolean("ro.config.sch_power_onoff_anim", false);
    private AlertDialog mCountdownDialog;
    private int mLeftTime = 60;
    private final Handler mUiHandler = new Handler();
    private final Runnable mUpdateCountdownRunnable = new Runnable() {
        public void run() {
            TimingShutdownService.this.updateCountdownMessage();
        }
    };
    private WakeLock mWakeLock;

    public void onCreate() {
        super.onCreate();
        if (Utils.isMonkeyRunning()) {
            stopSelf();
        }
        int themeId = getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId != 0) {
            setTheme(themeId);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return 2;
        }
        long timeDelta = Math.abs(System.currentTimeMillis() - intent.getLongExtra("next_timing_shutdown_timestamp", 0));
        if (!"settings.huawei.intent.action.TIMING_SHUTDOWN_SERVICE".equals(intent.getAction()) || timeDelta >= 1800000) {
            Log.e("TimingShutdownService", "Shutdown has been cancelled, timeDelta is out of range, timeDelta:" + timeDelta);
            stopSelf();
        } else if (Utils.isPhoneInUse(this)) {
            showCancelShutdownNotification();
            stopSelf();
        } else if (((PowerManager) getSystemService("power")).isScreenOn() || SHOW_SHUTDOWN_ANIM) {
            showShutdownCountdownDialog();
        } else {
            shutdownAndStopService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void shutdownAndStopService() {
        Intent newIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        newIntent.setFlags(268435456);
        startActivityAsUser(newIntent, new UserHandle(-3));
        stopSelf();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showShutdownCountdownDialog() {
        this.mCountdownDialog = new Builder(this).setTitle(2131627981).setMessage(getResources().getQuantityString(2131689527, this.mLeftTime, new Object[]{Integer.valueOf(this.mLeftTime)})).setCancelable(false).setPositiveButton(2131627982, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TimingShutdownService.this.shutdownAndStopService();
                TimingShutdownService.this.releaseWakeLock();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TimingShutdownService.this.mUiHandler.removeCallbacks(TimingShutdownService.this.mUpdateCountdownRunnable);
                TimingShutdownService.this.releaseWakeLock();
                TimingShutdownService.this.stopSelf();
            }
        }).create();
        this.mCountdownDialog.getWindow().setType(2003);
        LayoutParams attributes = this.mCountdownDialog.getWindow().getAttributes();
        attributes.privateFlags |= 16;
        this.mCountdownDialog.show();
        updateCountdownMessage();
        applyForWakeLock();
    }

    private void updateCountdownMessage() {
        if (this.mLeftTime <= 0) {
            shutdownAndStopService();
            return;
        }
        this.mCountdownDialog.setMessage(getResources().getQuantityString(2131689527, this.mLeftTime, new Object[]{Integer.valueOf(this.mLeftTime)}));
        synchronized (this) {
            this.mUiHandler.removeCallbacks(this.mUpdateCountdownRunnable);
            if (Utils.isPhoneInUse(this)) {
                showCancelShutdownNotification();
                stopSelf();
                return;
            }
            this.mUiHandler.postDelayed(this.mUpdateCountdownRunnable, 1000);
            this.mLeftTime--;
        }
    }

    private void showCancelShutdownNotification() {
        Resources res = getResources();
        RemoteViews views = new RemoteViews(getPackageName(), 2130968887);
        views.setImageViewResource(2131886147, 2130838434);
        views.setTextViewText(16908310, res.getString(2131627985));
        views.setTextViewText(16908290, res.getString(2131627986));
        Notification.Builder builder = new Notification.Builder(this).setSmallIcon(2130838686).setLargeIcon(((BitmapDrawable) res.getDrawable(2130838434)).getBitmap()).setTicker(res.getString(2131627986));
        builder.setContent(views);
        Notification notifcation = builder.build();
        notifcation.extras = Utils.getNotificationThemeData(2130838435, 2131886147, 1, 15);
        try {
            ((NotificationManager) getSystemService("notification")).notify(74802906, notifcation);
        } catch (Exception ex) {
            Log.e("TimingShutdownService", "showCancelShutdownNotification()-->Exception : " + ex);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (!SHOW_SHUTDOWN_ANIM) {
            releaseWakeLock();
        }
        this.mUiHandler.removeCallbacks(this.mUpdateCountdownRunnable);
        if (this.mCountdownDialog != null && this.mCountdownDialog.isShowing()) {
            this.mCountdownDialog.dismiss();
        }
        Intent intent = new Intent("settings.huawei.intent.action.DEPLOY_NEXT_SHUTDOWN");
        intent.setPackage("com.android.providers.settings");
        sendBroadcast(intent);
    }

    private void applyForWakeLock() {
        if (this.mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService("power");
            if (SHOW_SHUTDOWN_ANIM) {
                this.mWakeLock = pm.newWakeLock(268435466, "TimingShutdownService");
            } else {
                this.mWakeLock = pm.newWakeLock(1, "TimingShutdownService");
            }
        }
        if (SHOW_SHUTDOWN_ANIM) {
            this.mWakeLock.acquire(90000);
        } else {
            this.mWakeLock.acquire(60000);
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }
}
