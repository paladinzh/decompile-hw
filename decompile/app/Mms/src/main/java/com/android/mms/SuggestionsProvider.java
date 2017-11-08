package com.android.mms;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.mms.ui.SearchDataLoader;
import com.android.mms.ui.SearchDataLoader.ConversationMatcher;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AutoExtendArray;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.ArrayList;

public class SuggestionsProvider extends ContentProvider {

    private static class SuggestionsCursor implements CrossProcessCursor {
        int mColSourceId = -1;
        int mColWhichTable = -1;
        int mColumnCount;
        int mCurrentRow;
        Cursor mDatabaseCursor;
        private String mFavoritePrefix = "";
        String mQuery;
        ArrayList<Row> mRows = new ArrayList();
        int mSizeContact;
        private String[] mVirtualColumns = new String[]{"suggest_intent_data", "suggest_intent_action", "suggest_intent_extra_data", "suggest_text_1"};

        private static class Row {
            private int mRowNumber;
            private String mSnippet;
            private int mWhichTable;

            public Row(int row, String snippet) {
                this.mSnippet = snippet.trim();
                this.mRowNumber = row;
            }

            public Row(int row, int whichTable, String snippet) {
                this.mSnippet = snippet.trim();
                this.mRowNumber = row;
                this.mWhichTable = whichTable;
                this.mSnippet = snippet;
            }

            public String getSnippet() {
                return this.mSnippet;
            }

            public int getSourceId() {
                return this.mRowNumber;
            }

            public int getWhichTable() {
                return this.mWhichTable;
            }
        }

        public SuggestionsCursor(Context context, Cursor cursor, String query) {
            this.mDatabaseCursor = cursor;
            this.mQuery = query;
            this.mFavoritePrefix = context.getResources().getString(R.string.mms_myfavorite_common);
            this.mColWhichTable = this.mDatabaseCursor.getColumnIndex("table_to_use");
            if (this.mColWhichTable >= 0) {
                this.mColSourceId = this.mDatabaseCursor.getColumnIndex("source_id");
            }
            this.mColumnCount = 1;
            try {
                computeRowsContact(this.mQuery);
                computeRows();
            } catch (SQLiteException e) {
                this.mRows.clear();
            }
        }

        public int getCount() {
            return this.mRows.size();
        }

        public void setExtras(Bundle extras) {
        }

        private void computeRowsContact(String queryText) {
            AutoExtendArray<com.android.mms.ui.SearchDataLoader.Row> contactRows = ConversationMatcher.getDefault().query(queryText);
            this.mSizeContact = contactRows == null ? 0 : contactRows.size();
            for (int i = 0; i < this.mSizeContact; i++) {
                com.android.mms.ui.SearchDataLoader.Row r = (com.android.mms.ui.SearchDataLoader.Row) contactRows.get(i);
                int tid = (int) r.getLong(SearchDataLoader.getColPosThreadId());
                String number = r.getString(SearchDataLoader.getColPosAddress());
                if (!TextUtils.isEmpty(number)) {
                    this.mRows.add(new Row(tid, number));
                }
            }
        }

        private void computeRows() {
            int snippetCol = this.mDatabaseCursor.getColumnIndex("snippet");
            int subjectCol = this.mDatabaseCursor.getColumnIndex("is_sub");
            int count = this.mDatabaseCursor.getCount();
            for (int i = 0; i < count; i++) {
                this.mDatabaseCursor.moveToPosition(i);
                String snippet = this.mDatabaseCursor.getString(snippetCol);
                if (!TextUtils.isEmpty(snippet)) {
                    boolean isSubject;
                    if (subjectCol > 0) {
                        isSubject = "1".equals(this.mDatabaseCursor.getString(subjectCol));
                    } else {
                        isSubject = false;
                    }
                    if (isSubject) {
                        snippet = getUTFEncodedSubject(snippet);
                    }
                    if (this.mColSourceId > 0) {
                        int sId = (int) this.mDatabaseCursor.getLong(this.mColSourceId);
                        if (isSubject) {
                            sId = 0 - sId;
                        }
                        int whichTable = this.mDatabaseCursor.getInt(this.mColWhichTable);
                        if (whichTable == 9 || whichTable == 8) {
                            snippet = this.mFavoritePrefix + " " + snippet;
                        }
                        this.mRows.add(new Row(sId, whichTable, snippet));
                    } else {
                        this.mRows.add(new Row(i, snippet));
                    }
                }
            }
        }

