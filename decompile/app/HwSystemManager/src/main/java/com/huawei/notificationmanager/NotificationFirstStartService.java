package com.huawei.notificationmanager;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.NotificationBackend;
import com.huawei.notificationmanager.db.DBAdapter;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.notificationmanager.ui.NotificationFirstStartDialog;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class NotificationFirstStartService extends Service {
    private static final String KEY_NOTIFICATION_FIRSTSTART_DIALOG = "notification_firststart_dialog";
    private static final String LOG_TAG = "NotificationStartupService";
    private static final int SHOW_DIALOG_OPEN = 1;
    private final NotificationBackend mBackend = new NotificationBackend();
    private ContentObserver mContentObserver;
    private Context mContext;
    private Runnable mCurrentRunnable;
    private NotificationFirstStartDialog mDialog;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsShowDialog;
    private List<AppInfo> mLaterUpdateInfosForNegative = new ArrayList();
    private List<AppInfo> mLaterUpdateInfosForPositive = new ArrayList();
    private DBAdapter mNMDBAdapter;
    private List<NotificationCfgInfo> mNotificationCfgInfos;
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                NotificationFirstStartService.this.showNoticationChoiceDialogIfNesscessery(uid, pid);
            }
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private ContentObserver mShowDialogContentObserver;

    public interface Callback {
        void onResult(boolean z, NotificationCfgInfo notificationCfgInfo, AppInfo appInfo);
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        this.mHandlerThread = new HandlerThread("NotificationFirstStartService-Thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mNMDBAdapter = new DBAdapter(this.mContext);
        this.mHandler.post(new Runnable() {
            public void run() {
                boolean z = true;
                NotificationFirstStartService notificationFirstStartService = NotificationFirstStartService.this;
                if (Secure.getInt(NotificationFirstStartService.this.getContentResolver(), NotificationFirstStartService.KEY_NOTIFICATION_FIRSTSTART_DIALOG, 1) != 1) {
                    z = false;
                }
                notificationFirstStartService.mIsShowDialog = z;
                NotificationFirstStartService.this.mNotificationCfgInfos = NotificationFirstStartService.this.mNMDBAdapter.getCfgList();
            }
        });
        this.mContentObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                NotificationFirstStartService.this.mNotificationCfgInfos = NotificationFirstStartService.this.mNMDBAdapter.getCfgList();
                List<AppInfo> mTempList = new ArrayList();
                for (AppInfo appinfo : NotificationFirstStartService.this.mLaterUpdateInfosForPositive) {
                    NotificationCfgInfo info = NotificationFirstStartService.this.getNotificationCfgInfo(appinfo.mPkgName);
                    if (info != null) {
                        NotificationFirstStartService.this.saveNotificationChoice(true, info, appinfo);
                        mTempList.add(appinfo);
                    }
                }
                NotificationFirstStartService.this.mLaterUpdateInfosForPositive.removeAll(mTempList);
                mTempList.clear();
                for (AppInfo appinfo2 : NotificationFirstStartService.this.mLaterUpdateInfosForNegative) {
                    info = NotificationFirstStartService.this.getNotificationCfgInfo(appinfo2.mPkgName);
                    if (info != null) {
                        NotificationFirstStartService.this.saveNotificationChoice(false, info, appinfo2);
                        mTempList.add(appinfo2);
                    }
                }
                NotificationFirstStartService.this.mLaterUpdateInfosForNegative.removeAll(mTempList);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(DBProvider.URI_NOTIFICATION_CFG, true, this.mContentObserver);
        this.mShowDialogContentObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                super.onChange(selfChange);
                NotificationFirstStartService notificationFirstStartService = NotificationFirstStartService.this;
                if (Secure.getInt(NotificationFirstStartService.this.getContentResolver(), NotificationFirstStartService.KEY_NOTIFICATION_FIRSTSTART_DIALOG, 1) != 1) {
                    z = false;
                }
                notificationFirstStartService.mIsShowDialog = z;
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(KEY_NOTIFICATION_FIRSTSTART_DIALOG), true, this.mShowDialogContentObserver);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(LOG_TAG, "registerProcessObserver RemoteException!");
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i("LocalService", "Received start id " + startId + ": " + intent);
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(LOG_TAG, "unregisterProcessObserver RemoteException!");
        }
        if (this.mContext != null) {
            if (this.mContentObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
                this.mContentObserver = null;
            }
            if (this.mShowDialogContentObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mShowDialogContentObserver);
                this.mShowDialogContentObserver = null;
            }
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quit();
                this.mHandlerThread = null;
            }
            this.mContext = null;
        }
    }

    private NotificationCfgInfo getNotificationCfgInfo(String packageName) {
        if (packageName == null || this.mNotificationCfgInfos == null) {
            return null;
        }
        for (NotificationCfgInfo cfgInfo : this.mNotificationCfgInfos) {
            if (packageName.equals(cfgInfo.mPkgName)) {
                return cfgInfo;
            }
        }
        return null;
    }

    private void showNoticationChoiceDialogIfNesscessery(final int uid, final int pid) {
        this.mCurrentRunnable = new Runnable() {
            public void run() {
                AppInfo appInfo = ShareLib.getAppInfoByUidAndPid(NotificationFirstStartService.this.mContext, uid, pid, MonitorScenario.SCENARIO_NOTIFICATION_FIRSTSTART);
                if (appInfo == null || appInfo.mPkgName == null) {
                    HwLog.d(NotificationFirstStartService.LOG_TAG, "appinfo is null");
                    return;
                }
                Object valueOf;
                NotificationCfgInfo info = NotificationFirstStartService.this.getNotificationCfgInfo(appInfo.mPkgName);
                String str = NotificationFirstStartService.LOG_TAG;
                StringBuilder append = new StringBuilder().append("showNoticationChoiceDialogIfNesscessery").append(appInfo.mPkgName).append("  ");
                if (info != null) {
                    valueOf = Integer.valueOf(info.mFirstStartCfg);
                } else {
                    valueOf = null;
                }
                HwLog.d(str, append.append(valueOf).toString());
                if ((info == null || info.mFirstStartCfg == 0) && !Utility.isTestApp(appInfo.mPkgName)) {
                    if (NotificationFirstStartService.this.mDialog == null) {
                        NotificationFirstStartService.this.mDialog = new NotificationFirstStartDialog(NotificationFirstStartService.this.mContext, NotificationFirstStartService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null), new Callback() {
                            public void onResult(boolean isPositive, NotificationCfgInfo info, AppInfo appinfo) {
                                if (info != null) {
                                    NotificationFirstStartService.this.saveNotificationChoice(isPositive, info, appinfo);
                                } else if (isPositive) {
                                    NotificationFirstStartService.this.mLaterUpdateInfosForPositive.add(appinfo);
                                } else {
                                    NotificationFirstStartService.this.mLaterUpdateInfosForNegative.add(appinfo);
                                }
                            }
                        });
                    }
                    NotificationFirstStartService.this.mDialog.refresh(info, appInfo);
                    NotificationFirstStartService.this.mDialog.show();
                }
            }
        };
        if (this.mIsShowDialog) {
            this.mHandler.postDelayed(this.mCurrentRunnable, 1500);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveNotificationChoice(boolean isPositive, NotificationCfgInfo info, AppInfo appinfo) {
        if (!(info == null || appinfo == null || info.mFirstStartCfg != 0)) {
            info.mFirstStartCfg = 1;
            if (this.mNMDBAdapter != null) {
                this.mNMDBAdapter.updateCfg(info);
            }
            if (!isPositive) {
                this.mBackend.setNotificationsBanned(appinfo.mPkgName, appinfo.mAppUid, false);
            }
        }
    }
}
