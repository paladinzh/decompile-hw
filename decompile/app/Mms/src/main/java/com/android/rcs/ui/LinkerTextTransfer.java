package com.android.rcs.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.util.LruCache;
import com.android.mms.ui.MessageItem;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.CommonGatherLinks;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.TextSpan;
import java.util.List;

public class LinkerTextTransfer {
    private static LinkerTextTransfer mLinkerTextTransfer = null;
    private LruCache<String, LinkerData> mLinkerData;
    private int mScrollState;

    private static class LinkerData {
        int[] mAddrPos;
        long mDate;
        int[] mDatePos;

        LinkerData(int[] addrPos, int[] datePos, long date) {
            this.mAddrPos = addrPos;
            this.mDatePos = datePos;
            this.mDate = date;
        }
    }

    private class TransferTask extends AsyncTask<Void, Void, Void> {
        private int[] mAddrPos;
        private String mBody;
        private Context mContext;
        private long mDate;
        private int[] mDatePos;
        private CharSequence mFormattedMessage;
        private RcsGroupChatMessageItem mGroupChatMsgItem;
        private MessageItem mMsgItem;
        private SpandTextView mSpandTextView;

        private TransferTask() {
            this.mFormattedMessage = null;
            this.mMsgItem = null;
            this.mGroupChatMsgItem = null;
            this.mContext = null;
            this.mSpandTextView = null;
            this.mAddrPos = null;
            this.mDatePos = null;
            this.mBody = null;
            this.mDate = 0;
        }

        protected Void doInBackground(Void... params) {
            String body = this.mBody;
            this.mAddrPos = HwMessageUtils.getAddrFromTMRManager(body);
            this.mDatePos = HwMessageUtils.getTimePosition(body);
            return null;
        }

        protected void onPostExecute(Void unused) {
            List textSpan = CommonGatherLinks.getTextSpans(this.mAddrPos, this.mDatePos, this.mBody, this.mContext, this.mDate);
            this.mSpandTextView.setText(this.mFormattedMessage, textSpan);
            saveTextSpan(textSpan);
            LinkerTextTransfer.this.addTransferData(this.mBody, this.mAddrPos, this.mDatePos, this.mDate);
        }

        private void saveTextSpan(List<TextSpan> textSpan) {
            if (this.mGroupChatMsgItem != null) {
                this.mGroupChatMsgItem.mMsgtextSpan = textSpan;
            } else if (this.mMsgItem != null) {
                this.mMsgItem.mMsgtextSpan = textSpan;
            }
        }

        TransferTask setContext(Context context) {
            this.mContext = context;
            return this;
        }

        TransferTask setSpandTextView(SpandTextView spandTextView) {
            this.mSpandTextView = spandTextView;
            return this;
        }

        TransferTask setFormattedMessage(CharSequence formattedMessage) {
            this.mFormattedMessage = formattedMessage;
            return this;
        }

        TransferTask setGroupChatMessageItem(RcsGroupChatMessageItem msgItem) {
            this.mGroupChatMsgItem = msgItem;
            this.mBody = msgItem.mBody;
            this.mDate = msgItem.mDate;
            return this;
        }

        TransferTask setMessageItem(MessageItem msgItem) {
            this.mMsgItem = msgItem;
            this.mBody = msgItem.mBody;
            this.mDate = msgItem.mDate;
            return this;
        }
    }

    public void setScrollState(int state) {
        this.mScrollState = state;
    }

    private boolean isFlingState() {
        return this.mScrollState != 0;
    }

    private LinkerTextTransfer() {
        this.mLinkerData = null;
        this.mScrollState = 0;
        this.mLinkerData = new LruCache(VTMCDataCache.MAX_EXPIREDTIME);
    }

    public static LinkerTextTransfer getInstance() {
        if (mLinkerTextTransfer == null) {
            mLinkerTextTransfer = new LinkerTextTransfer();
        }
        return mLinkerTextTransfer;
    }