        private String getUTFEncodedSubject(String snippet) {
            try {
                return new String(snippet.getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                MLog.i("SuggestionsProvider", "got exception:" + e.getMessage());
                return snippet;
            }
        }

        public void fillWindow(int position, CursorWindow window) {
            int count = getCount();
            if (position >= 0 && position <= count + 1) {
                window.acquireReference();
                try {
                    int oldpos = getPosition();
                    window.clear();
                    window.setStartPosition(position);
                    int columnNum = getColumnCount();
                    window.setNumColumns(columnNum);
                    for (int pos = position; moveToPosition(pos) && window.allocRow(); pos++) {
                        for (int i = 0; i < columnNum; i++) {
                            String field = getString(i);
                            if (field != null) {
                                if (!window.putString(field, pos, i)) {
                                    window.freeLastRow();
                                    break;
                                }
                            } else if (!window.putNull(pos, i)) {
                                window.freeLastRow();
                                break;
                            }
                        }
                    }
                    moveToPosition(oldpos);
                } catch (IllegalStateException e) {
                } finally {
                    window.releaseReference();
                }
            }
        }

        public CursorWindow getWindow() {
            return null;
        }

        public boolean onMove(int oldPosition, int newPosition) {
            return ((CrossProcessCursor) this.mDatabaseCursor).onMove(oldPosition, newPosition);
        }

        public int getColumnCount() {
            return this.mColumnCount + this.mVirtualColumns.length;
        }

        public int getColumnIndex(String columnName) {
            for (int i = 0; i < this.mVirtualColumns.length; i++) {
                if (this.mVirtualColumns[i].equals(columnName)) {
                    return this.mColumnCount + i;
                }
            }
            return this.mDatabaseCursor.getColumnIndex(columnName);
        }

        public String[] getColumnNames() {
            String[] y = new String[getColumnCount()];
            int i = 1;
            y[0] = this.mDatabaseCursor.getColumnName(0);
            int k = 0;
            while (k < this.mVirtualColumns.length) {
                int i2 = i + 1;
                int k2 = k + 1;
                y[i] = this.mVirtualColumns[k];
                k = k2;
                i = i2;
            }
            return y;
        }

        public boolean moveToPosition(int position) {
            if (position < 0 || position >= this.mRows.size()) {
                return false;
            }
            this.mCurrentRow = position;
            this.mDatabaseCursor.moveToPosition(position - this.mSizeContact);
            return true;
        }

        public boolean move(int offset) {
            return moveToPosition(this.mCurrentRow + offset);
        }

        public boolean moveToFirst() {
            return moveToPosition(0);
        }

        public boolean moveToLast() {
            return moveToPosition(this.mRows.size() - 1);
        }

        public boolean moveToNext() {
            return moveToPosition(this.mCurrentRow + 1);
        }

        public boolean moveToPrevious() {
            return moveToPosition(this.mCurrentRow - 1);
        }

        public String getString(int column) {
            if (column < this.mColumnCount) {
                return this.mCurrentRow < this.mSizeContact ? getContactData(column) : this.mDatabaseCursor.getString(column);
            }
            Row row = (Row) this.mRows.get(this.mCurrentRow);
            switch (column - this.mColumnCount) {
                case 0:
                    return getIntentData(row);
                case 1:
                    return "com.android.mms.action.SEARCH_GOLBAL";
                case 2:
                    return row.getSnippet();
                case 3:
                    return row.getSnippet();
                default:
                    return null;
            }
        }

        private String getContactData(int column) {
            return ((Row) this.mRows.get(this.mCurrentRow)).getSnippet();
        }

        private String getIntentData(Row r) {
            Builder b = Uri.parse("content://com.android.mms.SuggestionsProvider/search").buildUpon();
            int sId = r.getSourceId();
            if (sId < 0) {
                sId = 0 - sId;
                b.appendQueryParameter("subject", "true");
            }
            String srcId = String.valueOf(sId);
            if (this.mCurrentRow < this.mSizeContact) {
                b.appendQueryParameter("thread_id", srcId);
            } else if (this.mColSourceId > 0) {
                b.appendQueryParameter("source_id", srcId);
                b.appendQueryParameter("table_to_use", String.valueOf(r.getWhichTable()));
            }
            return b.build().toString();
        }

        public void close() {
            this.mDatabaseCursor.close();
        }

        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
            this.mDatabaseCursor.copyStringToBuffer(columnIndex, buffer);
        }

