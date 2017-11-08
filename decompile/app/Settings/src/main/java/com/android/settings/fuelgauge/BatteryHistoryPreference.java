package com.android.settings.fuelgauge;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.Utils;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.BatteryInfo.BatteryDataParser;
import com.android.settingslib.R$id;
import com.android.settingslib.graph.UsageView;

public class BatteryHistoryPreference extends Preference {
    private BatteryInfo mBatteryInfo;
    private BatteryStatsHelper mHelper;

    public void performClick() {
        this.mHelper.storeStatsHistoryInFile("tmp_bat_history.bin");
        Bundle args = new Bundle();
        args.putString("stats", "tmp_bat_history.bin");
        args.putParcelable("broadcast", this.mHelper.getBatteryBroadcast());
        Utils.startWithFragment(getContext(), BatteryHistoryDetail.class.getName(), args, null, 0, 2131625972, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mBatteryInfo != null) {
            view.itemView.setClickable(true);
            view.setDividerAllowedAbove(true);
            ((TextView) view.findViewById(2131886276)).setText(this.mBatteryInfo.batteryPercentString);
            ((TextView) view.findViewById(2131886277)).setText(this.mBatteryInfo.remainingLabel);
            UsageView usageView = (UsageView) view.findViewById(2131886278);
            usageView.findViewById(R$id.label_group).setAlpha(0.7f);
            this.mBatteryInfo.bindHistory(usageView, new BatteryDataParser[0]);
        }
    }
}
