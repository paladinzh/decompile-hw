package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import com.android.settingslib.WirelessUtils;

public class AirplaneModeCondition extends Condition {

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                Condition cdc = ConditionManager.get(context).getCondition(AirplaneModeCondition.class);
                if (cdc != null) {
                    cdc.refreshState();
                }
            }
        }
    }

    public void refreshState() {
        setActive(WirelessUtils.isAirplaneModeOn(this.mManager.getContext()));
    }

    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838343);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627118);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627119);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627112)};
    }

    public void onPrimaryClick() {
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ConnectivityManager.from(this.mManager.getContext()).setAirplaneMode(false);
            setActive(false);
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 377;
    }
}
