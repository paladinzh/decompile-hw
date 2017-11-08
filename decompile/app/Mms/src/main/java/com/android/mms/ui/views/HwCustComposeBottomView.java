package com.android.mms.ui.views;

public class HwCustComposeBottomView {

    public interface ICustComposeBottomViewCallback {
        void setMmsTextViewVisible(boolean z);

        void setSmsTextCountVisible(boolean z);
    }

    public void updateSmsTextCount() {
    }

    public void updateMmsCapacitySize() {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void setHwCustCallback(ICustComposeBottomViewCallback callback) {
    }
}
