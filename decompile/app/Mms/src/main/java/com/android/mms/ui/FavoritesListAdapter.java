package com.android.mms.ui;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.FavoritesUtils;
import java.util.regex.Pattern;

public class FavoritesListAdapter extends MessageListAdapter {
    public FavoritesListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, listView, useDefaultColumnsMap, highlight, 2, 1);
    }

    protected int getItemViewResId(Context context, int boxType) {
        return R.layout.favorites_list_item;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MmsException e;
        NullPointerException ee;
        MessageItem item = (MessageItem) this.mMessageItemCache.get(Long.valueOf(MessageListAdapter.getKey(type, msgId)));
        if (item != null || c == null) {
            return item;
        }
        if (!isCursorValid(c)) {
            return item;
        }
        MessageItem messageItem;
        try {
            messageItem = new MessageItem(this.mContext, type, c, this.mColumnsMap, this.mHighlight, 1);
            try {
                if ("sms".equals(type)) {
                    messageItem.mSubId = this.mColumnsMap.mColumnSubId;
                    messageItem.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_SMS, messageItem.mMsgId);
                } else {
                    messageItem.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_MMS, messageItem.mMsgId);
                }
                this.mMessageItemCache.put(Long.valueOf(MessageListAdapter.getKey(messageItem.mType, messageItem.mMsgId)), messageItem);
                return messageItem;
            } catch (MmsException e2) {
                e = e2;
                MLog.e("FavMsgAdapter", "getCachedMessageItem: ", (Throwable) e);
                return messageItem;
            } catch (NullPointerException e3) {
                ee = e3;
                MLog.e("FavMsgAdapter", "getCachedMessageItem: ", (Throwable) ee);
                return messageItem;
            }
        } catch (MmsException e4) {
            e = e4;
            messageItem = item;
            MLog.e("FavMsgAdapter", "getCachedMessageItem: ", (Throwable) e);
            return messageItem;
        } catch (NullPointerException e5) {
            ee = e5;
            messageItem = item;
            MLog.e("FavMsgAdapter", "getCachedMessageItem: ", (Throwable) ee);
            return messageItem;
        }
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(Cursor cursor) {
        if ("sms".equals(cursor.getString(this.mColumnsMap.mColumnMsgType))) {
            return 0;
        }
        return 1;
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return this.mInflater.inflate(R.layout.favorites_list_item, parent, false);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        } else if (this.mDataValid) {
            View v;
            if (convertView == null) {
                v = newView(this.mContext, this.mCursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, this.mContext, this.mCursor);
            return v;
        } else {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
    }

    public void bindView(View view, Context context, Cursor cursor) {
        MessageItem msgItem = getCachedMessageItem(cursor.getString(this.mColumnsMap.mColumnMsgType), cursor.getLong(this.mColumnsMap.mColumnMsgId), cursor);
        if (msgItem == null) {
            MLog.e("FavMsgAdapter", "Can't bind a empty view");
            return;
        }
        FavoritesListItem mli = (FavoritesListItem) view;
        int position = cursor.getPosition();
        boolean isInEditMode = this.mListView != null ? this.mListView.isInEditMode() : false;
        boolean isSelected = this.mListView != null ? this.mListView.isSelected(msgItem.getItemId().longValue()) : false;
        if (mli.setTextScale(this.mScale)) {
            msgItem.setCachedFormattedMessage(null);
        }
        mli.bind(msgItem, false, position);
        mli.setMsgListItemHandler(this.mMsgListItemHandler);
        if (mli.getMessageItem().isMms() && this.mListView != null && 2 == this.mListView.getViewMode()) {
            mli.setCheckboxEnable(!isInEditMode);
            mli.setEditAble(isInEditMode);
        } else {
            mli.setCheckboxEnable(true);
            mli.setEditAble(isInEditMode, isSelected);
            mli.setClickable(false);
            mli.setLongClickable(false);
        }
    }

    public long getItemId(int pos) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(pos)) {
            return 0;
        }
        return MessageListAdapter.getKey(MessageItem.isMms(this.mCursor.getString(0)), this.mCursor.getLong(1));
    }

    public void onContactsChange() {
        for (MessageItem mi : this.mMessageItemCache.snapshot().values()) {
            mi.setContactList(null);
        }
    }

    public void onScaleChanged(float ScaleSize) {
        setTextScale(ScaleSize);
        ((ListView) this.mListView).invalidateViews();
    }

    public void setMsgListItemHandler(Handler handler) {
        this.mMsgListItemHandler = handler;
    }
}
