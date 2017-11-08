package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo.TrafficData;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficState;
import com.huawei.systemmanager.util.HwLog;

public class EntryNetwork extends SimpleEntrace {
    public static final String NAME = "EntryNetwork";
    private Runnable mLoadDataRunable = new Runnable() {
        public void run() {
            final String showInfo = EntryNetwork.this.getShowInfo();
            EntryNetwork.this.mUiHanlder.post(new Runnable() {
                public void run() {
                    if (EntryNetwork.this.mTitleTv != null) {
                        EntryNetwork.this.mTitleTv.setText(showInfo);
                    }
                }
            });
        }
    };
    private TextView mTitleTv;
    private Handler mUiHanlder = new Handler(Looper.getMainLooper());

    public void onResume() {
        refreshData();
    }

    protected int getIconResId() {
        return R.drawable.ic_data_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_mobiledata;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_NETWORKAPP;
    }

    public String getEntryName() {
        return NAME;
    }

    protected void onCreateView(View container) {
        this.mTitleTv = (TextView) container.findViewById(R.id.title);
    }

    public void refreshData() {
        SERIAL_EXECUTOR.execute(this.mLoadDataRunable);
    }

    private String getShowInfo() {
        Context ctx = getContext();
        if (ctx == null) {
            HwLog.e(NAME, "getShowInfo ctx is null!");
            return "";
        }
        String imsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        if (TextUtils.isEmpty(imsi)) {
            HwLog.i(NAME, "no preferred data card");
            return ctx.getString(R.string.systemmanager_module_title_mobiledata);
        } else if (!NatSettingManager.hasPackageSet(imsi)) {
            return ctx.getString(R.string.systemmanager_module_title_mobiledata);
        } else {
            int yearMonth = DateUtil.getYearMonth(imsi);
            int trafficState = TrafficState.getCurrentTrafficState(imsi);
            ITrafficInfo info = ITrafficInfo.create(imsi, yearMonth, trafficState);
            long total = info.getTotalLimit();
            if (trafficState == 303 || total >= 0) {
                int i;
                TrafficData td = info.getTrafficData();
                new StringBuilder().append(td.getTrafficLeftData()).append(td.getTrafficLeftUnit());
                if (td.isOverData()) {
                    i = R.string.main_screen_entry_network_beyond;
                } else {
                    i = R.string.main_screen_entry_network_rest;
                }
                return ctx.getString(i, new Object[]{builder.toString()});
            }
            HwLog.i(NAME, "not set traffic data");
            return ctx.getString(R.string.systemmanager_module_title_mobiledata);
        }
    }
}
