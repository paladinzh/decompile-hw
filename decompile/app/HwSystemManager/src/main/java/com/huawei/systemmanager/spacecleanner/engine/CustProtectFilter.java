package com.huawei.systemmanager.spacecleanner.engine;

import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCustomDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustAppGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;

public class CustProtectFilter implements ITrashFilter {
    private HwAppCustomMgr mAppCustMgr;

    public CustProtectFilter(ScanParams scanParams) {
        this.mAppCustMgr = (HwAppCustomMgr) scanParams.getCarry();
    }

    public void filter(Trash trash) {
        if (this.mAppCustMgr != null && !(trash instanceof HwAppCustomDataTrash) && !(trash instanceof HwCustAppGroup)) {
            for (String file : trash.getFiles()) {
                if (this.mAppCustMgr.isProtectTrash(file)) {
                    trash.removeFile(file);
                }
            }
        }
    }
}
