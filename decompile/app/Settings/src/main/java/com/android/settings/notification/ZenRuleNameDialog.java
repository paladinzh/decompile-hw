package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.util.ArraySet;
import com.android.settings.utils.ZenServiceListing;

public abstract class ZenRuleNameDialog {
    private static final boolean DEBUG = ZenModeSettings.DEBUG;
    private final Context mContext;
    private final AlertDialog mDialog;
    private final ZenRuleInfo[] mExternalRules = new ZenRuleInfo[3];
    private int mSelectedIndex = 0;

    public static class RuleInfo {
    }

    public abstract void onOk(String str, ZenRuleInfo zenRuleInfo);

    public ZenRuleNameDialog(Context context, ZenServiceListing serviceListing, String ruleName, ArraySet<String> arraySet) {
        this.mContext = context;
        String timeRule = this.mContext.getResources().getString(2131628620);
        String eventRule = this.mContext.getResources().getString(2131628619);
        this.mDialog = new Builder(context).setTitle(2131628609).setSingleChoiceItems(new String[]{timeRule, eventRule}, 0, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ZenRuleNameDialog.this.mSelectedIndex = which;
            }
        }).setPositiveButton(2131624573, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ZenRuleNameDialog.this.onOk(null, ZenRuleNameDialog.this.selectedRuleInfo());
            }
        }).setNegativeButton(2131624572, null).create();
    }

    public void show() {
        this.mDialog.show();
    }

    private ZenRuleInfo selectedRuleInfo() {
        switch (this.mSelectedIndex) {
            case 0:
                return defaultNewSchedule();
            case 1:
                return defaultNewEvent();
            default:
                return null;
        }
    }

    private ZenRuleInfo defaultNewSchedule() {
        ScheduleInfo schedule = new ScheduleInfo();
        schedule.days = ZenModeConfig.ALL_DAYS;
        schedule.startHour = 22;
        schedule.endHour = 7;
        ZenRuleInfo rt = new ZenRuleInfo();
        rt.settingsAction = "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS";
        rt.title = this.mContext.getString(2131626790);
        rt.packageName = ZenModeConfig.getEventConditionProvider().getPackageName();
        rt.defaultConditionId = ZenModeConfig.toScheduleConditionId(schedule);
        rt.serviceComponent = ZenModeConfig.getScheduleConditionProvider();
        rt.isSystem = true;
        return rt;
    }

    private ZenRuleInfo defaultNewEvent() {
        EventInfo event = new EventInfo();
        event.calendar = null;
        event.reply = 0;
        ZenRuleInfo rt = new ZenRuleInfo();
        rt.settingsAction = "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS";
        rt.title = this.mContext.getString(2131626792);
        rt.packageName = ZenModeConfig.getScheduleConditionProvider().getPackageName();
        rt.defaultConditionId = ZenModeConfig.toEventConditionId(event);
        rt.serviceComponent = ZenModeConfig.getEventConditionProvider();
        rt.isSystem = true;
        return rt;
    }

    public boolean isShowing() {
        return this.mDialog.isShowing();
    }

    public void dismiss() {
        this.mDialog.dismiss();
    }
}
