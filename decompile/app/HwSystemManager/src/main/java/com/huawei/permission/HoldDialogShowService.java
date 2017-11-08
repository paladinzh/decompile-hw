package com.huawei.permission;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.permission.IHoldService.Stub;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.ui.PermissionTableManager;
import com.huawei.permissionmanager.utils.CallBackHelper;
import com.huawei.permissionmanager.utils.CommonCountDownDialog;
import com.huawei.permissionmanager.utils.CountDownDialog;
import com.huawei.permissionmanager.utils.SendGroupSmsCountDownDialog;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.permissionmanager.utils.SmsParseUtil;
import com.huawei.systemmanager.spacecleanner.utils.HwMediaFile;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.Map;

public class HoldDialogShowService extends Service {
    private static final int GROUP_SEND_TIME_LIMIT = 45;
    private static final String LOG_TAG = "HoldDialogShowService";
    private static final String MUTIL_CARD_HOOK = "OFFHOOK";
    private static final String MUTIL_CARD_RING = "RINGING";
    private static final int WAIT_APP_START_TIME = 3000;
    private static final int WAIT_SCREEN_FREEZE_TIME = 150;
    private boolean isInSuperAppList = false;
    private int mAppUid = 0;
    private Binder mBinder = new Binder();
    private CallBackHelper mCallBackHelper = new CallBackHelper() {
        public void callBackAddRecord(int operationType, boolean click, AlertDialog dialog) {
            HoldDialogShowService.this.addRecord(operationType, click, dialog);
        }

        public void callBackRelease(int operationType, boolean stopService) {
            HoldDialogShowService.this.releaseHoldService(operationType, stopService);
        }

        public void addPendingCfg(int uid, int permType, int operationType, String pkg) {
            HoldDialogShowService.this.setPendingCfg(1, uid, permType, operationType, pkg);
        }

        public void removePendingCfg(int uid, int permType, int operationType, String pkg) {
            HoldDialogShowService.this.setPendingCfg(2, uid, permType, operationType, pkg);
        }

        public void stopService() {
            HoldDialogShowService.this.stopSelfService();
        }
    };
    private int mClientCount = 0;
    private Context mContext;
    private String mDialogContent = "";
    private int mDialogCountdownTime = 15;
    private String mDialogTitle = "";
    private Handler mHandler;
    private CountDownDialog mHoldDialog = null;
    private String mLabelName = "";
    private boolean mMayFreeze = false;
    private int mOrientation = 0;
    private int mPermissionCfg = 0;
    private int mPermissionCode = 0;
    private int mPermissionType = 0;
    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null || TextUtils.isEmpty(intent.getAction())) {
                HwLog.e(HoldDialogShowService.LOG_TAG, "mPhoneReceiver null intent!");
                return;
            }
            HoldDialogShowService.this.mPhoneReceiverState = intent.getStringExtra("state");
            HwLog.i(HoldDialogShowService.LOG_TAG, "mPhoneReceiverState = " + HoldDialogShowService.this.mPhoneReceiverState);
            if (HoldDialogShowService.MUTIL_CARD_RING.equals(HoldDialogShowService.this.mPhoneReceiverState) || HoldDialogShowService.MUTIL_CARD_HOOK.equals(HoldDialogShowService.this.mPhoneReceiverState)) {
                HoldDialogShowService.this.releaseHoldServiceForCall();
            }
        }
    };
    private String mPhoneReceiverState = "";
    private String mPkgName = "";
    private String mSmsContent = "";
    private Runnable mStartTimeout = new Runnable() {
        public void run() {
            HoldDialogShowService.this.mMayFreeze = false;
        }
    };
    private Runnable mStopFreezeIfHolding = new Runnable() {
        public void run() {
            if (HoldDialogShowService.this.mHoldDialog != null && HoldDialogShowService.this.mHoldDialog.isShowing()) {
                HoldDialogShowService.this.stopFreeze();
            }
        }
    };

    public void onCreate() {
        super.onCreate();
        HwLog.d(LOG_TAG, "The HoldDialogShowService onCreate!");
        this.mContext = getApplicationContext();
        this.mDialogCountdownTime = getTimerSecond(this.mContext);
        registerPhoneListener();
        initConfiguration();
        this.mHandler = new Handler();
    }

    public void onDestroy() {
        HwLog.d(LOG_TAG, "The HoldDialogShowService destory!");
        unregisterReceiver(this.mPhoneReceiver);
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this) {
            this.mClientCount++;
            HwLog.i(LOG_TAG, "Dialog service start, client count:" + this.mClientCount);
        }
        Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras == null) {
            HwLog.e(LOG_TAG, "onStartCommand intent or bundle null!");
            releaseHoldService(1, true);
            return 2;
        }
        this.mAppUid = extras.getInt(HoldServiceConst.APP_UID, -1);
        this.mPermissionType = extras.getInt("permissionType", 0);
        this.mPkgName = extras.getString("packageName");
        this.mLabelName = HsmPkgUtils.getLableFromPm(this.mContext, this.mPkgName);
        this.mPermissionCode = extras.getInt("permissionCode", 0);
        this.mPermissionCfg = extras.getInt("permissionCfg", 0);
        this.mSmsContent = SmsParseUtil.getSmsContent(extras.getString(HoldServiceConst.GROUP_SMS_CONTENT, ""));
        this.isInSuperAppList = extras.getBoolean(HoldServiceConst.PERMISSION_SUPER_LIST, false);
        if (getDilaogInfo(this.mPermissionType, this.mLabelName)) {
            showHoldDialog();
            return 2;
        }
        HwLog.e(LOG_TAG, "getDilaogInfo error!");
        releaseHoldService(1, true);
        return 2;
    }

    public IBinder onBind(Intent arg0) {
        HwLog.d(LOG_TAG, "onBind start!");
        return this.mBinder;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        int orientation = newConfig.orientation;
        HwLog.i(LOG_TAG, "old orientation:" + this.mOrientation + ", new:" + orientation);
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            onOrientationChanged();
        }
    }

    private void initConfiguration() {
        this.mOrientation = getResources().getConfiguration().orientation;
        HwLog.i(LOG_TAG, "init orientation:" + this.mOrientation);
    }

    private void onOrientationChanged() {
        this.mHandler.postDelayed(this.mStopFreezeIfHolding, 150);
        this.mMayFreeze = true;
        this.mHandler.postDelayed(this.mStartTimeout, 3000);
    }

    private void stopFreeze() {
        IBinder wm = ServiceManager.getService("window");
        if (wm != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                HwLog.i(LOG_TAG, "stop freeze");
                data.writeInterfaceToken("android.view.IWindowManager");
                wm.transact(HwMediaFile.FILE_TYPE_PNG, data, reply, 0);
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            } catch (RemoteException e) {
                HwLog.e(LOG_TAG, "remote exception.");
                e.printStackTrace();
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            } catch (Exception e2) {
                HwLog.e(LOG_TAG, "remote exception.");
                e2.printStackTrace();
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            } catch (Throwable th) {
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            }
        }
    }

    private boolean getDilaogInfo(int permissionType, String appLabel) {
        if (TextUtils.isEmpty(appLabel)) {
            HwLog.e(LOG_TAG, "appLabel isEmpty");
            return false;
        } else if (1000 == permissionType || 1001 == permissionType) {
            HwLog.d(LOG_TAG, "The operation is group send!");
            return true;
        } else {
            Permission permissionObj = PermissionTableManager.getInstance(this.mContext).getPermissionObjectByPermissionType(permissionType);
            if (permissionObj == null) {
                HwLog.e(LOG_TAG, "permissionObj == null");
                return false;
            }
            this.mDialogTitle = permissionObj.getmPermissionNames();
            this.mDialogContent = String.format(permissionObj.getPermissionPopupInfo(), new Object[]{appLabel});
            return true;
        }
    }

    private void showHoldDialog() {
        if (getHoldService() == null) {
            HwLog.e(LOG_TAG, "showHoldDialog get null holdService");
            return;
        }
        HwLog.d(LOG_TAG, "showHoldDialog begin!");
        AppInfo appInfo = new AppInfo();
        appInfo.mAppUid = this.mAppUid;
        appInfo.mAppLabel = this.mLabelName;
        appInfo.mPkgName = this.mPkgName;
        appInfo.mPermissionCode = this.mPermissionCode;
        appInfo.mPermissionCfg = this.mPermissionCfg;
        int themeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        if (1000 == this.mPermissionType || 1001 == this.mPermissionType) {
            this.mHoldDialog = new SendGroupSmsCountDownDialog(this.mContext, themeID, this.mLabelName, this.mSmsContent, appInfo, this.mPermissionType, this.mCallBackHelper);
            this.mHoldDialog.setCountdownTime(45);
        } else {
            this.mHoldDialog = new CommonCountDownDialog(this.mContext, themeID, this.mDialogContent, this.mDialogTitle, appInfo, this.mPermissionType, this.mCallBackHelper, this.isInSuperAppList);
            this.mHoldDialog.setCountdownTime(this.mDialogCountdownTime);
        }
        this.mHoldDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwLog.i(HoldDialogShowService.LOG_TAG, "onDismiss:" + dialog);
                HoldDialogShowService.this.mHoldDialog = null;
            }
        });
        this.mHoldDialog.getWindow().setType(2003);
        this.mHoldDialog.show();
        if (this.mMayFreeze) {
            HwLog.i(LOG_TAG, "going to wait, may freeze?" + this.mMayFreeze);
            stopFreeze();
        }
    }

    private static IHoldService getHoldService() {
        int myUid = UserHandle.myUserId();
        String servicekey = HoldServiceConst.HOLD_SERVICE_NAME;
        if (myUid != 0) {
            servicekey = servicekey + myUid;
        }
        try {
            IBinder binder = ServiceManager.getService(servicekey);
            if (binder != null) {
                return Stub.asInterface(binder);
            }
            HwLog.e(LOG_TAG, "binder is null");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            HwLog.e(LOG_TAG, "getHoldService get Exception!");
            return null;
        }
    }

    private void registerPhoneListener() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(this.mPhoneReceiver, commandFilter);
    }

    private void releaseHoldServiceForCall() {
        if (!(this.mHoldDialog == null || 128 == this.mHoldDialog.mPermissionType)) {
            HwLog.d(LOG_TAG, "releaseHoldServiceForCall remove holdDialog now!");
            this.mHoldDialog.setCallingFlag();
            this.mHoldDialog.cancel();
        }
        IHoldService holdService = getHoldService();
        if (holdService == null) {
            HwLog.e(LOG_TAG, "releaseHoldServiceForCall get null holdService");
            stopSelfService();
            return;
        }
        try {
            holdService.releaseHoldService(this.mAppUid, 2, this.mPermissionType);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            stopSelfService();
        }
    }

    private void releaseHoldService(int operationType, boolean stopService) {
        IHoldService holdService = getHoldService();
        if (holdService == null) {
            HwLog.e(LOG_TAG, "releaseHoldService get null holdService");
            if (stopService) {
                stopSelfService();
            }
            return;
        }
        try {
            holdService.releaseHoldService(this.mAppUid, operationType, this.mPermissionType);
            if (stopService) {
                stopSelfService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            if (stopService) {
                stopSelfService();
            }
        } catch (Throwable th) {
            if (stopService) {
                stopSelfService();
            }
        }
    }

    private void stopSelfService() {
        synchronized (this) {
            this.mClientCount--;
            HwLog.i(LOG_TAG, "dialog service stop, client count:" + this.mClientCount);
            if (this.mClientCount == 0) {
                stopSelf();
            }
        }
    }

    private void addRecord(int operationType, boolean click, AlertDialog dialog) {
        if (this.mHoldDialog != null) {
            HwLog.d(LOG_TAG, "addRecord remove holdDialog now!");
            this.mHoldDialog.cancel();
        }
        if (dialog != null) {
            if (dialog.isShowing()) {
                HwLog.w(LOG_TAG, "we have a isolate dialog!");
            }
            HwLog.d(LOG_TAG, "remove original dialog now!");
            dialog.cancel();
        }
        IHoldService holdService = getHoldService();
        if (holdService == null) {
            HwLog.e(LOG_TAG, "showHoldDialog get null holdService");
            return;
        }
        try {
            holdService.addRecord(this.mAppUid, this.mPermissionType, operationType, click);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setPendingCfg(int addOrRemove, int uid, int permType, int operationType, String pkg) {
        Bundle params = new Bundle();
        params.putInt(HoldServiceConst.EXTRA_CODE, addOrRemove);
        params.putInt("uid", uid);
        params.putInt("permissionType", permType);
        params.putInt(HoldServiceConst.EXTRA_MODE, operationType);
        params.putString("packageName", pkg);
        IHoldService holdService = getHoldService();
        if (holdService == null) {
            HwLog.e(LOG_TAG, "showHoldDialog get null holdService");
        } else {
            try {
                holdService.callHsmService("setPendingCfg", params);
            } catch (RemoteException e) {
            }
        }
    }

    private int getTimerSecond(Context context) {
        Map<String, Integer> permissionParameter = ShareLib.findPermissionParameterFromXml(context);
        if (permissionParameter.get("timer_second") != null) {
            return ((Integer) permissionParameter.get("timer_second")).intValue();
        }
        return 15;
    }
}
