package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.SimpleBaseInfo;

public class NormalTextInfo extends SimpleBaseInfo {
    private String mText;

    public NormalTextInfo(int resId) {
        this(GlobalContext.getString(resId));
    }

    public NormalTextInfo(String text) {
        this.mText = text;
    }

    public String getSubTitle() {
        return this.mText;
    }

    public int getType() {
        return 2;
    }
}
