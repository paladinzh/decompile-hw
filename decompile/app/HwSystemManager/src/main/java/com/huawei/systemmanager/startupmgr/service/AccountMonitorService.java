package com.huawei.systemmanager.startupmgr.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.startupmgr.comm.StartupFwkConst;
import com.huawei.systemmanager.startupmgr.confdata.PaymentPkgChecker;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class AccountMonitorService extends Service implements OnAccountsUpdateListener {
    private static final String ACTION_STARTUP_CONFIRM = "com.huawei.android.hsm.STARTUP_CONFIRM";
    private static final int CALLER_PID = Integer.MAX_VALUE;
    private static final String HSM_PACKAGE_NAME = "com.huawei.systemmanager";
    private static final String RECORD_TYPE_SERVICE = "s";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "AccountMonitorService";
    private final ArrayList<String> mAddedAccountList = new ArrayList();
    private Context mAppContext = null;
    private final ArrayList<String> mRemovedAccountList = new ArrayList();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        HwLog.i(TAG, "oncreate");
        super.onCreate();
        this.mAppContext = getApplicationContext();
        AccountManager am = AccountManager.get(this.mAppContext);
        if (am != null) {
            am.addOnAccountsUpdatedListener(this, null, true);
        }
        PaymentPkgChecker.getInstance().init(this.mAppContext);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public void onDestroy() {
        HwLog.i(TAG, "onDestroy");
        AccountManager am = AccountManager.get(this.mAppContext);
        if (am != null) {
            am.removeOnAccountsUpdatedListener(this);
        }
        super.onDestroy();
    }

    public void onAccountsUpdated(Account[] accounts) {
        if (isPolicyEnable() && Utility.isOwner()) {
            this.mRemovedAccountList.clear();
            if (accounts == null || accounts.length == 0) {
                this.mAddedAccountList.clear();
                return;
            }
            ArrayList<String> accountPkgNameList = getAccountPkgName(accounts);
            if (accountPkgNameList.size() > this.mAddedAccountList.size()) {
                for (String pkgName : accountPkgNameList) {
                    if (!this.mAddedAccountList.contains(pkgName)) {
                        this.mAddedAccountList.add(pkgName);
                        startConfirmService(pkgName);
                    }
                }
            } else if (accountPkgNameList.size() < this.mAddedAccountList.size()) {
                for (String pkgname : this.mAddedAccountList) {
                    if (!accountPkgNameList.contains(pkgname)) {
                        this.mRemovedAccountList.add(pkgname);
                    }
                }
                this.mAddedAccountList.removeAll(this.mRemovedAccountList);
            }
            HwLog.i(TAG, "mAddedAccountList = " + this.mAddedAccountList);
        }
    }

    private void startConfirmService(String pkgName) {
        Bundle bundle = new Bundle();
        bundle.putString(StartupFwkConst.KEY_TARGET_PACKAGE, pkgName);
        bundle.putString("B_CALLER_TYPE", "s");
        bundle.putInt(StartupFwkConst.KEY_CALLER_PID, Integer.MAX_VALUE);
        bundle.putInt(StartupFwkConst.KEY_CALLER_UID, 1000);
        Intent intentService = new Intent("com.huawei.android.hsm.STARTUP_CONFIRM");
        intentService.setPackage("com.huawei.systemmanager");
        intentService.putExtras(bundle);
        try {
            this.mAppContext.startServiceAsUser(intentService, UserHandle.CURRENT);
        } catch (RuntimeException e) {
            HwLog.w(TAG, "startService failed!");
        }
    }

    private Boolean isSystemAppOrPayApp(String pkgName) {
        boolean z = true;
        if (pkgName == null) {
            return Boolean.valueOf(true);
        }
        if (!isSystemUnRemovableApp(pkgName)) {
            z = PaymentPkgChecker.isPaymentPkg(pkgName);
        }
        return Boolean.valueOf(z);
    }

    public boolean isSystemUnRemovableApp(String pkgName) {
        try {
            ApplicationInfo ai = this.mAppContext.getPackageManager().getApplicationInfo(pkgName, 0);
            boolean isSystemUnRemovableApp = (ai.flags & 1) != 0 ? ai.hwFlags == 0 : false;
            return isSystemUnRemovableApp;
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "get application failed!");
            return false;
        }
    }

    private ArrayList<String> getAccountPkgName(Account[] accounts) {
        ArrayList<String> accountPkgNameList = new ArrayList();
        for (Account account : accounts) {
            String pkgName = getPackageForType(account.type);
            if (!isSystemAppOrPayApp(pkgName).booleanValue()) {
                accountPkgNameList.add(pkgName);
            }
        }
        return accountPkgNameList;
    }

    private String getPackageForType(String accountType) {
        AccountManager am = AccountManager.get(this.mAppContext);
        if (am == null) {
            return null;
        }
        AuthenticatorDescription[] authDescs = am.getAuthenticatorTypes();
        for (int i = 0; i < authDescs.length; i++) {
            if (TextUtils.equals(authDescs[i].type, accountType)) {
                return authDescs[i].packageName;
            }
        }
        return null;
    }

    private boolean isPolicyEnable() {
        return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }
}
