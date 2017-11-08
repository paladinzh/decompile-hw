package com.android.rcs.data;

import com.android.mms.data.Contact;
import com.android.rcs.RcsCommonConfig;
import com.huawei.rcs.utils.RcsUtility;

public class RcsContact {
    public boolean isGroupID(String groupID) {
        if (RcsCommonConfig.isRCSSwitchOn() && groupID != null && groupID.startsWith("Group_")) {
            return true;
        }
        return false;
    }

    public boolean needResetContactName(Contact orginalContact, String name, String number) {
        boolean z = false;
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        if (isGroupID(number) && !orginalContact.getName().equals(name)) {
            z = true;
        }
        return z;
    }

    public void clearGroupNameCache() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            RcsUtility.clearGroupNameCache();
        }
    }
}
