package com.android.settings.dashboard.conditional;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import com.android.settings.ItemUseStat;

public class CellularDataCondition extends Condition {

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ANY_DATA_STATE".equals(intent.getAction())) {
                Condition cdc = ConditionManager.get(context).getCondition(CellularDataCondition.class);
                if (cdc != null) {
                    cdc.refreshState();
                }
            }
        }
    }

    public CellularDataCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        boolean z = false;
        TelephonyManager telephony = (TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class);
        if (((ConnectivityManager) this.mManager.getContext().getSystemService(ConnectivityManager.class)).isNetworkSupported(0) && telephony.getSimState() == 5) {
            if (!telephony.getDataEnabled()) {
                z = true;
            }
            setActive(z);
            return;
        }
        setActive(false);
    }

    protected Class<?> getReceiverClass() {
        return Receiver.class;
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838419);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627123);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627124);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627113)};
    }

    public void onPrimaryClick() {
        try {
            Intent networkSettings = new Intent();
            networkSettings.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "start condition", ItemUseStat.getShortName(networkSettings.getComponent().getClassName()));
            this.mManager.getContext().startActivity(networkSettings);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ((TelephonyManager) this.mManager.getContext().getSystemService(TelephonyManager.class)).setDataEnabled(true);
            setActive(false);
            ItemUseStat.getInstance().handleClick(this.mManager.getContext(), 13, "condition onActionClick", "MobileNetworkSettings");
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 380;
    }
}
