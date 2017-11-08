package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import com.android.mms.ui.GroupSmsDetailsListAdapter.ColumnsSmsMap;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;

public class GroupSmsDetailsItem {
    private static String TAG = "GroupSmsDetailsItem";
    String mAddress;
    String mBody;
    long mDate = 0;
    DeliveryStatus mDeliveryStatus = DeliveryStatus.NONE;
    final long mMsgId;
    int mSubId;
    final long mThreadID;
    int mType;
    final long mUID;

    GroupSmsDetailsItem(Context context, String type, long uid, long threadid, Cursor cursor, ColumnsSmsMap columnsMap) throws MmsException {
        if ("sms".equals(type)) {
            this.mUID = uid;
            this.mThreadID = threadid;
            this.mSubId = cursor.getInt(columnsMap.mColumnSubId);
            this.mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
            this.mBody = cursor.getString(columnsMap.mColumnSmsBody);
            this.mAddress = cursor.getString(columnsMap.mColumnSmsAddress);
            this.mType = cursor.getInt(columnsMap.mColumnSmsType);
            long date = cursor.getLong(columnsMap.mColumnSmsDate);
            if (MessageUtils.IS_CHINA_TELECOM_OPTA_OPTB) {
                long date_sent = cursor.getLong(columnsMap.mColumnSmsDateSent);
                if (!(date_sent == 0 || date_sent == 1)) {
                    date = date_sent;
                }
            }
            if (0 != date) {
                this.mDate = date;
            } else {
                MLog.d(TAG, "the date of the sms is not exist and not show the timestamp. e.g icc sms");
            }
            long status = cursor.getLong(columnsMap.mColumnSmsStatus);
            if (status == -1) {
                this.mDeliveryStatus = DeliveryStatus.NONE;
                return;
            } else if (status >= 64) {
                this.mDeliveryStatus = DeliveryStatus.FAILED;
                return;
            } else if (status >= 32) {
                this.mDeliveryStatus = DeliveryStatus.PENDING;
                return;
            } else {
                this.mDeliveryStatus = DeliveryStatus.RECEIVED;
                return;
            }
        }
        throw new MmsException("Unknown type of the message: " + type);
    }
}
