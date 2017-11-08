package com.huawei.systemmanager.rainbow.client.connect.vercfg;

import com.google.common.base.Strings;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic;

public abstract class AbsVerConfigItem {
    private String mSpfKey = "";
    private String mUrl = "";
    private long mVer = 0;

    protected abstract String defaultSubUrl();

    public AbsVerConfigItem(String spkKey) {
        this.mSpfKey = spkKey;
    }

    public void setUrlAndVerCode(String url, long ver) {
        this.mUrl = url;
        this.mVer = ver;
    }

    public long getVersion() {
        return this.mVer;
    }

    public void setVersion(long ver) {
        this.mVer = ver;
    }

    public String getSpfKey() {
        return this.mSpfKey;
    }

    public String getUrl() {
        if (Strings.isNullOrEmpty(this.mUrl)) {
            return RainbowRequestBasic.getUrlForCommon(defaultSubUrl());
        }
        return this.mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }
}
