package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;

public class GroupSmsDetailsListAdapter extends CursorAdapter {
    static final String[] DETAILS_PROJECTION = new String[]{"_id", "address", "body", "sub_id", "date", "date_sent", NumberInfo.TYPE_KEY, "status", "locked", "group_id"};
    public final ColumnsSmsMap mColumnsMap = new ColumnsSmsMap();
    protected LayoutInflater mInflater;
    private ArrayList<GroupSmsDetailsItem> mMsgDetailList;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private int mPosition;

    public interface OnDataSetChangedListener {
        void onContentChanged(GroupSmsDetailsListAdapter groupSmsDetailsListAdapter);

        void onDataSetChanged(GroupSmsDetailsListAdapter groupSmsDetailsListAdapter);
    }

    public static class ColumnsSmsMap {
        public int mColumnMsgId = 0;
        public int mColumnSmsAddress = 1;
        public int mColumnSmsBody = 2;
        public int mColumnSmsDate = 4;
        public int mColumnSmsDateSent = 5;
        public int mColumnSmsStatus = 7;
        public int mColumnSmsType = 6;
        public int mColumnSubId = 3;
    }

    public GroupSmsDetailsListAdapter(Context context, Cursor c) {
        super(context, c, 2);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.mCursor.moveToPosition(position)) {
            View v;
            this.mPosition = position;
            if (convertView == null) {
                v = newView(this.mContext, this.mCursor, parent);
                MLog.d("GroupSmsDetailsListAdapter", "convertView is null, infalte view");
            } else {
                v = convertView;
            }
            MLog.d("GroupSmsDetailsListAdapter", "bindView");
            bindView(v, this.mContext, this.mCursor);
            return v;
        } else {
            MLog.w("GroupSmsDetailsListAdapter", "couldn't move cursor to position " + position);
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return this.mInflater.inflate(R.layout.group_sms_details_list_item, parent, false);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof GroupSmsDetailsListItem) {
            GroupSmsDetailsItem msgItem = (GroupSmsDetailsItem) getItem(this.mPosition);
            if (msgItem == null) {
                MLog.e("GroupSmsDetailsListAdapter", "bind view item is null");
                return;
            } else {
                ((GroupSmsDetailsListItem) view).bind(msgItem);
                return;
            }
        }
        MLog.e("GroupSmsDetailsListAdapter", "Unexpected bound view: " + view);
    }

    public int getItemViewType(int position) {
        if (getItem(position) == null) {
            return -1;
        }
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        this.mOnDataSetChangedListener = l;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (MessageUtils.getZoomFlag()) {
            MessageUtils.setZoomFlag(false);
        }
        if (this.mOnDataSetChangedListener != null) {
            this.mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed() && this.mOnDataSetChangedListener != null) {
            this.mOnDataSetChangedListener.onContentChanged(this);
        }
    }

    public void setData(ArrayList<GroupSmsDetailsItem> list) {
        this.mMsgDetailList = list;
    }

    public Object getItem(int position) {
        return this.mMsgDetailList == null ? null : this.mMsgDetailList.get(position);
    }

    public int getCount() {
        if (this.mMsgDetailList != null) {
            return this.mMsgDetailList.size();
        }
        return 0;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null && !old.isClosed()) {
            old.close();
        }
    }
}
