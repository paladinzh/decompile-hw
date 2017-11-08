package com.android.mms.data;

import android.content.Context;
import java.util.List;

public class HwCustWorkingMessage {
    public void setHwCustWorkingMessage(Context context) {
    }

    public boolean isHasDraft(boolean hasDraft) {
        return hasDraft;
    }

    public boolean isImOnlyConversation() {
        return false;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void asyncDeleteDraftMmsMessageCust(WorkingMessage workingMessage, Conversation conv, Context context) {
    }

    public boolean supportSendToEmail() {
        return false;
    }

    public boolean customizeUpdateState(List<String> list, Conversation conversation) {
        return false;
    }
}
