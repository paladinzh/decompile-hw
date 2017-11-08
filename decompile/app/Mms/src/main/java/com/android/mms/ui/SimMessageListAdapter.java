package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ListView;
import com.android.mms.MmsConfig;
import com.android.mms.util.LruSoftCache;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import java.util.regex.Pattern;

public class SimMessageListAdapter extends MessageListAdapter {
    public LruSoftCache<Integer, Drawable> mDrawableCache = new LruSoftCache(5);

    public SimMessageListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, listView, useDefaultColumnsMap, highlight, 2);
        Drawable defaultContactImage = context.getResources().getDrawable(R.drawable.ab_bottom_emui);
        Drawable defaultContactImage64 = context.getResources().getDrawable(R.drawable.ab_top_pic);
        this.mDrawableCache.put(Integer.valueOf(R.drawable.ab_bottom_emui), defaultContactImage);
        this.mDrawableCache.put(Integer.valueOf(R.drawable.ab_top_pic), defaultContactImage64);
    }

    protected int getItemViewResId(Context context, int boxType) {
        if (MmsConfig.isExtraHugeEnabled(context.getResources().getConfiguration().fontScale)) {
            return R.layout.sim_message_list_item_extra_huge;
        }
        return R.layout.sim_message_list_item;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MessageItem messageItem;
        MmsException e;
        MessageItem item = (MessageItem) this.mMessageItemCache.get(Long.valueOf(MessageListAdapter.getKey(type, msgId)));
        if (item != null || c == null) {
            return item;
        }
        if (!isCursorValid(c)) {
            return item;
        }
        try {
            messageItem = new MessageItem(this.mContext, type, c, this.mColumnsMap, this.mHighlight, 3);
            try {
                this.mMessageItemCache.put(Long.valueOf(MessageListAdapter.getKey(messageItem.mType, messageItem.mMsgId)), messageItem);
                return messageItem;
            } catch (MmsException e2) {
                e = e2;
            }
        } catch (MmsException e3) {
            e = e3;
            messageItem = item;
            MLog.e("SimMessageListAdapter", "getCachedMessageItem: ", (Throwable) e);
            return messageItem;
        }
    }

    public int getViewTypeCount() {
        return 1;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof SimMessageListItem) {
            MessageItem msgItem = getCachedMessageItem(cursor.getString(this.mColumnsMap.mColumnMsgType), cursor.getLong(this.mColumnsMap.mColumnMsgId), cursor);
            if (msgItem != null) {
                SimMessageListItem mli = (SimMessageListItem) view;
                long idx = getItemId(cursor);
                int position = cursor.getPosition();
                boolean isInEditMode = this.mListView != null ? this.mListView.isInEditMode() : false;
                boolean isSelected = this.mListView != null ? this.mListView.isSelected(idx) : false;
                mli.bind(msgItem, position, this.mDrawableCache);
                mli.setMsgListItemHandler(this.mMsgListItemHandler);
                mli.setEditAble(isInEditMode, isSelected);
                mli.setClickable(false);
                mli.setLongClickable(false);
            }
        }
    }

    public long getItemId(int pos) {
        if (this.mCursor != null && this.mCursor.moveToPosition(pos)) {
            return getItemId(this.mCursor);
        }
        MLog.i("SimMessageListAdapter", "Can not get item id of position.");
        return -1;
    }

    public long getItemId(Cursor c) {
        try {
            return c.getLong(c.getColumnIndexOrThrow("index_on_icc"));
        } catch (Exception e) {
            e.printStackTrace();
            MLog.e("SimMessageListAdapter", "getItemId error.");
            return -1;
        }
    }
}
