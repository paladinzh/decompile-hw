package com.huawei.systemmanager.adblock.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.widget.Toast;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;
import com.huawei.systemmanager.rainbow.client.util.NetWorkHelper;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;

public class AdBlockService implements HsmService, MessageHandler {
    static final long MSG_DELAY_MILLS = 15000;
    static final int MSG_REGISTER_RECEIVER = 1;
    static final int MSG_UNREGISTER_RECEIVER = 2;
    static final int MSG_UPDATE_PART = 3;
    public static final String TAG = "AdBlockService";
    private final Context mAppContext;
    private HsmSingleExecutor mDlAppExecutor = new HsmSingleExecutor();
    private ContentObserver mDlAppObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (AdBlockService.this.mDlAppExecutor.getDequeTaskNum() <= 0) {
                AdBlockService.this.mDlAppExecutor.execute(new DlAppChangedRunnable(AdBlockService.this.mAppContext));
            }
        }
    };
    private Handler mHandler = new GenericHandler(this);
    private HsmSingleExecutor mHsmSingleExecutor = new HsmSingleExecutor();
    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwLog.i(AdBlockService.TAG, "onReceive action=" + action);
                if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    AdBlockService.this.mHsmSingleExecutor.execute(new PackageAddedRunnable(AdBlockService.this.mAppContext, AdBlockService.this.getPkgFromUri(intent.getData()), AdBlockService.this.mHandler));
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    AdBlockService.this.mHsmSingleExecutor.execute(new PackageRemovedRunnable(AdBlockService.this.mAppContext, AdBlockService.this.getPkgFromUri(intent.getData())));
                }
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwLog.i(AdBlockService.TAG, "onReceive action=" + action);
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    AdBlockService.this.onConnectivityChanged(context);
                } else if (AdConst.ACTION_AD_APKDL_URL_DETECTED.equals(action)) {
                    AdUtils.checkUrl(context, intent.getExtras());
                } else if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                    AdBlockService.this.mHsmSingleExecutor.execute(new UserChangedRunnable(context, "android.intent.action.USER_REMOVED".equals(action)));
                }
            }
        }
    };

    public AdBlockService(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public void init() {
        if (CloudSwitchHelper.isCloudEnabled()) {
            this.mHandler.sendEmptyMessage(1);
            if (Utility.isOwner()) {
                AdUtils.dispatchAllAsync(this.mAppContext);
            }
            return;
        }
        HwLog.i(TAG, "init cloud is not enable, just return");
    }

    public void onDestroy() {
        if (CloudSwitchHelper.isCloudEnabled()) {
            this.mHandler.sendEmptyMessage(2);
        } else {
            HwLog.i(TAG, "onDestroy cloud is not enable, just return");
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (AdConst.ACTION_AD_UPDATE_RESULT.equals(action)) {
                handleUpdateResult(this.mAppContext, intent);
            } else if (AdConst.ACTION_AD_BLOCKED_TOAST.equals(action)) {
                showBlockedToast(intent);
            }
        }
    }

    private void registerReceiver() {
        IntentFilter apkDlFilter = new IntentFilter();
        apkDlFilter.addAction(AdConst.ACTION_AD_APKDL_URL_DETECTED);
        this.mAppContext.registerReceiver(this.mReceiver, apkDlFilter, AdConst.PERMISSION_AD_APKDL_STRATEGY, null);
        HwLog.i(TAG, "registerReceiver is owner?" + Utility.isOwner());
        if (Utility.isOwner()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            this.mAppContext.registerReceiver(this.mReceiver, filter);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageFilter.addDataScheme("package");
            this.mAppContext.registerReceiverAsUser(this.mPackageReceiver, UserHandle.ALL, packageFilter, null, null);
            this.mAppContext.getContentResolver().registerContentObserver(Global.getUriFor(AdConst.DOWNLOAD_APPS_KEY), true, this.mDlAppObserver);
        }
    }

    private void unregisterReceiver() {
        this.mAppContext.unregisterReceiver(this.mReceiver);
        if (Utility.isOwner()) {
            this.mAppContext.unregisterReceiver(this.mPackageReceiver);
            this.mAppContext.getContentResolver().unregisterContentObserver(this.mDlAppObserver);
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                registerReceiver();
                return;
            case 2:
                unregisterReceiver();
                return;
            case 3:
                updatePart(this.mAppContext);
                return;
            default:
                return;
        }
    }

    private void onConnectivityChanged(Context context) {
        if (NetWorkHelper.isNetworkAvaialble(context)) {
            long now = System.currentTimeMillis();
            long alarmTime = AdBlockPref.getAlarmTime(context);
            if (now >= alarmTime) {
                HwLog.i(TAG, "onConnectivityChanged alarm time is up");
                AdBlockPref.setUpdateType(context, 2);
                updateAdStrategy(context, 2);
                return;
            } else if (518400000 + now < alarmTime) {
                HwLog.i(TAG, "onConnectivityChanged system time is changed too early");
                AdBlockPref.setAlarmTime(context, now);
                return;
            } else {
                int updateType = AdBlockPref.getUpdateType(context);
                if (updateType == 0) {
                    HwLog.i(TAG, "onConnectivityChanged last update success, just return");
                    return;
                }
                if (now - AdBlockPref.getLastChangeTime(context) > 240000) {
                    HwLog.i(TAG, "onConnectivityChanged retry for last update failed");
                    AdBlockPref.setLastChangeTime(context, now);
                    updateAdStrategy(context, updateType);
                }
                return;
            }
        }
        HwLog.i(TAG, "onConnectivityChanged network unavailable, no need update");
    }

    private String getPkgFromUri(Object object) {
        if (object instanceof Uri) {
            return ((Uri) object).getSchemeSpecificPart();
        }
        HwLog.w(TAG, "getPkgFromUrl object is not a Uri");
        return "";
    }

    private void updatePart(Context context) {
        int updateType = AdBlockPref.getUpdateType(context);
        if (updateType < 1) {
            updateType = 1;
            AdBlockPref.setUpdateType(context, 1);
        }
        updateAdStrategy(context, updateType);
    }

    private static void updateAdStrategy(Context context, int updateType) {
        AdUtils.update(context, updateType);
    }

    private void handleUpdateResult(Context context, Intent intent) {
        boolean success = intent.getBooleanExtra(AdConst.BUNDLE_KEY_UPDATE_RESULT, false);
        int type = intent.getIntExtra(AdConst.BUNDLE_KEY_UPDATE_TYPE, 0);
        HwLog.i(TAG, "handleUpdateResult type=" + type + ", success=" + success);
        if (2 == type) {
            AdBlockPref.setAlarmTime(context, (System.currentTimeMillis() + ClientServerSync.getIntervalTimeFromServer()) + ((long) new Random().nextInt(21600000)));
        }
        int oldType = AdBlockPref.getUpdateType(context);
        if (success && type >= oldType) {
            AdBlockPref.setUpdateType(context, 0);
        }
    }

    private void showBlockedToast(Intent intent) {
        Toast.makeText(this.mAppContext, intent.getStringExtra(AdConst.BUNDLE_BLOCKED_MESSAGE), 1).show();
    }
}
