package com.huawei.powergenie.integration.adapter.pged;

public final class PgedAdapterFactory {
    public static FreezeInterface getFreezeAdapter() {
        return PgedBinderAdapter.getInstance(null);
    }

    public static KStateInterface getKStateAdapter(KStateMonitor monitor) {
        return PgedBinderAdapter.getInstance(monitor);
    }
}
