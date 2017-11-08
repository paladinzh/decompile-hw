package com.android.systemui.statusbar.policy;

import android.text.Editable;
import android.widget.EditText;

public class HwCustRemoteInputView {
    protected RemoteInputView mRemoteInputView;

    public HwCustRemoteInputView(RemoteInputView remoteInputView) {
        this.mRemoteInputView = remoteInputView;
    }

    public void setOnePageSmsText(Editable s, EditText redit) {
    }
}
