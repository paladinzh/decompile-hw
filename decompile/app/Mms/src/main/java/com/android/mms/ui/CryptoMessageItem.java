package com.android.mms.ui;

import android.text.TextUtils;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;

public class CryptoMessageItem {
    private int mEncryptSmsType = 0;

    public int getEncryptSmsType() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            return this.mEncryptSmsType;
        }
        return 0;
    }

    public void setEncryptSmsType(String messageBody) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && !TextUtils.isEmpty(messageBody)) {
            int type = CryptoMessageServiceProxy.getEncryptedType(messageBody);
            if (4 == type) {
                this.mEncryptSmsType = 1;
            } else if (2 == type) {
                this.mEncryptSmsType = 2;
            } else if (3 == type) {
                this.mEncryptSmsType = 3;
            }
        }
    }

    public boolean isEncryptSms(MessageItem messageItem) {
        boolean z = true;
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !messageItem.isSms()) {
            return false;
        }
        if (!(1 == this.mEncryptSmsType || 2 == this.mEncryptSmsType || 3 == this.mEncryptSmsType)) {
            z = false;
        }
        return z;
    }

    public int getMessageSubId(MessageItem messageItem) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            return messageItem.mSubId;
        }
        return -1;
    }
}
