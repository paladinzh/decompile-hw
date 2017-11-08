package com.android.mms.ui;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.view.View;
import com.android.mms.data.Conversation;
import com.android.mms.data.HwCustConversation;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;

public class HwCustConversationListItem {
    public HwCustConversationListItem(Context context) {
    }

    public void initCustViewStub(View view) {
    }

    public void bindCustView(View view, Conversation mConversation) {
    }

    public void showUndeliveredView(boolean isNotShow) {
    }

    public void addConversationSmileySpans(CharSequence text, SMILEY_TYPE smileyType, SpannableStringBuilder builder, Conversation conversation, SmileyParser sp) {
        sp.addSmileySpans(text, smileyType, builder);
    }

    public String getGroupDefaultName(Context context, String defaultValue, Conversation conv, Resources res) {
        return defaultValue;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void showThumbnailAttachment(ConversationListItem aConversationListItem, HwCustConversation aHwCustConversation, boolean aHasAttachment) {
    }

    public void initlizeThumbnailAttachment(ConversationListItem aConversationListItem) {
    }

    public int getAttachmentVisiblity(int aVisiblity) {
        return aVisiblity;
    }

    public void release() {
    }
}
