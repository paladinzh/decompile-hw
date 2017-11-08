package com.huawei.systemmanager.securitythreats.comm;

public class SecurityThreatsConst {
    public static final String ACTION_VIRUS_NEW_INSTALL = "com.huawei.systemmanager.action.VIRUS_NEW";
    public static final String ACTION_VIRUS_NOTIFY = "com.huawei.systemmanager.action.VIRUS_NOTIFY";
    public static final String ACTION_VIRUS_NOTIFY_FINISH = "com.huawei.systemmanager.action.VIRUS_NOTIFY_FINISH";
    public static final String ACTION_VIRUS_PKG_UPDATE = "com.huawei.systemmanager.action.VIRUS_PKGS_UPDATE";
    public static final String BUNDLE_KEY_NEED_CALLBACK = "need_callback";
    public static final String BUNDLE_KEY_NEED_STAT = "need_stat";
    public static final String BUNDLE_KEY_PACKAGE_NAME = "package_name";
    public static final String BUNDLE_KEY_VIRUS_LEVEL = "virus_level";
    public static final String CHECK_UNINSTALL_PKG_NAME = "name";
    public static final String CHECK_UNINSTALL_PKG_PATH = "path";
    public static final String CHECK_UNINSTALL_PKG_SOURCE = "source";
    public static final String CHECK_UNINSTALL_RESULT_CODE = "result_code";
    public static final String CHECK_UNINSTALL_VIRUS_TYPE = "virus_type";
    public static final String METHOD_CHECK_UNINSTALL_APK = "checkUninstallApk";
    public static final String METHOD_NOTIFY_INSTALL_VIRUS = "notifyInstallVirus";
    public static final String PUSH_FILE_FILE_NAME = "hsm_virus_pkgs.xml";
    public static final String PUSH_FILE_MODULE = "HwSystemManager";
    public static final String PUSH_FILE_PACKAGE_NAME = "com.huawei.systemmanager";
    public static final String PUSH_FILE_ROM = "EMUI-ALL";
    public static final String PUSH_FILE_URI = "content://com.huawei.systemmanager.push.provider/HwSystemManager/hsm_virus_pkgs.xml";
    public static final String SEPARATOR = "$$";
    public static final int VIRUS_LEVEL_OK = 0;
    public static final int VIRUS_LEVEL_RISK = 1;
    public static final int VIRUS_LEVEL_UNKNOWN = -1;
    public static final int VIRUS_LEVEL_VIRUS = 2;
}
