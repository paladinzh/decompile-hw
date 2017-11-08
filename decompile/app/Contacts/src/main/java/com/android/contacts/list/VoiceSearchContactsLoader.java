package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;
import com.android.contacts.compatibility.QueryUtil;
import com.google.common.collect.Lists;
import java.util.List;

public class VoiceSearchContactsLoader extends CursorLoader {
    private boolean mHasProfile;
    private boolean mLoadProfile;
    private String[] mProjection = new String[]{"_id", "display_name", "contact_presence", "contact_status", "photo_id", "photo_thumb_uri", "lookup", "is_user_profile", "name_raw_contact_id", "sort_key", "times_contacted"};

    public VoiceSearchContactsLoader(Context context) {
        super(context);
    }

    public void setLoadProfile(boolean flag) {
        this.mLoadProfile = flag;
    }

    public void setProjection(String[] projection) {
        super.setProjection(projection);
        if (projection == null) {
            this.mProjection = null;
        } else {
            this.mProjection = (String[]) projection.clone();
        }
    }

    public String[] getProjection() {
        return (String[]) this.mProjection.clone();
    }

    public Cursor loadInBackground() {
        List<Cursor> cursors = Lists.newArrayList();
        if (this.mLoadProfile && QueryUtil.isSystemAppForContacts()) {
            MatrixCursor profileCuror = loadProfile();
            if (profileCuror != null) {
                cursors.add(profileCuror);
            }
        } else {
            this.mHasProfile = doesProfileExists();
        }
        final Cursor contactsCursor = super.loadInBackground();
        cursors.add(contactsCursor);
        return new MergeCursor((Cursor[]) cursors.toArray(new Cursor[cursors.size()])) {
            public Bundle getExtras() {
                if (contactsCursor == null) {
                    return null;
                }
                Bundle b = contactsCursor.getExtras();
                if (b == null) {
                    return null;
                }
                b.putBoolean("has_profile", VoiceSearchContactsLoader.this.mHasProfile);
                return b;
            }
        };
    }

    private MatrixCursor loadProfile() {
        Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, this.mProjection, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            MatrixCursor matrix = new MatrixCursor(this.mProjection);
            Object[] row = new Object[this.mProjection.length];
            if (cursor.moveToPosition(-1)) {
                while (cursor.moveToNext()) {
                    for (int i = 0; i < row.length; i++) {
                        row[i] = cursor.getString(i);
                    }
                    matrix.addRow(row);
                }
            }
            cursor.close();
            return matrix;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private boolean doesProfileExists() {
        Cursor cursor = getContext().getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
        if (cursor == null) {
            return false;
        }
        try {
            if (cursor.moveToFirst()) {
                return true;
            }
            cursor.close();
            return false;
        } finally {
            cursor.close();
        }
    }
}
