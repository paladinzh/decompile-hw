package com.android.rcs.transaction;

import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.huawei.rcs.utils.RcsUtility;

public class RcsMessagingNotificationInfo {
    public final int mImChatType;

    public RcsMessagingNotificationInfo() {
        this.mImChatType = 1;
    }

    public RcsMessagingNotificationInfo(int extType) {
        this.mImChatType = extType;
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }

    public boolean isGroupChat() {
        return isRcsSwitchOn() && this.mImChatType == 3;
    }

    public boolean isRcsChat() {
        return isRcsSwitchOn() && this.mImChatType == 2;
    }

    public String buildRcsMessage(String number, String msg, long threadId) {
        if (!isGroupChat()) {
            return msg;
        }
        number = RcsUtility.getGroupContactShowName(number, threadId);
        if (MessageUtils.isNeedLayoutRtl()) {
            return msg + ":" + number;
        }
        return number + ":" + msg;
    }
}
