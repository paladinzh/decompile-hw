package com.huawei.rcs.util;

import android.content.Context;
import android.os.Bundle;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsTransaction;

public class RcsDelaySendManager {
    private static final boolean sIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public boolean isNeedSendDelayRcsMsg(String msgType) {
        boolean z = false;
        if (!sIsRcsOn) {
            return false;
        }
        if (getRcsMsgType(msgType) != 0) {
            z = true;
        }
        return z;
    }

    public void sendDelayRcsMsg(Context context, long msgId, String msgType, Bundle data) {
        if (sIsRcsOn) {
            MLog.i("HwCustDelaySendManagerImpl", "sendDelayRcsMsg: msgType:" + msgType);
            if (data == null) {
                MLog.i("HwCustDelaySendManagerImpl", "sendDelayRcsMsg: data is null");
            }
            switch (getRcsMsgType(msgType)) {
                case 1:
                    RcsTransaction.sendRCSMessageWithDelay(context, msgId, msgType);
                    break;
                case 2:
                    RcsTransaction.sendGroupChatMsgDelay(context, msgId);
                    break;
                case 3:
                    RcsTransaction.sendGroupFileMsgDelay(context, msgId);
                    break;
            }
        }
    }

    private int getRcsMsgType(String msgType) {
        if ("chat".equals(msgType)) {
            return 1;
        }
        if ("rcs_group_text".equals(msgType)) {
            return 2;
        }
        if ("rcs_group_file".equals(msgType)) {
            return 3;
        }
        return 0;
    }
}
