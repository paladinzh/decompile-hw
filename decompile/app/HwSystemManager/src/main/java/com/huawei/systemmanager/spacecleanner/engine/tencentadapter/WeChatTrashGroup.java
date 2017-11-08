package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;

public class WeChatTrashGroup extends TrashGroup {
    private String mName;
    private int mWeChatTrashType;

    public WeChatTrashGroup(int subType, String name) {
        super(1048576);
        this.mWeChatTrashType = subType;
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public int getWeChatTrashType() {
        return this.mWeChatTrashType;
    }
}
