package com.android.mms.ui;

import android.content.Context;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.google.android.mms.MmsException;

public class HwCustMessageItem {
    public int mRcsMsgType;

    public void setHwCustMessageItem(Context context, MessageItem item) {
    }

    public DeliveryStatus getDeliveryExtendStatue(long status, DeliveryStatus in) {
        return in;
    }

    public boolean init() {
        return false;
    }

    public void initMore() {
    }

    public boolean isOutgoingExtMessage() {
        return false;
    }

    public boolean isFailedExtMessage() {
        return false;
    }

    public boolean isRcsChat() {
        return false;
    }

    public boolean isUndeliveredIm() {
        return false;
    }

    public boolean isReadIm() {
        return false;
    }

    public boolean isInComingExtMessage() {
        return false;
    }

    public void initForFav(ColumnsMap columnsMap, int loadtype) throws MmsException {
    }
}
