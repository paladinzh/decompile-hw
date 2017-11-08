package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchContactsCursor;
import com.android.contacts.list.ContactEntryListFragment.ContactsSearchLoader;

public class ContactListLoader extends CursorLoader implements ContactsSearchLoader {
    private String mQueryString;

    public ContactListLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public void setQueryString(String queryStr) {
        this.mQueryString = queryStr;
    }

    public String getQueryString() {
        return this.mQueryString;
    }

    public Cursor loadInBackground() {
        Cursor cursor = super.loadInBackground();
        if (TextUtils.isEmpty(this.mQueryString) || !QueryUtil.isUseHwSearch()) {
            return cursor;
        }
        return new HwSearchContactsCursor(cursor);
    }
}
