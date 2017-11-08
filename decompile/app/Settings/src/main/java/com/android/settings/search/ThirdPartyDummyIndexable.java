package com.android.settings.search;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.settings.ParentControl;
import com.android.settings.Utils;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartyDummyIndexable implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            String screenTitle;
            SearchIndexableRaw data;
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            if (ActivityManager.getCurrentUser() == 0) {
            }
            String action = "com.huawei.android.intent.action.settings.HICLOUD_ENTTRANCE";
            if (Utils.hasIntentActivity(context.getPackageManager(), action)) {
                screenTitle = res.getString(2131627478);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838360;
                data.intentAction = action;
                result.add(data);
            }
            action = "com.huawei.membranetouch.action.MT_SETTINGS";
            if (Utils.hasIntentActivity(context.getPackageManager(), action) && SystemProperties.getBoolean("ro.config.hw_touchplus_enabled", false)) {
                screenTitle = res.getString(2131627765);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838339;
                data.intentAction = action;
                result.add(data);
            }
            if (Utils.isWifiOnly(context)) {
                action = "huawei.intent.action.HSM_TRAFFIC_RANKING_LIST";
            } else {
                action = "huawei.intent.action.HSM_NET_ASSISTANT_MAIN_ACTIVITY";
            }
            if (Utils.hasIntentActivity(context.getPackageManager(), action) && System.getInt(context.getContentResolver(), "hsm_netassistant", 0) != 0) {
                screenTitle = res.getString(2131627578);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838363;
                data.intentAction = action;
                result.add(data);
            }
            action = "huawei.intent.action.POWER_MANAGER";
            if (Utils.hasIntentActivity(context.getPackageManager(), action) && System.getInt(context.getContentResolver(), "hsm_powermanager", 0) != 0) {
                screenTitle = res.getString(2131625949);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838348;
                data.intentAction = action;
                result.add(data);
            }
            action = "huawei.intent.action.NOTIFICATIONMANAGER";
            if (Utils.hasIntentActivity(context.getPackageManager(), action) && System.getInt(context.getContentResolver(), "hsm_notification", 0) != 0) {
                screenTitle = res.getString(2131628598);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838394;
                data.intentAction = action;
                result.add(data);
            }
            action = "huawei.intent.action.HSM_PERMISSION_MANAGER";
            if (Utils.hasIntentActivity(context.getPackageManager(), action) && System.getInt(context.getContentResolver(), "hsm_permission", 0) != 0) {
                screenTitle = res.getString(2131627482);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838398;
                data.intentAction = action;
                result.add(data);
            }
            action = "com.huawei.action.MOTION_SETTINGS";
            if (Utils.hasIntentActivity(context.getPackageManager(), action)) {
                screenTitle = res.getString(2131627412);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838339;
                data.intentAction = action;
                result.add(data);
            }
            action = "com.huawei.essistant.open_main_page";
            if (Utils.hasIntentActivity(context.getPackageManager(), new Intent(action))) {
                screenTitle = res.getString(2131627903);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838339;
                data.intentAction = action;
                result.add(data);
            }
            if (Utils.isOwnerUser()) {
                screenTitle = res.getString(2131627577);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838400;
                data.intentAction = "android.settings.SYSTEM_UPDATE_SETTINGS";
                data.intentTargetPackage = "com.huawei.android.hwouc";
                data.intentTargetClass = "com.huawei.android.hwouc.ui.activities.MainEntranceActivity";
                result.add(data);
            }
            String pkg = "com.huawei.android.airsharing";
            if (Utils.isAirSharingExist(context)) {
                action = "com.huawei.android.airsharing.action.ACTION_DEVICE_SELECTOR";
                screenTitle = res.getString(2131628187);
            } else {
                action = "com.huawei.android.mirrorshare.action.ACTION_DEVICE_SELECTOR";
                screenTitle = res.getString(2131628188);
                pkg = "com.huawei.android.mirrorshare";
            }
            new Intent(action).setPackage(pkg);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838342;
            data.intentAction = action;
            data.intentTargetPackage = pkg;
            if (Utils.isOwnerUser()) {
                result.add(data);
            }
            action = "com.huawei.hwid.ACTION_START_FOR_GOTO_ACCOUNTCENTER";
            if (Utils.hasIntentActivity(context.getPackageManager(), action)) {
                screenTitle = res.getString(2131628189);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838377;
                data.intentAction = action;
                result.add(data);
            }
            if (ParentControl.isParentControlValid(context)) {
                screenTitle = res.getString(2131628199);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838342;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = "com.huawei.parentcontrol.ui.activity.HomeActivity";
                data.intentTargetPackage = "com.huawei.parentcontrol";
                result.add(data);
            }
            action = "huawei.intent.action.WILDKIDS_SETTINGS";
            if (Utils.hasIntentActivity(context.getPackageManager(), action)) {
                screenTitle = res.getString(2131628507);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838342;
                data.intentAction = action;
                result.add(data);
            }
            Intent intent = Utils.getHuaweiBackupIntent(context);
            if (intent != null) {
                screenTitle = res.getString(2131628061);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838342;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = intent.getComponent().getClassName();
                data.intentTargetPackage = intent.getComponent().getPackageName();
                result.add(data);
                screenTitle = res.getString(2131628062);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838342;
                data.intentAction = "com.android.settings.action.unknown";
                data.intentTargetClass = intent.getComponent().getClassName();
                data.intentTargetPackage = intent.getComponent().getPackageName();
                result.add(data);
            }
            return ThirdPartyDummyIndexable.mHwCustSearchIndexProvider.addOtherAppsRawDataToIndex(context, result, res);
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
}
