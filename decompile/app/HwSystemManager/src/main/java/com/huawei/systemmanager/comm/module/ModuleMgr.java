package com.huawei.systemmanager.comm.module;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.Utility;
import java.util.List;

public class ModuleMgr {
    public static final IHsmModule MODULE_ADDVIEW = new ModuleAddViewMonitor();
    public static final IHsmModule MODULE_APPLOCK = new ModuleAppLockMgr();
    public static final IHsmModule MODULE_APPMANAGER = new ModuleAppManager();
    public static final IHsmModule MODULE_BOOTUP = new ModuleStartupMgr();
    public static final IHsmModule MODULE_HARASSMENT = new ModuleHarassmentFilter();
    public static final IHsmModule MODULE_NETWORKAPP = new ModuleNetworkMgr();
    public static final IHsmModule MODULE_NOTIFICATION = new ModuleNotificationMgr();
    public static final IHsmModule MODULE_PERMISSION = new ModulePermissionMgr();
    public static final IHsmModule MODULE_PHONEACCELERATOR = new ModulePhoneAccelerator();
    public static final IHsmModule MODULE_POWERMGR = new ModulePowerMgr();
    public static final IHsmModule MODULE_PROTECTAPP = new ModuleProtectApp();
    public static final IHsmModule MODULE_SECURITYPATCH = new ModuleSecurityPatch();
    public static final IHsmModule MODULE_STORAGECLEANNER = new ModuleStorageCleaner();
    public static final ModuleVirusScanner MODULE_VIRUSSCANNER = new ModuleVirusScanner();

    public static List<IHsmModule> getNormalModules() {
        Object[] entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_NETWORKAPP, MODULE_HARASSMENT, MODULE_POWERMGR, MODULE_PERMISSION, MODULE_VIRUSSCANNER, MODULE_NOTIFICATION, MODULE_BOOTUP, MODULE_APPLOCK, MODULE_APPMANAGER, MODULE_SECURITYPATCH, MODULE_PROTECTAPP};
        if (Utility.isWifiOnlyMode()) {
            entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_POWERMGR, MODULE_NOTIFICATION, MODULE_PERMISSION, MODULE_BOOTUP, MODULE_ADDVIEW, MODULE_VIRUSSCANNER, MODULE_APPMANAGER, MODULE_APPLOCK};
        } else if (Utility.isDataOnlyMode()) {
            entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_POWERMGR, MODULE_NETWORKAPP, MODULE_NOTIFICATION, MODULE_PERMISSION, MODULE_BOOTUP, MODULE_ADDVIEW, MODULE_VIRUSSCANNER, MODULE_APPMANAGER, MODULE_APPLOCK};
        }
        return Lists.newArrayList(entryList);
    }

    public static List<IHsmModule> getAbroadModules() {
        Object[] entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_NETWORKAPP, MODULE_HARASSMENT, MODULE_POWERMGR, MODULE_NOTIFICATION, MODULE_VIRUSSCANNER, MODULE_PERMISSION, MODULE_ADDVIEW, MODULE_BOOTUP, MODULE_APPLOCK, MODULE_APPMANAGER, MODULE_PROTECTAPP};
        if (Utility.isWifiOnlyMode()) {
            entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_POWERMGR, MODULE_NOTIFICATION, MODULE_VIRUSSCANNER, MODULE_ADDVIEW, MODULE_ADDVIEW, MODULE_APPLOCK, MODULE_PERMISSION, MODULE_BOOTUP};
        } else if (Utility.isDataOnlyMode()) {
            entryList = new IHsmModule[]{MODULE_STORAGECLEANNER, MODULE_POWERMGR, MODULE_NETWORKAPP, MODULE_NOTIFICATION, MODULE_VIRUSSCANNER, MODULE_ADDVIEW, MODULE_APPLOCK, MODULE_PERMISSION, MODULE_BOOTUP};
        }
        return Lists.newArrayList(entryList);
    }

    public static List<IHsmModule> getPlugInModules() {
        return Lists.newArrayList(new IHsmModule[0]);
    }
}
