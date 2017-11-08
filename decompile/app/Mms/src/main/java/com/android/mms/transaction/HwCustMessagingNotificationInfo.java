package com.android.mms.transaction;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import com.android.mms.data.Contact;

public class HwCustMessagingNotificationInfo {
    public HwCustMessagingNotificationInfo(int extType) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public int getChatType() {
        return 1;
    }

    public boolean appendString(SpannableStringBuilder spannableStringBuilder, Contact sender, long threadId) {
        return false;
    }

    public boolean appendString(SpannableStringBuilder spannableStringBuilder) {
        return false;
    }

    public void setRcsGroupSpanAndSubject(SpannableStringBuilder spannableStringBuilder, Context context, String subject, TextAppearanceSpan notificationSenderSpan, Contact sender) {
    }

    public boolean isGroupChat() {
        return false;
    }

    public String buildRcsMessage(String number, String msg) {
        return msg;
    }

    public String buildRcsMessage(String number, String msg, long threadId) {
        return msg;
    }
}
