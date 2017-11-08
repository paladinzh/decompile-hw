package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.permission.HoldService;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permission.cloud.CloudPermissionControlChange;
import com.huawei.permission.hota.BlackListHotaHandler;
import com.huawei.permission.hota.WhiteListHotaHandler;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.rainbow.vaguerule.VagueManager;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;
import java.util.ArrayList;

public class ComBroadcastReceiver extends HsmBroadcastReceiver {
    private static final String INSTALL_PERMISSION = "com.huawei.install.permission";
    private static final String LOG_TAG = "ComBroadcastReceiver";
    private static final String NOTIFICATION_PRIVATE_ACTIVITY = "com.huawei.permissionmanager.notification.private.activity";
    private static final String NOTIFICATION_PRIVATE_DELETE = "com.huawei.permissionmanager.notification.private.delete";
    private static final int SPINNER_ALLOW = 0;
    private static final int SPINNER_NO_USE = -1;
    private static final int SPINNER_PROHIBIT = 2;
    private static final int SPINNER_REMIND = 1;
    private static SparseArray<String> mNoticeMgrs = new SparseArray();
    private static SparseArray<ArrayList<String>> mPrivateMgrs = new SparseArray();
    private int mCallerUid;

    public static class PermissionSetting {
        private int permissionCfg = 0;
        private int permissionCode = 0;
        private String pkgName;
        private int uid;

        PermissionSetting(String name, int id) {
            this.pkgName = name;
            this.uid = id;
        }

        public String getPkgName() {
            return this.pkgName;
        }

        public int getUid() {
            return this.uid;
        }

        public int getPermissionCode() {
            return this.permissionCode;
        }

        public int getPermissionCfg() {
            return this.permissionCfg;
        }

        public PermissionSetting setPermissionCode(int code) {
            this.permissionCode = code;
            return this;
        }

        public PermissionSetting setPermissionCfg(int cfg) {
            this.permissionCfg = cfg;
            return this;
        }
    }

