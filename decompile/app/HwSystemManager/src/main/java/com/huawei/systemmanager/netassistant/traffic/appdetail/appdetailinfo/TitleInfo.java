package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.SimpleBaseInfo;

public class TitleInfo extends SimpleBaseInfo {
    private String mText;

    public TitleInfo(int resId) {
        this(GlobalContext.getString(resId));
    }

    public TitleInfo(String text) {
        this.mText = text;
    }

    public int getType() {
        return 0;
    }

    public String getTitle() {
        return this.mText;
    }
}
