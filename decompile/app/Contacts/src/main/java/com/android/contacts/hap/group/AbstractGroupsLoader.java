package com.android.contacts.hap.group;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import com.android.contacts.compatibility.QueryUtil;

public abstract class AbstractGroupsLoader extends CursorLoader {

    static class GroupSummaryCursor extends CursorWrapper {
        private int mCountIndex;
        private int[] mCounts;

        public GroupSummaryCursor(Cursor cursor, int[] counts, int countIndex) {
            super(cursor);
            this.mCounts = counts;
            this.mCountIndex = countIndex;
        }

        public int getInt(int columnIndex) {
            if (columnIndex == this.mCountIndex) {
                return this.mCounts[getPosition()];
            }
            return super.getInt(columnIndex);
        }
    }

    protected abstract int getGroupIdIndex();

    protected abstract int getSummaryCountIndex();

    public AbstractGroupsLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor loadInBackground() {
        Cursor c = super.loadInBackground();
        if (QueryUtil.isHAPProviderInstalled() || c == null || getSummaryCountIndex() == -1 || getGroupIdIndex() == -1) {
            return c;
        }
        ContentResolver cr = getContext().getContentResolver();
        Cursor cur = null;
        String selection = new StringBuffer("mimetype").append(" = '").append("vnd.android.cursor.item/group_membership").append("' AND ").append("raw_contact_id").append(" in (select ").append("_id").append(" from raw_contacts where ").append("deleted").append(" =0) AND ").append("data1").append(" = ?").toString();
        int[] counts = new int[c.getCount()];
        int i = 0;
        while (c.moveToNext()) {
            if (c.getInt(getSummaryCountIndex()) != 0) {
                cur = cr.query(Data.CONTENT_URI, new String[]{"contact_id"}, selection, new String[]{c.getString(getGroupIdIndex())}, null);
                if (cur != null) {
                    counts[i] = cur.getCount();
                } else {
                    try {
                        counts[i] = 0;
                    } catch (Throwable th) {
                        if (cur != null) {
                            cur.close();
                        }
                    }
                }
                if (cur != null) {
                    cur.close();
                    cur = null;
                }
            }
            i++;
        }
        return new GroupSummaryCursor(c, counts, getSummaryCountIndex());
    }
}
