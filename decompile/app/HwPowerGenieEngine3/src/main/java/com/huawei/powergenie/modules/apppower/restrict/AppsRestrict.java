package com.huawei.powergenie.modules.apppower.restrict;

import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.modules.apppower.restrict.alarmcontrol.AlarmRestrict;
import com.huawei.powergenie.modules.apppower.restrict.cleanup.AppsCleanup;
import com.huawei.powergenie.modules.apppower.restrict.gpsrestrict.AppsGPSRestrict;

public final class AppsRestrict {
    private AlarmRestrict mAlarmRestrict;
    private AppsCleanup mAppsCleanup;
    private AppsGPSRestrict mAppsGPSRestrict;
    private DeviceIdleCtrl mDeviceIdleCtrl;

    public AppsRestrict(ICoreContext coreContext) {
        this.mAppsCleanup = new AppsCleanup(coreContext);
        this.mAlarmRestrict = new AlarmRestrict(coreContext);
        this.mAppsGPSRestrict = new AppsGPSRestrict(coreContext);
        this.mDeviceIdleCtrl = new DeviceIdleCtrl(coreContext);
    }

    public void handleStart() {
        if (this.mAppsCleanup != null) {
            this.mAppsCleanup.handleStart();
        }
        if (this.mAlarmRestrict != null) {
            this.mAlarmRestrict.handleStart();
        }
    }

    public boolean handleAction(PowerAction action) {
        if (this.mAppsCleanup != null) {
            this.mAppsCleanup.handleAction(action);
        }
        if (this.mAlarmRestrict != null) {
            this.mAlarmRestrict.handleAction(action);
        }
        if (this.mAppsGPSRestrict != null) {
            this.mAppsGPSRestrict.handleAction(action);
        }
        if (this.mDeviceIdleCtrl != null) {
            this.mDeviceIdleCtrl.handleAction(action);
        }
        return true;
    }
}
