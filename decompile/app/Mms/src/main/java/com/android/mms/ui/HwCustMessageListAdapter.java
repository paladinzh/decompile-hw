package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.mms.MmsException;

public class HwCustMessageListAdapter {
    private static final String TAG = "HwCustMessageListAdapter";
    protected MessageListAdapter mAdapter;

    public HwCustMessageListAdapter(Context context) {
    }

    public void setHwCustMessageListAdapter(Context context, MessageListAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public String[] getHwCustProjection() {
        return MessageListAdapter.SINGLE_VIEW_PROJECTION;
    }

    public Handler getRcseEventHandler() {
        return null;
    }

    public void setRcseEventHandler(Handler mHandler) {
    }

    public boolean isScrollRcs() {
        return false;
    }

    public void setScrollRcs(boolean isScrolling) {
    }

    public MessageListItem bindView(View view, Cursor cursor, MessageListItem mli) {
        return mli;
    }

    public View newView(LayoutInflater mInflater, ViewGroup parent, int boxType, View view) {
        return view;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c, int loadType, boolean isShowAutoLink, boolean isGroupCov) throws MmsException {
        return new MessageItem(this.mAdapter.mContext, type, c, this.mAdapter.mColumnsMap, this.mAdapter.mHighlight, loadType, isShowAutoLink, isGroupCov);
    }

    public void setFromSimCardSms(boolean flag) {
    }

    public int getViewTypeCount() {
        return 4;
    }

    public boolean isChatType(String type) {
        return false;
    }

    public int getItemViewType(Cursor cursor, int boxId) {
        if (boxId == 1 || boxId == 0) {
            return 0;
        }
        return 1;
    }

    public MessageItem getMessageItemWithMsgId(String msgType, long msgId, Cursor c) {
        return null;
    }

    public MessageItem getMessageItemWithIdAssigned(int position, Cursor c) {
        return null;
    }

    public void setThreadId(MessageListItem msgListItem) {
    }

    public void setConversationId(long threadId) {
    }

    public void setSearchString(String aSearchString) {
    }

    public String getSearchString() {
        return "";
    }

    public void setPositionList(Integer[] aPositions) {
    }

    public void highlightMessageListItem(MessageListItem aMessageListItem, int position, String aMsgItem, int aBoxType) {
    }
}
