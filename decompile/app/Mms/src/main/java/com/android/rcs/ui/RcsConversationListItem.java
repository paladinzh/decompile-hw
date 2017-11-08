package com.android.rcs.ui;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsUtility;

public class RcsConversationListItem {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;
    private ImageView mGroupNotDisturbImg;
    private View mUndeliveredIndicator;

    public RcsConversationListItem(Context context) {
        this.mContext = context;
    }

    public void initCustViewStub(View view) {
        if (this.isRcsOn) {
            ViewStub undeliveredStub = (ViewStub) view.findViewById(R.id.stub_rcs_undelivered_icon);
            undeliveredStub.setLayoutResource(R.layout.rcs_undelivered_image);
            this.mUndeliveredIndicator = (ImageView) undeliveredStub.inflate();
            if (this.mUndeliveredIndicator == null) {
                MLog.w("HwCustConversationListItemImpl", "undelivered img cant findviewby");
            }
            this.mGroupNotDisturbImg = (ImageView) view.findViewById(R.id.rcs_group_not_disturb_image);
            if (this.mGroupNotDisturbImg == null) {
                MLog.w("HwCustConversationListItemImpl", "rcs group not disturb img cant findviewby");
            }
        }
    }

    public void bindCustView(View view, Conversation conversation) {
        if (this.isRcsOn && conversation != null && conversation.getHwCust() != null) {
            this.mGroupNotDisturbImg.setVisibility(8);
            if (isGroupChat(conversation)) {
                if (conversation.getHwCust().isGroupChatNotDisturb(this.mContext, conversation.getThreadId())) {
                    this.mGroupNotDisturbImg.setVisibility(0);
                } else {
                    this.mGroupNotDisturbImg.setVisibility(8);
                }
            }
        }
    }

    public boolean isGroupChat(Conversation conversation) {
        return conversation.getHwCust().getRcsThreadType() == 4;
    }

    public void showUndeliveredView(boolean isNotShow) {
        if (this.isRcsOn) {
            updateVisibility(this.mUndeliveredIndicator, 8);
        }
    }

    private void updateVisibility(View v, int visibility) {
        if (visibility != v.getVisibility()) {
            v.setVisibility(visibility);
        }
    }

    public void addConversationSmileySpans(CharSequence text, SMILEY_TYPE smileyType, SpannableStringBuilder builder, Conversation conversation, SmileyParser sp) {
        if (!this.isRcsOn || conversation == null || conversation.getHwCust() == null) {
            sp.addSmileySpans(text, smileyType, builder);
            return;
        }
        CharSequence custSnippet = RcsUtility.getCustSnippet(text, conversation, this.mContext);
        int fileType = conversation.getHwCust().getFileType();
        boolean isCannotConvert = (109 == fileType || 111 == fileType || 108 == fileType) ? true : 110 == fileType;
        if (conversation.getHwCust().getRcsThreadType() == 4 && !conversation.hasDraft()) {
            RcsUtility.addConversationSmileySpans(custSnippet, smileyType, builder, conversation, sp, this.mContext, isCannotConvert);
        } else if (isCannotConvert) {
            builder.append(custSnippet);
        } else {
            sp.addSmileySpans(custSnippet, smileyType, builder);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getGroupDefaultName(Context context, String defaultValue, Conversation conv, Resources res) {
        if (!this.isRcsOn || context == null || defaultValue == null || conv == null || res == null || conv.getHwCust() == null || conv.getHwCust().getRcsThreadType() != 4) {
            return defaultValue;
        }
        String name = ((Contact) conv.getRecipients().get(0)).getOnlyName();
        if (TextUtils.isEmpty(name)) {
            name = res.getString(R.string.chat_topic_default);
        }
        if (!name.equals(res.getString(R.string.chat_topic_default)) || !RcsProfile.isGroupChatMemberTopicEnable()) {
            return name;
        }
        String groupId = conv.getHwCust().getGroupId();
        long threadId = conv.getThreadId();
        if (groupId != null && threadId > 0) {
            String groupMemberName = RcsUtility.getGroupMemberName(context, groupId);
            if (!TextUtils.isEmpty(groupMemberName)) {
                name = groupMemberName;
            }
        }
        return name;
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }
}
