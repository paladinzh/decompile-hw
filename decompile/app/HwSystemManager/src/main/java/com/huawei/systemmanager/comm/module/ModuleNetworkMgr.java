package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.ui.NetAssistantMainActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleNetworkMgr extends AbsHsmModule {
    private static HwCustModuleCustomize mCust = ((HwCustModuleCustomize) HwCustUtils.createObj(HwCustModuleCustomize.class, new Object[0]));

    public boolean entryEnabled(Context ctx) {
        if (mCust.hasNetworkCustConfig()) {
            return mCust.networkEntryEnabled();
        }
        return super.entryEnabled(ctx);
    }

    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, NetAssistantMainActivity.class);
    }
}
