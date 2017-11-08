package com.huawei.systemmanager.rainbow.client.connect.result;

import com.huawei.systemmanager.rainbow.client.connect.vercfg.AbsVerConfigItem;

class CheckVersionConfig {
    private long mVersionCode = 0;
    private String mVersionName = "";
    private String mVersionUrl = "";

    public CheckVersionConfig(String versionName, long versionCode, String versionUrl) {
        this.mVersionName = versionName;
        this.mVersionCode = versionCode;
        this.mVersionUrl = versionUrl;
    }

    public void setVerConfig(AbsVerConfigItem item) {
        item.setUrlAndVerCode(this.mVersionUrl, this.mVersionCode);
    }

    public String getVersionName() {
        return this.mVersionName;
    }
}
