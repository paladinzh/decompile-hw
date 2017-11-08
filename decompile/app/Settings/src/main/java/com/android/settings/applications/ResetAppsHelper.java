package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager;
import android.net.NetworkPolicyManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.webkit.IWebViewUpdateService;
import java.util.List;

public class ResetAppsHelper implements OnClickListener, OnDismissListener {
    private final AppOpsManager mAom;
    private final Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (30 == msg.what && ResetAppsHelper.this.mListener != null) {
                ResetAppsHelper.this.mListener.onResetCompleted();
            }
        }
    };
    private final IPackageManager mIPm;
    private OnResetCompletedListener mListener;
    private final INotificationManager mNm;
    private final NetworkPolicyManager mNpm;
    private final PackageManager mPm;
    private AlertDialog mResetDialog;
    private final IWebViewUpdateService mWvus;

    public interface OnResetCompletedListener {
        void onResetCompleted();
    }

    public ResetAppsHelper(Context context) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mIPm = Stub.asInterface(ServiceManager.getService("package"));
        this.mNm = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        this.mWvus = IWebViewUpdateService.Stub.asInterface(ServiceManager.getService("webviewupdate"));
        this.mNpm = NetworkPolicyManager.from(context);
        this.mAom = (AppOpsManager) context.getSystemService("appops");
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getBoolean("resetDialog")) {
            buildResetDialog();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mResetDialog != null) {
            outState.putBoolean("resetDialog", true);
        }
    }

    public void stop() {
        if (this.mResetDialog != null) {
            this.mResetDialog.dismiss();
            this.mResetDialog = null;
        }
    }

    void buildResetDialog() {
        if (this.mResetDialog == null) {
            this.mResetDialog = new Builder(this.mContext).setTitle(2131625636).setMessage(2131628290).setPositiveButton(2131625638, this).setNegativeButton(2131624572, null).setOnDismissListener(this).show();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mResetDialog == dialog) {
            this.mResetDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mResetDialog == dialog) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    List<ApplicationInfo> apps = ResetAppsHelper.this.mPm.getInstalledApplications(512);
                    for (int i = 0; i < apps.size(); i++) {
                        ApplicationInfo app = (ApplicationInfo) apps.get(i);
                        try {
                            ResetAppsHelper.this.mNm.setNotificationsEnabledForPackage(app.packageName, app.uid, true);
                        } catch (RemoteException e) {
                        }
                        if (!(app.enabled || ResetAppsHelper.this.mPm.getApplicationEnabledSetting(app.packageName) != 3 || ResetAppsHelper.this.isNonEnableableFallback(app.packageName))) {
                            ResetAppsHelper.this.mPm.setApplicationEnabledSetting(app.packageName, 0, 1);
                        }
                    }
                    try {
                        ResetAppsHelper.this.mIPm.resetApplicationPreferences(UserHandle.myUserId());
                    } catch (RemoteException e2) {
                    }
                    ResetAppsHelper.this.mAom.resetAllModes();
                    Intent intent = new Intent("com.huawei.systemmanager.action.RESET_USER_SETTINGS");
                    intent.setPackage("com.huawei.systemmanager");
                    if (ResetAppsHelper.this.mContext != null) {
                        Log.i("ResetAppsHelper", "send broadcast:" + intent);
                        ResetAppsHelper.this.mContext.sendBroadcast(intent);
                    }
                    int[] restrictedUids = ResetAppsHelper.this.mNpm.getUidsWithPolicy(1);
                    int currentUserId = ActivityManager.getCurrentUser();
                    for (int uid : restrictedUids) {
                        if (UserHandle.getUserId(uid) == currentUserId) {
                            ResetAppsHelper.this.mNpm.setUidPolicy(uid, 0);
                        }
                    }
                    ResetAppsHelper.this.mHandler.sendEmptyMessage(30);
                }
            });
        }
    }

    private boolean isNonEnableableFallback(String packageName) {
        try {
            return this.mWvus.isFallbackPackage(packageName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnResetCompletedListener(OnResetCompletedListener listener) {
        this.mListener = listener;
    }
}
