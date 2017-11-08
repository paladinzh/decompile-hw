package com.android.contacts.hap.list;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import com.android.contacts.model.account.AccountWithDataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class RawContactsPhotoFetcher extends Thread {
    private AccountWithDataSet mAccount;
    private RawContactsPhotoFetchListener mListener;
    private HashMap<Long, Long> mPhotoIdForRawContactIdMap;
    private Long[] mRawContactIDs;
    private ContentResolver mResolver;

    public interface RawContactsPhotoFetchListener {
        void onPhotoFetchComplete(HashMap<Long, Long> hashMap);
    }

    public RawContactsPhotoFetcher(AccountWithDataSet aAccount, ContentResolver aResolver) {
        this.mAccount = aAccount;
        this.mResolver = aResolver;
    }

    public RawContactsPhotoFetcher(AccountWithDataSet aAccount, ContentResolver aResolver, Long[] aRawContactIDs) {
        this.mAccount = aAccount;
        this.mResolver = aResolver;
        setRawContactIds(aRawContactIDs);
    }

    private void setRawContactIds(Long[] aRawContactIDs) {
        this.mRawContactIDs = aRawContactIDs;
    }

    public void setRawContactsPhotoFetchListener(RawContactsPhotoFetchListener aListener) {
        this.mListener = aListener;
    }

    public void run() {
        if ((this.mAccount != null || this.mRawContactIDs != null) && this.mResolver != null) {
            Uri rawContactPhotoUri = Data.CONTENT_URI.buildUpon().build();
            String[] projection = new String[]{"_id", "raw_contact_id", "photo_id"};
            StringBuffer selectionBuf = new StringBuffer();
            List<String> selectionArgs = new ArrayList();
            if (this.mRawContactIDs != null && this.mRawContactIDs.length > 0) {
                selectionBuf.append("raw_contact_id IN (");
                for (int i = 0; i < this.mRawContactIDs.length; i++) {
                    selectionBuf.append(this.mRawContactIDs[i]);
                    if (i + 1 < this.mRawContactIDs.length) {
                        selectionBuf.append(",");
                    }
                }
                selectionBuf.append(") AND ");
            }
            selectionBuf.append("mimetype=?");
            selectionArgs.add("vnd.android.cursor.item/photo");
            if (this.mAccount != null) {
                selectionBuf.append(" AND ");
                selectionBuf.append("account_name=? AND ").append("account_type=?");
                selectionArgs.add(this.mAccount.name);
                selectionArgs.add(this.mAccount.type);
            }
            if (this.mAccount != null) {
                if (this.mAccount.dataSet != null) {
                    selectionBuf.append(" AND data_set=?");
                    selectionArgs.add(this.mAccount.dataSet);
                } else {
                    selectionBuf.append(" AND data_set IS NULL");
                }
            }
            Cursor rawContactsPhotoCursor = this.mResolver.query(rawContactPhotoUri, projection, selectionBuf.toString(), (String[]) selectionArgs.toArray(new String[0]), null);
            this.mPhotoIdForRawContactIdMap = new HashMap();
            if (rawContactsPhotoCursor != null) {
                try {
                    if (rawContactsPhotoCursor.moveToFirst()) {
                        do {
                            this.mPhotoIdForRawContactIdMap.put(Long.valueOf(rawContactsPhotoCursor.getLong(1)), Long.valueOf(rawContactsPhotoCursor.getLong(2)));
                        } while (rawContactsPhotoCursor.moveToNext());
                    }
                    rawContactsPhotoCursor.close();
                } catch (Throwable th) {
                    rawContactsPhotoCursor.close();
                }
            }
            if (this.mListener != null) {
                this.mListener.onPhotoFetchComplete(this.mPhotoIdForRawContactIdMap);
            }
        }
    }
}
