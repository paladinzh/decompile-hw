package com.android.mms.ui.views;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import com.android.mms.ui.views.ComposeRecipientsView.IRecipientsHoler;
import com.huawei.mms.ui.HwRecipientsEditor;

public class HwCustComposeRecipientsView {
    public void setContactIntent(Intent intent) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean postRcsDelayed(Handler h, Runnable r) {
        return false;
    }

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return false;
    }

    public void checkUpdateTitle(HwRecipientsEditor mRecipientsEditor, int count, IRecipientsHoler mHolder) {
    }

    public void updateTitle(IRecipientsHoler mHolder, View v) {
    }
}
