package com.android.common.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class GroupingListAdapter extends BaseAdapter {
    protected ContentObserver mChangeObserver = new ContentObserver(new Handler()) {
        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean selfChange) {
            GroupingListAdapter.this.onContentChanged();
        }
    };
    private Context mContext;
    private int mCount;
    private Cursor mCursor;
    protected DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onChanged() {
            GroupingListAdapter.this.notifyDataSetChanged();
        }

        public void onInvalidated() {
            GroupingListAdapter.this.notifyDataSetInvalidated();
        }
    };
    private int mGroupCount;
    private long[] mGroupMetadata;
    private int mLastCachedCursorPosition;
    private int mLastCachedGroup;
    private int mLastCachedListPosition;
    private SparseIntArray mPositionCache = new SparseIntArray();
    private PositionMetadata mPositionMetadata = new PositionMetadata();
    private int mRowIdColumnIndex;

    protected static class PositionMetadata {
        int childCount;
        int cursorPosition;
        private int groupPosition;
        boolean isExpanded;
        int itemType;
        private int listPosition = -1;

        protected PositionMetadata() {
        }
    }

    protected abstract void addGroups(Cursor cursor);

    protected abstract void bindChildView(View view, Context context, Cursor cursor);

    protected abstract void bindGroupView(View view, Context context, Cursor cursor, int i, boolean z);

    protected abstract void bindStandAloneView(View view, Context context, Cursor cursor);

    protected abstract View newChildView(Context context, ViewGroup viewGroup);

    protected abstract View newGroupView(Context context, ViewGroup viewGroup);

    protected abstract View newStandAloneView(Context context, ViewGroup viewGroup);

    public void obtainPositionMetadata(com.android.common.widget.GroupingListAdapter.PositionMetadata r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.common.widget.GroupingListAdapter.obtainPositionMetadata(com.android.common.widget.GroupingListAdapter$PositionMetadata, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.common.widget.GroupingListAdapter.obtainPositionMetadata(com.android.common.widget.GroupingListAdapter$PositionMetadata, int):void");
    }

    public GroupingListAdapter(Context context) {
        this.mContext = context;
        resetCache();
    }

    private void resetCache() {
        this.mCount = -1;
        this.mLastCachedListPosition = -1;
        this.mLastCachedCursorPosition = -1;
        this.mLastCachedGroup = -1;
        this.mPositionMetadata.listPosition = -1;
        this.mPositionCache.clear();
    }

    protected void onContentChanged() {
    }

    public void changeCursor(Cursor cursor) {
        if (cursor != this.mCursor) {
            if (this.mCursor != null) {
                this.mCursor.unregisterContentObserver(this.mChangeObserver);
                this.mCursor.unregisterDataSetObserver(this.mDataSetObserver);
                this.mCursor.close();
            }
            this.mCursor = cursor;
            resetCache();
            findGroups();
            if (cursor != null) {
                cursor.registerContentObserver(this.mChangeObserver);
                cursor.registerDataSetObserver(this.mDataSetObserver);
                this.mRowIdColumnIndex = cursor.getColumnIndexOrThrow("_id");
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public Cursor getCursor() {
        return this.mCursor;
    }

    private void findGroups() {
        this.mGroupCount = 0;
        this.mGroupMetadata = new long[16];
        if (this.mCursor != null) {
            addGroups(this.mCursor);
        }
    }

    protected void addGroup(int cursorPosition, int size, boolean expanded) {
        if (this.mGroupCount >= this.mGroupMetadata.length) {
            long[] array = new long[idealLongArraySize(this.mGroupMetadata.length + 128)];
            System.arraycopy(this.mGroupMetadata, 0, array, 0, this.mGroupCount);
            this.mGroupMetadata = array;
        }
        long metadata = (((long) size) << 32) | ((long) cursorPosition);
        if (expanded) {
            metadata |= Long.MIN_VALUE;
        }
        long[] jArr = this.mGroupMetadata;
        int i = this.mGroupCount;
        this.mGroupCount = i + 1;
        jArr[i] = metadata;
    }

    private int idealLongArraySize(int need) {
        return idealByteArraySize(need * 8) / 8;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    public int getCount() {
        if (this.mCursor == null) {
            return 0;
        }
        if (this.mCount != -1) {
            return this.mCount;
        }
        int cursorPosition = 0;
        int count = 0;
        for (int i = 0; i < this.mGroupCount; i++) {
            boolean expanded;
            long metadata = this.mGroupMetadata[i];
            int offset = (int) (4294967295L & metadata);
            if ((Long.MIN_VALUE & metadata) != 0) {
                expanded = true;
            } else {
                expanded = false;
            }
            int size = (int) ((9223372032559808512L & metadata) >> 32);
            count += offset - cursorPosition;
            if (expanded) {
                count += size + 1;
            } else {
                count++;
            }
            cursorPosition = offset + size;
        }
        this.mCount = (this.mCursor.getCount() + count) - cursorPosition;
        return this.mCount;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(int position) {
        obtainPositionMetadata(this.mPositionMetadata, position);
        return this.mPositionMetadata.itemType;
    }

    public Object getItem(int position) {
        if (this.mCursor == null) {
            return null;
        }
        obtainPositionMetadata(this.mPositionMetadata, position);
        if (this.mCursor.moveToPosition(this.mPositionMetadata.cursorPosition)) {
            return this.mCursor;
        }
        return null;
    }

    public long getItemId(int position) {
        if (getItem(position) != null) {
            return this.mCursor.getLong(this.mRowIdColumnIndex);
        }
        return -1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        obtainPositionMetadata(this.mPositionMetadata, position);
        View view = convertView;
        if (convertView == null) {
            switch (this.mPositionMetadata.itemType) {
                case 0:
                    view = newStandAloneView(this.mContext, parent);
                    break;
                case 1:
                    view = newGroupView(this.mContext, parent);
                    break;
                case 2:
                    view = newChildView(this.mContext, parent);
                    break;
            }
        }
        this.mCursor.moveToPosition(this.mPositionMetadata.cursorPosition);
        switch (this.mPositionMetadata.itemType) {
            case 0:
                bindStandAloneView(view, this.mContext, this.mCursor);
                break;
            case 1:
                bindGroupView(view, this.mContext, this.mCursor, this.mPositionMetadata.childCount, this.mPositionMetadata.isExpanded);
                break;
            case 2:
                bindChildView(view, this.mContext, this.mCursor);
                break;
        }
        return view;
    }
}
