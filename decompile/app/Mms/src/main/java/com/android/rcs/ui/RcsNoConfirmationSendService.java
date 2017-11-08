package com.android.rcs.ui;

import android.content.Context;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;

public class RcsNoConfirmationSendService {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isCanSendIm(String[] address) {
        if (mIsRcsOn && address != null && address.length == 1 && RcsProfile.getRcsService() != null) {
            try {
                if (RcsProfile.isImAvailable(address[0])) {
                    return true;
                }
            } catch (Exception rme) {
                MLog.e("RcsNoConfirmationSendService", "isCanSendIm error " + rme.toString());
            }
        }
        return false;
    }

    public void sendImFirst(String message, String address, Context context) {
        if (mIsRcsOn) {
            RcsTransaction.preSendImMessage(context, message, address);
        }
    }
}
