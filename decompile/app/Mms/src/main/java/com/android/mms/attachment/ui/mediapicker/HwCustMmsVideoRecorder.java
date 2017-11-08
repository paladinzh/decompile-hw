package com.android.mms.attachment.ui.mediapicker;

public class HwCustMmsVideoRecorder {
    private static long MIN_DURATION_LIMIT_SECONDS = 25;

    public void initVideoParam() {
    }

    public int getVideoQuality() {
        return 4;
    }

    public long getDurationSeconds() {
        return MIN_DURATION_LIMIT_SECONDS;
    }

    public String getCustVideoParam() {
        return null;
    }
}