    private void handleSettingFromIntentForNewPermissions(com.huawei.permissionmanager.ui.ComBroadcastReceiver.PermissionSetting r1, int r2, int r3, int r4) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.ui.ComBroadcastReceiver.handleSettingFromIntentForNewPermissions(com.huawei.permissionmanager.ui.ComBroadcastReceiver$PermissionSetting, int, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.ui.ComBroadcastReceiver.handleSettingFromIntentForNewPermissions(com.huawei.permissionmanager.ui.ComBroadcastReceiver$PermissionSetting, int, int, int):void");
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            HwLog.e(LOG_TAG, "ComBroadcastReceiver null intent!");
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            HwLog.e(LOG_TAG, "ComBroadcastReceiver null action!");
            return;
        }
        HwLog.i(LOG_TAG, "ComBroadcastReceiver action = " + action);
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            context.startService(new Intent(context, HoldService.class));
            HwLog.d(LOG_TAG, "Start Hold Service");
            sendToBackground(context, intent);
        } else if (ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION.equals(action) || "com.rainbow.blacklist.change".equals(action)) {
            sendToBackground(context, intent);
        } else if (action.equals(INSTALL_PERMISSION)) {
            sendToBackground(context, intent);
        } else {
            this.mCallerUid = intent.getIntExtra("callerUid", 0);
            if (action.equals(NOTIFICATION_PRIVATE_ACTIVITY)) {
                mPrivateMgrs.remove(this.mCallerUid);
                Intent appIntent = new Intent();
                appIntent.putExtra(ShareCfg.SINGLE_APP_UID, this.mCallerUid);
                appIntent.putExtra(ShareCfg.SINGLE_APP_LABEL, intent.getStringExtra(ShareCfg.APP_LABEL));
                appIntent.putExtra(ShareCfg.SINGLE_APP_PKGNAME, intent.getStringExtra(ShareCfg.SINGLE_APP_PKGNAME));
                appIntent.setClass(context, SingleAppActivity.class);
                appIntent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
                context.startActivity(appIntent);
            } else if (action.equals(NOTIFICATION_PRIVATE_DELETE)) {
                mPrivateMgrs.remove(this.mCallerUid);
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                int uid = intent.getIntExtra("android.intent.extra.UID", 0);
                mNoticeMgrs.remove(uid);
                mPrivateMgrs.remove(uid);
            } else {
                sendToBackground(context, intent);
            }
        }
    }

    private void setPermissionFromIntent(Context context, Intent intent) {
        if (context == null) {
            HwLog.e(LOG_TAG, "setPermissionFromIntent get the context is null!");
            return;
        }
        Bundle bundle = intent != null ? intent.getExtras() : null;
        if (bundle != null) {
            String pkgName = bundle.getString("packageName");
            int uid = bundle.getInt("uid");
            boolean isTrust = bundle.getBoolean(DBHelper.KEY_TRUST_APP);
            PermissionSetting settingValue = new PermissionSetting(pkgName, uid);
            HwAppPermissions aps = HwAppPermissions.create(context, pkgName);
            getSettingFromIntent(aps, intent, settingValue, 1, "READ_CONTACTS");
            getSettingFromIntent(aps, intent, settingValue, 4, "READ_SMS");
            getSettingFromIntent(aps, intent, settingValue, 2, "READ_CALL_LOG");
            getSettingFromIntent(aps, intent, settingValue, 2048, "READ_CALENDAR");
            getSettingFromIntent(aps, intent, settingValue, 8, "READ_LOCATION");
            getSettingFromIntent(aps, intent, settingValue, 16, "READ_PHONEID");
            getSettingFromIntent(aps, intent, settingValue, 16384, "WRITE_CONTACTS");
            getSettingFromIntent(aps, intent, settingValue, 65536, "WRITE_SMS");
            getSettingFromIntent(aps, intent, settingValue, 32768, "WRITE_CALL_LOG");
            getSettingFromIntent(aps, intent, settingValue, 524288, "DELETE_SMS");
            getSettingFromIntent(aps, intent, settingValue, 64, "MAKE_PHONE");
            getSettingFromIntent(aps, intent, settingValue, 32, "SEND_SMS");
            getSettingFromIntent(aps, intent, settingValue, 1024, "TAKE_PHOTO");
            getSettingFromIntent(aps, intent, settingValue, 128, "SOUND_RECORDER");
            getSettingFromIntent(aps, intent, settingValue, 4194304, "OPEN_NETWORK");
            getSettingFromIntent(aps, intent, settingValue, 2097152, "OPEN_WIFI");
            getSettingFromIntent(aps, intent, settingValue, 8388608, "OPEN_BT");
            getSettingFromIntent(aps, intent, settingValue, 8192, "SEND_MMS");
            getSettingFromIntent(aps, intent, settingValue, 33554432, "PACKAGE_LIST");
            getSettingFromIntent(aps, intent, settingValue, 16777216, "SHORTCUT");
            getSettingFromIntent(aps, intent, settingValue, 2048, "READ_CALENDAR");
            getSettingFromIntent(aps, intent, settingValue, ShareCfg.PERMISSION_MODIFY_CALENDAR, "MODIFY_CALENDAR");
            getSettingFromIntent(aps, intent, settingValue, 1073741824, "ACCESS_BROWSER_RECORDS");
            getSettingFromIntent(aps, intent, settingValue, 1048576, "CALL_FORWARD");
            int compareCode = AppInfo.getComparePermissionCode(context, pkgName);
            int sdkVersion = VagueManager.getInstance(context).getPkgSdkVersion(pkgName);
            handleSettingFromIntentForNewPermissions(settingValue, 1073741824, compareCode, sdkVersion);
            handleSettingFromIntentForNewPermissions(settingValue, 1048576, compareCode, sdkVersion);
            CommonFunctionUtil.uptatePermissionFromPackageInstaller(context, settingValue.getUid(), pkgName, settingValue.getPermissionCode(), settingValue.getPermissionCfg(), isTrust);
            if (isTrust) {
                AddViewAppManager.trust(context, uid, pkgName);
            }
        }
    }

    private void getSettingFromIntent(HwAppPermissions aps, Intent intent, PermissionSetting setting, int codeType, String key) {
        if (intent != null && aps != null) {
            int selectValue = intent.getIntExtra(key, -1);
            switch (selectValue) {
                case 0:
                    setting.setPermissionCode(setting.getPermissionCode() | codeType);
                    if (MPermissionUtil.isClassAType(codeType) || MPermissionUtil.isClassBType(codeType)) {
                        aps.setSystemPermission(codeType, 1, false, "package installer");
                        break;
                    }
                case 1:
                    HwLog.w(LOG_TAG, "should not set remind from package installer.");
                    break;
                case 2:
                    setting.setPermissionCode(setting.getPermissionCode() | codeType);
                    setting.setPermissionCfg(setting.getPermissionCfg() | codeType);
                    if (MPermissionUtil.isClassAType(codeType) || MPermissionUtil.isClassBType(codeType)) {
                        aps.setSystemPermission(codeType, 2, false, "package installer");
                        break;
                    }
                default:
                    HwLog.w(LOG_TAG, "getSettingFromIntent invalid selections:" + selectValue + ", key:" + key);
                    break;
            }
        }
    }

    public void doInBackground(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            new WhiteListHotaHandler(context).handleHotaUpgradeIfNeeded();
            new BlackListHotaHandler(context).handleHotaUpgradeIfNeeded();
        }
        if (ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION.equals(action)) {
            new CloudPermissionControlChange(context).handleWhiteListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        }
        if ("com.rainbow.blacklist.change".equals(action)) {
            new CloudPermissionControlChange(context).handleBlackListChange(intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY), intent.getStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY));
        }
        if ("com.huawei.systemmanager.action.SET_GROUP_PERMISSION".equals(action)) {
            if (CustomizeWrapper.isPermissionEnabled()) {
                String pkg = intent.getStringExtra("packageName");
                String grpName = intent.getStringExtra(DBHelper.PERM_GROUP);
                if (Log.HWINFO) {
                    HwLog.i(LOG_TAG, "receive action " + action + ", pkg:" + pkg + ", grp name:" + grpName);
                }
                if (TextUtils.isEmpty(pkg)) {
                    HwLog.w(LOG_TAG, "Invalid arguments, pkg name" + pkg + ", grpName:" + grpName);
                    return;
                }
            }
            return;
        }
        if ("com.huawei.systemmanager.action.RESET_USER_SETTINGS".equals(action) && Log.HWINFO) {
            HwLog.i(LOG_TAG, "receive action " + action);
        }
        if (INSTALL_PERMISSION.equals(action)) {
            setPermissionFromIntent(context, intent);
        }
    }
}
