package com.huawei.mms.util;

import com.android.mms.MmsApp;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;

public class CryptoHwBackgroundLoader {
    public void initAppSettings() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            CryptoMessageServiceProxy.init(MmsApp.getApplication().getApplicationContext());
        }
    }

    public void onTerminate() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            CryptoMessageServiceProxy.deInit();
        }
    }
}
