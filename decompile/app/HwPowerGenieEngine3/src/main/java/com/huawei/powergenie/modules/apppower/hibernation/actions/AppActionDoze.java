package com.huawei.powergenie.modules.apppower.hibernation.actions;

import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;

public class AppActionDoze extends AppAction {
    public AppActionDoze(AppStateRecord record) {
        super(record);
    }

    public void performAction() {
        ASHLog.d("perform doze actions!");
    }

    public void clearAction() {
        ASHLog.d("clear doze actions!");
    }

    public void handleSreenUnlock() {
        cancelUnifiedHeartbeat("D unlock");
    }
}
