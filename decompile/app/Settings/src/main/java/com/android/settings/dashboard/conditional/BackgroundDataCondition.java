package com.android.settings.dashboard.conditional;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.NetworkPolicyManager;
import com.android.settings.ItemUseStat;

public class BackgroundDataCondition extends Condition {
    public BackgroundDataCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        setActive(NetworkPolicyManager.from(this.mManager.getContext()).getRestrictBackground());
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838420);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627125);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627126);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627112)};
    }

    public void onPrimaryClick() {
        try {
            Intent dataSaver = new Intent();
            dataSaver.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverActivity");
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "start condition", ItemUseStat.getShortName(dataSaver.getComponent().getClassName()));
            this.mManager.getContext().startActivity(dataSaver);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getMetricsConstant() {
        return 378;
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "condition onActionClick", "DataSaverActivity");
            NetworkPolicyManager.from(this.mManager.getContext()).setRestrictBackground(false);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }
}
