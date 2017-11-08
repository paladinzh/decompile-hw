package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import com.android.mms.data.Contact;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.huawei.cspcommon.MLog;

public class RcsConversationListAdapter {
    private boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public RcsConversationListAdapter(Context context) {
    }

    public long getItemId(int position, Cursor cursor) {
        if (!this.mIsRcsOn || cursor == null || !cursor.moveToPosition(position)) {
            return 0;
        }
        int type;
        long threadId = cursor.getLong(0);
        try {
            type = cursor.getInt(cursor.getColumnIndexOrThrow("thread_type"));
        } catch (IllegalArgumentException ex) {
            type = 1;
            MLog.e("RcsConversationListAdapter", "getItemId setRcsThreadType" + ex.toString());
        }
        return RcsConversationUtils.judgeRcsThreadId(threadId, type);
    }

    public boolean isRcsSwitchOn() {
        return this.mIsRcsOn;
    }

    public int getRcsItemType(int position, Cursor cursor) {
        if (!this.mIsRcsOn || cursor == null || !cursor.moveToPosition(position)) {
            return -1;
        }
        int type;
        try {
            type = cursor.getInt(cursor.getColumnIndexOrThrow("thread_type"));
            String rids = cursor.getString(cursor.getColumnIndexOrThrow("recipient_ids"));
            if (!Contact.isEmailAddress(rids) && Contact.getHwCustContact().isGroupID(rids)) {
                type = 4;
            }
        } catch (IllegalArgumentException ex) {
            type = 1;
            MLog.e("RcsConversationListAdapter", "getItemId setRcsThreadType" + ex.toString());
        }
        return type;
    }

    public String getRcsGroupId(int position, Cursor cursor) {
        if (!this.mIsRcsOn || cursor == null || !cursor.moveToPosition(position)) {
            return "";
        }
        String groupId;
        try {
            groupId = cursor.getString(cursor.getColumnIndexOrThrow("recipient_ids"));
        } catch (IllegalArgumentException ex) {
            groupId = "";
            MLog.e("RcsConversationListAdapter", "getItemId getRcsGroupId" + ex.toString());
        }
        return groupId;
    }
}
