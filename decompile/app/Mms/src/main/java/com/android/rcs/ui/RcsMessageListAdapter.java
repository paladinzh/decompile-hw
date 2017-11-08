package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.ui.RcsFileTransMessageListItem;
import com.huawei.rcs.ui.RcsScrollListAdapter;
import com.huawei.rcs.utils.RcsProfileUtils;

public class RcsMessageListAdapter implements RcsScrollListAdapter {
    static final String[] SINGLE_VIEW_PROJECTION = new String[]{"transport_type", "_id", "thread_id", "address", "body", "sub_id", "date", "date_sent", "read", NumberInfo.TYPE_KEY, "status", "locked", "error_code", "sub", "sub_cs", "date", "date_sent", "read", "m_type", "msg_box", "d_rpt", "rr", "err_type", "locked", "st", "network_type", "text_only", "date_favadd", "origin_id", "subject", "service_center", "addr_body", "time_body", "group_id", "group_all", "group_sent", "group_fail", "service_kind"};
    private boolean isFromSimCardSms = false;
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private boolean isScrollingRcs = false;
    protected MessageListAdapter mAdapter;
    private Handler mRcseEventHandler;
    public long mThreadId;

    public MessageListAdapter getMessageListAdapter() {
        return this.mAdapter;
    }

    public RcsMessageListAdapter(Context context, MessageListAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public void setRcsMessageListAdapter(Context context, MessageListAdapter mAdapter) {
        if (!this.isRcsOn) {
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }

    public void setRcseEventHandler(Handler mHandler) {
        if (this.isRcsOn) {
            this.mRcseEventHandler = mHandler;
        }
    }

    public boolean isScrollRcs() {
        return this.isRcsOn ? this.isScrollingRcs : false;
    }

    public void setScrollRcs(boolean isScrolling) {
        if (this.isRcsOn) {
            this.isScrollingRcs = isScrolling;
        }
    }

    public void resetListScrollAnimation() {
        this.mAdapter.resetListScrollAnimation();
    }

    public MessageListItem bindView(View view, Cursor cursor, MessageListItem mli) {
        if (!this.isRcsOn) {
            return mli;
        }
        MessageListItem msgListItem = loadMessageListItemByMsgType(view, cursor);
        if (msgListItem instanceof RcsFileTransMessageListItem) {
            if (this.isScrollingRcs) {
                msgListItem.setTag("isScrolling");
            } else {
                msgListItem.setTag(null);
            }
            if (this.mAdapter != null) {
                ((RcsFileTransMessageListItem) msgListItem).setMultiChoice(this.mAdapter.mListView != null ? this.mAdapter.mListView.isInEditMode() : false);
            }
            ((RcsFileTransMessageListItem) mli).setFileTransHandler(this.mRcseEventHandler);
        }
        return msgListItem;
    }

    public View newView(LayoutInflater mInflater, ViewGroup parent, int boxType, View view) {
        if (!this.isRcsOn) {
            return view;
        }
        switch (boxType) {
            case 4:
                view = mInflater.inflate(R.layout.rcs_message_list_filetrans_item_recv, parent, false);
                break;
            case 5:
                view = mInflater.inflate(R.layout.rcs_message_list_filetrans_item_send, parent, false);
                break;
        }
        return view;
    }

    private MessageListItem loadMessageListItemByMsgType(View view, Cursor cursor) {
        MessageListItem messageListItem = (MessageListItem) view;
        if (RcsProfileUtils.getRcsMsgType(cursor) == 3) {
            return (RcsFileTransMessageListItem) view;
        }
        return messageListItem;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c, int loadType, boolean isShowAutoLink, boolean isGroupCov) throws MmsException {
        if (this.isRcsOn) {
            return createItemByMsgType(type, c, loadType, isShowAutoLink, isGroupCov);
        }
        return new MessageItem(this.mAdapter.mContext, type, c, this.mAdapter.mColumnsMap, this.mAdapter.mHighlight, loadType, isShowAutoLink, isGroupCov);
    }

    private MessageItem createItemByMsgType(String type, Cursor c, int loadType, boolean isShowAutoLink, boolean isGroupCov) throws MmsException {
        int rcsMsgType = RcsProfileUtils.getRcsMsgType(c);
        if (rcsMsgType == 3 || rcsMsgType == 4) {
            return new RcsFileTransMessageItem(this.mAdapter.mContext, type, c, this.mAdapter.mColumnsMap, this.mAdapter.mHighlight, this.isScrollingRcs, isGroupCov);
        }
        return new MessageItem(this.mAdapter.mContext, type, c, this.mAdapter.mColumnsMap, this.mAdapter.mHighlight, loadType, isShowAutoLink, isGroupCov);
    }

    public MessageItem getMessageItemWithIdAssigned(int position, Cursor c) {
        if (!this.isRcsOn) {
            return null;
        }
        MessageItem item = null;
        if (this.mAdapter.isCursorValid(c)) {
            int posBefore = c.getPosition();
            if (c.moveToPosition(position)) {
                String msgType = c.getString(0);
                item = this.mAdapter.getCachedMessageItem(msgType, c.getLong(this.mAdapter.mColumnsMap.mColumnMsgId), c);
                if (item == null) {
                    MLog.e("RcsMessageListAdapter", "getCachedMessageItem is null !");
                } else if ("sms".equals(msgType) && this.isFromSimCardSms) {
                    item.mSubId = this.mAdapter.mColumnsMap.mColumnSubId;
                }
                c.moveToPosition(posBefore);
            }
        }
        return item;
    }

    public void setFromSimCardSms(boolean flag) {
        if (this.isRcsOn) {
            this.isFromSimCardSms = flag;
        }
    }

    public int getViewTypeCount() {
        return this.isRcsOn ? 6 : 4;
    }

    public boolean isChatType(String type) {
        return this.isRcsOn ? "chat".equals(type) : false;
    }

    public int getItemViewType(Cursor cursor, int boxId) {
        int i = 1;
        if (this.isRcsOn) {
            int rcsMsgType = RcsProfileUtils.getRcsMsgType(cursor);
            if (boxId == 1 || boxId == 0) {
                if (rcsMsgType == 3) {
                    return 4;
                }
                return 0;
            } else if (rcsMsgType == 3) {
                return 5;
            } else {
                return 1;
            }
        }
        if (boxId == 1 || boxId == 0) {
            i = 0;
        }
        return i;
    }

    public MessageItem getMessageItemWithMsgId(String msgType, long msgId, Cursor c) {
        if (!this.isRcsOn) {
            return null;
        }
        MessageItem item;
        if (this.mAdapter.isCursorValid(c)) {
            int posBefore = c.getPosition();
            c.moveToLast();
            while (!c.isBeforeFirst()) {
                if (c.getString(0).equals(msgType) && msgId == c.getLong(1)) {
                    try {
                        item = new MessageItem(this.mAdapter.mContext, msgType, c, this.mAdapter.mColumnsMap, this.mAdapter.mHighlight);
                        break;
                    } catch (Throwable e) {
                        MLog.e("RcsMessageListAdapter", "getMessageItemWithMsgId: ", e);
                        item = null;
                    }
                } else {
                    c.moveToPrevious();
                }
            }
            item = null;
            c.moveToPosition(posBefore);
        } else {
            item = null;
        }
        return item;
    }

    public void setConversationId(long threadId) {
        if (this.isRcsOn) {
            this.mThreadId = threadId;
        }
    }

    public void setThreadId(MessageListItem msgListItem) {
        if (this.isRcsOn && (msgListItem instanceof RcsFileTransMessageListItem)) {
            ((RcsFileTransMessageListItem) msgListItem).setThreadId(this.mThreadId);
        }
    }
}
