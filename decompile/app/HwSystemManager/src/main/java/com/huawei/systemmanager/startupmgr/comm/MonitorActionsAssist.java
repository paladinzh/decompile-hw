package com.huawei.systemmanager.startupmgr.comm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antimal.UserBehaviorManager;
import java.util.List;

public class MonitorActionsAssist {
    private static final String PACKAGE_SCHEMA_NAME = "package";
    private static final String PACKAGE_SCHEMA_PARAM = "com.fake.package";
    private static final List<ActionCfg> mActionCfgList = Lists.newArrayList();

    public static class ActionCfg {
        String mAction;
        String mDataParam = null;
        String mDataSchema = null;
        boolean mIsBootAction;
        int mResId = 0;

        ActionCfg(String action, boolean isBootAction, int resId) {
            this.mAction = action;
            this.mIsBootAction = isBootAction;
            this.mResId = resId;
        }

        ActionCfg(String action, String dataSchema, String dataParam, boolean isBootAction, int resId) {
            this.mAction = action;
            this.mDataSchema = dataSchema;
            this.mDataParam = dataParam;
            this.mIsBootAction = isBootAction;
            this.mResId = resId;
        }

        public String actionName() {
            return this.mAction;
        }

        public boolean isBootAction() {
            return this.mIsBootAction;
        }

        public String resString(Context ctx) {
            if (this.mResId == 0) {
                return this.mAction;
            }
            return ctx.getString(this.mResId);
        }

        public boolean match(String action) {
            return this.mAction.equals(action);
        }

        public Intent getSearchIntent() {
            Intent intent = new Intent(this.mAction);
            if (!(this.mDataSchema == null || this.mDataParam == null)) {
                intent.setData(Uri.fromParts(this.mDataSchema, this.mDataParam, null));
            }
            return intent;
        }
    }

    static {
        mActionCfgList.add(new ActionCfg("android.intent.action.BOOT_COMPLETED", true, R.string.startupmgr_action_description_boot_completed));
        mActionCfgList.add(new ActionCfg("android.net.conn.CONNECTIVITY_CHANGE", false, R.string.startupmgr_action_description_connectivity_action));
        mActionCfgList.add(new ActionCfg("android.intent.action.PACKAGE_ADDED", "package", PACKAGE_SCHEMA_PARAM, false, R.string.startupmgr_action_description_package_added));
        mActionCfgList.add(new ActionCfg("android.intent.action.PACKAGE_REPLACED", "package", PACKAGE_SCHEMA_PARAM, false, R.string.startupmgr_action_description_package_replaced));
        mActionCfgList.add(new ActionCfg("android.intent.action.PACKAGE_REMOVED", "package", PACKAGE_SCHEMA_PARAM, false, R.string.startupmgr_action_description_package_removed));
        mActionCfgList.add(new ActionCfg("android.intent.action.PACKAGE_DATA_CLEARED", false, R.string.startupmgr_action_description_package_data_cleared));
        mActionCfgList.add(new ActionCfg("android.intent.action.PACKAGE_RESTARTED", false, R.string.startupmgr_action_description_package_restarted));
        mActionCfgList.add(new ActionCfg("android.intent.action.SCREEN_ON", false, R.string.startupmgr_action_description_screen_on));
        mActionCfgList.add(new ActionCfg("android.intent.action.SCREEN_OFF", false, R.string.startupmgr_action_description_screen_off));
        mActionCfgList.add(new ActionCfg("android.intent.action.DATE_CHANGED", false, R.string.startupmgr_action_description_date_changed));
        mActionCfgList.add(new ActionCfg(UserBehaviorManager.ACTION_DATE_CHANGED, false, R.string.startupmgr_action_description_time_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.TIMEZONE_CHANGED", false, R.string.startupmgr_action_description_time_zone_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.USER_PRESENT", false, R.string.startupmgr_action_description_user_present));
        mActionCfgList.add(new ActionCfg("android.intent.action.ACTION_POWER_CONNECTED", false, R.string.startupmgr_action_description_power_connected));
        mActionCfgList.add(new ActionCfg("android.intent.action.ACTION_POWER_DISCONNECTED", false, R.string.startupmgr_action_description_power_disconnected));
        mActionCfgList.add(new ActionCfg("android.intent.action.BATTERY_CHANGED", false, R.string.startupmgr_action_description_battery_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.BATTERY_LOW", false, R.string.startupmgr_action_description_battery_low));
        mActionCfgList.add(new ActionCfg("android.intent.action.BATTERY_OKAY", false, R.string.startupmgr_action_description_battery_low));
        mActionCfgList.add(new ActionCfg("android.intent.action.CLOSE_SYSTEM_DIALOGS", false, R.string.startupmgr_action_description_close_system_dialog));
        mActionCfgList.add(new ActionCfg("android.intent.action.CONFIGURATION_CHANGED", false, R.string.startupmgr_action_description_configuration_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.LOCALE_CHANGED", false, R.string.startupmgr_action_description_locale_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.PHONE_STATE", false, R.string.startupmgr_action_description_phone_state_changed));
        mActionCfgList.add(new ActionCfg("android.bluetooth.adapter.action.STATE_CHANGED", false, R.string.startupmgr_action_description_bluetooth_state_changed));
        mActionCfgList.add(new ActionCfg("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED", false, R.string.startupmgr_action_description_bluetooth_connection_state_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.DEVICE_STORAGE_LOW", false, R.string.startupmgr_action_description_device_storage_low));
        mActionCfgList.add(new ActionCfg("android.intent.action.DEVICE_STORAGE_OK", false, R.string.startupmgr_action_description_device_storage_ok));
        mActionCfgList.add(new ActionCfg("android.intent.action.INPUT_METHOD_CHANGED", false, R.string.startupmgr_action_description_input_method_changed));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_BUTTON", false, R.string.startupmgr_action_description_media_button));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_BAD_REMOVAL", false, R.string.startupmgr_action_description_media_bad_removal));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_CHECKING", false, R.string.startupmgr_action_description_media_checking));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_EJECT", false, R.string.startupmgr_action_description_media_eject));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_MOUNTED", false, R.string.startupmgr_action_description_media_mounted));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_REMOVED", false, R.string.startupmgr_action_description_media_removed));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_SCANNER_FINISHED", false, R.string.startupmgr_action_description_media_scanner_finished));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_SCANNER_STARTED", false, R.string.startupmgr_action_description_media_scanner_started));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_SHARED", false, R.string.startupmgr_action_description_media_shared));
        mActionCfgList.add(new ActionCfg("android.intent.action.MEDIA_UNMOUNTED", false, R.string.startupmgr_action_description_media_unmounted));
        mActionCfgList.add(new ActionCfg("android.intent.action.NEW_OUTGOING_CALL", false, R.string.startupmgr_action_description_new_outgoing_call));
    }

    public static List<String> convertReadableActions(Context ctx, List<String> actions) {
        List<String> result = Lists.newArrayList();
        for (String action : actions) {
            result.add(actionDescptionString(ctx, action));
        }
        return result;
    }

    public static List<ActionCfg> copyOfMonitorActions() {
        return Lists.newArrayList(mActionCfgList);
    }

    private static String actionDescptionString(Context ctx, String action) {
        for (ActionCfg cfg : mActionCfgList) {
            if (cfg.match(action)) {
                return cfg.resString(ctx);
            }
        }
        return action;
    }
}
