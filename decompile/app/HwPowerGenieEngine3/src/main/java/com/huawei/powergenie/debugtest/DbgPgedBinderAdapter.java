package com.huawei.powergenie.debugtest;

import android.util.Log;
import com.huawei.powergenie.integration.adapter.pged.FreezeInterface;
import com.huawei.powergenie.integration.adapter.pged.KStateInterface;
import com.huawei.powergenie.integration.adapter.pged.KStateMonitor;
import com.huawei.powergenie.integration.adapter.pged.PgedAdapterFactory;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DbgPgedBinderAdapter extends DbgBaseAdapter {
    private FreezeInterface mFreezeAdapter;
    private KStateInterface mKStateAdapter;

    private class KStateMonitorImpl implements KStateMonitor {
        private KStateMonitorImpl() {
        }

        public void onKStateEvent(int type, int value1, String value2, ArrayList<Integer> arrayList) {
            Log.i("DbgPgedBinderAdapter", "onKStateEvent");
        }
    }

    DbgPgedBinderAdapter() {
        this.mFreezeAdapter = null;
        this.mKStateAdapter = null;
        this.mFreezeAdapter = PgedAdapterFactory.getFreezeAdapter();
        this.mKStateAdapter = PgedAdapterFactory.getKStateAdapter(new KStateMonitorImpl());
    }

    protected void startTest(PrintWriter pw) {
        super.startTest(pw);
        Log.i("DbgPgedBinderAdapter", "PgedBinderAdapter Adapter Test!");
        pw.println("\nPgedBinderAdapter Adapter Test!");
        pw.println("  KStateInterface Test :");
        printlnResult("checkPgedRunning()", getResult(this.mKStateAdapter.checkPgedRunning()));
        printlnResult("registerHwPgedListener()", getResult(this.mKStateAdapter.registerHwPgedListener()));
        printlnResult("closeKState()", getResult(this.mKStateAdapter.closeKState(8)));
        printlnResult("openKState()", getResult(this.mKStateAdapter.openKState(8)));
        printlnResult("unregisterHwPgedListener()", getResult(this.mKStateAdapter.unregisterHwPgedListener()));
        pw.println("\n  FreezeInterface Test :");
    }
}
