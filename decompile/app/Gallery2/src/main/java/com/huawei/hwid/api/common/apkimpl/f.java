package com.huawei.hwid.api.common.apkimpl;

class f implements Runnable {
    final /* synthetic */ OtaDownloadActivity a;

    f(OtaDownloadActivity otaDownloadActivity) {
        this.a = otaDownloadActivity;
    }

    public void run() {
        this.a.finish();
    }
}
