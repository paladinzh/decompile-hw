package com.huawei.systemmanager.rainbow.client.connect.vercfg;

public class RecommendConfig extends AbsVerConfigItem {
    public RecommendConfig(String spfKey) {
        super(spfKey);
    }

    protected String defaultSubUrl() {
        return "getRecApkRight.do";
    }
}
