package com.huawei.powergenie.modules.apppower.hibernation.actions;

import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;

public class AppActionRunning extends AppAction {
    public AppActionRunning(AppStateRecord record) {
        super(record);
    }

    public void performAction() {
        ASHLog.i("perform running actions!");
        if (!this.mAppRecord.isScreenOff() && isUnifiedHeartbeat()) {
            cancelUnifiedHeartbeat("R scron");
        }
    }

    public void clearAction() {
        ASHLog.d("clear running actions!");
    }

    public void handleSreenUnlock() {
        cancelUnifiedHeartbeat("R unlock");
    }
}
