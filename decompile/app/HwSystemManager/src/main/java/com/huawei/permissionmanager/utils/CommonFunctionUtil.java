package com.huawei.permissionmanager.utils;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.provider.Settings.Secure;
import com.android.internal.telephony.SmsApplication;
import com.huawei.csp.util.MmsInfo;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.util.HwLog;

public class CommonFunctionUtil {
    private static final String LOG_TAG = "CommonFunctionUtil";
    private static final int[] SMS_PERMISSIONS = new int[]{4, 32, 8192};
    private static String sHwOrginalSmsPackageName = null;

    public static int uptateHsmPermissionsForTrust(android.content.Context r1, int r2, java.lang.String r3, int r4, int r5, boolean r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.utils.CommonFunctionUtil.uptateHsmPermissionsForTrust(android.content.Context, int, java.lang.String, int, int, boolean):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.utils.CommonFunctionUtil.uptateHsmPermissionsForTrust(android.content.Context, int, java.lang.String, int, int, boolean):int");
    }

    public static int uptatePermissionFromPackageInstaller(android.content.Context r1, int r2, java.lang.String r3, int r4, int r5, boolean r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.utils.CommonFunctionUtil.uptatePermissionFromPackageInstaller(android.content.Context, int, java.lang.String, int, int, boolean):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.utils.CommonFunctionUtil.uptatePermissionFromPackageInstaller(android.content.Context, int, java.lang.String, int, int, boolean):int");
    }

    public static void changeSmsPermission(Context context) {
        boolean needReset = false;
        String oldPackageName = Secure.getString(context.getContentResolver(), ShareCfg.KEY_DEFAULT_SMS_APP);
        ComponentName currentSmsComponent = SmsApplication.getDefaultSmsApplication(context, false);
        if (currentSmsComponent != null) {
            String currentPackageName = currentSmsComponent.getPackageName();
            try {
                HwAppPermissions aps = HwAppPermissions.create(context, currentPackageName);
                HwLog.i(LOG_TAG, "changeSmsPermission, allow sms for " + currentPackageName + ", prev:" + oldPackageName);
                aps.grantRuntimePermission(MPermissionUtil.GROUP_SMS, null);
                AppInfo info = aps.getAppInfo();
                if (info != null) {
                    DBAdapter.setSinglePermission(context, info.mAppUid, info.mPkgName, 8192, 1);
                } else {
                    HwLog.w(LOG_TAG, "changeSmsPermission, appinfo null.");
                }
            } catch (Exception e) {
                HwLog.e(LOG_TAG, "currentPackageName " + currentPackageName, e);
            }
            updateDefaultSmsApp(context, currentPackageName);
            if (!currentPackageName.equals(oldPackageName)) {
                needReset = true;
            }
        } else if (oldPackageName != null) {
            HwLog.e(LOG_TAG, "No default SMS app has been found");
            updateDefaultSmsApp(context, null);
            needReset = true;
        }
        if (needReset && oldPackageName != null && !"com.android.mms".equals(oldPackageName)) {
            try {
                restorePermissionFromCloud(context, HwAppPermissions.create(context, oldPackageName), SMS_PERMISSIONS);
            } catch (Exception e2) {
                HwLog.e(LOG_TAG, "oldPackageName " + oldPackageName, e2);
            }
        }
    }

