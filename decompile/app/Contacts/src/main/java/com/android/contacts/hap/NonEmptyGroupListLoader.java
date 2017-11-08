package com.android.contacts.hap;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader.ForceLoadContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import com.android.contacts.hap.rcs.RcsNonEmptyGroupListLoader;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.List;

public class NonEmptyGroupListLoader extends CursorLoader {
    private static int CHINA_PHONE_NUMBER_LENGTH = 11;
    private static final String[] GROUP_COLUMNS = new String[]{"account_name", "account_type", "data_set", "_id", "title", "deleted", "group_is_read_only", "title_res", "res_package", "sync4", "sync1"};
    private static final Uri GROUP_LIST_URI = Groups.CONTENT_SUMMARY_URI.buildUpon().appendQueryParameter("remove_duplicate_entries", "contact_id").build();
    private int mFilterType;
    private boolean mHasFavourites;
    private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver(this);
    private RcsNonEmptyGroupListLoader mRcsCust = null;

    public NonEmptyGroupListLoader(Context context, int filterType) {
        super(context);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsNonEmptyGroupListLoader(context, filterType);
        }
        this.mFilterType = filterType;
        configureGroupLoader();
    }

    public Cursor loadInBackground() {
        Cursor cursor = null;
        List<Cursor> cursors = Lists.newArrayList();
        Cursor mGroupsCursor = super.loadInBackground();
        if (mGroupsCursor != null) {
            try {
                if (isHasFavorites()) {
                    cursors.add(loadFavoritesHeader());
                }
                cursors.add(loadGroupsCursor(mGroupsCursor));
                cursor = new MergeCursor((Cursor[]) cursors.toArray(new Cursor[cursors.size()]));
            } catch (Throwable th) {
                if (mGroupsCursor != null) {
                    mGroupsCursor.close();
                }
            }
        }
        if (mGroupsCursor != null) {
            mGroupsCursor.close();
        }
        return cursor;
    }

    private boolean isHasFavorites() {
        String mimetypes;
        String filterStr;
        Throwable th;
        Cursor cursor = null;
        switch (this.mFilterType) {
            case 211:
                mimetypes = "'vnd.android.cursor.item/phone_v2'";
                filterStr = "has_phone_number = 1";
                break;
            case 212:
                mimetypes = "'vnd.android.cursor.item/email_v2'";
                filterStr = "has_email = 1";
                break;
            default:
                mimetypes = "'vnd.android.cursor.item/phone_v2'";
                filterStr = "has_phone_number = 1";
                break;
        }
        try {
            Uri uri = Contacts.CONTENT_URI;
            String selection = "starred = 1 AND " + filterStr;
            if (this.mRcsCust != null) {
                uri = Data.CONTENT_URI;
                selection = this.mRcsCust.appendCustomizationsFilterForDatasSelection("starred = 1 AND mimetype IN (" + mimetypes + ")");
            }
            try {
                cursor = getContext().getContentResolver().query(uri, new String[]{"_id"}, selection, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    this.mHasFavourites = false;
                    return false;
                }
                this.mHasFavourites = true;
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            } catch (Throwable th2) {
                th = th2;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            String[] strArr = null;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void configureGroupLoader() {
        String selection = String.format("account_type NOT NULL AND account_name NOT NULL AND auto_add= 0 AND favorites= 0 AND deleted=0 AND (sync1 IS NULL  OR sync1 != '%1$s')", new Object[]{getContext().getString(R.string.private_group_sync1)});
        if (this.mRcsCust != null) {
            selection = this.mRcsCust.appendCustomizationsFilterForGroupsSelection(selection);
        }
        if (this.mFilterType == 212) {
            selection = selection + " and _id in (select distinct data1 from view_data where mimetype = 'vnd.android.cursor.item/group_membership' AND has_email=1)";
        } else {
            selection = selection + " and _id in (select distinct data1 from view_data where mimetype = 'vnd.android.cursor.item/group_membership' AND has_phone_number=1)";
        }
        setUri(GROUP_LIST_URI);
        setProjection(GROUP_COLUMNS);
        setSelection(selection);
        setSortOrder("account_type, account_name, data_set, title COLLATE LOCALIZED ASC");
    }

    private MatrixCursor loadFavoritesHeader() {
        MatrixCursor matrix = new MatrixCursor(GROUP_COLUMNS);
        Object[] row = new Object[GROUP_COLUMNS.length];
        row[4] = getContext().getString(R.string.contactsFavoritesLabel);
        row[3] = Integer.valueOf(0);
        matrix.addRow(row);
        return matrix;
    }

    private MatrixCursor loadGroupsCursor(Cursor mGroupsCursor) {
        MatrixCursor matrix = new MatrixCursor(GROUP_COLUMNS);
        if (mGroupsCursor.moveToFirst()) {
            int columnId = mGroupsCursor.getColumnIndex("_id");
            do {
                Object[] row = new Object[GROUP_COLUMNS.length];
                row[3] = Long.valueOf(mGroupsCursor.getLong(columnId));
                row[0] = mGroupsCursor.getString(0);
                row[1] = mGroupsCursor.getString(1);
                row[2] = mGroupsCursor.getString(2);
                row[6] = Integer.valueOf(mGroupsCursor.getInt(6));
                row[8] = mGroupsCursor.getString(8);
                row[10] = mGroupsCursor.getString(10);
                row[9] = mGroupsCursor.getString(9);
                row[7] = Integer.valueOf(mGroupsCursor.getInt(7));
                row[5] = Integer.valueOf(mGroupsCursor.getInt(5));
                row[4] = mGroupsCursor.getString(4);
                matrix.addRow(row);
            } while (mGroupsCursor.moveToNext());
        }
        matrix.setNotificationUri(getContext().getContentResolver(), GROUP_LIST_URI);
        matrix.registerContentObserver(this.mObserver);
        return matrix;
    }

    public boolean hasFavouritesValue() {
        return this.mHasFavourites;
    }
}
