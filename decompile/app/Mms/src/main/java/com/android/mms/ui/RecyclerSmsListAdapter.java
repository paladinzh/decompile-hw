package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import com.google.android.gms.R;
import com.huawei.mms.ui.EditableList;

public class RecyclerSmsListAdapter extends CursorAdapter {
    private long mActivityViewTime = -1;
    Context mContext;
    protected LayoutInflater mInflater;
    private int mItemResourseId;
    EditableList mListView;

    public RecyclerSmsListAdapter(Context context, Cursor cursor, EditableList listView, int resourceId) {
        super(context, cursor, resourceId);
        this.mContext = context;
        this.mListView = listView;
        this.mItemResourseId = resourceId;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof RecyclerSmsListItem) {
            ((RecyclerSmsListItem) view).bind(cursor, this.mListView.isInEditMode(), this.mListView.isSelected(cursor.getLong(0)), this.mActivityViewTime);
            return;
        }
        Log.e("mms:recyclerlistadapter", "bindView:: the view type is not RecyclerSmsListItem!!");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.mCursor.moveToPosition(position)) {
            View view;
            if (convertView == null) {
                view = newView(this.mContext, this.mCursor, parent);
            } else {
                view = convertView;
            }
            bindView(view.findViewById(R.id.mms_animation_list_item_view), this.mContext, this.mCursor);
            return view;
        } else {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return this.mInflater.inflate(this.mItemResourseId, viewGroup, false);
    }

    public long getItemId(int position) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(position)) {
            return -1;
        }
        return this.mCursor.getLong(0);
    }

    public void setActivityViewTime(long currentTime) {
        this.mActivityViewTime = currentTime;
    }
}
