package com.huawei.systemmanager.power.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.System;
import com.huawei.android.thememanager.aidl.IApplyTheme;
import com.huawei.android.thememanager.aidl.IApplyTheme.Stub;
import com.huawei.android.thememanager.aidl.IRequestCallBack;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.util.HwLog;

public class DarkThemeChanageService extends Service {
    private static int CHANGE_THEME_REQUEST = 0;
    public static final String CHANGE_UI_ACTION = "changeUiAction";
    public static final String DARK_THEME_IS_CHECK_NAME = "darkThemeIsCheck";
    public static final String DB_DARK_THEME = "power_save_theme_status";
    private static int FAIL_CHANGE_REQUEST = 1;
    public static final int FAIL_CHANGE_THEME = 1;
    public static final int FAIL_LACK_THEME = 2;
    private static final int MSG_DELAY_CLOSE_AIDL_CONNECTION = 1;
    private static final int MSG_DELAY_FAIL_OUT_OF_TIME = 2;
    private static final int OUT_OF_RECEVICE_CALLBACK_TIME = 30;
    private static int SUCCESS_CHANGE_REQUEST = 0;
    public static final int SUCCESS_CHANGE_THEME = 0;
    private static final String TAG = "DarkThemeChanageService";
    private static final int TIME_TO_CLOSE_CONNECTION = 60;
    private IApplyTheme applyThemeService;
    private boolean bindThemeServiceState;
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            DarkThemeChanageService.this.applyThemeService = Stub.asInterface(service);
            DarkThemeChanageService.this.mHandler.removeMessages(1);
            DarkThemeChanageService.this.mHandler.sendMessageDelayed(DarkThemeChanageService.this.mHandler.obtainMessage(1, null), 60000);
            HwLog.i(DarkThemeChanageService.TAG, "IApplyTheme is connected ");
            DarkThemeChanageService.this.requestChangeAfterChange();
        }

        public void onServiceDisconnected(ComponentName name) {
            DarkThemeChanageService.this.applyThemeService = null;
        }
    };
    private boolean darkThemeIsCheck;
    private Context mContext;
    private WorkHandler mHandler;
    private HandlerThread mHandlerThread;
    private IRequestCallBack themeCallback = new IRequestCallBack.Stub() {
        public void onRequestResult(int requestCode, int resultCode, Intent data) {
            DarkThemeChanageService.this.mHandler.removeMessages(2);
            if (requestCode != DarkThemeChanageService.CHANGE_THEME_REQUEST) {
                HwLog.i(DarkThemeChanageService.TAG, "Request Code dosen't match original code");
                return;
            }
            if (resultCode == DarkThemeChanageService.SUCCESS_CHANGE_REQUEST) {
                HwLog.i(DarkThemeChanageService.TAG, "Get the callback of success");
                DarkThemeChanageService.this.successChangeTheme();
            } else {
                HwLog.i(DarkThemeChanageService.TAG, "Get the callback of fail");
                DarkThemeChanageService.this.failChangeTheme(true);
            }
        }
    };

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    DarkThemeChanageService.this.unBindRemoteService();
                    return;
                case 2:
                    if (!SharePrefWrapper.getPrefValue(DarkThemeChanageService.this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, true)) {
                        DarkThemeChanageService.this.failChangeTheme(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = GlobalContext.getContext();
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new WorkHandler(this.mHandlerThread.getLooper());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean booleanExtra;
        if (intent != null) {
            booleanExtra = intent.getBooleanExtra(DARK_THEME_IS_CHECK_NAME, false);
        } else {
            booleanExtra = false;
        }
        this.darkThemeIsCheck = booleanExtra;
        changeSystemSettings();
        SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, false);
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, null), 30000);
        if (this.applyThemeService == null) {
            bindRemoteService();
            HwLog.i(TAG, "connect the aidl service");
        } else {
            requestChangeAfterChange();
        }
        return 2;
    }

    public void onDestroy() {
        unBindRemoteService();
        SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, true);
        super.onDestroy();
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setAction(ActionConst.INTENT_CHANGE_POWER_SAVE_THEME);
        intent.setPackage("com.huawei.android.thememanager");
        HwLog.i(TAG, "Bind service");
        this.bindThemeServiceState = this.mContext.bindService(intent, this.connection, 1);
    }

    private void unBindRemoteService() {
        if (this.applyThemeService != null && this.bindThemeServiceState) {
            HwLog.i(TAG, "unbind remote service");
            try {
                this.mContext.unbindService(this.connection);
                this.bindThemeServiceState = false;
            } catch (IllegalArgumentException e) {
                HwLog.i(TAG, "Fail to unbind service");
            }
            this.applyThemeService = null;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
        }
    }

    private void requestChangeAfterChange() {
        try {
            Intent remoteIntent = new Intent();
            remoteIntent.putExtra("packageName", getPackageName());
            if (this.applyThemeService == null) {
                HwLog.i(TAG, "Fail to connect the aidl service");
                failChangeTheme(false);
                return;
            }
            this.applyThemeService.requestSwitchTheme(remoteIntent, CHANGE_THEME_REQUEST, this.themeCallback);
        } catch (RemoteException e) {
            HwLog.e(TAG, "RemoteException >> " + e);
            failChangeTheme(false);
            unBindRemoteService();
        }
    }

    private void failChangeTheme(boolean isLackTheme) {
        boolean z;
        if (this.darkThemeIsCheck) {
            z = false;
        } else {
            z = true;
        }
        this.darkThemeIsCheck = z;
        changeSystemSettings();
        if (isLackTheme) {
            sendBroadCastToUI(2);
        } else {
            sendBroadCastToUI(1);
        }
    }

    private void successChangeTheme() {
        sendBroadCastToUI(0);
    }

    private void sendBroadCastToUI(int actionBundle) {
        SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.DARK_THEME_SWITCH_KEY, true);
        Intent intent = new Intent(ActionConst.INTENT_CHANGE_POWER_SAVE_THEME_SELF);
        Bundle bundle = new Bundle();
        bundle.putInt(CHANGE_UI_ACTION, actionBundle);
        intent.putExtras(bundle);
        sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    private void changeSystemSettings() {
        System.putIntForUser(this.mContext.getContentResolver(), DB_DARK_THEME, this.darkThemeIsCheck ? 1 : 0, -2);
    }
}
