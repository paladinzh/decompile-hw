package com.huawei.systemmanager.mainscreen.entrance;

import com.huawei.systemmanager.comm.module.IHsmModule;
import java.util.List;

public class HwCustBaseEntryMgr {
    public boolean needUseCustModules() {
        return false;
    }

    public List<IHsmModule> getCustModules() {
        return null;
    }
}
