package com.huawei.systemmanager.power.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import com.huawei.netassistant.analyse.TrafficNotifyAfterLocked;
import com.huawei.systemmanager.antimal.AntiMalService;
import com.huawei.systemmanager.antimal.AntiMalUtils;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.data.charge.ChargeInfo;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.power.model.UsageStatusHelper;
import com.huawei.systemmanager.power.notification.NotificationListEditor;
import com.huawei.systemmanager.power.provider.ProviderWrapper;
import com.huawei.systemmanager.power.util.PowerNotificationUtils;
import com.huawei.systemmanager.power.util.PushAppUtil;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class BgPowerManagerService extends Service implements IPackageChangeListener {
    private static final String AUTHORITY = "com.huawei.vdrive";
    private static final double BATTERY_LEVEL_THRESHOLD = 0.01d;
    private static final Uri CONTENT_URI = Uri.parse("content://com.huawei.vdrive/setting");
    private static long FIVEMINUTES = TrafficNotifyAfterLocked.SCREEN_LOCK_NO_CHEK_DELAY;
    private static final int HIGH_POWER_THRESHOD = 20;
    private static final long INTERVAL_TIME_UNIT = 3600000;
    private static final long INTERVAL_TIME_UNIT_MIN = 60000;
    private static final int MSG_BATTERY_CHANGE = 3;
    private static final int MSG_POWER_CONNECTED = 5;
    private static final int MSG_POWER_DISCONNECTED = 4;
    private static final int MSG_SCREEN_OFF = 2;
    private static final int MSG_SCREEN_ON = 1;
    private static final int MSG_SCREEN_ON_FOR_USAGESTATUS = 6;
    private static final String TABLENAME = "setting";
    private static final String TAG = "BgPowerManagerService";
    private static final String VALUE = "value";
    private static int mCurBatterylevel = 0;
    private static int mScreenOffBatterylevel = 0;
    private static Long mScreenOffTime = Long.valueOf(0);
    private static Long mScreenOmTime = Long.valueOf(0);
    private long firstTime = 0;
    private int firstmBatteryCapacity = 0;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    BgPowerManagerService.mCurBatterylevel = BatteryInfo.getBatteryCapacityRmValue();
                    BgPowerManagerService.this.mRawLevel = intent.getIntExtra("level", 0);
                    BgPowerManagerService.this.mCurPlugType = intent.getIntExtra("plugged", 0);
                    if (BgPowerManagerService.this.mCurPlugType != 0) {
                        BgPowerManagerService.this.pluged = BgPowerManagerService.this.mCurPlugType;
                        if (BgPowerManagerService.this.mRawLevel < 90) {
                            BgPowerManagerService.this.mDeleteBatteryInfo = false;
                        } else if (!BgPowerManagerService.this.mDeleteBatteryInfo) {
                            BatteryStatisticsHelper.deleteBatteryInfoAllInfos();
                            BgPowerManagerService.this.mDeleteBatteryInfo = true;
                        }
                    }
                    BgPowerManagerService.this.mHandler.removeMessages(3);
                    BgPowerManagerService.this.mHandler.sendMessage(BgPowerManagerService.this.mHandler.obtainMessage(3));
                    if (AntiMalUtils.isAlerted(context)) {
                        int temperature = intent.getIntExtra("temperature", 0) / 10;
                        int temperMax = AntiMalService.getInstance(context).getCfgThermal();
                        HwLog.i(BgPowerManagerService.TAG, "temperature = " + temperature + ";temperMax = " + temperMax);
                        if (temperature >= temperMax) {
                            AntiMalUtils.sendNotification(context);
                        }
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    BgPowerManagerService.this.mHandler.removeMessages(1);
                    BgPowerManagerService.this.mHandler.sendMessageDelayed(BgPowerManagerService.this.mHandler.obtainMessage(1), 3000);
                    BgPowerManagerService.this.mHandler.removeMessages(6);
                    BgPowerManagerService.this.mHandler.sendMessage(BgPowerManagerService.this.mHandler.obtainMessage(6));
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    BgPowerManagerService.this.mHandler.removeMessages(2);
                    BgPowerManagerService.this.mHandler.sendMessage(BgPowerManagerService.this.mHandler.obtainMessage(2));
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    BgPowerManagerService.this.mHandler.removeMessages(4);
                    BgPowerManagerService.this.mHandler.sendMessage(BgPowerManagerService.this.mHandler.obtainMessage(4));
                } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    BgPowerManagerService.this.mHandler.removeMessages(5);
                    BgPowerManagerService.this.mHandler.sendMessage(BgPowerManagerService.this.mHandler.obtainMessage(5));
                }
            }
        }
    };
    private int mBatteryCapacity = 0;
    private Context mContext;
    private int mCurPlugType = 0;
    private boolean mDeleteBatteryInfo = false;
    protected Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsAutoEnterSaveMode = false;
    private boolean mIsShowDialog = false;
    private int mLastBatteryLevel = 0;
    private int mLastRawLevel = 0;
    private NotificationListEditor mListEditor;
    private boolean mLowBatterySaveMode = false;
    private boolean mLowBatterySuperMode = false;
    private ArrayList<Integer> mNotifyList = new ArrayList();
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            if (BgPowerManagerService.this.mListEditor != null) {
                BgPowerManagerService.this.mListEditor.removeNotifyList(uid);
            }
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private ContentObserver mPushWihteObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwLog.i(PushAppUtil.TAG, "push white database changed!");
            BgPowerManagerService.this.doSendPushWhiteApps();
        }
    };
    private int mRawLevel = 0;
    private boolean mSaveModeRemind = false;
    private Executor mSendPushTaskExecutor = new HsmSingleExecutor();
    private int mUpdateRawLevel = 0;
    private int pluged = 2;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        initComponent();
        HsmPackageManager.registerListener(this);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "registerProcessObserver RemoteException!");
        }
        this.mBatteryCapacity = BatteryInfo.getBatteryCapacity();
        this.mContext = getApplicationContext();
        initPowerModeNotificationStatus();
        registerBatteryReceiver();
        HwLog.d(TAG, "BgPowerManagerService  mBatteryCapacity is " + this.mBatteryCapacity);
        registerPushWihteObserver();
        doSendPushWhiteApps();
        UsageStatusHelper.updateRatio();
        BatteryStatisticsHelper.scheduleRecordPowerConsume();
        RemainingTimeSceneHelper.scheduleRecordTimeScene();
    }

    private void initPowerModeNotificationStatus() {
        HwLog.i(TAG, "initPowerModeNotificationStatus");
        SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, false);
        SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, false);
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mBatInfoReceiver);
        HsmPackageManager.unregisterListener(this);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterProcessObserver RemoteException!");
        }
        destroyComponent();
        unRegisterPushWhiteObserver();
    }

    private void destroyComponent() {
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i(TAG, "onStartCommand");
        if (intent == null || intent.getAction() == null) {
            return 0;
        }
        handlerOnStartCommandWithAction(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handlerOnStartCommandWithAction(Intent intent) {
        String action = intent.getAction();
        HwLog.d(TAG, "handlerOnStartCommandWithAction action: " + action);
        if (ActionConst.INNER_SERVICE_ACTION_NOTIFY_LIST.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                this.mNotifyList = bundle.getIntegerArrayList(ApplicationConstant.BGSERVICE_BUNDLE_NOTIFY_UID_LIST);
                this.mListEditor = new NotificationListEditor(this.mContext, this.mNotifyList);
                HwLog.d(TAG, "bg mNotifyList = " + this.mNotifyList);
            }
        }
    }

    private void registerBatteryReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        Intent batteryStatus = registerReceiver(this.mBatInfoReceiver, filter);
        if (batteryStatus != null) {
            this.mRawLevel = batteryStatus.getIntExtra("level", 0);
            autoLowBatteryNotification(this.mRawLevel, batteryStatus.getIntExtra("plugged", 0));
            HwLog.d(TAG, "registerBatteryReceiver");
            return;
        }
        HwLog.i(TAG, " batteryStatus is null");
    }

    private void remindWithBatteryChange() {
        if (this.mContext == null) {
            this.mContext = getApplicationContext();
        }
        boolean bInSuperMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (this.mLastRawLevel > 10 && this.mRawLevel <= 10) {
            this.mIsShowDialog = true;
        }
        this.mLastRawLevel = this.mRawLevel;
        if (Utility.superPowerEntryEnable() && this.mIsShowDialog && this.mRawLevel <= 10 && this.mCurPlugType == 0 && !bInSuperMode && Utility.isOwnerUser(false) && !getVdriveState() && isStartupGuideFinished()) {
            HwLog.i(TAG, "low battery 10%, show super power mode enter notification.");
            PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 2, this.mRawLevel);
            SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, true);
        }
        this.mIsShowDialog = false;
    }

    private void schedulePowerSaveModeNotification() {
        if (this.mContext == null) {
            this.mContext = getApplicationContext();
        }
        boolean bInSuperMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (this.mLastBatteryLevel > 20 && this.mRawLevel <= 20) {
            this.mSaveModeRemind = true;
        }
        this.mLastBatteryLevel = this.mRawLevel;
        if (!bInSuperMode && this.mSaveModeRemind && this.mCurPlugType == 0 && PowerModeControl.getInstance(this.mContext).readSaveMode() != 4) {
            HwLog.i(TAG, "low battery 20%, show power mode enter notification.");
            PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 1, this.mRawLevel);
            SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, true);
        }
        this.mSaveModeRemind = false;
    }

    private boolean isStartupGuideFinished() {
        boolean z = true;
        try {
            if (1 != Secure.getInt(getContentResolver(), "device_provisioned")) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException ex) {
            HwLog.d(TAG, "isStartupGuideFinished catch SettingNotFoundException: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private void initComponent() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        BgPowerManagerService.this.handleVlaueClear();
                        BgPowerManagerService.this.handleScreenOn();
                        BgPowerManagerService.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                PowerManagementModel.getInstance(BgPowerManagerService.this.mContext).setAlarmsPending(false);
                            }
                        }, 5000);
                        return;
                    case 2:
                        BgPowerManagerService.this.handleScreenOff();
                        BgPowerManagerService.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                PowerManagementModel.getInstance(BgPowerManagerService.this.mContext).setAlarmsPending(true);
                            }
                        }, 5000);
                        UsageStatusHelper.recordScreenStatus(false);
                        return;
                    case 3:
                        BgPowerManagerService.this.handleBatteryChange();
                        return;
                    case 4:
                        BgPowerManagerService.this.handleVlaueClear();
                        ChargeInfo.doRecordsChargeInfo(BgPowerManagerService.this.pluged, 1, BgPowerManagerService.this.mContext);
                        ProviderWrapper.updateWakeupNumDB(BgPowerManagerService.this.mContext);
                        BgPowerManagerService.this.autoLowBatteryNotification(BgPowerManagerService.this.mRawLevel, BgPowerManagerService.this.mCurPlugType);
                        return;
                    case 5:
                        BgPowerManagerService.this.handleVlaueClear();
                        ChargeInfo.doRecordsChargeInfo(BgPowerManagerService.this.pluged, 0, BgPowerManagerService.this.mContext);
                        PowerNotificationUtils.canclePowerModeNotification(BgPowerManagerService.this.mContext);
                        return;
                    case 6:
                        UsageStatusHelper.recordScreenStatus(true);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void autoLowBatteryNotification(int realLevel, int plugType) {
        HwLog.i(TAG, "autoLowBatteryNotification realLevel= " + realLevel + " ,plugType= " + plugType);
        boolean lowBatterySuperMode = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, false);
        boolean lowBatterySaveMode = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, false);
        boolean bInSuperMode = SystemProperties.getBoolean("sys.super_power_save", false);
        boolean isSaveMode = PowerModeControl.getInstance(this.mContext).readSaveMode() == 4;
        if (realLevel <= 20 && !((Utility.superPowerEntryEnable() && realLevel <= 10) || lowBatterySaveMode || plugType != 0 || bInSuperMode || isSaveMode || this.mIsAutoEnterSaveMode)) {
            HwLog.i(TAG, "autoLowBatteryNotification, open power save mode low battery notification.");
            PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 1, realLevel);
            SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, true);
        }
        if (Utility.superPowerEntryEnable() && realLevel <= 10 && !lowBatterySuperMode && plugType == 0 && Utility.isOwnerUser(false) && !getVdriveState() && isStartupGuideFinished() && !bInSuperMode) {
            HwLog.i(TAG, "autoLowBatteryNotification, open super power save mode low battery notification.");
            PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 2, realLevel);
            SharePrefWrapper.setPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, true);
        }
        this.mIsAutoEnterSaveMode = false;
    }

    private void updateLowBatteryNotification() {
        if (this.mUpdateRawLevel > this.mRawLevel) {
            this.mUpdateRawLevel = this.mRawLevel;
            this.mLowBatterySuperMode = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SUPER_MODE_LOW_BATTERY_NOTIFICATION, false);
            this.mLowBatterySaveMode = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_LOW_BATTERY_NOTIFICATION, false);
            if (this.mLowBatterySuperMode) {
                PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 2, this.mRawLevel);
                return;
            } else if (this.mLowBatterySaveMode) {
                if (!Utility.superPowerEntryEnable() || (this.mRawLevel > 10 && this.mRawLevel < 20)) {
                    PowerNotificationUtils.showPowerModeEnterNotification(this.mContext, 1, this.mRawLevel);
                }
                return;
            }
        }
        this.mUpdateRawLevel = this.mRawLevel;
    }

    private void handleScreenOff() {
        mScreenOffTime = Long.valueOf(System.currentTimeMillis());
        mScreenOffBatterylevel = mCurBatterylevel;
    }

    private void handleVlaueClear() {
        this.firstTime = 0;
        this.firstmBatteryCapacity = 0;
    }

    private void handleScreenOn() {
        mScreenOmTime = Long.valueOf(System.currentTimeMillis());
        long time = mScreenOmTime.longValue() - mScreenOffTime.longValue();
        if (time > FIVEMINUTES) {
            long batterylevel = (long) (mScreenOffBatterylevel - mCurBatterylevel);
            if (batterylevel > (time / 3600000) * 20) {
                Intent intent = new Intent(ActionConst.INTENT_POWER_STATISTIC);
                Bundle bundle = new Bundle();
                bundle.putInt(ApplicationConstant.HIGH_POWER_VIP_SHOW_KEY, 1);
                intent.putExtras(bundle);
                sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                HwLog.i(TAG, "handleScreenOn Trigger INTENT_POWER_STATISTIC, batterylevel= " + batterylevel + ",time= " + time);
            }
        }
    }

    private void handleBatteryChange() {
        boolean flag2 = false;
        if (ActivityManager.isUserAMonkey()) {
            HwLog.d(TAG, "Monkey testing!");
            return;
        }
        updateLowBatteryNotification();
        schedulePowerSaveModeNotification();
        remindWithBatteryChange();
        if (this.firstmBatteryCapacity == 0) {
            this.firstTime = SystemClock.elapsedRealtime();
            this.firstmBatteryCapacity = mCurBatterylevel;
        }
        if (SystemClock.elapsedRealtime() - this.firstTime >= FIVEMINUTES) {
            if (((double) (this.firstmBatteryCapacity - mCurBatterylevel)) >= ((double) BatteryInfo.getBatteryCapacity()) * BATTERY_LEVEL_THRESHOLD) {
                flag2 = true;
            }
            if (flag2) {
                Intent intent = new Intent(ActionConst.INTENT_POWER_STATISTIC);
                Bundle bundle = new Bundle();
                bundle.putInt(ApplicationConstant.HIGH_POWER_VIP_SHOW_KEY, 1);
                intent.putExtras(bundle);
                sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                HwLog.i(TAG, "handleHighPowerPolicy Trigger INTENT_POWER_STATISTIC. mRawLevel= " + this.mRawLevel);
            }
            handleVlaueClear();
        }
    }

    public boolean getVdriveState() {
        boolean serviceState = isProcessRunning(this.mContext, AUTHORITY);
        HwLog.d(TAG, "getVdriveState serviceState: " + serviceState);
        if (serviceState) {
            boolean dbState = getVdriveStateByDB();
            HwLog.d(TAG, "getVdriveStateByDB dbState: " + dbState);
            if (dbState) {
                return true;
            }
        }
        return false;
    }

    private boolean getVdriveStateByDB() {
        Cursor cursor = null;
        String where = "name = 'vdrive_state'";
        try {
            cursor = this.mContext.getContentResolver().query(CONTENT_URI, new String[]{"value"}, where, null, null);
            if (cursor == null) {
                HwLog.d(TAG, "getVdriveStateFromDB Can't get vdrive state from " + CONTENT_URI);
                return false;
            } else if (cursor.moveToFirst()) {
                String value = cursor.getString(0);
                HwLog.d(TAG, "getVdriveStateFromDB value: " + value);
                if ("1".equals(value)) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
                }
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isProcessRunning(Context context, String processName) {
        if (TextUtils.isEmpty(processName)) {
            return false;
        }
        try {
            for (RunningAppProcessInfo ra : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
                if (processName.equals(ra.processName)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void onPackagedAdded(String pkgName) {
    }

    public void onPackageRemoved(String pkgName) {
        SavingSettingUtil.deleteRogueFromSmartProviderDB(this.mContext, pkgName);
    }

    public void onPackageChanged(String pkgName) {
    }

    public void onExternalChanged(String[] packages, boolean available) {
    }

    private void registerPushWihteObserver() {
        getContentResolver().registerContentObserver(PushBlackList.CONTENT_OUTERTABLE_URI, true, this.mPushWihteObserver);
    }

    private void unRegisterPushWhiteObserver() {
        getContentResolver().unregisterContentObserver(this.mPushWihteObserver);
    }

    private void doSendPushWhiteApps() {
        this.mSendPushTaskExecutor.execute(new Runnable() {
            public void run() {
                PushAppUtil.initPushWhiteApps(BgPowerManagerService.this.mContext);
            }
        });
    }
}
