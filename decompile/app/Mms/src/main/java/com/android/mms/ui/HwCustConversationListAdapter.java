package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;

public class HwCustConversationListAdapter {
    public HwCustConversationListAdapter(Context context) {
    }

    public long getItemId(int position, Cursor cursor) {
        return 0;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }
}
