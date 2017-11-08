package com.huawei.systemmanager.startupmgr.comm;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.google.android.collect.Lists;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.pfw.HwPFWStartupControlScope;
import huawei.android.pfw.HwPFWStartupSetting;
import huawei.android.pfw.IHwPFWManager;
import huawei.android.pfw.IHwPFWManager.Stub;
import java.util.List;

public class StartupBinderAccess {
    private static final int POLICY_TYPE_AUTO_STARTUP = 3;
    private static final String TAG = "StartupBinderAccess";

    public static void writeSingleDataSettingToFwk(int fwkType, String pkgName, boolean allow) {
        int i = 0;
        if (SysCallUtils.checkUser()) {
            IHwPFWManager itf = getPFWService();
            if (itf != null) {
                List<HwPFWStartupSetting> settingList = Lists.newArrayList();
                if (allow) {
                    i = 1;
                }
                settingList.add(new HwPFWStartupSetting(pkgName, fwkType, i));
                try {
                    itf.updateStartupSettings(settingList, false);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "writeSingleDataSettingToFwk catch RemoteException when updateStartupSettings");
                }
            } else {
                HwLog.w(TAG, "writeSingleDataSettingToFwk can't get valid IHwPFWManager");
            }
            return;
        }
        HwLog.i(TAG, "writeSingleDataSettingToFwk check user failed");
    }

    public static void writeStartupSettingList(List<HwPFWStartupSetting> settingList, boolean clearFirst) {
        if (SysCallUtils.checkUser()) {
            IHwPFWManager itf = getPFWService();
            if (itf != null) {
                try {
                    itf.updateStartupSettings(settingList, clearFirst);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "writeStartupSettingList catch RemoteException when updateStartupSettings");
                }
            } else {
                HwLog.w(TAG, "writeStartupSettingList can't get valid IHwPFWManager");
            }
            return;
        }
        HwLog.i(TAG, "writeStartupSettingList check user failed");
    }

    public static void removeStartupSetting(String pkgName) {
        if (SysCallUtils.checkUser()) {
            IHwPFWManager itf = getPFWService();
            if (itf != null) {
                try {
                    itf.removeStartupSetting(pkgName);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "removeStartupSetting catch RemoteException when updateStartupSettings");
                }
            } else {
                HwLog.w(TAG, "removeStartupSetting can't get valid IHwPFWManager");
            }
            return;
        }
        HwLog.i(TAG, "removeStartupSetting check user failed");
    }

    public static HwPFWStartupControlScope startupXmlControlScope() {
        if (SysCallUtils.checkUser()) {
            IHwPFWManager itf = getPFWService();
            if (itf != null) {
                try {
                    return itf.getStartupControlScope();
                } catch (RemoteException e) {
                    HwLog.e(TAG, "startupXmlControlScope catch RemoteException when updateStartupSettings");
                }
            } else {
                HwLog.w(TAG, "startupXmlControlScope can't get valid IHwPFWManager");
                return null;
            }
        }
        HwLog.i(TAG, "HwPFWStartupControlScope check user failed");
        return null;
    }

    public static void setAutoStartupPolicyEnabled(boolean enabled) {
        if (SysCallUtils.checkUser()) {
            IHwPFWManager itf = getPFWService();
            if (itf != null) {
                try {
                    HwLog.d(TAG, "setAutoStartupPolicyEnabled " + enabled);
                    itf.setPolicyEnabled(3, enabled);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "setAutoStartupPolicyEnabled catch RemoteException when updateStartupSettings");
                }
            } else {
                HwLog.w(TAG, "setAutoStartupPolicyEnabled can't get valid IHwPFWManager");
            }
            return;
        }
        HwLog.i(TAG, "setAutoStartupPolicyEnabled check user failed");
    }

    private static IHwPFWManager getPFWService() {
        if (SysCallUtils.checkUser()) {
            IBinder b = ServiceManager.getService("hwPfwService");
            if (b != null) {
                return Stub.asInterface(b);
            }
            HwLog.w(TAG, "getPFWService can't find service null");
            return null;
        }
        HwLog.i(TAG, "getPFWService check user failed");
        return null;
    }
}
