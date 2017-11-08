package com.android.rcs.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import java.util.List;

public class RcsChatMessageForwarder extends RcsBaseForwarder<String> {
    private static final boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public void processMassOrSingle(List<String> rcsList, int rcsUsers) {
        if (isRcsOn && this.mFragment != null) {
            this.mFragment.startActivity(getGroupSmsIntent(rcsList));
        }
    }

    private Intent getGroupSmsIntent(List<String> addrList) {
        StringBuilder sb = new StringBuilder();
        sb.append("smsto:");
        for (int i = 0; i < addrList.size(); i++) {
            sb.append((String) addrList.get(i)).append(";");
        }
        Intent chatIntent = new Intent("android.intent.action.SENDTO", Uri.parse(sb.toString()));
        chatIntent.putExtra("sms_body", (String) this.mMessageData);
        chatIntent.putExtra("sameMemberForward", this.isSameMemberForward);
        chatIntent.setClassName(this.mContext, "com.android.mms.ui.ForwardMessageActivity");
        return chatIntent;
    }

    private Bundle formatBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("bundle_ext_msg", (String) this.mMessageData);
        return bundle;
    }

    public void setMessageData(String msgBody) {
        if (isRcsOn) {
            this.mMessageData = msgBody;
        }
    }

    public void dispatchGroupChatIntent(String groupId) {
        if (isRcsOn) {
            Intent intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
            intent.putExtra("bundle_group_id", groupId);
            intent.putExtras(formatBundle());
            if (this.mFragment != null) {
                this.mFragment.startActivity(intent);
                if (this.mFragment instanceof ComposeMessageFragment) {
                    ((ComposeMessageFragment) this.mFragment).finishSelf(false);
                }
            }
        }
    }
}
