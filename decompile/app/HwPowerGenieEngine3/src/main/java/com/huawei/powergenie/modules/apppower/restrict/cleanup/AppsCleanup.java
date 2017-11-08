package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.PowerAction;

public final class AppsCleanup {
    private final ExtremeClean mExtremeClean;
    private final ScreenLockClean mScreenLockClean;
    private final ThermalClean mThermalClean;

    public AppsCleanup(ICoreContext coreContext) {
        this.mScreenLockClean = new ScreenLockClean(coreContext);
        this.mExtremeClean = new ExtremeClean(coreContext);
        this.mThermalClean = new ThermalClean(coreContext);
    }

    public void handleStart() {
        if (this.mScreenLockClean != null) {
            this.mScreenLockClean.handleStart();
        }
    }

    public void handleAction(PowerAction action) {
        switch (action.getActionId()) {
            case 208:
            case 230:
                this.mScreenLockClean.handleAppFront(action.getPkgName());
                return;
            case 245:
                this.mScreenLockClean.handleAppStart(action.getPkgName());
                return;
            case 252:
                this.mThermalClean.handleThermalClean(action.getExtraLong());
                return;
            case 263:
                this.mExtremeClean.handleAddTopView(true, action.getExtraInt());
                return;
            case 276:
                this.mScreenLockClean.handleCleanDBChange();
                return;
            case 282:
                this.mExtremeClean.handleReserveAppsChange();
                return;
            case 300:
                this.mScreenLockClean.handleScreenState(true);
                this.mExtremeClean.handleScreenState(true);
                return;
            case 301:
                this.mScreenLockClean.handleScreenState(false);
                this.mExtremeClean.handleScreenState(false);
                return;
            case 350:
                this.mExtremeClean.handlePowerModeChange(action.getExtraInt());
                return;
            default:
                return;
        }
    }
}
