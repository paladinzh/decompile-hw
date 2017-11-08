package com.android.settings.dashboard.conditional;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.PersistableBundle;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import com.android.settings.ItemUseStat;

public class DndCondition extends Condition {
    private ZenModeConfig mConfig;
    private int mZen;

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL".equals(intent.getAction())) {
                Condition cdc = ConditionManager.get(context).getCondition(DndCondition.class);
                if (cdc != null) {
                    cdc.refreshState();
                }
            }
        }
    }

    public DndCondition(ConditionManager manager) {
        super(manager);
        this.mManager.getContext().getPackageManager().setComponentEnabledSetting(new ComponentName(this.mManager.getContext(), Receiver.class), 1, 1);
    }

    public void refreshState() {
        boolean zenModeEnabled = false;
        NotificationManager notificationManager = (NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class);
        this.mZen = notificationManager.getZenMode();
        if (this.mZen != 0) {
            zenModeEnabled = true;
        }
        if (zenModeEnabled) {
            this.mConfig = notificationManager.getZenModeConfig();
        } else {
            this.mConfig = null;
        }
        setActive(zenModeEnabled);
    }

    boolean saveState(PersistableBundle bundle) {
        bundle.putInt("state", this.mZen);
        return super.saveState(bundle);
    }

    void restoreState(PersistableBundle bundle) {
        super.restoreState(bundle);
        this.mZen = bundle.getInt("state", 0);
    }

    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    private CharSequence getZenState() {
        switch (this.mZen) {
            case 1:
                return this.mManager.getContext().getString(2131626718);
            case 2:
                return this.mManager.getContext().getString(2131626720);
            case 3:
                return this.mManager.getContext().getString(2131626719);
            default:
                return null;
        }
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838389);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627120, new Object[]{getZenState()});
    }

    public CharSequence getSummary() {
        boolean isForever;
        if (this.mConfig == null || this.mConfig.manualRule == null) {
            isForever = false;
        } else {
            boolean z;
            if (this.mConfig.manualRule.conditionId == null) {
                z = true;
            } else {
                z = false;
            }
            isForever = z;
        }
        if (isForever) {
            return this.mManager.getContext().getString(17040807);
        }
        String resName = ZenModeConfig.getConditionSummary(this.mManager.getContext(), this.mConfig, ActivityManager.getCurrentUser(), false);
        Resources res = this.mManager.getContext().getResources();
        int resId = res.getIdentifier(resName, "string", "android");
        Log.d("DndCondition", "summary, get resource id: " + resId + ", name: " + resName);
        if (resId != 0) {
            resName = res.getString(resId);
        }
        return resName;
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627112)};
    }

    public void onPrimaryClick() {
        try {
            Intent dNdSettings = new Intent();
            dNdSettings.setClassName("com.android.settings", "com.android.settings.Settings$ZenModeSettingsActivity");
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "start condition", ItemUseStat.getShortName(dNdSettings.getComponent().getClassName()));
            this.mManager.getContext().startActivity(dNdSettings);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ((NotificationManager) this.mManager.getContext().getSystemService(NotificationManager.class)).setZenMode(0, null, "DndCondition");
            setActive(false);
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "condition onActionClick", "Settings$ZenModeSettingsActivity");
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 381;
    }
}
