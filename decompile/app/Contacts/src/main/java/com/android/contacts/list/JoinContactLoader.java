package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.compatibility.QueryUtil;

public class JoinContactLoader extends CursorLoader {
    private String[] mProjection;
    private Uri mSuggestionUri;

    public static class JoinContactLoaderResult extends CursorWrapper {
        public final Cursor suggestionCursor;

        public JoinContactLoaderResult(Cursor baseCursor, Cursor suggestionCursor) {
            super(baseCursor);
            this.suggestionCursor = suggestionCursor;
        }

        public void close() {
            try {
                this.suggestionCursor.close();
            } finally {
                super.close();
            }
        }
    }

    public JoinContactLoader(Context context) {
        super(context, null, null, null, null, null);
    }

    public void setSuggestionUri(Uri uri) {
        this.mSuggestionUri = uri;
    }

    public void setProjection(String[] projection) {
        super.setProjection(projection);
        if (projection == null) {
            this.mProjection = null;
        } else {
            this.mProjection = (String[]) projection.clone();
        }
    }

    public Cursor loadInBackground() {
        Cursor suggestionsCursor = getContext().getContentResolver().query(this.mSuggestionUri, this.mProjection, null, null, null);
        if (!(QueryUtil.isHAPProviderInstalled() || suggestionsCursor == null || !suggestionsCursor.moveToFirst())) {
            StringBuilder sb = new StringBuilder("_id IN (");
            do {
                sb.append(suggestionsCursor.getLong(suggestionsCursor.getColumnIndex("_id"))).append(",");
            } while (suggestionsCursor.moveToNext());
            sb.setLength(sb.length() - 1);
            sb.append(")");
            sb.append(" AND _id NOT IN (SELECT DISTINCT contact_id FROM view_raw_contacts  WHERE account_type IN ('com.android.huawei.sim','com.android.huawei.secondsim'))");
            suggestionsCursor.close();
            suggestionsCursor = getContext().getContentResolver().query(Contacts.CONTENT_URI, this.mProjection, sb.toString(), null, null);
        }
        return new JoinContactLoaderResult(super.loadInBackground(), suggestionsCursor);
    }
}