    public void clear() {
        this.mLinkerData.evictAll();
    }

    private void addTransferData(String body, int[] addrPos, int[] datePos, long date) {
        if (body != null && !linkerTextTransferred(body)) {
            this.mLinkerData.put(body, new LinkerData(addrPos, datePos, date));
        }
    }

    private boolean linkerTextTransferred(String body) {
        boolean z = false;
        if (body == null) {
            return false;
        }
        if (this.mLinkerData.get(body) != null) {
            z = true;
        }
        return z;
    }

    private int[] getAddrPos(String body) {
        if (body == null || !linkerTextTransferred(body)) {
            return null;
        }
        return ((LinkerData) this.mLinkerData.get(body)).mAddrPos;
    }

    private int[] getDatePos(String body) {
        if (body == null || !linkerTextTransferred(body)) {
            return null;
        }
        return ((LinkerData) this.mLinkerData.get(body)).mDatePos;
    }

    private long getLinkerDate(String body) {
        if (body == null || !linkerTextTransferred(body)) {
            return -1;
        }
        return ((LinkerData) this.mLinkerData.get(body)).mDate;
    }

    private boolean isDelayMsg(RcsGroupChatMessageItem msgItem) {
        return DelaySendManager.getInst().isDelayMsg(msgItem.getCancelId(), msgItem.getMessageType());
    }

    private boolean isDelayMsg(MessageItem msgItem) {
        return DelaySendManager.getInst().isDelayMsg(msgItem.getCancelId(), msgItem.mType, msgItem.mIsMultiRecipients);
    }

    public void setSpandText(Context context, SpandTextView spandTextView, CharSequence formattedMessage, RcsGroupChatMessageItem msgItem) {
        if (context != null && spandTextView != null && msgItem != null) {
            List textSpan = msgItem.mMsgtextSpan;
            if (msgItem.mBody == null || isDelayMsg(msgItem)) {
                spandTextView.setText(formattedMessage, textSpan);
                return;
            }
            String body = msgItem.mBody;
            if (linkerTextTransferred(body)) {
                if (msgItem.mMsgtextSpan == null || msgItem.mDate != getLinkerDate(body)) {
                    textSpan = CommonGatherLinks.getTextSpans(getAddrPos(body), getDatePos(body), body, context, msgItem.mDate);
                    msgItem.mMsgtextSpan = textSpan;
                }
                spandTextView.setText(formattedMessage, textSpan);
            } else if (isFlingState()) {
                spandTextView.setText(formattedMessage, textSpan);
            } else {
                spandTextView.setText(formattedMessage, textSpan);
                new TransferTask().setContext(context).setSpandTextView(spandTextView).setGroupChatMessageItem(msgItem).setFormattedMessage(formattedMessage).executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            }
        }
    }

    public void setSpandText(Context context, SpandTextView spandTextView, CharSequence formattedMessage, MessageItem msgItem) {
        if (context != null && spandTextView != null && msgItem != null) {
            List textSpan = msgItem.mMsgtextSpan;
            if (!msgItem.isRcsChat() || msgItem.mBody == null || isDelayMsg(msgItem)) {
                spandTextView.setText(formattedMessage, textSpan);
                return;
            }
            String body = msgItem.mBody;
            if (linkerTextTransferred(body)) {
                if (msgItem.mMsgtextSpan == null || msgItem.mDate != getLinkerDate(body)) {
                    textSpan = CommonGatherLinks.getTextSpans(getAddrPos(body), getDatePos(body), body, context, msgItem.mDate);
                    msgItem.mMsgtextSpan = textSpan;
                }
                spandTextView.setText(formattedMessage, textSpan);
            } else if (isFlingState()) {
                spandTextView.setText(formattedMessage, textSpan);
            } else {
                spandTextView.setText(formattedMessage, textSpan);
                new TransferTask().setContext(context).setSpandTextView(spandTextView).setMessageItem(msgItem).setFormattedMessage(formattedMessage).executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            }
        }
    }
}
