package com.android.rcs.ui.views;

import com.android.rcs.RcsCommonConfig;
import com.huawei.rcs.utils.RcseMmsExt;

public class RcsComposeBottomView {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private ICustComposeBottomViewCallback mCallback;

    public interface ICustComposeBottomViewCallback {
        void setMmsTextViewVisible(boolean z);

        void setSmsTextCountVisible(boolean z);
    }

    public void updateSmsTextCount() {
        if (this.isRcsOn && RcseMmsExt.isRcsMode()) {
            this.mCallback.setSmsTextCountVisible(false);
        }
    }

    public void updateMmsCapacitySize() {
        if (this.isRcsOn && RcseMmsExt.isRcsMode()) {
            this.mCallback.setMmsTextViewVisible(false);
        }
    }

    public void setHwCustCallback(ICustComposeBottomViewCallback callback) {
        this.mCallback = callback;
    }
}
