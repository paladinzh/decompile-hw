package com.android.settings.wifi.ap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;
import java.util.List;

public class HotspotPowerManagerService extends Service {
    private AlarmManager mAlarmManager;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    HotspotPowerManagerService.this.onPowerModeSettingsChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String wifi_ap_action = "android.net.wifi.WIFI_AP_STATE_CHANGED";
            if ("android.hotspotpowermanager.turn_off_wifi_hotspot".equals(action)) {
                Log.d("HotspotPowerManagerService", "On receiving INTENT_TURN_OFF_WIFI_HOTSPOT, now turn off wifi hotspot.");
                HotspotPowerManagerService.this.mTimerStarted = false;
                HotspotPowerManagerService.this.mTurnoffHotspotIntent = null;
                HotspotPowerManagerService.this.mWifiManager.setWifiApEnabled(null, false);
                HotspotPowerManagerService.this.notifyHotspotDisabled();
                HotspotPowerManagerService.this.stopSelf();
            } else if (wifi_ap_action.equals(action)) {
                int wifiApState = intent.getIntExtra("wifi_state", -1);
                if ((wifiApState == 11 || wifiApState == 10) && HotspotPowerManagerService.this.mTurnoffHotspotIntent != null) {
                    HotspotPowerManagerService.this.mTimerStarted = false;
                    HotspotPowerManagerService.this.mAlarmManager.cancel(HotspotPowerManagerService.this.mTurnoffHotspotIntent);
                    HotspotPowerManagerService.this.mTurnoffHotspotIntent = null;
                    HotspotPowerManagerService.this.stopSelf();
                }
            }
        }
    };
    private NotificationManager mNotificationManager;
    private SettingsObserver mSettingsObserver;
    private boolean mTimerStarted = false;
    private PendingIntent mTurnoffHotspotIntent = null;
    private WifiManager mWifiManager;

    private static class SettingsObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        SettingsObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    public void onCreate() {
        super.onCreate();
        startForeground(0, null);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.mAlarmManager = (AlarmManager) getSystemService("alarm");
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 2);
        getContentResolver().registerContentObserver(Secure.getUriFor("hotspot_power_mode"), true, this.mSettingsObserver);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hotspotpowermanager.turn_off_wifi_hotspot");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(this.mIntentReceiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean wifiApEnabled = false;
        if (this.mWifiManager.getWifiApState() == 13) {
            wifiApEnabled = true;
        }
        if (wifiApEnabled) {
            this.mNotificationManager.cancelAll();
            long delayTime = getDelayTime();
            if (-1 == delayTime) {
                Log.d("HotspotPowerManagerService", "Wifi hotspot power mode is set always on, stop timer and return.");
                stopTimer();
                return super.onStartCommand(intent, flags, startId);
            }
            List<WifiApClientInfo> list = WifiApClientUtils.getInstance(this).getConnectedList();
            int apListNum = 0;
            if (list != null) {
                apListNum = list.size();
            }
            Log.d("HotspotPowerManagerService", "Got wifi ap num: " + apListNum);
            if (apListNum == 0) {
                startTimer(delayTime);
            } else {
                stopTimer();
            }
            return super.onStartCommand(intent, flags, startId);
        }
        Log.d("HotspotPowerManagerService", "Wifi hotspot is disabled, stop timer and return.");
        stopTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    private long getDelayTime() {
        int powerMode = Secure.getInt(getContentResolver(), "hotspot_power_mode", 1);
        if (2 == powerMode) {
            return -1;
        }
        if (powerMode == 0) {
            return 300000;
        }
        return 600000;
    }

    private void startTimer(long delay) {
        Log.d("HotspotPowerManagerService", "On start timer. Delay = " + delay + ", mTimerStarted = " + this.mTimerStarted);
        if (!this.mTimerStarted) {
            this.mTurnoffHotspotIntent = PendingIntent.getBroadcast(this, 0, new Intent("android.hotspotpowermanager.turn_off_wifi_hotspot"), 0);
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + delay, this.mTurnoffHotspotIntent);
            this.mTimerStarted = true;
        }
    }

    private void stopTimer() {
        Log.d("HotspotPowerManagerService", "On stop timer. mTimerStarted = " + this.mTimerStarted);
        if (this.mTimerStarted) {
            if (this.mTurnoffHotspotIntent != null) {
                this.mAlarmManager.cancel(this.mTurnoffHotspotIntent);
                this.mTurnoffHotspotIntent = null;
            }
            this.mTimerStarted = false;
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onDestroy() {
        stopForeground(true);
        if (this.mTurnoffHotspotIntent != null) {
            this.mAlarmManager.cancel(this.mTurnoffHotspotIntent);
            this.mTurnoffHotspotIntent = null;
            this.mTimerStarted = false;
        }
        unregisterReceiver(this.mIntentReceiver);
    }

    private void notifyHotspotDisabled() {
        Intent intent = new Intent("android.settings.WIFI_AP_SETTINGS");
        intent.setFlags(805306368);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification(2130838175, getString(2131629133), 0);
        notification.largeIcon = loadBitmap(2130838307);
        notification.flags |= 16;
        notification.setLatestEventInfo(this, getString(2131629131), getString(2131629132), pendingIntent);
        this.mNotificationManager.notify(0, notification);
    }

    private Bitmap loadBitmap(int id) {
        return Bitmap.createBitmap(((BitmapDrawable) getResources().getDrawable(id)).getBitmap());
    }

    private void onPowerModeSettingsChanged() {
        boolean wifiApEnabled = false;
        Log.d("HotspotPowerManagerService", "On power mode settings changed.");
        if (this.mWifiManager.getWifiApState() == 13) {
            wifiApEnabled = true;
        }
        if (wifiApEnabled) {
            this.mNotificationManager.cancelAll();
            long delayTime = getDelayTime();
            if (-1 == delayTime) {
                Log.d("HotspotPowerManagerService", "Wifi hotspot power mode is set always on, stop timer and return.");
                stopTimer();
                return;
            }
            List<WifiApClientInfo> list = WifiApClientUtils.getInstance(this).getConnectedList();
            int apListNum = 0;
            if (list != null) {
                apListNum = list.size();
            }
            Log.d("HotspotPowerManagerService", "Got wifi ap num: " + apListNum);
            if (apListNum == 0) {
                stopTimer();
                startTimer(delayTime);
            } else {
                stopTimer();
            }
            return;
        }
        Log.d("HotspotPowerManagerService", "Wifi hotspot is disabled, stop timer and return.");
        stopTimer();
    }
}
