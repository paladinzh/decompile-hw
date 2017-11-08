package com.android.server.wifi;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

final class WifiNotificationController {
    private static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND";
    private static final String ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE = "com.huawei.wifipro.action.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE";
    private static final boolean HWFLOW;
    private static final int ICON_NETWORKS_AVAILABLE = 17303218;
    private static final int NUM_SCANS_BEFORE_ACTUALLY_SCANNING = 3;
    private static final String TAG = "WifiNotificationController";
    private final long NOTIFICATION_REPEAT_DELAY_MS;
    private final Context mContext;
    private DetailedState mDetailedState = DetailedState.IDLE;
    private FrameworkFacade mFrameworkFacade;
    private boolean mIsAccessAPFound;
    private NetworkInfo mNetworkInfo;
    private Builder mNotificationBuilder;
    private boolean mNotificationEnabled;
    private NotificationEnabledSettingObserver mNotificationEnabledSettingObserver;
    private long mNotificationRepeatTime;
    private boolean mNotificationShown;
    private int mNumScansSinceNetworkStateChange;
    private volatile int mWifiState = 4;
    private final WifiStateMachine mWifiStateMachine;

    private class NotificationEnabledSettingObserver extends ContentObserver {
        public NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            WifiNotificationController.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_networks_available_notification_on"), true, this);
            synchronized (WifiNotificationController.this) {
                WifiNotificationController.this.mNotificationEnabled = getValue();
            }
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (WifiNotificationController.this) {
                WifiNotificationController.this.mNotificationEnabled = getValue();
                WifiNotificationController.this.resetNotification();
            }
        }

