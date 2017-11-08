package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;
import com.huawei.systemmanager.mainscreen.view.BatteryIconView;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class EntryPowerMgr extends SimpleEntrace {
    private static final int MAX_BATTERY_PERCENT = 100;
    public static final String NAME = "EntryPowerMgr";
    private int mBatteryPercent = 0;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    EntryPowerMgr.this.refreshBattery(intent.getIntExtra("level", 0));
                }
            }
        }
    };
    private boolean mBatteryReceiverRegisted;
    private BatteryIconView mBatteryView;
    private TextView mTitle;

    public EntryPowerMgr() {
        initBattery();
    }

    public void onResume() {
        registerBatteryReceiver();
    }

    public void onPause() {
        unRegisterBatterReceiver();
    }

    public View createView(LayoutInflater inflater, int position, ViewGroup parent) {
        View view = inflater.inflate(R.layout.main_screen_entry_power_item, parent, false);
        this.mTitle = (TextView) view.findViewById(R.id.title);
        this.mBatteryView = (BatteryIconView) view.findViewById(R.id.icon);
        refreshBattery(this.mBatteryPercent);
        Context ctx = GlobalContext.getContext();
        if (ctx != null) {
            view.setContentDescription(ctx.getString(R.string.power_management_title));
        }
        view.setTag(R.id.convertview_tag_item, this);
        return view;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_POWERMGR;
    }

    public String getEntryName() {
        return NAME;
    }

    private void initBattery() {
        Intent intent = GlobalContext.getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (intent != null) {
            this.mBatteryPercent = intent.getIntExtra("level", 0);
        }
    }

    public void refreshBattery(int percent) {
        this.mBatteryPercent = percent;
        if (this.mTitle != null) {
            Context ctx = GlobalContext.getContext();
            if (percent >= 100) {
                this.mTitle.setText(ctx.getString(R.string.power_management_title));
            } else {
                String percentStr = NumberLocationPercent.getPercent((double) percent);
                this.mTitle.setText(ctx.getString(R.string.main_screen_entry_power_percent_info, new Object[]{percentStr}));
            }
        }
        if (this.mBatteryView != null) {
            this.mBatteryView.setBatteryPercent(percent);
        }
    }

    public void registerBatteryReceiver() {
        if (!this.mBatteryReceiverRegisted) {
            Context ctx = GlobalContext.getContext();
            this.mBatteryReceiverRegisted = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
            ctx.registerReceiver(this.mBatteryReceiver, intentFilter);
        }
    }

    public void unRegisterBatterReceiver() {
        if (this.mBatteryReceiverRegisted) {
            Context ctx = GlobalContext.getContext();
            this.mBatteryReceiverRegisted = false;
            ctx.unregisterReceiver(this.mBatteryReceiver);
        }
    }
}
