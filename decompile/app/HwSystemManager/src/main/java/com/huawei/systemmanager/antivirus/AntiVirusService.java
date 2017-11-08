package com.huawei.systemmanager.antivirus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.cache.VirusAppsManager;
import com.huawei.systemmanager.antivirus.cache.VirusBgDataSyncManager;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.notify.TimerRemindNotify;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.Serializable;

public class AntiVirusService extends Service {
    public static final String TAG = "AntiVirusService";
    private IAntiVirusEngine mAntiVirusEngine = null;
    private Context mContext = null;
    private VirusBgDataSyncManager mDataSyncManager = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String message = "";
            switch (msg.what) {
                case 3:
                    AntiVirusService.this.updateVirusLib();
                    break;
                case 9:
                    AntiVirusTools.setAutoUpdateStamp(AntiVirusService.this.mContext, System.currentTimeMillis());
                    AntiVirusService.this.mIsSussAutoUpdate = true;
                    if (AntiVirusService.this.mDataSyncManager != null) {
                        AntiVirusService.this.mDataSyncManager.request(AntiVirusService.this.mContext);
                        break;
                    }
                    break;
                case 10:
                    AntiVirusService.this.mIsSussAutoUpdate = false;
                    break;
                case 11:
                    AntiVirusService.this.autoUpdateVirusLib();
                    break;
                case 20:
                    AntiVirusService.this.showToast(AntiVirusService.this.getResources().getString(R.string.msg_url_check_result_harm_Toast), 3000);
                    break;
                case 21:
                    AntiVirusService.this.showToast(AntiVirusService.this.getResources().getString(R.string.msg_url_check_result_shadiness), 3000);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean mIsSussAutoUpdate = true;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    if (!AntiVirusService.this.mIsSussAutoUpdate) {
                        long lastTime = AntiVirusTools.getAutoUpdateStamp(context);
                        if (System.currentTimeMillis() - lastTime > ((long) AntiVirusTools.getUpdateRate(AntiVirusService.this.mContext)) * 86400000) {
                            AntiVirusService.this.mHandler.sendEmptyMessage(11);
                        }
                    }
                } else if (AntiVirusTools.ACTION_UPDATE_VIRUS_DATA.equals(intent.getAction()) && AntiVirusService.this.mDataSyncManager != null) {
                    AntiVirusService.this.mDataSyncManager.request(AntiVirusService.this.mContext);
                }
            }
        }
    };
    private Toast mToast = null;

    private static class DeleteVirusApkRunable implements Runnable {
        private Intent intent;

        public DeleteVirusApkRunable(Intent intent) {
            this.intent = intent;
        }

        public void run() {
            HwLog.i(AntiVirusService.TAG, "handleDeleteVirusApk");
            String data = this.intent.getStringExtra("package_name");
            if (data == null || "".equals(data)) {
                HwLog.w(AntiVirusService.TAG, "handleDeleteVirusApk data is null");
                return;
            }
            VirusAppsManager.getIntance().deleteVirusApp(data.substring(data.indexOf(58) + 1));
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        this.mContext = getApplicationContext();
        this.mAntiVirusEngine = AntiVirusEngineFactory.newInstance();
        this.mAntiVirusEngine.onInit(this.mContext);
        boolean isAllowBkgConnection = UserAgreementHelper.getUserAgreementState(this.mContext);
        HwLog.d(TAG, "onCreate: getAgreeProtocal = " + isAllowBkgConnection);
        if (isAllowBkgConnection && AntiVirusTools.isAutoUpdate(this.mContext)) {
            int updateRate = AntiVirusTools.getUpdateRate(this.mContext);
            AntiVirusTools.startAutoUpdateVirusLibAlarm(this.mContext, updateRate);
            HwLog.i(TAG, "onCreate: AntiVirus auto-update is enabled ,rate = " + updateRate);
        }
        TimerRemindNotify notify = new TimerRemindNotify();
        if (isAllowBkgConnection && AntiVirusTools.isGlobalTimerSwitchOn(this.mContext)) {
            notify.schduleTimingNotify(this.mContext);
        } else {
            notify.cancelTimingNotify(this.mContext);
        }
        this.mDataSyncManager = new VirusBgDataSyncManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction(AntiVirusTools.ACTION_UPDATE_VIRUS_DATA);
        this.mContext.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        super.onCreate();
    }

    public void onDestroy() {
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        if (this.mDataSyncManager != null) {
            this.mDataSyncManager.destory();
            this.mDataSyncManager = null;
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 2;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return 2;
        }
        if (AntiVirusTools.ACTION_AUTO_UPDATE_VIRUS_LIB.equals(action) && this.mIsSussAutoUpdate) {
            HwLog.d(TAG, "start auto update virus lib");
            this.mIsSussAutoUpdate = false;
            autoUpdateVirusLib();
            return 3;
        } else if (AntiVirusTools.ACTION_SCAN_ONE_PACKAGE.equals(action)) {
            Utility.initSDK(this.mContext);
            handleInstalledApkCheck(intent);
            return 3;
        } else if (AntiVirusTools.ACTION_CHECK_URL.equals(action)) {
            Utility.initSDK(this.mContext);
            checkUrl(intent);
            return 2;
        } else {
            if (TimerRemindNotify.ACTION_VIRUS_TIMING_NOTIFY.equals(action)) {
                new TimerRemindNotify().doAction(this.mContext);
            } else if (AntiVirusTools.ACTION_DELETE_VIRUS_APK.equals(action)) {
                handleDeleteVirusApk(intent);
                return 3;
            }
            return 2;
        }
    }

    private void autoUpdateVirusLib() {
        new Thread(new Runnable() {
            public void run() {
                Context context = AntiVirusService.this.getApplicationContext();
                boolean isWifiOnlyUpdate = AntiVirusTools.isWiFiOnlyUpdate(context);
                NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                if (networkInfo == null || AntiVirusTools.isCloudClose(context) || !networkInfo.isAvailable()) {
                    AntiVirusService.this.mHandler.sendEmptyMessage(10);
                    return;
                }
                int connectType = networkInfo.getType();
                if (!isWifiOnlyUpdate) {
                    Utility.initSDK(AntiVirusService.this.mContext);
                    AntiVirusService.this.mAntiVirusEngine.onCheckVirusLibVersion(AntiVirusService.this.mHandler);
                } else if (connectType == 1) {
                    Utility.initSDK(AntiVirusService.this.mContext);
                    AntiVirusService.this.mAntiVirusEngine.onCheckVirusLibVersion(AntiVirusService.this.mHandler);
                } else {
                    AntiVirusService.this.mHandler.sendEmptyMessage(10);
                }
            }
        }, "AntiVirus_autoUpdateVirusLib").start();
    }

    private void updateVirusLib() {
        new Thread(new Runnable() {
            public void run() {
                AntiVirusService.this.mAntiVirusEngine.onUpdateVirusLibVersion(AntiVirusService.this.mHandler);
            }
        }, "AntiVirus_updateVirusLib_srv").start();
    }

    private void checkUrl(final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                String url = intent.getStringExtra("url");
                if (url != null && !"".equals(url)) {
                    AntiVirusService.this.mAntiVirusEngine.onCheckUrl(url, AntiVirusService.this.mHandler);
                }
            }
        }, "AntiVirus_checkUrl").start();
    }

    private void showToast(String msg, int duration) {
        if (this.mToast == null) {
            this.mToast = Toast.makeText(this.mContext, msg, duration);
        } else {
            this.mToast.setText(msg);
        }
        this.mToast.show();
    }

    private void handleInstalledApkCheck(final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                boolean isAllowBkgConnection = UserAgreementHelper.getUserAgreementState(AntiVirusService.this.mContext);
                NetworkInfo networkInfo = ((ConnectivityManager) AntiVirusService.this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
                boolean isNetworkConnected = false;
                if (networkInfo != null) {
                    isNetworkConnected = networkInfo.isConnected();
                }
                boolean z = (AntiVirusTools.isCloudScanSwitchOn(AntiVirusService.this.mContext) && isAllowBkgConnection) ? isNetworkConnected : false;
                String data = intent.getStringExtra("package_name");
                if (data != null && !"".equals(data)) {
                    Serializable result = null;
                    try {
                        result = AntiVirusService.this.mAntiVirusEngine.onCheckInstalledApk(AntiVirusService.this.mContext, data.substring(data.indexOf(58) + 1), null, z);
                    } catch (RuntimeException e) {
                        HwLog.w(AntiVirusService.TAG, "handleInstalledApkCheck", e);
                    }
                    if (result != null) {
                        HwLog.i(AntiVirusService.TAG, "result != null, found virusscan pkg, send broadcast. pkg:" + result.getPackageName());
                        Intent intent = new Intent(AntiVirusTools.ACTION_FOUND_VIRUSSCAN_APP);
                        intent.putExtra("key_result", result);
                        intent.setPackage(AntiVirusService.this.getPackageName());
                        AntiVirusService.this.sendBroadcastAsUser(intent, UserHandle.OWNER, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                        return;
                    }
                    HwLog.i(AntiVirusService.TAG, "result == null");
                }
            }
        }, "AntiVirus_handleInstalledApkCheck").start();
    }

    private void handleDeleteVirusApk(Intent intent) {
        new Thread(new DeleteVirusApkRunable(intent), "AntiVirus_handleDeleteVirusApk").start();
    }
}
