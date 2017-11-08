package com.android.contacts.dialpad;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.android.contacts.dialpad.database.DatabaseHelperManager;
import com.android.contacts.dialpad.database.DialerDatabaseHelper.ContactNumber;
import com.android.contacts.util.HwLog;
import com.huawei.cspcommon.util.SmartDialType;
import java.util.ArrayList;

public class SmartDialCursorLoader extends AsyncTaskLoader<Cursor> {
    private final boolean DEBUG = HwLog.HWDBG;
    private final String TAG = SmartDialCursorLoader.class.getSimpleName();
    private final Context mContext;
    private Cursor mCursor;
    private SmartDialNameMatcher mNameMatcher;
    private String mQuery;

    public SmartDialCursorLoader(Context context) {
        super(context);
        this.mContext = context;
    }

    public void configureQuery(String query) {
        this.mQuery = SmartDialNameMatcher.normalizeNumber(query, SmartDialPrefix.getMap());
        this.mNameMatcher = new SmartDialNameMatcher(this.mQuery, SmartDialPrefix.getMap());
    }

    public Cursor loadInBackground() {
        ArrayList<ContactNumber> allMatches = DatabaseHelperManager.getDatabaseHelper(this.mContext).getLooseMatches(this.mQuery, this.mNameMatcher);
        if (this.DEBUG) {
            HwLog.v(this.TAG, "Loaded matches " + String.valueOf(allMatches.size()));
        }
        MatrixCursor cursor = new MatrixCursor(SmartDialType.getProjection());
        Object[] row = new Object[SmartDialType.getProjection().length];
        for (ContactNumber contact : allMatches) {
            row[0] = Long.valueOf(contact.dataId);
            row[1] = contact.displayName;
            row[2] = contact.sortKey;
            row[3] = Long.valueOf(contact.photoId);
            row[4] = contact.photoUri;
            row[5] = contact.data1;
            row[6] = contact.data2;
            row[7] = contact.data3;
            row[8] = contact.data10;
            row[9] = contact.data11;
            row[10] = Long.valueOf(contact.id);
            row[11] = contact.lookup;
            row[12] = Long.valueOf(contact.timeContacted);
            row[13] = contact.company;
            cursor.addRow(row);
        }
        return cursor;
    }

    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            releaseResources(cursor);
            return;
        }
        Cursor oldCursor = this.mCursor;
        this.mCursor = cursor;
        if (isStarted()) {
            super.deliverResult(cursor);
        }
        if (!(oldCursor == null || oldCursor == cursor)) {
            releaseResources(oldCursor);
        }
    }

    protected void onStartLoading() {
        if (this.mCursor != null) {
            deliverResult(this.mCursor);
        }
        if (this.mCursor == null) {
            forceLoad();
        }
    }

    protected void onStopLoading() {
        cancelLoad();
    }

    protected void onReset() {
        onStopLoading();
        if (this.mCursor != null) {
            releaseResources(this.mCursor);
            this.mCursor = null;
        }
    }

    public void onCanceled(Cursor cursor) {
        super.onCanceled(cursor);
        releaseResources(cursor);
    }

    private void releaseResources(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
