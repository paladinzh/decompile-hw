package com.huawei.powergenie.core;

import android.content.Context;
import android.os.Looper;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.modulesmanager.ModuleManager;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import com.huawei.powergenie.integration.adapter.MultiWinServiceAdapter;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.adapter.PGManagerAdapter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public final class CoreContext implements ICoreContext {
    private final HashMap<String, Object> CACHE_SERVICES = new HashMap();
    private final Context mContext;
    private ModuleManager mModuleManager;

    protected CoreContext(Context context) {
        this.mContext = context;
    }

    public void addAction(int modId, int actionId) {
        if (this.mModuleManager != null) {
            this.mModuleManager.addAction(modId, actionId);
        }
    }

    public void removeAction(int modId, int actionId) {
        if (this.mModuleManager != null) {
            this.mModuleManager.removeAction(modId, actionId);
        }
    }

    public void removeAllActions(int modId) {
        if (this.mModuleManager != null) {
            this.mModuleManager.removeAllActions(modId);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public Looper getActionsHandlerLooper() {
        return this.mModuleManager.getActionsHandlerLooper();
    }

    protected void registerService(String serviceName, Object service) {
        this.CACHE_SERVICES.put(serviceName, service);
        if ("module".equals(serviceName)) {
            this.mModuleManager = (ModuleManager) service;
        }
    }

    public Object getService(String name) {
        return this.CACHE_SERVICES.get(name);
    }

    public boolean isScreenOff() {
        return ((IDeviceState) getService("device")).isScreenOff();
    }

    public boolean isHisiPlatform() {
        if (NativeAdapter.getPlatformType() == 3 || NativeAdapter.getPlatformType() == 2) {
            return true;
        }
        return false;
    }

    public boolean isQcommPlatform() {
        return NativeAdapter.getPlatformType() == 0;
    }

    public boolean isMultiWinDisplay() {
        return MultiWinServiceAdapter.getMWMaintained() == 1;
    }

    public boolean releaseRRC() {
        return CommonAdapter.offRRC(this.mContext);
    }

    public boolean setCABC(boolean savePower) {
        return NativeAdapter.setCABC(savePower);
    }

    public boolean setLcdRatio(int ratio, boolean autoAdjust) {
        return PGManagerAdapter.setLcdRatio(ratio, autoAdjust);
    }

    public boolean configBrightnessRange(boolean updateRange) {
        return CommonAdapter.configBrightnessRange(this.mContext, updateRange);
    }

    public boolean isRestartAfterCrash() {
        return ((IDeviceState) getService("device")).isRestartAfterCrash();
    }

    public void dumpAll(PrintWriter pw, String[] args, String serviceName) {
        BaseService service;
        if (serviceName != null) {
            service = (BaseService) this.CACHE_SERVICES.get(serviceName);
            if (service != null) {
                service.dump(pw, args);
            }
            return;
        }
        BaseService dbgtest = null;
        for (Entry entry : this.CACHE_SERVICES.entrySet()) {
            service = (BaseService) entry.getValue();
            String name = (String) entry.getKey();
            if ("dbgtest".equals(name)) {
                dbgtest = service;
            } else if (!"module".equals(name)) {
                service.dump(pw, args);
            }
        }
        this.mModuleManager.dump(pw, args);
        if (dbgtest != null) {
            dbgtest.dump(pw, args);
        }
    }
}
