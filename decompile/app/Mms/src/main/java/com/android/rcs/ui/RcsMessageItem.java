package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony.Sms;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageItem.DeliveryStatus;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.VcardMessageHelper;
import com.android.rcs.RcsCommonConfig;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.util.RCSConst;
import com.huawei.rcs.utils.RcsProfileUtils;

public class RcsMessageItem {
    protected Context mContext = null;
    public RcsFileTransGroupMessageItem mFileItem;
    private boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    protected MessageItem mMsgItem = null;
    public int mRcsMsgExtType = 0;
    public int mRcsMsgType;

    public void setRcsMessageItem(Context context, MessageItem item) {
        if (this.mIsRcsOn) {
            this.mContext = context;
            this.mMsgItem = item;
        }
    }

    public DeliveryStatus getDeliveryExtendStatue(long status, DeliveryStatus in) {
        if (this.mIsRcsOn && status == 16) {
            return DeliveryStatus.NONE;
        }
        return in;
    }

    public boolean init() {
        if (!this.mIsRcsOn || !"chat".equals(this.mMsgItem.mType)) {
            return false;
        }
        MLog.d("RcsMessageItem", "init chat item");
        this.mMsgItem.mReadReport = false;
        this.mMsgItem.mDate = this.mMsgItem.mCursor.getLong(this.mMsgItem.mColumnsMap.mColumnSmsDate);
        this.mRcsMsgType = RcsProfileUtils.getRcsMsgType(this.mMsgItem.mCursor);
        this.mRcsMsgExtType = RcsProfileUtils.getRcsMsgExtType(this.mMsgItem.mCursor);
        if (MmsConfig.getMultiRecipientSingleViewEnabled()) {
            this.mMsgItem.mStrUid = this.mMsgItem.mCursor.getString(this.mMsgItem.mColumnsMap.mColumnUID);
            if (this.mMsgItem.mStrUid != null) {
                StringBuilder lSelection = new StringBuilder("uid");
                lSelection.append(" = ?");
                Cursor lCusr = SqliteWrapper.query(this.mContext, RCSConst.RCS_URI_CONVERSATIONS, new String[]{"count(*)"}, lSelection.toString(), new String[]{this.mMsgItem.mStrUid}, null);
                if (lCusr != null) {
                    if (lCusr.moveToFirst()) {
                        this.mMsgItem.mMsgItemRecipientNo = lCusr.getInt(0);
                    }
                    lCusr.close();
                }
                lSelection.append(" AND ").append(NumberInfo.TYPE_KEY).append(" = ?");
                lCusr = SqliteWrapper.query(this.mContext, Sms.CONTENT_URI, new String[]{"count(*)"}, lSelection.toString(), new String[]{this.mMsgItem.mStrUid, String.valueOf(2)}, null);
                if (lCusr != null) {
                    if (lCusr.moveToFirst()) {
                        this.mMsgItem.mNoOfSent = lCusr.getInt(0);
                    }
                    lCusr.close();
                }
            }
        }
        return true;
    }

