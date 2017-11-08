package com.android.contacts.compatibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Process;
import com.android.contacts.detail.EspaceDialer;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.numbermark.NumberMarkUtil;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.util.HwLog;

public class ContactsProviderReceiver extends BroadcastReceiver {
    public void onReceive(final Context c, Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String packageName = uri.getEncodedSchemeSpecificPart();
            boolean isProvider = "com.android.providers.contacts".equals(packageName);
            boolean isContacts = "com.android.contacts".equals(packageName);
            boolean isW3App = "huawei.w3".equals(packageName);
            boolean isHwYellowPage = "com.huawei.yellowpage".equals(packageName);
            boolean isEspace = "com.huawei.espacev2".equals(packageName);
            if (isProvider || isContacts || isW3App || isHwYellowPage || isEspace) {
                String intentAction = intent.getAction();
                Context context = c;
                if (isEspace) {
                    if ("android.intent.action.PACKAGE_ADDED".equals(intentAction)) {
                        EspaceDialer.setIsShowEspace(true);
                        EspaceDialer.setIsCheckSuppot(EspaceDialer.querySupport(c));
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(intentAction)) {
                        EspaceDialer.setIsShowEspace(false);
                        EspaceDialer.setIsCheckSuppot(false);
                    } else if ("android.intent.action.PACKAGE_REPLACED".equals(intentAction)) {
                        EspaceDialer.setIsShowEspace(true);
                        EspaceDialer.setIsCheckSuppot(EspaceDialer.querySupport(c));
                    } else if ("android.intent.action.PACKAGE_CHANGED".equals(intentAction)) {
                        EspaceDialer.setIsShowEspace(CommonUtilMethods.checkApkExist(c, "com.huawei.espacev2"));
                        EspaceDialer.setIsCheckSuppot(EspaceDialer.querySupport(c));
                    }
                    return;
                }
                if (isW3App) {
                    if ("android.intent.action.PACKAGE_ADDED".equals(intentAction)) {
                        NumberMarkUtil.setW3AppInstalled(true);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(intentAction)) {
                        NumberMarkUtil.setW3AppInstalled(false);
                    }
                }
                if ((isProvider && "android.intent.action.PACKAGE_ADDED".equals(intentAction)) || ((isW3App && "android.intent.action.PACKAGE_ADDED".equals(intentAction)) || "android.intent.action.PACKAGE_REMOVED".equals(intentAction) || "android.intent.action.PACKAGE_REPLACED".equals(intentAction))) {
                    new Thread(new Runnable() {
                        public void run() {
                            ProviderFeatureChecker.refreshInstance(c.getApplicationContext());
                        }
                    }).start();
                } else if (isContacts && "android.intent.action.PACKAGE_CHANGED".equals(intentAction)) {
                    BackgroundGenricHandler.getInstance().post(new Runnable() {
                        public void run() {
                            if (SimFactoryManager.isDualSim()) {
                                ContactsProviderReceiver.this.rehandleSimStateLoadedOnPackageChange(SimFactoryManager.getSharedPreferences("SimInfoFile", 0), 0);
                                ContactsProviderReceiver.this.rehandleSimStateLoadedOnPackageChange(SimFactoryManager.getSharedPreferences("SimInfoFile", 1), 1);
                                return;
                            }
                            ContactsProviderReceiver.this.rehandleSimStateLoadedOnPackageChange(SimFactoryManager.getSharedPreferences("SimInfoFile", -1), -1);
                        }
                    });
                } else if (isHwYellowPage && "android.intent.action.PACKAGE_CHANGED".equals(intentAction)) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("ContactsProviderReceiver", "HwYellowPage package change, finish Contacts app.");
                    }
                    Process.killProcess(Process.myPid());
                }
            }
        }
    }

    private void rehandleSimStateLoadedOnPackageChange(SharedPreferences prefs, int slotId) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactsProviderReceiver", "rehandleSimStateLoadedOnPackageChange");
        }
        SimFactoryManager.rehandleSimStateLoaded(prefs, slotId);
    }
}
