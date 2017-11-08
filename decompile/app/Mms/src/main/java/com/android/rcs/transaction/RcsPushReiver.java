package com.android.rcs.transaction;

import android.content.Context;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.huawei.rcs.util.MLog;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.Collection;

public class RcsPushReiver {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public RcsPushReiver(Context context) {
    }

    public void handleRcsStatusSent(long newMmsThreadId, Context context) {
        if (mIsRcsOn && newMmsThreadId > 0 && RcsTransaction.isShowUndeliveredIcon()) {
            Collection<Long> mmsThreadIds = new ArrayList();
            mmsThreadIds.add(Long.valueOf(newMmsThreadId));
            MLog.d("RcsPushReiver", "RcsPushReiver handleRcsStatusSent " + newMmsThreadId);
            Collection<Long> otherRcsThreadIds = RcsConversationUtils.getHwCustUtils().getOtherThreadFromGivenThread(context, mmsThreadIds, 1);
            if (otherRcsThreadIds != null && otherRcsThreadIds.size() == 1) {
                long rcsThreadId = ((Long) otherRcsThreadIds.iterator().next()).longValue();
                MLog.d("RcsPushReiver", "RcsPushReiver handleRcsStatusSent rcsThreadId " + rcsThreadId);
                RcsMessagingNotification.updateUndeliveredStatus(rcsThreadId, context);
            }
        }
    }
}
