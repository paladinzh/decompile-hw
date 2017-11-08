package com.android.mms.data;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsApp;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageUtil;

public class CryptoConversation {
    public void updateSwitchState(int token, Object cookie, int result) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            Context context = MmsApp.getApplication().getApplicationContext();
            switch (token) {
                case AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL /*1801*/:
                    MLog.d("CryptoConversation", "updateSwitchState: start to update switch state after delete conversations");
                    CryptoMessageUtil.updateSwitchState(context);
                    break;
            }
        }
    }
}
