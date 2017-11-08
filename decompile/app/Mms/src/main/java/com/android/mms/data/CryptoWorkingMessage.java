package com.android.mms.data;

import android.content.Context;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;

public class CryptoWorkingMessage {
    public boolean isCryptoMessage(Context context, ContactList recipients) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return false;
        }
        boolean messageEncrypted = CryptoMessageUtil.isSmsEncryptionSwitchOn(context, recipients);
        MLog.d("CryptoWorkingMessage", "isCryptoMessage: messageEncrypted=" + messageEncrypted);
        return messageEncrypted;
    }

    public String localEncrypt(String msg, int subID, Context context, ContactList recipients) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isCryptoMessage(context, recipients)) {
            return CryptoMessageServiceProxy.localEncrypt(msg, subID);
        }
        return msg;
    }
}
