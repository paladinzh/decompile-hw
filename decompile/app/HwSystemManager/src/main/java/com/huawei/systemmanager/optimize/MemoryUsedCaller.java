package com.huawei.systemmanager.optimize;

import android.content.Context;
import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.service.CustomCaller;

public class MemoryUsedCaller extends CustomCaller {
    public String getMethodName() {
        return "getMemoryUsed";
    }

    public Bundle call(Bundle params) {
        Context ctx = GlobalContext.getContext();
        long total = MemoryManager.getTotal(ctx);
        long free = MemoryManager.getFreeMemoryWithBackground(ctx);
        long used = total - free;
        int usedPercent = total <= 0 ? 0 : (int) ((100 * used) / total);
        Bundle res = new Bundle();
        res.putLong("totalMemory", total);
        res.putLong("freeMemory", free);
        res.putLong("usedMemory", used);
        res.putInt("usedPercent", usedPercent);
        return res;
    }

    public boolean shouldEnforcePermission() {
        return true;
    }
}
