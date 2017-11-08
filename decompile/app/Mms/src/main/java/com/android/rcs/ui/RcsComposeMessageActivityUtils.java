package com.android.rcs.ui;

import android.content.Context;
import android.content.Intent;
import com.android.rcs.RcsCommonConfig;

public class RcsComposeMessageActivityUtils {
    private static RcsComposeMessageActivityUtils mHwCust = new RcsComposeMessageActivityUtils();
    private boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public static RcsComposeMessageActivityUtils getHwCust() {
        return mHwCust;
    }

    public Intent setRcsConversationMode(Context context, Intent orgIntent) {
        if (!this.mIsRcsOn) {
            return orgIntent;
        }
        orgIntent.putExtra("conversation_mode", 3);
        return orgIntent;
    }
}
