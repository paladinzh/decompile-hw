package com.huawei.systemmanager.rainbow.comm.meta.item;

import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;

public class CommCfgItem extends AbsConfigItem {
    private int mId = -1;

    private CommCfgItem(int id) {
        this.mId = id;
    }

    public int getCfgItemId() {
        return this.mId;
    }

    public static AbsConfigItem createItem(int id) {
        return new CommCfgItem(id);
    }

    public String toString() {
        return super.toString();
    }
}
