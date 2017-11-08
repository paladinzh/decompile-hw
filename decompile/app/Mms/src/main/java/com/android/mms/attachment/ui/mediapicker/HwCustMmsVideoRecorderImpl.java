package com.android.mms.attachment.ui.mediapicker;

import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;

public class HwCustMmsVideoRecorderImpl extends HwCustMmsVideoRecorder {
    private static long mDurationSeconds;
    private static int mVideoQuality;

    public int getVideoQuality() {
        return mVideoQuality;
    }

    public long getDurationSeconds() {
        return mDurationSeconds;
    }

    public void initVideoParam() {
        String custValueList = HwCustMmsConfigImpl.getCustVideoParam();
        if (!TextUtils.isEmpty(custValueList)) {
            String[] custValues = custValueList.split(",");
            mVideoQuality = Integer.parseInt(custValues[0]);
            mDurationSeconds = Long.parseLong(custValues[1]);
        }
    }

    public String getCustVideoParam() {
        return HwCustMmsConfigImpl.getCustVideoParam();
    }
}
