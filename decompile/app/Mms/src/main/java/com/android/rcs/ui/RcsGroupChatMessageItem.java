package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter.GroupMessageColumn;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.TextSpan;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import java.util.List;
import java.util.regex.Pattern;

public class RcsGroupChatMessageItem {
    private static final String FT_TAG = (TAG + " FileTrans: ");
    private static String TAG = "RcsGroupChatMessageItem";
    public String mAddress;
    public String mBody;
    private CharSequence mCachedFormattedMessage;
    public String mContact;
    Cursor mCursor;
    public long mDate;
    public boolean mDelivered = false;
    public DeliveryStatus mDeliveryStatus;
    public RcsFileTransGroupMessageItem mFtGroupMsgItem;
    public String mGlobalID;
    public Pattern mHighLight = null;
    public String mMsgId;
    public List<TextSpan> mMsgtextSpan = null;
    public boolean mReadReport;
    private int mStatus;
    public int mThreadId;
    public String mTimeHM;
    public String mTimestamp;
    public int mType;

    public RcsGroupChatMessageItem(Context context, Cursor cursor, GroupMessageColumn columnMap, boolean isScroll, Pattern highLight) throws MmsException {
        this.mHighLight = highLight;
        this.mThreadId = cursor.getInt(columnMap.columnThreadID);
        this.mType = cursor.getInt(columnMap.columnType);
        this.mAddress = cursor.getString(columnMap.columnAddress);
        if (this.mAddress == null) {
            this.mAddress = "";
        }
        this.mDate = cursor.getLong(columnMap.columnDate);
        this.mTimestamp = MessageUtils.getMessageShowTime(context, this.mDate);
        this.mTimeHM = MessageUtils.getMessageShowTime(context, this.mDate, false);
        this.mBody = cursor.getString(columnMap.columnBody);
        this.mStatus = cursor.getInt(columnMap.columnStatus);
        this.mReadReport = cursor.getInt(columnMap.columnRead) == 0;
        this.mGlobalID = cursor.getString(columnMap.columnGlobalID);
        this.mMsgId = cursor.getString(columnMap.columnMessageID);
        this.mCursor = cursor;
        if (this.mStatus == 0) {
            this.mDeliveryStatus = DeliveryStatus.PENDING;
        } else if (this.mStatus == 1 || this.mStatus == 2) {
            this.mDeliveryStatus = DeliveryStatus.RECEIVED;
        } else if (this.mStatus == 4) {
            this.mDeliveryStatus = DeliveryStatus.FAILED;
        } else {
            this.mDeliveryStatus = DeliveryStatus.NONE;
        }
        if (this.mStatus == 101) {
            this.mDelivered = true;
        } else {
            this.mDelivered = false;
        }
        MLog.i(FT_TAG, "group message status = " + this.mStatus);
        if (100 == this.mType || 101 == this.mType) {
            MLog.i(FT_TAG, "checkthe type " + this.mType);
            if (RcsCommonConfig.isRCSSwitchOn()) {
                this.mFtGroupMsgItem = new RcsFileTransGroupMessageItem(context, cursor, columnMap, isScroll, 2);
            }
            if (this.mFtGroupMsgItem == null || this.mFtGroupMsgItem.mImAttachmentStatus != 101) {
                this.mDelivered = false;
            } else {
                this.mDelivered = true;
            }
        }
        if (isOutgoingMessage()) {
            this.mContact = context.getString(R.string.message_sender_from_self);
        } else if (!TextUtils.isEmpty(this.mAddress) && !this.mAddress.contains(",")) {
            this.mContact = Contact.get(this.mAddress, false).getName();
        }
    }

    public String toString() {
        return "type: " + this.mType + " address: " + this.mAddress + " read: " + this.mReadReport + " delivery status: " + this.mDeliveryStatus + " contact: " + this.mContact;
    }

    public boolean isOutgoingMessage() {
        if (4 == this.mType || 100 == this.mType) {
            return true;
        }
        return false;
    }

    public boolean isFailedMessage() {
        if (4 == this.mStatus) {
            return true;
        }
        return false;
    }

    public boolean isDeliveredMessage() {
        return isOutgoingMessage() && 101 == this.mStatus;
    }

    public boolean isSentMessage() {
        if (isOutgoingMessage()) {
            return 1 == this.mStatus || 2 == this.mStatus;
        } else {
            return false;
        }
    }

    public boolean isFtSentMessage() {
        boolean z = false;
        if (this.mFtGroupMsgItem == null) {
            MLog.e(FT_TAG, "isFtSentMessage Group mFtGroupMsgItem  is null ");
            return false;
        }
        if (1002 == this.mFtGroupMsgItem.mImAttachmentStatus) {
            z = true;
        }
        return z;
    }

    public boolean isFileTransMessage() {
        if (100 == this.mType || 101 == this.mType) {
            return true;
        }
        return false;
    }

    public void setCachedFormattedMessage(CharSequence formattedMessage) {
        this.mCachedFormattedMessage = formattedMessage;
    }

    public CharSequence getCachedFormattedMessage() {
        return this.mCachedFormattedMessage;
    }

    public boolean isNotDelayMsg() {
        if (System.currentTimeMillis() - this.mDate > 8000 || !isOutgoingMessage()) {
            return true;
        }
        return false;
    }

    public long getCancelId() {
        if (this.mMsgId.isEmpty()) {
            return 0;
        }
        return Long.parseLong(this.mMsgId);
    }

    public String getMessageType() {
        String type = "unknow";
        switch (this.mType) {
            case 4:
                return "rcs_group_text";
            case 100:
                return "rcs_group_file";
            default:
                return type;
        }
    }
}
