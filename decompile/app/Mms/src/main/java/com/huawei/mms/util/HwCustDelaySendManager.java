package com.huawei.mms.util;

import android.content.Context;
import android.os.Bundle;

public class HwCustDelaySendManager {
    public boolean isNeedSendDelayRcsMsg(String msgType) {
        return false;
    }

    public void sendDelayRcsMsg(Context context, long msgId, String msgType, Bundle data) {
    }
}
