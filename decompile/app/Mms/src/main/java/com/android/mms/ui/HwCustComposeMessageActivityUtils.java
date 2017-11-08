package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import com.huawei.cust.HwCustUtils;

public class HwCustComposeMessageActivityUtils {
    private static HwCustComposeMessageActivityUtils mHwCust = ((HwCustComposeMessageActivityUtils) HwCustUtils.createObj(HwCustComposeMessageActivityUtils.class, new Object[0]));

    public static HwCustComposeMessageActivityUtils getHwCust() {
        return mHwCust;
    }

    public Intent setRcsConversationMode(Context context, Intent orgIntent) {
        return orgIntent;
    }
}
