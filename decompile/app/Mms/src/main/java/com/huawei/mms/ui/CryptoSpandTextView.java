package com.huawei.mms.ui;

import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.util.TextSpan;
import java.util.List;

public class CryptoSpandTextView {
    private ClickCallback mClickCallback;
    private List<TextSpan> mEncryptListTextSpan;
    private int mEncryptSmsType = 0;

    public interface ClickCallback {
        void onClickDone();
    }

    public void setSmsType(int smsType) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mEncryptSmsType = smsType;
        }
    }

    public void setClickCallback(ClickCallback clickCallback) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mClickCallback = clickCallback;
        }
    }

    public void onSpandTextViewClickEvent(boolean isDoubleClick) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && !isDoubleClick && this.mClickCallback != null && isSmsEncrypted()) {
            this.mClickCallback.onClickDone();
        }
    }

    private boolean isSmsEncrypted() {
        if (1 == this.mEncryptSmsType || 3 == this.mEncryptSmsType || 2 == this.mEncryptSmsType) {
            return true;
        }
        return false;
    }

    public void setEncryptSpanList(List<TextSpan> listSpan) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mEncryptListTextSpan = listSpan;
        }
    }
}