    public static void restorePermissionFromCloud(Context context, HwAppPermissions aps, int[] permissionTypes) {
        if (permissionTypes == null || aps.getAppInfo() == null) {
            HwLog.w(LOG_TAG, "restorePermissionFromCloud will no types:" + aps);
            return;
        }
        int uid = aps.getAppInfo().mAppUid;
        String pkg = aps.getAppInfo().mPkgName;
        ContentValues value = DBAdapter.getInitialConfig(context, pkg, uid, true);
        if (value == null) {
            HwLog.w(LOG_TAG, "restorePermissionFromCloud value is null");
            return;
        }
        Integer code = value.getAsInteger("permissionCode");
        Integer cfg = value.getAsInteger("permissionCfg");
        if (code == null || cfg == null) {
            HwLog.w(LOG_TAG, "restorePermissionFromCloud code or cfg is null");
            return;
        }
        for (int type : permissionTypes) {
            int v = DBAdapter.getValue(type, code.intValue(), cfg.intValue());
            HwLog.i(LOG_TAG, "restorePermissionFromCloud code:" + code + ", cfg:" + cfg + ", v:" + v);
            if (MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type)) {
                aps.setSystemPermission(type, v, false, "restore");
            } else if (MPermissionUtil.isClassEType(type)) {
                DBAdapter.setSinglePermission(context, uid, pkg, type, v);
            }
        }
    }

    public static void updateDefaultSmsApp(Context context, String packageName) {
        Secure.putString(context.getContentResolver(), ShareCfg.KEY_DEFAULT_SMS_APP, packageName);
    }

    public static boolean isSmsPermission(int permissionType) {
        for (int permission : SMS_PERMISSIONS) {
            if (permission == permissionType) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDefaultSmsApp(Context context, String packageName) {
        try {
            ComponentName currentSmsComponent = SmsApplication.getDefaultSmsApplication(context, false);
            if (currentSmsComponent == null) {
                HwLog.e(LOG_TAG, "there is no default SMS app!");
            } else if (currentSmsComponent.getPackageName().equals(packageName)) {
                HwLog.i(LOG_TAG, "pkg:" + packageName + " is default sms app");
                return true;
            }
            return false;
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "packageName " + packageName, e);
            return false;
        }
    }

    public static boolean isDefaultSmsPermission(Context context, String packageName, int permissionType) {
        if (isSmsPermission(permissionType)) {
            return isDefaultSmsApp(context, packageName);
        }
        return false;
    }

    public static void requestDefaultSmsAppActivity(Context context, String packageName) {
        Intent intent = new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT");
        intent.putExtra("package", packageName);
        if (context != null) {
            context.startActivity(intent);
        }
    }

    public static String getHwOrginalSmsPackageName(Context context) {
        if (sHwOrginalSmsPackageName != null) {
            return sHwOrginalSmsPackageName;
        }
        sHwOrginalSmsPackageName = MmsInfo.getSmsAppName(context);
        if (sHwOrginalSmsPackageName == null) {
            HwLog.e(LOG_TAG, "can not find SMS app!");
        }
        return sHwOrginalSmsPackageName;
    }

    public static boolean isPackageHasPhoneStatePermission(PackageInfo pkgInfo) {
        if (pkgInfo.applicationInfo == null) {
            return false;
        }
        String[] permissions = pkgInfo.requestedPermissions;
        if (permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ShareCfg.PHONE_STATE_PERMISSION.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPackageHasShortCutPermission(PackageInfo pkgInfo) {
        if (pkgInfo.applicationInfo == null) {
            return false;
        }
        String[] permissions = pkgInfo.requestedPermissions;
        if (permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ShareCfg.INSTALL_SHORTCUT_PERMISSION.equals(permission) || ShareCfg.UNINSTALL_SHORTCUT_PERMISSION.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean getTrustValue(ContentValues contentValues) {
        if (contentValues.getAsInteger("trust").intValue() == 1) {
            return true;
        }
        return false;
    }

    public static boolean checkAppTrustStatus(Context context, String pkg, int uid, HwAppPermissions aps, boolean dbTrust) {
        HwLog.i(LOG_TAG, "checkAppTrustStatus for " + pkg + ", db trust:" + dbTrust + ", aps:" + aps);
        if (!dbTrust) {
            return false;
        }
        boolean checked = AddViewAppManager.getInstance(context).getCurrentAppAddviewValue(pkg);
        if (!checked) {
            HwLog.i(LOG_TAG, "checkAppTrustStatus return false caused by add view value:" + checked);
            DBAdapter.updateTrustValue(context, uid, pkg, 0);
            return false;
        } else if (aps != null && aps.allPermissionsAllowed()) {
            return true;
        } else {
            DBAdapter.updateTrustValue(context, uid, pkg, 0);
            return false;
        }
    }
}
