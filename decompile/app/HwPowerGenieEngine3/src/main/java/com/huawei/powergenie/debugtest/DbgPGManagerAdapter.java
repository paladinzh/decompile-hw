package com.huawei.powergenie.debugtest;

import android.util.Log;
import com.huawei.powergenie.integration.adapter.PGManagerAdapter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DbgPGManagerAdapter extends DbgBaseAdapter {
    private List<String> mPkgList = new ArrayList<String>() {
        {
            add("com.huawei.superassistant");
        }
    };
    private List<String> mProxyActionList = new ArrayList<String>() {
        {
            add("android.intent.action.ANY_DATA_STATE");
        }
    };

    DbgPGManagerAdapter() {
    }

    protected void startTest(PrintWriter pw) {
        boolean z;
        super.startTest(pw);
        Log.i("DbgPGManagerAdapter", "PGManager Adapter Test!");
        pw.println("\nPGManager Adapter Test!");
        String str = "proxyBroadcast";
        if (PGManagerAdapter.proxyBroadcast(this.mPkgList, true) != -1) {
            z = true;
        } else {
            z = false;
        }
        printlnResult(str, getResult(z));
        printlnResult("proxyBCConfig", getResult(PGManagerAdapter.proxyBCConfig(4, "20", null)));
        printlnResult("setProxyBCActions", getResult(PGManagerAdapter.setProxyBCActions(this.mProxyActionList)));
        printlnResult("setActionExcludePkg", getResult(PGManagerAdapter.setActionExcludePkg((String) this.mProxyActionList.get(0), (String) this.mPkgList.get(0))));
        printlnResult("proxyWakeLockByPidUid", getResult(PGManagerAdapter.proxyWakeLockByPidUid(-1, -1, true)));
        printlnResult("forceReleaseWakeLockByPidUid", getResult(PGManagerAdapter.forceReleaseWakeLockByPidUid(-1, -1)));
        printlnResult("forceRestoreWakeLockByPidUid", getResult(PGManagerAdapter.forceRestoreWakeLockByPidUid(-1, -1)));
        str = "isHoldWakeLock";
        if (PGManagerAdapter.isHoldWakeLock(-1, -1) != -1) {
            z = true;
        } else {
            z = false;
        }
        printlnResult(str, getResult(z));
        printlnResult("setLcdRatio", getResult(PGManagerAdapter.setLcdRatio(100, true)));
        printlnResult("proxyApp", getResult(PGManagerAdapter.proxyApp((String) this.mPkgList.get(0), -1, true)));
    }
}
