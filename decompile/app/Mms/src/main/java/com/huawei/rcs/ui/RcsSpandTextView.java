package com.huawei.rcs.ui;

import com.android.mms.ui.MessageItem;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.TextSpan;
import java.util.List;

public class RcsSpandTextView {
    private final boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private List<TextSpan> mListTextSpan;

    public void setSpanList(List<TextSpan> listSpan) {
        if (this.isRcsOn) {
            this.mListTextSpan = listSpan;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearSpanList(MessageItem msgItem) {
        MLog.d("RcsPeopleActionBar", "clearSpanList");
        if (this.isRcsOn && this.mListTextSpan != null && msgItem.isRcsChat()) {
            this.mListTextSpan.clear();
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }
}
