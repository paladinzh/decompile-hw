package com.huawei.systemmanager.customize;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import com.huawei.systemmanager.util.HwLog;

public class HsmCfgIntentService extends IntentService {
    private static final String TAG = "HsmCfgIntentService";
    private Context mContext;

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
    }

    public HsmCfgIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            HwLog.e(TAG, "onHandleIntent get null intent!");
            return;
        }
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            dealWithBootCompleteEvent();
        }
    }

    private void dealWithBootCompleteEvent() {
        ContentResolver contentResover = this.mContext.getContentResolver();
        if (contentResover == null) {
            HwLog.e(TAG, "Get null contentResover!");
            return;
        }
        setSecurityPermissionManagerSwitch(contentResover);
        setSecurityBootStartupSwitch(contentResover);
        setPermissionSwitch(contentResover);
        setBootStartupSwitch(contentResover);
        setDontDisdturbSwitch(contentResover);
        setCommonSwitch(contentResover);
        setAbroadSwitch(contentResover);
        setNetAssistantSwitch(contentResover);
    }

    private void setSecurityPermissionManagerSwitch(ContentResolver contentResover) {
        int permissionCurrentStatus;
        int permissionValue = Secure.getInt(contentResover, OverseaCfgConst.SETTINGS_SECURITY_PERMISSIONMANAGER_STATUS, -1);
        if (CustomizeWrapper.isPermissionEnabled()) {
            permissionCurrentStatus = 1;
        } else {
            permissionCurrentStatus = 0;
        }
        if (permissionValue != permissionCurrentStatus) {
            Secure.putInt(contentResover, OverseaCfgConst.SETTINGS_SECURITY_PERMISSIONMANAGER_STATUS, permissionCurrentStatus);
        }
    }

    private void setSecurityBootStartupSwitch(ContentResolver contentResover) {
        int bootStartupCurrentStatus;
        int bootStartupValue = Secure.getInt(contentResover, OverseaCfgConst.SETTINGS_SECURITY_BOOTSTARTUP_STATUS, -1);
        if (CustomizeWrapper.isBootstartupEnabled()) {
            bootStartupCurrentStatus = 1;
        } else {
            bootStartupCurrentStatus = 0;
        }
        if (bootStartupValue != bootStartupCurrentStatus) {
            Secure.putInt(contentResover, OverseaCfgConst.SETTINGS_SECURITY_BOOTSTARTUP_STATUS, bootStartupCurrentStatus);
        }
    }

    private void setPermissionSwitch(ContentResolver contentResover) {
        int permissionCurrentStatus;
        int permissionValue = System.getInt(contentResover, FearureConfigration.PERMISSION_MANAGER, -1);
        if (CustomizeWrapper.isPermissionEnabled()) {
            permissionCurrentStatus = 1;
        } else {
            permissionCurrentStatus = 0;
        }
        if (permissionValue != permissionCurrentStatus) {
            System.putInt(contentResover, FearureConfigration.PERMISSION_MANAGER, permissionCurrentStatus);
        }
    }

    private void setBootStartupSwitch(ContentResolver contentResover) {
        int bootStartupCurrentStatus;
        int bootStartupValue = System.getInt(contentResover, FearureConfigration.BOOT_STARTUP_MANAGER, -1);
        if (CustomizeWrapper.isBootstartupEnabled()) {
            bootStartupCurrentStatus = 1;
        } else {
            bootStartupCurrentStatus = 0;
        }
        if (bootStartupValue != bootStartupCurrentStatus) {
            System.putInt(contentResover, FearureConfigration.BOOT_STARTUP_MANAGER, bootStartupCurrentStatus);
        }
    }

    private void setDontDisdturbSwitch(ContentResolver resolver) {
        if (System.getInt(resolver, FearureConfigration.NODISTURB_MANAGER, -1) != 0) {
            System.putInt(resolver, FearureConfigration.NODISTURB_MANAGER, 0);
        }
    }

    private void setNetAssistantSwitch(ContentResolver contentResover) {
        if (System.getInt(contentResover, FearureConfigration.NETASSISTANT_MANAGER, -1) != 1) {
            System.putInt(contentResover, FearureConfigration.NETASSISTANT_MANAGER, 1);
        }
    }

    private void setCommonSwitch(ContentResolver contentResover) {
        if (1 != System.getInt(contentResover, FearureConfigration.COMMON_FEATURE, -1)) {
            System.putInt(contentResover, FearureConfigration.PROCESS_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.SPACE_FREE_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.FILE_DELETE_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.PHONE_SCAN_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.HARASSMENT_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.POWER_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.NETWORK_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.NOTIFICATION_MANAGER, 1);
            System.putInt(contentResover, FearureConfigration.PROTECT_APP_MANAGER, 1);
        }
    }

    private void setAbroadSwitch(ContentResolver contentResover) {
        int currentStatus;
        if (AbroadUtils.isAbroad()) {
            currentStatus = 0;
        } else {
            currentStatus = 1;
        }
        int antivirusValue = System.getInt(contentResover, FearureConfigration.ANTIVIRUS_MANAGER, -1);
        int adDetectValue = System.getInt(contentResover, FearureConfigration.AD_DETECT_MANAGER, -1);
        if (currentStatus != antivirusValue) {
            System.putInt(contentResover, FearureConfigration.ANTIVIRUS_MANAGER, currentStatus);
        }
        if (currentStatus != adDetectValue) {
            System.putInt(contentResover, FearureConfigration.AD_DETECT_MANAGER, currentStatus);
        }
    }
}
