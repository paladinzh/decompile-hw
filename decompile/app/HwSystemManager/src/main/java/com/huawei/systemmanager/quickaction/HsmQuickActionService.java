package com.huawei.systemmanager.quickaction;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import com.google.common.collect.Lists;
import com.huawei.android.quickaction.ActionIcon;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import com.huawei.harassmentinterception.ui.InterceptionActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;
import com.huawei.systemmanager.spacecleanner.SpaceManagerActivity;
import java.util.List;

public class HsmQuickActionService extends QuickActionService {
    public List<QuickAction> onGetQuickActions(ComponentName componentName) {
        List<QuickAction> list = Lists.newArrayList();
        ActionIcon scIcon = ActionIcon.createWithResource((Context) this, (int) R.drawable.ic_home_quickaction_storagecleaner);
        ComponentName scCName = new ComponentName(this, SpaceCleanActivity.class);
        list.add(new QuickAction(getString(R.string.systemmanager_module_title_cleanup), scIcon, scCName, getIntentSender(scCName)));
        ActionIcon paIcon = ActionIcon.createWithResource((Context) this, (int) R.drawable.ic_home_quickaction_storage);
        ComponentName paCName = new ComponentName(this, SpaceManagerActivity.class);
        list.add(new QuickAction(getString(R.string.space_clean_space_manager), paIcon, paCName, getSpaceIntentSender(paCName)));
        ActionIcon pmIcon = ActionIcon.createWithResource((Context) this, (int) R.drawable.ic_home_quickaction_batterymanager);
        ComponentName pmCName = new ComponentName(this, HwPowerManagerActivity.class);
        list.add(new QuickAction(getString(R.string.power_title_battery_manager), pmIcon, pmCName, getIntentSender(pmCName)));
        ActionIcon hfIcon = ActionIcon.createWithResource((Context) this, (int) R.drawable.ic_home_quickaction_harassment_filter);
        ComponentName hfCName = new ComponentName(this, InterceptionActivity.class);
        list.add(new QuickAction(getString(R.string.systemmanager_main_view_harassment), hfIcon, hfCName, getIntentSender(hfCName)));
        return list;
    }

    private IntentSender getIntentSender(ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(32768);
        return PendingIntent.getActivity(this, 0, intent, 0).getIntentSender();
    }

    private IntentSender getSpaceIntentSender(ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(32768);
        intent.putExtra(SpaceManagerActivity.KEY_CREATE_NEW_HANDLER_ID, true);
        intent.putExtra(SpaceManagerActivity.KEY_ONLY_SCAN_INTERNAL, false);
        return PendingIntent.getActivity(this, 0, intent, 0).getIntentSender();
    }
}