    public void initMore() {
        boolean z = false;
        if (this.mIsRcsOn) {
            this.mMsgItem.mBody = this.mMsgItem.mCursor.getString(this.mMsgItem.mColumnsMap.mColumnSmsBody);
            if (-1 != this.mMsgItem.mColumnsMap.mColumnSubId) {
                this.mMsgItem.mSubId = this.mMsgItem.mCursor.getInt(this.mMsgItem.mColumnsMap.mColumnSubId);
            } else {
                MLog.w("RcsMessageItem", "initMore columnsMap.mColumnSubId == COLUMN_INVALID, set mSubId = -1 !");
                this.mMsgItem.mSubId = -1;
            }
            if (-1 != this.mMsgItem.mColumnsMap.mColumnNetworkType) {
                this.mMsgItem.mNetworkType = this.mMsgItem.mCursor.getInt(this.mMsgItem.mColumnsMap.mColumnNetworkType);
                MLog.d("RcsMessageItem", "initMore MessageItem im network type: " + this.mMsgItem.mNetworkType);
            }
            long date = this.mMsgItem.mCursor.getLong(this.mMsgItem.mColumnsMap.mColumnSmsDate);
            if (MmsConfig.isDisplaySentTime()) {
                long date_sent = this.mMsgItem.mCursor.getLong(this.mMsgItem.mColumnsMap.mColumnSmsDateSent);
                if (!(date_sent == 0 || date_sent == 1)) {
                    date = date_sent;
                }
            }
            if (0 != date) {
                this.mMsgItem.mTimestamp = MessageUtils.getMessageShowTime(this.mContext, date);
                this.mMsgItem.mTimeHM = MessageUtils.getMessageShowTime(this.mContext, date, false);
                this.mMsgItem.mDate = date;
            } else {
                this.mMsgItem.mTimestamp = " ";
                this.mMsgItem.mTimeHM = " ";
                MLog.d("RcsMessageItem", "initMore the date of the im is not exist and not show the timestamp");
            }
            if (!this.mMsgItem.isOutgoingMessage()) {
                if (this.mMsgItem.mCursor.getColumnIndex("subject") != -1) {
                    this.mMsgItem.mSubject = this.mMsgItem.mCursor.getString(this.mMsgItem.mColumnsMap.mColumnSmsSubject);
                }
                if (VcardMessageHelper.isVCardSmsSubject(this.mMsgItem.mSubject) && (this.mMsgItem.mVSmsOriginalData == null || !this.mMsgItem.mVSmsOriginalData.equals(this.mMsgItem.mSubject))) {
                    this.mMsgItem.mVSmsOriginalData = this.mMsgItem.mSubject;
                }
            }
            MessageItem messageItem = this.mMsgItem;
            if (this.mMsgItem.mCursor.getInt(this.mMsgItem.mColumnsMap.mColumnSmsLocked) != 0) {
                z = true;
            }
            messageItem.mLocked = z;
            this.mMsgItem.mErrorCode = this.mMsgItem.mCursor.getInt(this.mMsgItem.mColumnsMap.mColumnSmsErrorCode);
            long status = this.mMsgItem.mCursor.getLong(this.mMsgItem.mColumnsMap.mColumnSmsStatus);
            if (status == -1 || isInComingExtMessage()) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status >= 64) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= 32) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.PENDING;
            } else if (status == 16) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status == 2) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status == 0) {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.READ;
            } else {
                this.mMsgItem.mDeliveryStatus = DeliveryStatus.RECEIVED;
            }
        }
    }

    public boolean isOutgoingExtMessage() {
        boolean isOutgoingIm = true;
        if (!this.mIsRcsOn) {
            return false;
        }
        if (!isRcsChat()) {
            isOutgoingIm = false;
        } else if (!(this.mMsgItem.mBoxId == 5 || this.mMsgItem.mBoxId == 4)) {
            isOutgoingIm = false;
        }
        return isOutgoingIm;
    }

    public boolean isFailedExtMessage() {
        if (!this.mIsRcsOn) {
            return false;
        }
        boolean isFailedIM = false;
        if (isRcsChat()) {
            isFailedIM = this.mMsgItem instanceof RcsFileTransMessageItem ? this.mMsgItem.mBoxId != 5 ? ((RcsFileTransMessageItem) this.mMsgItem).isFailedFileTransMessage() : true : this.mMsgItem.mBoxId == 5;
        }
        return isFailedIM;
    }

    public boolean isRcsChat() {
        if (this.mIsRcsOn) {
            return this.mMsgItem.mType.equals("chat");
        }
        return false;
    }

    private boolean isIncomingFavGroupFile() {
        if (this.mFileItem == null || this.mFileItem.mtype != 101) {
            return false;
        }
        MLog.d("RcsMessageItem", "isFavGroupReceiveFile the file is groupchat receive");
        return true;
    }

    public void initForFav(ColumnsMap columnsMap, int loadtype) throws MmsException {
        if (this.mIsRcsOn && 1 == loadtype) {
            long orignalId = this.mMsgItem.mCursor.getLong(this.mMsgItem.mCursor.getColumnIndex("origin_id"));
            this.mRcsMsgType = RcsProfileUtils.getRcsMsgType(this.mMsgItem.mCursor);
            MLog.d("RcsMessageItem", "initForFav MessageItem: mRcsMsgType = " + this.mRcsMsgType + ", orignalId = " + orignalId);
            switch (this.mRcsMsgType) {
                case 0:
                    if (isRcsMsg(this.mMsgItem.mCursor)) {
                        this.mMsgItem.mMessageType = this.mRcsMsgType;
                        break;
                    }
                    break;
                case 3:
                    if (this.mFileItem == null) {
                        this.mFileItem = new RcsFileTransGroupMessageItem(this.mContext, this.mMsgItem.mCursor, columnsMap, false, 100, 1);
                    }
                    this.mMsgItem.mMessageType = this.mRcsMsgType;
                    break;
                case 5:
                    if (this.mFileItem == null) {
                        this.mFileItem = new RcsFileTransGroupMessageItem(this.mContext, this.mMsgItem.mCursor, columnsMap, false, 101, 1);
                    }
                    this.mMsgItem.mMessageType = this.mRcsMsgType;
                    break;
                case 6:
                    if (this.mFileItem == null) {
                        this.mFileItem = new RcsFileTransGroupMessageItem(this.mContext, this.mMsgItem.mCursor, columnsMap, false, 102, 1);
                    }
                    this.mMsgItem.mMessageType = this.mRcsMsgType;
                    break;
            }
        }
    }

    public boolean isUndeliveredIm() {
        boolean z = false;
        if (!this.mIsRcsOn) {
            return false;
        }
        boolean isUndelivered;
        if (isRcsChat() && this.mMsgItem.mBoxId == 2) {
            if (this.mMsgItem.mDeliveryStatus == DeliveryStatus.FAILED) {
                z = true;
            }
            isUndelivered = z;
        } else {
            isUndelivered = false;
        }
        return isUndelivered;
    }

    public boolean isReadIm() {
        boolean z = false;
        if (!this.mIsRcsOn) {
            return false;
        }
        boolean isReaded;
        if (isRcsChat() && this.mMsgItem.mBoxId == 2) {
            if (this.mMsgItem.mDeliveryStatus == DeliveryStatus.READ) {
                z = true;
            }
            isReaded = z;
        } else {
            isReaded = false;
        }
        return isReaded;
    }

    public boolean isInComingExtMessage() {
        boolean isInComingIm = true;
        if (!this.mIsRcsOn) {
            return false;
        }
        if (!(isRcsChat() && this.mMsgItem.mBoxId == 1)) {
            isInComingIm = isIncomingFavGroupFile();
        }
        return isInComingIm;
    }

    public RcsFileTransGroupMessageItem getFileItem() {
        return this.mFileItem;
    }

    private boolean isRcsMsg(Cursor c) {
        try {
            String service_center_db = c.getString(c.getColumnIndexOrThrow("service_center"));
            if ("rcs.im".equals(service_center_db) || "rcs.groupchat".equals(service_center_db)) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            MLog.e("RcsMessageItem", "isSingleOrGroupChatIm error");
            return false;
        }
    }
}
