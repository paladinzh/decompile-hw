package com.huawei.permission;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.permission.binderhandler.GetModeHandler;
import com.huawei.permission.binderhandler.HoldServiceBinderHandler;
import com.huawei.permission.binderhandler.PendingUserCfgHandler;
import com.huawei.permission.binderhandler.SetModeHandler;
import com.huawei.permission.binderhandler.ShouldMonitorCheckerHandler;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.AppInitializer;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.permissionmanager.db.HistoryRecord;
import com.huawei.permissionmanager.db.PermissionDbVisitor;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.permissionmanager.utils.SuperAppPermisionChecker;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.addviewmonitor.AddViewConst;
import com.huawei.systemmanager.addviewmonitor.AddViewUtils;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.InitCloudDBServHandle;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.service.HsmCaller;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.trustlook.sdk.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class HoldService extends Service {
    private static final String ACTION_KIDS_MODE_DISABLED = "com.huawei.kidsmode.disabled";
    private static final String ACTION_KIDS_MODE_ENABLED = "com.huawei.kidsmode.enabled";
    private static final String CONTENT_PREPERMISSION_URI = "content://com.huawei.permissionmanager.provider.PermissionDataProvider/prePermission";
    private static final String CSP_PACKAGE_NAME = "com.android.contacts";
    private static final String CUST_PREPERMISSION_FILE = "/data/cust/xml/hw_permission.xml";
    private static final String CUST_PREPERMISSION_FILE_SPF_KEY = "file_time";
    private static final String ETC_PREPERMISSION_FILE = "/system/etc/xml/hw_permission.xml";
    private static final String ETC_PREPERMISSION_FILE_SPF_KEY = "etc_file_time";
    private static final String LOG_TAG = "HoldService";
    private static final int MAX_SHOW_LOCATION_FORBID_COUNT = 2;
    public static final int MIN_APPLICATION_UID = 10000;
    private static final long MIN_INTERVAL_TIME = 5000;
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final int MONITOR_TYPE = 1609624831;
    private static final String MUTIL_CARD_RING = "RINGING";
    private static final int PERMISSION_CFG_CLOSE = 0;
    private static final int PERMISSION_CFG_INVAILD = -1;
    private static final int PERMISSION_CFG_OPEN = 1;
    private static final int PERMISSION_TYPE_ALLOWED = 1;
    private static final int PERMISSION_TYPE_BLOCKED = 2;
    private static final int PERMISSION_TYPE_FAIL = 0;
    private static final int PERMISSION_TYPE_REMIND = 3;
    private static final String PREPERMISSION_SPF_FILE = "permissionShare";
    private static boolean deviceProvisioned = false;
    private static final Map<String, Integer> permissionToType = new HashMap();
    private static Object sSyn = new Object();
    private BroadcastReceiver mAddViewReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent != null ? intent.getAction() : "";
            Bundle extras = intent != null ? intent.getExtras() : null;
            if (context != null && intent != null && !TextUtils.isEmpty(action) && extras != null) {
                String packageName = extras.getString("package");
                if (AddViewConst.ACTION_ADD_VIEW_PREVENTNOTIFY.equals(action)) {
                    if (!HoldService.this.mTipsLifecycle.isTipRecordExist(packageName, AddViewConst.ADD_VIEW_TIPS_KEY) && !AddViewUtils.shouldBeSilent(packageName)) {
                        HoldService.this.mTipsLifecycle.addTipRecord(packageName, AddViewConst.ADD_VIEW_TIPS_KEY, true);
                        HoldService.this.sendToastBroadcast(R.string.addview_reject_runtime_toast, packageName);
                    }
                } else if (AddViewConst.ACTION_ADD_VIEW_ENABLED.equals(action)) {
                    HoldService.this.mTipsLifecycle.removeTipRecord(packageName, AddViewConst.ADD_VIEW_TIPS_KEY);
                }
            }
        }
    };
    private AppInfo mAppInfo = null;
    private int mCSPUid = 0;
    private Context mContext = null;
    private int mCurrentForegroundUid = 0;
    private final Executor mHistoryRecordExecutor = new HsmSingleExecutor();
    private boolean mIsKidsMode = false;
    private BroadcastReceiver mKidsModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwLog.e(HoldService.LOG_TAG, "mKidsModeReceiver null intent!");
                return;
            }
            HwLog.i(HoldService.LOG_TAG, "action = " + intent.getAction());
            if (HoldService.ACTION_KIDS_MODE_ENABLED.equals(intent.getAction())) {
                HoldService.this.mKidsModeRefusePackages = intent.getStringArrayListExtra("kidsmoderefusepackages");
                HoldService.this.mIsKidsMode = true;
            }
            if (HoldService.ACTION_KIDS_MODE_DISABLED.equals(intent.getAction())) {
                HoldService.this.mIsKidsMode = false;
            }
        }
    };
    private ArrayList<String> mKidsModeRefusePackages;
    private int mMmsUid = 0;
    PendingUserCfgCache mPendingCfgCache = null;
    private int mPermissionSwitch = -1;
    private int mPermissionType = 0;
    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwLog.e(HoldService.LOG_TAG, "mPhoneReceiver null intent!");
            } else {
                HoldService.this.mPhoneReceiverState = intent.getStringExtra("state");
            }
        }
    };
    private String mPhoneReceiverState = "";
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                HoldService.this.mCurrentForegroundUid = uid;
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (HoldService.this.sHoldServiceBinder != null) {
                HoldService.this.sHoldServiceBinder.removeRecord(uid);
            }
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private HashMap<BlockedAppInfo, String> mRecordBlockTime = new HashMap();
    private RuntimePermissionHandler mRtPermHandler = null;
    private SettingsObserver mSettingsObserver = null;
    private SuperAppPermisionChecker mSuperAppPermissionChecker;
    private TipsLifecycle mTipsLifecycle = new TipsLifecycle();
    private int mUserSelection;
    private HoldServiceBinder sHoldServiceBinder = null;

    private static class BlockedAppInfo {
        private int mPermissionType;
        private String mPkgName;

        public BlockedAppInfo(String pkgName, int permissionType) {
            this.mPkgName = pkgName;
            this.mPermissionType = permissionType;
        }

        public int hashCode() {
            return ((this.mPermissionType + 31) * 31) + (this.mPkgName == null ? 0 : this.mPkgName.hashCode());
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BlockedAppInfo other = (BlockedAppInfo) obj;
            if (this.mPermissionType != other.mPermissionType) {
                return false;
            }
            if (this.mPkgName == null) {
                if (other.mPkgName != null) {
                    return false;
                }
            } else if (!this.mPkgName.equals(other.mPkgName)) {
                return false;
            }
            return true;
        }
    }

    public final class HoldServiceBinder extends IHoldService.Stub {
        private static final int MSG_RELEASE_HOLD_SERVICE = 1;
        private static final int UN_USED_PERMISSION = 0;
        private static final int UN_USED_UID = 0;
        private final Map<String, HoldServiceBinderHandler> handlers;
        Handler mHandler;
        private ServiceConnection mHoldShowServiceConnection;
        private ArrayList<RecordItem> mRecordList;
        private Object mSyncObj;

        public class RecordItem {
            public boolean isClick;
            public int state;
            public long time;
            public int type;
            public int uid;
        }

        HoldServiceBinder() {
            this.mSyncObj = null;
            this.mRecordList = new ArrayList();
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            HoldServiceBinder.this.releaseHoldService(0, 2, 0);
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
            this.handlers = new HashMap();
            this.mHoldShowServiceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder service) {
                    HwLog.d(HoldService.LOG_TAG, "HoldDialogShowService connect service");
                }

                public void onServiceDisconnected(ComponentName className) {
                    HwLog.i(HoldService.LOG_TAG, "HoldDialogShowService disconnect service");
                    HoldServiceBinder.this.mHandler.sendEmptyMessage(1);
                }
            };
            this.mSyncObj = new Object();
            initHandlers();
        }

        public boolean checkPreBlock(int callUid, int permissionType, boolean showToast) {
            if (isCallerInvalid(callUid)) {
                return false;
            }
            int resId = -1;
            boolean shouldBlock = false;
            if (Utility.isWifiOnlyMode() && ShareCfg.isPermissionFrozen(permissionType)) {
                resId = R.string.call_and_msg_permission_deny_toast;
                shouldBlock = true;
            } else if (Utility.isDataOnlyMode() && ShareCfg.isPermissionFrozen(permissionType)) {
                resId = R.string.call_permission_deny_toast;
                shouldBlock = true;
            }
            final int toastResId = resId;
            if (shouldBlock) {
                HwLog.i(HoldService.LOG_TAG, "checkPreBlock:callUid=" + callUid + ", permissionType=" + permissionType + ", request block!");
                if (showToast) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            HoldService.this.sendPreBlockToastBroadcast(toastResId, null);
                        }
                    });
                }
            }
            return shouldBlock;
        }

        public boolean checkSystemAppInternal(int callUid, boolean notUsed) {
            if (isCallerInvalid(callUid) || UserHandle.getAppId(callUid) < 10000) {
                return true;
            }
            PackageManager packageManager = HoldService.this.mContext.getPackageManager();
            if (packageManager == null) {
                return true;
            }
            try {
                String[] pkgName = packageManager.getPackagesForUid(callUid);
                if (pkgName == null || pkgName.length <= 0) {
                    HwLog.e(HoldService.LOG_TAG, "pkgName = null");
                    return true;
                }
                ApplicationInfo appInfo = packageManager.getApplicationInfo(pkgName[0], 0);
                if (appInfo == null) {
                    HwLog.e(HoldService.LOG_TAG, "appInfo = null");
                    return true;
                }
                String packageName = appInfo.packageName;
                boolean z = false;
                long identity = Binder.clearCallingIdentity();
                try {
                    z = GRuleManager.getInstance().shouldMonitor(HoldService.this.mContext, MonitorScenario.SCENARIO_PERMISSION, packageName);
                } catch (Exception e) {
                    HwLog.e(HoldService.LOG_TAG, "checkSystemAppInternal get Exception!");
                    return z;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
                if (z || Utility.isTestApp(pkgName[0])) {
                    return true;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return true;
            }
        }

        public int checkBeforeShowDialogWithPid(int uid, int pid, int permissionType, String desAddr) {
            if (!isMonitorPermissionType(permissionType)) {
                return 1;
            }
            if (isCallerInvalid(uid)) {
                return 1;
            }
            if (8 == permissionType) {
                if (HoldService.this.isLocationEnabled()) {
                    HwLog.i(HoldService.LOG_TAG, "locationInfo switch is on . pid = " + pid);
                } else {
                    HwLog.i(HoldService.LOG_TAG, "locationInfo switch is off  return block . pid = " + pid);
                    return 2;
                }
            }
            AppInfo appInfo = ShareLib.getAppInfoByUidAndPid(HoldService.this.mContext, uid, pid);
            if (appInfo == null || appInfo.mPkgName == null) {
                HwLog.d(HoldService.LOG_TAG, "ShareLib.getAppInfoByUidAndPid(mContext, uid , pid) is null");
                return 1;
            }
            if (HoldService.this.mIsKidsMode && HoldService.this.mKidsModeRefusePackages != null && HoldService.this.mKidsModeRefusePackages.size() > 0 && (8192 == permissionType || 32 == permissionType)) {
                for (String appName : HoldService.this.mKidsModeRefusePackages) {
                    if (appName.equals(appInfo.mPkgName)) {
                        return 2;
                    }
                }
            }
            if (!CustomizeWrapper.isPermissionEnabled()) {
                return 1;
            }
            if (16 == permissionType) {
                if (isSystemApp(appInfo.mPkgName)) {
                    return 1;
                }
                if (checkPermission(appInfo.mPkgName, permissionType, uid)) {
                    HwLog.i(HoldService.LOG_TAG, "PHONE_CODE_PERMISSION  is allowed  . packagename =  " + appInfo.mPkgName);
                } else {
                    HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                    showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                    HwLog.i(HoldService.LOG_TAG, "PHONE_CODE_PERMISSION  is blocked  . packagename =  " + appInfo.mPkgName);
                    return 2;
                }
            }
            if ((MPermissionUtil.isClassAType(permissionType) || MPermissionUtil.isClassBType(permissionType)) && permissionType != 32) {
                if (checkPermission(appInfo.mPkgName, permissionType, uid)) {
                    HoldService.this.recordPermissionAllowAction(appInfo.mPkgName, permissionType);
                } else {
                    HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                    showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                }
                return 1;
            } else if (permissionType == 32 && !checkPermission(appInfo.mPkgName, permissionType, uid)) {
                HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                return 1;
            } else if (MPermissionUtil.isClassDType(permissionType)) {
                return 3;
            } else {
                if (!HoldService.deviceProvisioned) {
                    try {
                        if (Global.getInt(HoldService.this.mContext.getContentResolver(), "device_provisioned") == 1) {
                            HoldService.deviceProvisioned = true;
                        } else {
                            HwLog.d(HoldService.LOG_TAG, "device not provisioned");
                            return 1;
                        }
                    } catch (SettingNotFoundException ex) {
                        HwLog.d(HoldService.LOG_TAG, "SettingNotFoundException", ex);
                    }
                }
                if (67108864 == permissionType) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        if (HoldService.this.mRtPermHandler.addRuntimePermission(appInfo.mPkgName, permissionType, uid) != null) {
                            if (Log.HWINFO) {
                                HwLog.i(HoldService.LOG_TAG, "It's a new runtime record, pkg:" + appInfo.mPkgName + ", type:" + permissionType);
                            }
                            AppInitializer.initializeRuntiemPermission(HoldService.this.mContext, appInfo.mPkgName, uid, permissionType);
                        }
                        Binder.restoreCallingIdentity(identity);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
                int configValue = preHoldShow(uid, appInfo.mPkgName, permissionType, desAddr);
                if (Log.HWINFO) {
                    HwLog.i(HoldService.LOG_TAG, "checkBeforeShowDialogWithPid, current value:" + configValue + ", type:" + permissionType + ", pkg:" + appInfo.mPkgName);
                }
                if (32 == permissionType) {
                    configValue = 1;
                }
                if (CommonFunctionUtil.isDefaultSmsPermission(HoldService.this.mContext, appInfo.mPkgName, permissionType)) {
                    return 1;
                }
                if (configValue == 2 && HoldService.this.mSuperAppPermissionChecker.checkIfIsInAppPermissionList(appInfo.mPkgName, permissionType)) {
                    return 3;
                }
                if (2 == configValue) {
                    HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                    showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                } else if (3 == configValue && HoldService.this.isInCallSatate()) {
                    HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                    showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                    return 1;
                } else if (1 == configValue) {
                    HoldService.this.recordPermissionAllowAction(appInfo.mPkgName, permissionType);
                }
                if (HoldService.this.mPermissionSwitch == 0) {
                    return configValue;
                }
                return HoldServiceSmsHelper.getPrePermissionTypeAfterGroupSendFilter(configValue, permissionType, appInfo.mPkgName, HoldService.this.mContext, desAddr);
            }
        }

        private boolean isSystemApp(String packageName) {
            PackageManager pm = HoldService.this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                    return true;
                }
            } catch (NameNotFoundException e) {
                HwLog.i(HoldService.LOG_TAG, "isSystemApp NameNotFoundException");
            }
            return false;
        }

        private boolean checkPermission(String mPkgName, int permissionType, int uid) {
            boolean z = true;
            PackageManager pm = HoldService.this.mContext.getPackageManager();
            if (pm == null) {
                return true;
            }
            String permission = (String) MPermissionUtil.typeToSinglePermission.get(permissionType);
            if (permission == null) {
                return true;
            }
            try {
                if (PackageManagerWrapper.getPackageInfo(pm, mPkgName, 0).applicationInfo.targetSdkVersion <= 22) {
                    AppOpsManager appOps = (AppOpsManager) HoldService.this.mContext.getSystemService("appops");
                    if (appOps == null) {
                        return true;
                    }
                    if (appOps.checkOpNoThrow(AppOpsManager.permissionToOp(permission), uid, mPkgName) != 0) {
                        z = false;
                    }
                    return z;
                }
                if (pm.checkPermission(permission, mPkgName) != 0) {
                    z = false;
                }
                return z;
            } catch (Exception e) {
                HwLog.w(HoldService.LOG_TAG, "get info fail for " + mPkgName, e);
                return true;
            }
        }

        public void removeRuntimePermissions(String packageName) {
            HoldService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HoldService.this.mRtPermHandler.removeRuntimePermission(packageName);
        }

        public int checkBeforeShowDialog(int uid, int permissionType, String desAddr) {
            return checkBeforeShowDialogWithPid(uid, HoldServiceConst.FAKE_PID, permissionType, desAddr);
        }

        public int holdServiceByRequestPermission(int uid, int pid, int permissionType, String desAddr) {
            if (Log.HWINFO) {
                HwLog.i(HoldService.LOG_TAG, "in holdServiceByRequestPermission , uid:" + uid + ", pid " + pid + " permissionType:" + permissionType);
            }
            if (!isMonitorPermissionTypeWhenShowDialog(permissionType) || isCallerInvalid(uid) || HoldService.this.mPermissionSwitch == 0) {
                return 1;
            }
            if ((MPermissionUtil.isClassAType(permissionType) || MPermissionUtil.isClassBType(permissionType)) && 1000 != permissionType && 1001 != permissionType) {
                return 1;
            }
            RecordItem latestItem = getRecord(uid, permissionType);
            if (latestItem != null) {
                HwLog.i(HoldService.LOG_TAG, "find request record");
                return latestItem.state;
            }
            AppInfo appInfo = ShareLib.getAppInfoByUidAndPid(HoldService.this.mContext, uid, pid);
            int showDialogResult = _holdServiceByRequestPermissionLocked(uid, pid, permissionType, desAddr, appInfo);
            if (Log.HWLog) {
                HwLog.d(HoldService.LOG_TAG, "in holdServiceByRequestPermission ,showDialogResult:" + showDialogResult);
            }
            if (appInfo != null) {
                if (showDialogResult == 2) {
                    HoldService.this.recordPermissionRejectAction(appInfo.mPkgName, permissionType);
                } else {
                    HoldService.this.recordPermissionAllowAction(appInfo.mPkgName, permissionType);
                }
            }
            return showDialogResult;
        }

        private void initHandlers() {
            this.handlers.put("getMode", new GetModeHandler(HoldService.this.mContext));
            this.handlers.put("checkShoudMonitor", new ShouldMonitorCheckerHandler(HoldService.this.mContext));
            this.handlers.put("setMode", new SetModeHandler(HoldService.this.mContext));
            this.handlers.put("setPendingCfg", new PendingUserCfgHandler(HoldService.this.mPendingCfgCache));
        }

        public Bundle callHsmService(String method, Bundle params) {
            if ("notify_permission_blocked".equals(method)) {
                HwLog.i(HoldService.LOG_TAG, "handleMPermissionBlocked notify_permission_blocked..");
                handleMPermissionBlocked(params);
            } else {
                HoldServiceBinderHandler handler = (HoldServiceBinderHandler) this.handlers.get(method);
                if (handler != null) {
                    return handler.handleTransact(params);
                }
            }
            return HsmCaller.call(HoldService.this.getApplicationContext(), method, params);
        }

        private void handleMPermissionBlocked(Bundle params) {
            if (CustomizeWrapper.isPermissionEnabled()) {
                HoldService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
                String pkg = params.getString("packageName");
                if (pkg == null) {
                    HwLog.w(HoldService.LOG_TAG, "handleMPermissionBlocked pkg is null.");
                    return;
                }
                HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkg);
                if (info == null) {
                    HwLog.w(HoldService.LOG_TAG, "handleMPermissionBlocked pkg info is null.");
                    return;
                }
                String permission = params.getString("permissionName");
                if (permission == null) {
                    HwLog.w(HoldService.LOG_TAG, "handleMPermissionBlocked permission name is null.");
                    return;
                }
                Integer permissionType = (Integer) HoldService.permissionToType.get(permission);
                HwLog.i(HoldService.LOG_TAG, "handleMPermissionBlocked type:" + permissionType);
                if (permissionType != null) {
                    if (GRuleManager.getInstance().shouldMonitor(HoldService.this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkg)) {
                        HoldService.this.recordPermissionRejectAction(pkg, permissionType.intValue());
                        showNotificationWhenBlocked(info.mUid, HoldServiceConst.FAKE_PID, pkg, permissionType.intValue());
                    } else {
                        HwLog.i(HoldService.LOG_TAG, "handleMPermissionBlocked pkg not monitor.");
                    }
                }
            }
        }

        private boolean isMonitorPermissionType(int permissionType) {
            boolean z = true;
            if (permissionType < 0) {
                return false;
            }
            boolean isExponentialOf2;
            if ((permissionType & (permissionType - 1)) == 0) {
                isExponentialOf2 = true;
            } else {
                isExponentialOf2 = false;
            }
            if (!isExponentialOf2) {
                return false;
            }
            if ((HoldService.MONITOR_TYPE | permissionType) != HoldService.MONITOR_TYPE) {
                z = false;
            }
            return z;
        }

        private boolean isMonitorPermissionTypeWhenShowDialog(int permissionType) {
            if (1000 == permissionType || 1001 == permissionType) {
                return true;
            }
            return isMonitorPermissionType(permissionType);
        }

        private boolean isCallerInvalid(int passedUid) {
            int callerUid = Binder.getCallingUid();
            if (10000 > UserHandle.getAppId(callerUid) || passedUid == callerUid) {
                return false;
            }
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized int _holdServiceByRequestPermissionLocked(int uid, int pid, int permissionType, String desAddr, AppInfo appInfo) {
            RecordItem latestItem = getRecord(uid, permissionType);
            if (latestItem != null) {
                HwLog.i(HoldService.LOG_TAG, "find request record");
                return latestItem.state;
            }
            if (appInfo != null) {
                if (appInfo.mPkgName != null) {
                    int pendingOperation = HoldService.this.mPendingCfgCache.getPendingCfg(uid, "", permissionType);
                    if (1 == pendingOperation || 2 == pendingOperation) {
                        HwLog.i(HoldService.LOG_TAG, "_holdServiceByRequestPermissionLocked, returned by pending user choice:" + pendingOperation);
                        return pendingOperation;
                    }
                    int currentStatus = 1;
                    if (1000 == permissionType || 1001 == permissionType) {
                        currentStatus = 3;
                    } else if (MPermissionUtil.isClassDType(permissionType)) {
                        currentStatus = 3;
                    } else if (MPermissionUtil.isClassEType(permissionType)) {
                        Cursor cursor = null;
                        try {
                            cursor = getCursorByUid(uid, appInfo.mPkgName);
                            if (cursor == null) {
                                HwLog.e(HoldService.LOG_TAG, "getCurrentStatus, the cursor is null!");
                                currentStatus = 1;
                            } else if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                int permissionCfg = cursor.getInt(cursor.getColumnIndex("permissionCfg")) & permissionType;
                                if ((cursor.getInt(cursor.getColumnIndex("permissionCode")) & permissionType) != permissionType) {
                                    currentStatus = 1;
                                } else if (permissionCfg == permissionType) {
                                    currentStatus = 2;
                                } else {
                                    currentStatus = 1;
                                }
                            } else {
                                currentStatus = 1;
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        } catch (Throwable th) {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                    if (!MPermissionUtil.shouldControlByHsm(permissionType)) {
                        return 1;
                    }
                    boolean isInSuperAppList = HoldService.this.mSuperAppPermissionChecker.checkIfIsInAppPermissionList(appInfo.mPkgName, permissionType);
                    if (3 == currentStatus || isInSuperAppList) {
                        if (isInSuperAppList) {
                            isInSuperAppList = currentStatus == 2;
                        }
                        if (HoldService.this.isInCallSatate()) {
                            showNotificationWhenBlocked(uid, pid, appInfo.mPkgName, permissionType);
                            return 2;
                        }
                        synchronized (this.mSyncObj) {
                            HoldService.this.mUserSelection = 1;
                        }
                        if ((Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) && ShareCfg.isPermissionFrozen(permissionType)) {
                            return 2;
                        }
                        try {
                            if (!updatePermissionInfo(uid, pid, permissionType, appInfo)) {
                                return 1;
                            }
                            int -get12;
                            HwLog.i(HoldService.LOG_TAG, "sendHoldBroadcast begin!");
                            startHoldMainService(desAddr, isInSuperAppList);
                            try {
                                synchronized (this.mSyncObj) {
                                    this.mSyncObj.wait();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                this.mSyncObj.notifyAll();
                            }
                            try {
                                HoldService.this.mContext.unbindService(this.mHoldShowServiceConnection);
                            } catch (IllegalArgumentException e2) {
                                HwLog.e(HoldService.LOG_TAG, "IllegalArgumentException happens when unbindService.");
                            }
                            HwLog.d(HoldService.LOG_TAG, "holdService returned");
                            synchronized (this.mSyncObj) {
                                -get12 = HoldService.this.mUserSelection;
                            }
                            return -get12;
                        } catch (Exception e3) {
                            e3.printStackTrace();
                            return 1;
                        }
                    } else if (Log.HWINFO) {
                        HwLog.i(HoldService.LOG_TAG, "The permission is already awarded by use!");
                    }
                }
            }
            return 1;
        }

        public int releaseHoldService(int uid, int userSelection, int permissionType) {
            HoldService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            synchronized (this.mSyncObj) {
                this.mHandler.removeMessages(1);
                if (Log.HWINFO) {
                    HwLog.i(HoldService.LOG_TAG, "releaseHoldService uid is: " + uid + " userSelection is: " + userSelection + " permissionType is: " + permissionType);
                }
                synchronized (this.mSyncObj) {
                    HoldService.this.mUserSelection = userSelection;
                }
                if (HoldService.this.mAppInfo != null) {
                    HoldServiceSmsHelper.removeTheTableRecorderForGivenPackage(HoldService.this.mAppInfo.mPkgName, permissionType);
                }
                this.mSyncObj.notifyAll();
            }
            return 0;
        }

        private void startHoldMainService(String smsContent, boolean isInSuperAppList) {
            Intent serviceIntent = new Intent(HoldServiceConst.HOLD_MAIN_SERVICE_ACTION);
            serviceIntent.setClass(HoldService.this.mContext, HoldDialogShowService.class);
            Bundle bundle = new Bundle();
            bundle.putInt(HoldServiceConst.APP_UID, HoldService.this.mAppInfo.mAppUid);
            bundle.putInt("permissionCode", HoldService.this.mAppInfo.mPermissionCode);
            bundle.putInt("permissionCfg", HoldService.this.mAppInfo.mPermissionCfg);
            bundle.putInt("permissionType", HoldService.this.mPermissionType);
            bundle.putString("packageName", HoldService.this.mAppInfo.mPkgName);
            bundle.putString(HoldServiceConst.GROUP_SMS_CONTENT, smsContent);
            bundle.putBoolean(HoldServiceConst.PERMISSION_SUPER_LIST, isInSuperAppList);
            serviceIntent.putExtras(bundle);
            HoldService.this.mContext.bindServiceAsUser(serviceIntent, this.mHoldShowServiceConnection, 1, Process.myUserHandle());
            HoldService.this.mContext.startServiceAsUser(serviceIntent, Process.myUserHandle());
            HsmStat.statPerssmisonDialogAction("d", HoldService.this.mPermissionType);
        }

        private boolean updatePermissionInfo(int uid, int pid, int type, AppInfo appInfo) {
            if (uid == 0 || type == 0 || appInfo == null) {
                HwLog.e(HoldService.LOG_TAG, "Error: uid == 0 || permissionType == 0");
                return false;
            }
            HoldService.this.mPermissionType = type;
            HoldService.this.mAppInfo = appInfo;
            return true;
        }

        public void addRecord(int uid, int permissionType, int state, boolean click) {
            if ((HoldService.MONITOR_TYPE & permissionType) == 0) {
                HoldService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
                RecordItem item = new RecordItem();
                item.uid = uid;
                item.type = permissionType;
                item.state = state;
                item.time = System.currentTimeMillis();
                item.isClick = click;
                this.mRecordList.add(item);
            }
        }

        private RecordItem getRecord(int uid, int type) {
            Iterator<RecordItem> it = this.mRecordList.iterator();
            while (it.hasNext()) {
                boolean isInTime = false;
                boolean isExist = false;
                RecordItem item = (RecordItem) it.next();
                boolean isClick = item.isClick;
                if (System.currentTimeMillis() - item.time < DBHelper.HISTORY_MAX_SIZE) {
                    isInTime = true;
                }
                if (item.uid == uid && item.type == type) {
                    isExist = true;
                }
                if (isClick && isExist) {
                    return item;
                }
                if (isInTime && isExist) {
                    return item;
                }
                if (!(isClick || isInTime)) {
                    it.remove();
                }
            }
            return null;
        }

        public void removeRecord(int uid) {
            Iterator<RecordItem> it = this.mRecordList.iterator();
            while (it.hasNext()) {
                if (((RecordItem) it.next()).uid == uid) {
                    it.remove();
                }
            }
        }

        @FindBugsSuppressWarnings({"REC_CATCH_EXCEPTION"})
        private Cursor getCursorByUid(int uid, String pkgName) {
            String[] args = new String[]{pkgName};
            long identity = Binder.clearCallingIdentity();
            Cursor cursor = null;
            Cursor cursor2;
            try {
                cursor = HoldService.this.mContext.getContentResolver().query(DBHelper.BLOCK_TABLE_NAME_URI, null, "packageName = ?", args, null, null);
                if (cursor == null) {
                    HwLog.e(HoldService.LOG_TAG, "Cursor null");
                    cursor2 = null;
                    return cursor2;
                } else if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    if (uid != cursor.getInt(cursor.getColumnIndex("uid"))) {
                        List<ContentValues> contentValuesList = new ArrayList();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("packageName", pkgName);
                        contentValues.put("uid", Integer.valueOf(uid));
                        contentValues.put("permissionCode", Integer.valueOf(cursor.getInt(cursor.getColumnIndex("permissionCode"))));
                        contentValues.put("permissionCfg", Integer.valueOf(cursor.getInt(cursor.getColumnIndex("permissionCfg"))));
                        contentValues.put("trust", Integer.valueOf(1 == cursor.getInt(cursor.getColumnIndex("trust")) ? 1 : 0));
                        contentValuesList.add(contentValues);
                        DBAdapter.updateAppsPermissions(HoldService.this.mContext, contentValuesList);
                    }
                    Binder.restoreCallingIdentity(identity);
                    return cursor;
                } else {
                    cursor.close();
                    HwLog.i(HoldService.LOG_TAG, "Can't match pkgName :" + pkgName);
                    AppInitializer.initilizeNewAppAndSyncToSys(HoldService.this.mContext, pkgName, uid, "check in hold");
                    cursor = HoldService.this.mContext.getContentResolver().query(DBHelper.BLOCK_TABLE_NAME_URI, null, "packageName = ?", args, null, null);
                    Binder.restoreCallingIdentity(identity);
                    return cursor;
                }
            } catch (Exception e) {
                cursor2 = HoldService.LOG_TAG;
                HwLog.e(cursor2, "getCursorByUid get Exception!");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private int preHoldShow(int uid, String pkgName, int permissionType, String desAddr) {
            Cursor c = getCursorByUid(uid, pkgName);
            if (c == null) {
                HwLog.e(HoldService.LOG_TAG, "preHoldShow, the cursor is null!");
                return 0;
            } else if (c.getCount() <= 0) {
                HwLog.e(HoldService.LOG_TAG, "preHoldShow, the cursor is empty!");
                c.close();
                if (HoldService.this.mPermissionSwitch == 0) {
                    return 1;
                }
                if (16 == permissionType) {
                    return 1;
                }
                if (16777216 == permissionType) {
                    return 2;
                }
                return 1;
            } else {
                c.moveToFirst();
                int permissionCode = c.getInt(c.getColumnIndex("permissionCode"));
                int permissionCfg = c.getInt(c.getColumnIndex("permissionCfg"));
                c.close();
                if ((permissionCode & permissionType) == permissionType) {
                    if ((permissionCfg & permissionType) == permissionType) {
                        return 2;
                    }
                    return 1;
                } else if (HoldService.this.mPermissionSwitch == 0) {
                    return 1;
                } else {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        int[] res = CloudDBAdapter.applyDefaultPolicy(HoldService.this.mContext, AppInfo.getComparePermissionCode(HoldService.this.mContext, pkgName), pkgName, permissionCode, permissionCfg);
                        HwLog.i(HoldService.LOG_TAG, "clear remind status for " + pkgName + ", previous:" + permissionCode + SqlMarker.COMMA_SEPARATE + permissionCfg + ", after:" + res[0] + ConstValues.SEPARATOR_KEYWORDS_EN + res[1]);
                        DBAdapter.updateAppPermission(HoldService.this.mContext, uid, pkgName, res[0], res[1], false);
                    } catch (Exception e) {
                        HwLog.w(HoldService.LOG_TAG, "clear remind status for " + pkgName, e);
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                    return 1;
                }
            }
        }

        private void showNotificationWhenBlocked(int uid, int pid, String pkgName, int permissionType) {
            showNotificationWhenBlockedInner(uid, pid, pkgName, permissionType, false);
        }

        private void showNotificationWhenBlockedInner(int uid, int pid, String pkgName, int permissionType, boolean groupBehavior) {
            if (Log.HWLog) {
                HwLog.i(HoldService.LOG_TAG, "showNotificationWhenBlocked uid:" + uid + " pid:" + pid + " pkgName:" + pkgName + " permissionType:" + permissionType);
            }
            long identify = Binder.clearCallingIdentity();
            try {
                if (DBAdapter.getToastSwitchOpenStatus(HoldService.this.mContext)) {
                    Binder.restoreCallingIdentity(identify);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date(System.currentTimeMillis());
                    BlockedAppInfo blockedAppInfo = new BlockedAppInfo(pkgName, permissionType);
                    synchronized (HoldService.this.mRecordBlockTime) {
                        if (HoldService.this.mRecordBlockTime.containsKey(blockedAppInfo)) {
                            String currentBlockedDate = format.format(date);
                            if (!currentBlockedDate.equals((String) HoldService.this.mRecordBlockTime.get(blockedAppInfo))) {
                                HoldService.this.mRecordBlockTime.put(blockedAppInfo, currentBlockedDate);
                                showPermissionBlockedNotification(uid, pkgName, permissionType, groupBehavior);
                            }
                        } else {
                            HoldService.this.mRecordBlockTime.put(blockedAppInfo, format.format(date));
                            showPermissionBlockedNotification(uid, pkgName, permissionType, groupBehavior);
                        }
                    }
                    return;
                }
                HwLog.w(HoldService.LOG_TAG, "The toast remind switch is close!");
            } finally {
                Binder.restoreCallingIdentity(identify);
            }
        }

        private void showPermissionBlockedNotification(int uid, String pkgName, int permissionType, boolean groupBehavior) {
            if (HoldService.this.mCurrentForegroundUid == uid || HoldService.this.isTopPackageInstaller()) {
                String permissionContentForTicker;
                String permissionContentForNotification;
                if (groupBehavior) {
                    permissionContentForTicker = getPermissionContentForGroupBehavior(permissionType, 0);
                    permissionContentForNotification = getPermissionContentForGroupBehavior(permissionType, 1);
                } else {
                    permissionContentForTicker = getPermissionContent(permissionType, 0);
                    permissionContentForNotification = getPermissionContent(permissionType, 1);
                }
                if (TextUtils.isEmpty(permissionContentForTicker)) {
                    HwLog.w(HoldService.LOG_TAG, "showToast getPermissionContent failed");
                    return;
                }
                String label = HsmPackageManager.getInstance().getLabel(pkgName);
                String ticker = HoldService.this.mContext.getString(R.string.permission_block_ticker, new Object[]{label, permissionContentForTicker});
                final String notificationContent = HoldService.this.mContext.getString(R.string.permission_block_notification_content, new Object[]{label});
                final String notificationTitle = HoldService.this.mContext.getString(R.string.permission_block_notification_title, new Object[]{label, permissionContentForNotification});
                if (groupBehavior) {
                    ticker = notificationTitle;
                }
                int fUid = uid;
                String fPkgName = pkgName;
                final String fTicker = ticker;
                String fNotificationContent = notificationContent;
                String fNotificationTitle = notificationTitle;
                final int i = uid;
                final String str = pkgName;
                final boolean z = groupBehavior;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putInt(HoldServiceConst.APP_UID, i);
                        bundle.putString("packageName", str);
                        bundle.putString(HoldServiceConst.PERMISSION_BLOCKED_CONTENT, notificationContent);
                        bundle.putString(HoldServiceConst.NOTIFICATION_TICKER, fTicker);
                        bundle.putString(HoldServiceConst.PERMISSION_BLOCED_TITLE, notificationTitle);
                        bundle.putBoolean(HoldServiceConst.GROUP_BEHAVIOR, z);
                        HoldService.this.sendNotificationBroadcast(bundle);
                    }
                });
                return;
            }
            HwLog.w(HoldService.LOG_TAG, "showToast return because the blocked app is not foreground!");
        }

        private String getPermissionContent(int permissionType, int usedFor) {
            SparseIntArray stringIdMap;
            String permissionContent = "";
            if (usedFor == 0) {
                stringIdMap = ShareLib.getBlockedSingalStringIdMap();
            } else {
                stringIdMap = ShareLib.getBlockedNotificationStringIdMap();
            }
            if (stringIdMap.size() == 0) {
                HwLog.w(HoldService.LOG_TAG, "The stringIdMap is invalid!");
                return permissionContent;
            }
            int permissionStringId = stringIdMap.get(permissionType);
            if (permissionStringId != 0) {
                permissionContent = HoldService.this.mContext.getString(permissionStringId);
            }
            return permissionContent;
        }

        private String getPermissionContentForGroupBehavior(int permissionType, int usedFor) {
            if (!MPermissionUtil.isClassAType(permissionType)) {
                return getPermissionContent(permissionType, usedFor);
            }
            int groupType = ((Integer) MPermissionUtil.typeToPermCode.get(permissionType, Integer.valueOf(-1))).intValue();
            if (-1 == groupType) {
                HwLog.w(HoldService.LOG_TAG, "getPermissionContentForGroupBehavior, type error, should not happen.");
                return "";
            }
            int resId = R.string.permission_group_use_message;
            switch (groupType) {
                case 1:
                    resId = R.string.permission_group_use_contact;
                    break;
                case 4:
                    resId = R.string.permission_group_use_message;
                    break;
                case 64:
                    resId = R.string.permission_group_use_phone;
                    break;
            }
            return HoldService.this.mContext.getString(resId);
        }

        public PendingIntent getPendingIntent(int requestCode, Intent intent, int flags) {
            HwLog.i(HoldService.LOG_TAG, "In holdservice, to get pending intent:" + intent);
            return null;
        }
    }

    @FindBugsSuppressWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
    private class ReadPermissionTask extends AsyncTask<Void, Void, Void> {
        private ReadPermissionTask() {
        }

        protected Void doInBackground(Void... a) {
            new InitCloudDBServHandle().handleIntent(HoldService.this.mContext, null);
            File preFile = new File(HoldService.CUST_PREPERMISSION_FILE);
            File etcFile = new File(HoldService.ETC_PREPERMISSION_FILE);
            if (preFile.exists()) {
                HoldService.this.getCustOrEtcFileData(preFile, HoldService.CUST_PREPERMISSION_FILE_SPF_KEY);
            } else if (etcFile.exists()) {
                HoldService.this.getCustOrEtcFileData(etcFile, HoldService.ETC_PREPERMISSION_FILE_SPF_KEY);
            } else {
                Cursor cursor = HoldService.this.getContentResolver().query(Uri.parse(HoldService.CONTENT_PREPERMISSION_URI), null, null, null, null);
                if (cursor != null && cursor.getCount() == 0) {
                    InputStream inputStream = null;
                    try {
                        inputStream = HoldService.this.getAssets().open("hw_permission.xml");
                        HoldService.this.readHuaweiPermissionFromXml(inputStream);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            DBAdapter.getInstance(HoldService.this.mContext);
            CommonFunctionUtil.changeSmsPermission(HoldService.this.mContext);
            return null;
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            HwLog.i(HoldService.LOG_TAG, "Default sms app has been changed!");
            CommonFunctionUtil.changeSmsPermission(HoldService.this.mContext);
        }
    }

    static {
        HwAppPermissions.initPermissionToTypeMap(permissionToType);
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        this.mRtPermHandler = new RuntimePermissionHandler(this.mContext);
        this.mPendingCfgCache = new PendingUserCfgCache();
        this.sHoldServiceBinder = getHoldServiceBinderInstance();
        if (this.sHoldServiceBinder != null) {
            getPermissionSwitch(this.mContext);
            int myUid = UserHandle.myUserId();
            String servicekey = HoldServiceConst.HOLD_SERVICE_NAME;
            if (myUid != 0) {
                servicekey = servicekey + myUid;
            }
            try {
                ServiceManager.addService(servicekey, this.sHoldServiceBinder);
                registerPhoneListener();
                HsmPackageManager.registerListener(this.mTipsLifecycle);
                registerAddViewListener();
                registerKidsModeListener();
                HwLog.i(LOG_TAG, "HoldService onCreate() execution complete.");
                try {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    if (am != null) {
                        am.registerProcessObserver(this.mProcessObserver);
                    }
                } catch (RemoteException e) {
                    HwLog.e(LOG_TAG, "registerProcessObserver RemoteException!");
                }
                this.mSettingsObserver = new SettingsObserver(null);
                this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("sms_default_application"), true, this.mSettingsObserver);
                this.mSuperAppPermissionChecker = SuperAppPermisionChecker.getInstance(this.mContext);
                new ReadPermissionTask().execute(new Void[0]);
                initContactsAndMmsUid();
            } catch (SecurityException e2) {
                HwLog.e(LOG_TAG, "Hold Service or cloudhelper service create fail.");
            }
        }
    }

    private void initContactsAndMmsUid() {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            ApplicationInfo contacts = pm.getApplicationInfo("com.android.contacts", 0);
            if (contacts != null) {
                this.mCSPUid = contacts.uid;
            }
            ApplicationInfo mms = pm.getApplicationInfo(MMS_PACKAGE_NAME, 0);
            if (mms != null) {
                this.mMmsUid = mms.uid;
            }
        } catch (NameNotFoundException e) {
            HwLog.e(LOG_TAG, "get uid of csp fail.", e);
        } catch (Exception e2) {
            HwLog.e(LOG_TAG, "get uid of csp fail.", e2);
        }
        HwLog.i(LOG_TAG, "cspPkgInfo's UID is : " + this.mCSPUid + " mmsPkgInfo's UID is : " + this.mMmsUid);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i("LocalService", "Received start id " + startId + ": " + intent);
        return 1;
    }

    public IBinder onBind(Intent intent) {
        return getHoldServiceBinderInstance();
    }

    public HoldServiceBinder getHoldServiceBinderInstance() {
        synchronized (sSyn) {
            try {
                if (this.sHoldServiceBinder == null) {
                    this.sHoldServiceBinder = new HoldServiceBinder();
                }
            } catch (NoClassDefFoundError e) {
                HwLog.e(LOG_TAG, "getHoldServiceBinderInstance error");
                return null;
            }
        }
        return this.sHoldServiceBinder;
    }

    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        synchronized (sSyn) {
            this.sHoldServiceBinder = null;
        }
        unregisterReceiver(this.mPhoneReceiver);
        unregisterReceiver(this.mAddViewReceiver);
        unregisterReceiver(this.mKidsModeReceiver);
        HsmPackageManager.unregisterListener(this.mTipsLifecycle);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(LOG_TAG, "unregisterProcessObserver RemoteException!");
        }
        if (this.mSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
            this.mSettingsObserver = null;
        }
        this.mContext = null;
    }

    private boolean isInCallSatate() {
        if (!MUTIL_CARD_RING.equals(this.mPhoneReceiverState) || 128 == this.mPermissionType) {
            return false;
        }
        HwLog.e(LOG_TAG, "CALL_STATE_RINGING PERMISSION_TYPE_BLOCKED");
        return true;
    }

    public void registerPhoneListener() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(this.mPhoneReceiver, commandFilter);
    }

    private void getPermissionSwitch(Context context) {
        if (CustomizeManager.getInstance().isFeatureEnabled(1)) {
            this.mPermissionSwitch = 1;
        } else {
            this.mPermissionSwitch = 0;
        }
    }

    private void readHuaweiPermissionFromXml(InputStream inStream) throws Throwable {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(inStream, "UTF-8");
        ContentResolver contentResolver = getContentResolver();
        ArrayList<ContentValues> contValuesList = new ArrayList();
        ContentValues mContentValues = new ContentValues();
        boolean hasSub = false;
        int permissionCode = 0;
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            switch (eventType) {
                case 2:
                    String name = parser.getName();
                    if (!"package".equals(name)) {
                        if ("subPermission".equals(name) && hasSub) {
                            permissionCode += getPermissionCode(parser.getAttributeValue(0), parser.nextText());
                            mContentValues.put("permissionCode", Integer.valueOf(permissionCode));
                            break;
                        }
                    }
                    int i = 0;
                    while (i < parser.getAttributeCount()) {
                        if (parser.getAttributeName(i).equals("trust") && parser.getAttributeValue(i).equals("false")) {
                            hasSub = true;
                        }
                        mContentValues.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                        i++;
                    }
                    permissionCode = 0;
                    break;
                case 3:
                    if (!"package".equals(parser.getName())) {
                        break;
                    }
                    if (!hasSub) {
                        mContentValues.put("permissionCode", Integer.valueOf(MONITOR_TYPE));
                    }
                    int cfg = 0;
                    int code = mContentValues.getAsInteger("permissionCode").intValue() | 16;
                    if ((16777216 & code) == 0) {
                        code |= 16777216;
                        cfg = 16777216;
                    }
                    mContentValues.put("permissionCode", Integer.valueOf(code));
                    mContentValues.put("permissionCfg", Integer.valueOf(cfg));
                    contValuesList.add(new ContentValues(mContentValues));
                    permissionCode = 0;
                    mContentValues.clear();
                    hasSub = false;
                    break;
                default:
                    break;
            }
        }
        if (contValuesList.size() > 0) {
            contentResolver.bulkInsert(Uri.parse(CONTENT_PREPERMISSION_URI), (ContentValues[]) contValuesList.toArray(new ContentValues[contValuesList.size()]));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getPermissionCode(String subName, String value) {
        if (subName == null || value == null || "remind".equals(value)) {
            return 0;
        }
        if ("contacts".equals(subName)) {
            return ShareCfg.PERMISSION_GROUP_CONTACT;
        }
        if ("calllog".equals(subName)) {
            return 32770;
        }
        if (Constants.PAYLOAD_MESSAGE.equals(subName)) {
            return 589828;
        }
        if ("location".equals(subName)) {
            return 8;
        }
        if ("phoneNumber".equals(subName)) {
            return 16;
        }
        if ("sendSms".equals(subName)) {
            return 32;
        }
        if ("callPhone".equals(subName)) {
            return 64;
        }
        if ("audioRecord".equals(subName)) {
            return 128;
        }
        if ("camera".equals(subName)) {
            return 1024;
        }
        if ("calendar".equals(subName)) {
            return ShareCfg.PERMISSION_GROUP_CALENDAR;
        }
        return 0;
    }

    private void getCustOrEtcFileData(File file, String spfKey) {
        FileNotFoundException e;
        Throwable e2;
        Throwable th;
        SharedPreferences sharedPreferences = getSharedPreferences(PREPERMISSION_SPF_FILE, 0);
        long fileTime = file.lastModified();
        if (fileTime != sharedPreferences.getLong(spfKey, 0)) {
            ProviderUtils.deleteAll(getApplicationContext(), Uri.parse(CONTENT_PREPERMISSION_URI));
            FileInputStream fileInputStream = null;
            try {
                FileInputStream fileInputStream2 = new FileInputStream(file);
                try {
                    readHuaweiPermissionFromXml(fileInputStream2);
                    Editor editor = sharedPreferences.edit();
                    editor.putLong(spfKey, fileTime);
                    editor.commit();
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    fileInputStream = fileInputStream2;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                e.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Throwable th3) {
                e2 = th3;
                e2.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        }
    }

    public void registerAddViewListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddViewConst.ACTION_ADD_VIEW_ENABLED);
        intentFilter.addAction(AddViewConst.ACTION_ADD_VIEW_PREVENTNOTIFY);
        registerReceiver(this.mAddViewReceiver, intentFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void sendToastBroadcast(int resId, String pkgName) {
        Intent intent = new Intent(HoldServiceConst.ADD_VIEW_TOAST_ACTION);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra(HoldServiceConst.ADD_VIEW_TOAST_CONTENT, "");
        intent.putExtra("toastResId", resId);
        intent.putExtra("packageName", pkgName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void sendPreBlockToastBroadcast(int resId, String pkgName) {
        Intent intent = new Intent(HoldServiceConst.PERMISSION_BLOCKED_TOAST_ACTION);
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra("toastResId", resId);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void sendNotificationBroadcast(Bundle bundle) {
        HwLog.d(LOG_TAG, "sendNotificationBroadcast");
        Intent intent = new Intent(HoldServiceConst.PERMISSION_BLOCKED_ACTION);
        intent.setClass(this.mContext, HoldReceiver.class);
        intent.putExtra("bundle", bundle);
        intent.setPackage(this.mContext.getPackageName());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void recordPermissionAction(String pkgName, int permissionType, int action) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.w(LOG_TAG, "recordPermissionAction pkgName is empty");
            return;
        }
        final HistoryRecord record = new HistoryRecord(pkgName, permissionType, action, 0, 0, System.currentTimeMillis());
        this.mHistoryRecordExecutor.execute(new Runnable() {
            public void run() {
                PermissionDbVisitor.insertHistoryRecord(HoldService.this.mContext, record);
            }
        });
    }

    private void recordPermissionRejectAction(String pkgName, int permissionType) {
        recordPermissionAction(pkgName, permissionType, 10);
    }

    private void recordPermissionAllowAction(String pkgName, int permissionType) {
        recordPermissionAction(pkgName, permissionType, 11);
    }

    private boolean isTopPackageInstaller() {
        boolean z = true;
        HsmPkgInfo piInfo = HsmPackageManager.getInstance().getPkgInfo("com.android.packageinstaller");
        if (piInfo == null) {
            List<RunningTaskInfo> taskInfos = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
            if (taskInfos == null || taskInfos.isEmpty()) {
                HwLog.w(LOG_TAG, "Invalid result of getTopTaskInfo");
                return false;
            }
            RunningTaskInfo info = (RunningTaskInfo) taskInfos.get(0);
            String pkg = info.topActivity == null ? "" : info.topActivity.getPackageName();
            HwLog.i(LOG_TAG, "top activity's pkg:" + pkg);
            return "com.android.packageinstaller".equals(pkg);
        }
        HwLog.i(LOG_TAG, "current foreground uid:" + this.mCurrentForegroundUid + ", pi uid:" + piInfo.mUid);
        if (this.mCurrentForegroundUid != piInfo.mUid) {
            z = false;
        }
        return z;
    }

    public void registerKidsModeListener() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_KIDS_MODE_ENABLED);
        commandFilter.addAction(ACTION_KIDS_MODE_DISABLED);
        registerReceiver(this.mKidsModeReceiver, commandFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private boolean isLocationEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0;
    }
}
