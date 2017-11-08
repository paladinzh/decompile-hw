package com.android.contacts.hap.rcs.activities;

import android.content.Intent;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.HwLog;

public class RcsContactDetailActivityHelp {
    private boolean isRcsOn = EmuiFeatureManager.isRcsFeatureEnable();
    private String mPersonToSendFile;

    public void setPersonToSendFile(String personToSendFile) {
        if (this.isRcsOn) {
            this.mPersonToSendFile = personToSendFile;
        }
    }

    public boolean isFromRcsGroupChat(Intent intent) {
        if (intent != null) {
            return intent.getBooleanExtra("isFromRcsGroupChat", false);
        }
        HwLog.w("RcsContactDetailActivityHelp", "isFromRcsGroupChat intent is null");
        return false;
    }

    public void initFromRcsGroupChat(Intent intent) {
        if (this.isRcsOn) {
            intent.setData(null);
        }
    }

    public String getRcsGroupChatAddress(Intent intent) {
        return intent.getStringExtra("address");
    }

    public String getRcsGroupChatNickname(Intent intent) {
        return intent.getStringExtra("nickName");
    }
}