        private boolean getValue() {
            return WifiNotificationController.this.mFrameworkFacade.getIntegerSetting(WifiNotificationController.this.mContext, "wifi_networks_available_notification_on", 1) == 1;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    WifiNotificationController(Context context, Looper looper, WifiStateMachine wsm, FrameworkFacade framework, Builder builder) {
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mFrameworkFacade = framework;
        this.mNotificationBuilder = builder;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        filter.addAction(ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND);
        filter.addAction(ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
            final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

            private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
                if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                    return -android-net-NetworkInfo$DetailedStateSwitchesValues;
                }
                int[] iArr = new int[DetailedState.values().length];
                try {
                    iArr[DetailedState.AUTHENTICATING.ordinal()] = 4;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DetailedState.BLOCKED.ordinal()] = 5;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 1;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DetailedState.CONNECTED.ordinal()] = 2;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DetailedState.CONNECTING.ordinal()] = 6;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[DetailedState.DISCONNECTED.ordinal()] = 3;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[DetailedState.FAILED.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[DetailedState.IDLE.ordinal()] = 9;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
                } catch (NoSuchFieldError e10) {
                }
                try {
                    iArr[DetailedState.SCANNING.ordinal()] = 11;
                } catch (NoSuchFieldError e11) {
                }
                try {
                    iArr[DetailedState.SUSPENDED.ordinal()] = 12;
                } catch (NoSuchFieldError e12) {
                }
                try {
                    iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
                } catch (NoSuchFieldError e13) {
                }
                -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
                return iArr;
            }

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    WifiNotificationController.this.mWifiState = intent.getIntExtra("wifi_state", 4);
                    WifiNotificationController.this.resetNotification();
                } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                    WifiNotificationController.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiNotificationController.this.mNetworkInfo != null) {
                        DetailedState detailedState = WifiNotificationController.this.mNetworkInfo.getDetailedState();
                        if (!(detailedState == DetailedState.SCANNING || detailedState == WifiNotificationController.this.mDetailedState)) {
                            WifiNotificationController.this.mDetailedState = detailedState;
                            switch (AnonymousClass1.-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[WifiNotificationController.this.mDetailedState.ordinal()]) {
                                case 3:
                                    if (WifiNotificationController.this.mWifiStateMachine.isWifiProEnabled() && WifiNotificationController.this.mIsAccessAPFound) {
                                        break;
                                    }
                                case 1:
                                case 2:
                                    WifiNotificationController.this.resetNotification();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                    if (!WifiNotificationController.this.mWifiStateMachine.isWifiProEnabled()) {
                        WifiNotificationController.this.checkAndSetNotification(WifiNotificationController.this.mNetworkInfo, WifiNotificationController.this.mWifiStateMachine.syncGetScanResultsList());
                    }
                } else if (intent.getAction().equals(WifiNotificationController.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND)) {
                    Log.d(WifiNotificationController.TAG, "find access ap, mNotificationShown = " + WifiNotificationController.this.mNotificationShown);
                    if (WifiNotificationController.this.mNotificationEnabled && !WifiNotificationController.this.mNotificationShown) {
                        WifiNotificationController.this.mIsAccessAPFound = true;
                        WifiNotificationController.this.setNotificationVisible(false, 1, false, 0);
                    }
                } else if (intent.getAction().equals(WifiNotificationController.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE)) {
                    Log.d(WifiNotificationController.TAG, "access ap , mNotificationShown = " + WifiNotificationController.this.mNotificationShown + ", mIsAccessAPFound = " + WifiNotificationController.this.mIsAccessAPFound);
                    if (WifiNotificationController.this.mIsAccessAPFound && WifiNotificationController.this.mNotificationShown) {
                        WifiNotificationController.this.mIsAccessAPFound = false;
                        WifiNotificationController.this.resetNotification();
                    }
                }
            }
        }, filter);
        this.NOTIFICATION_REPEAT_DELAY_MS = ((long) this.mFrameworkFacade.getIntegerSetting(context, "wifi_networks_available_repeat_delay", 900)) * 1000;
        this.mNotificationEnabledSettingObserver = new NotificationEnabledSettingObserver(new Handler(looper));
        this.mNotificationEnabledSettingObserver.register();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void checkAndSetNotification(NetworkInfo networkInfo, List<ScanResult> scanResults) {
        if (!this.mNotificationEnabled) {
            return;
        }
        if (this.mWifiState == 3) {
            State state = State.DISCONNECTED;
            if (networkInfo != null) {
                state = networkInfo.getState();
            }
            if (HWFLOW) {
                Log.i(TAG, "checkAndSetNotification, state:" + state);
            }
            if (state == State.DISCONNECTED || state == State.UNKNOWN) {
                if (scanResults != null) {
                    int numOpenNetworks = 0;
                    for (int i = scanResults.size() - 1; i >= 0; i--) {
                        ScanResult scanResult = (ScanResult) scanResults.get(i);
                        if (scanResult.capabilities != null && scanResult.capabilities.equals("[ESS]")) {
                            numOpenNetworks++;
                        }
                    }
                    Log.d(TAG, "Open network num:" + numOpenNetworks);
                    if (numOpenNetworks > 0) {
                        int i2 = this.mNumScansSinceNetworkStateChange + 1;
                        this.mNumScansSinceNetworkStateChange = i2;
                        if (i2 >= 3) {
                            setNotificationVisible(false, numOpenNetworks, false, 0);
                        }
                    }
                } else {
                    Log.d(TAG, "scanResults is null");
                }
            }
            setNotificationVisible(false, 0, false, 0);
        }
    }

    private synchronized void resetNotification() {
        this.mNotificationRepeatTime = 0;
        this.mNumScansSinceNetworkStateChange = 0;
        setNotificationVisible(false, 0, false, 0);
    }

    private void setNotificationVisible(boolean visible, int numNetworks, boolean force, int delay) {
        if (visible || this.mNotificationShown || force) {
            NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (!visible) {
                notificationManager.cancelAsUser(null, ICON_NETWORKS_AVAILABLE, UserHandle.ALL);
            } else if (System.currentTimeMillis() >= this.mNotificationRepeatTime) {
                if (this.mNotificationBuilder == null) {
                    this.mNotificationBuilder = new Builder(this.mContext).setWhen(0).setSmallIcon(ICON_NETWORKS_AVAILABLE).setAutoCancel(true).setContentIntent(TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(new Intent("android.net.wifi.PICK_WIFI_NETWORK")).getPendingIntent(0, 0, null, UserHandle.CURRENT)).setColor(this.mContext.getResources().getColor(17170519));
                    Bitmap bmp = BitmapFactory.decodeResource(this.mContext.getResources(), 33751594);
                    if (bmp != null) {
                        this.mNotificationBuilder.setLargeIcon(bmp);
                    }
                }
                CharSequence title = this.mContext.getResources().getQuantityText(18087942, numNetworks);
                CharSequence details = this.mContext.getResources().getQuantityText(18087943, numNetworks);
                this.mNotificationBuilder.setTicker(title);
                this.mNotificationBuilder.setContentTitle(title);
                this.mNotificationBuilder.setContentText(details);
                this.mNotificationRepeatTime = System.currentTimeMillis() + this.NOTIFICATION_REPEAT_DELAY_MS;
                notificationManager.notifyAsUser(null, ICON_NETWORKS_AVAILABLE, this.mNotificationBuilder.build(), UserHandle.ALL);
            } else {
                return;
            }
            this.mNotificationShown = visible;
        }
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mNotificationEnabled " + this.mNotificationEnabled);
        pw.println("mNotificationRepeatTime " + this.mNotificationRepeatTime);
        pw.println("mNotificationShown " + this.mNotificationShown);
        pw.println("mNumScansSinceNetworkStateChange " + this.mNumScansSinceNetworkStateChange);
    }
}
