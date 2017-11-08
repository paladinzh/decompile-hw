package com.android.mms.ui;

import android.content.Context;
import android.text.InputFilter;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.huawei.mms.ui.EditTextWithSmiley;

public class HwCustRichMessageEditor {
    public void setHwCustRichMessageEditor(Context context, RichMessageEditor editor) {
    }

    public void onKeyboardStateChanged(boolean isHintShowSendIm) {
    }

    public void changeHint(EditTextWithSmiley editText) {
    }

    public boolean isDelete(int index, boolean isDelete) {
        return isDelete;
    }

    public boolean checkftToMms() {
        return false;
    }

    public void setFTtoMmsSendMessageModeAndDeleteChat() {
    }

    public InputFilter createInputFilter(EditTextWithSmiley editText, InputFilter inputFilter) {
        return inputFilter;
    }

    public boolean isEmptyThread(ComposeMessageFragment fragment, long threadId, Conversation conv, WorkingMessage workingMessage) {
        return false;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void changeHint() {
    }

    public void setRcsSaveDraftWhenFt(boolean rcsSaveDraftWhenFt) {
    }

    public boolean getRcsSaveDraftWhenFt() {
        return false;
    }

    public void setRcsLoadDraftFt(boolean rcsLoadDraftFt) {
    }

    public boolean getRcsLoadDraftFt() {
        return false;
    }

    public boolean getSaveMmsEmailAdress() {
        return false;
    }

    public boolean isImUIStyle() {
        return false;
    }
}
