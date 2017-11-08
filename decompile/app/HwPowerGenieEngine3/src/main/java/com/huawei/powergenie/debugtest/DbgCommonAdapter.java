package com.huawei.powergenie.debugtest;

import android.content.Context;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import java.io.PrintWriter;

public class DbgCommonAdapter extends DbgBaseAdapter {
    private Context mContext;

    DbgCommonAdapter(Context context) {
        this.mContext = context;
    }

    protected void startTest(PrintWriter pw) {
        boolean z = false;
        super.startTest(pw);
        Log.i("DbgCommonAdapter", "Common Adapter Test!");
        pw.println("\nCommon Adapter Test!");
        printlnResult("offRRC", getResult(CommonAdapter.offRRC(this.mContext)));
        printlnResult("getBatteryLevelFromNode", Integer.valueOf(CommonAdapter.getBatteryLevelFromNode()));
        printlnResult("setLCDBrightness(100)", getResult(CommonAdapter.setLCDBrightness(this.mContext, 100)));
        printlnResult("setMobileDataEnabled(false)", getResult(CommonAdapter.setMobileDataEnabled(this.mContext, false)));
        String str = "getNetworkMode";
        if (CommonAdapter.getNetworkMode(this.mContext) != -1) {
            z = true;
        }
        printlnResult(str, getResult(z));
        printlnResult("setNetworkMode(1)", getResult(CommonAdapter.setNetworkMode(1)));
        printlnResult("getDefaultSmsApplication", CommonAdapter.getDefaultSmsApplication(this.mContext));
        printlnResult("isHwProduct", Boolean.valueOf(CommonAdapter.isHwProduct()));
        printlnResult("isChinaRegion", Boolean.valueOf(CommonAdapter.isChinaRegion()));
        printlnResult("isChinaMarketProduct", Boolean.valueOf(CommonAdapter.isChinaMarketProduct()));
        printlnResult("getPropLowBatteryLevel", Integer.valueOf(CommonAdapter.getPropLowBatteryLevel()));
    }
}