        public void deactivate() {
            this.mDatabaseCursor.deactivate();
        }

        public byte[] getBlob(int columnIndex) {
            return null;
        }

        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            return 0;
        }

        public String getColumnName(int columnIndex) {
            return null;
        }

        public double getDouble(int columnIndex) {
            return 0.0d;
        }

        public Bundle getExtras() {
            return Bundle.EMPTY;
        }

        public float getFloat(int columnIndex) {
            return 0.0f;
        }

        public int getInt(int columnIndex) {
            return 0;
        }

        public long getLong(int columnIndex) {
            return 0;
        }

        public int getPosition() {
            return this.mCurrentRow;
        }

        public short getShort(int columnIndex) {
            return (short) 0;
        }

        public boolean getWantsAllOnMoveCalls() {
            return false;
        }

        public boolean isAfterLast() {
            return this.mCurrentRow >= this.mRows.size();
        }

        public boolean isBeforeFirst() {
            return this.mCurrentRow < 0;
        }

        public boolean isClosed() {
            return this.mDatabaseCursor.isClosed();
        }

        public boolean isFirst() {
            return this.mCurrentRow == 0;
        }

        public boolean isLast() {
            return this.mCurrentRow == this.mRows.size() + -1;
        }

        public int getType(int columnIndex) {
            throw new UnsupportedOperationException();
        }

        public boolean isNull(int columnIndex) {
            return false;
        }

        public void registerContentObserver(ContentObserver observer) {
            this.mDatabaseCursor.registerContentObserver(observer);
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            this.mDatabaseCursor.registerDataSetObserver(observer);
        }

        public boolean requery() {
            return false;
        }

        public Bundle respond(Bundle extras) {
            return this.mDatabaseCursor.respond(extras);
        }

        public void setNotificationUri(ContentResolver cr, Uri uri) {
            this.mDatabaseCursor.setNotificationUri(cr, uri);
        }

        public Uri getNotificationUri() {
            return this.mDatabaseCursor.getNotificationUri();
        }

        public void unregisterContentObserver(ContentObserver observer) {
            this.mDatabaseCursor.unregisterContentObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            this.mDatabaseCursor.unregisterDataSetObserver(observer);
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Builder bulder = Uri.parse("content://mms-sms/searchSuggest").buildUpon();
        bulder.appendQueryParameter("pattern", selectionArgs[0]);
        bulder.appendQueryParameter("search_with_intent_data", "true");
        Cursor c = SqliteWrapper.query(getContext(), bulder.build(), null, null, null, null);
        if (c != null) {
            return new SuggestionsCursor(getContext(), c, selectionArgs[0]);
        }
        MLog.e("SuggestionsProvider", "query Suggestion fail " + selectionArgs[0]);
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
