package com.android.contacts.hap.list;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.DirectoryPartition;
import com.google.android.gms.R;

public abstract class DataListAdapter extends ContactEntryListAdapter {
    static final String[] PROJECTION_DATA = new String[]{"contact_id", "_id", "display_name", "display_name_alt", "photo_id", "photo_thumb_uri", "lookup", "data1", "data2", "data3", "mimetype", "is_primary", "company", "is_super_primary"};
    private int mDisplayNameColumnIndex;
    private CharSequence mUnknownNameText;

    public abstract long getContactId(int i);

    public abstract int getDataPrimary(int i);

    public abstract int getDataType(int i);

    public abstract int getDataTypeByNum(int i);

    public abstract String getSelectedData(int i);

    public abstract long getSelectedDataId(int i);

    public abstract Uri getSelectedDataUri(int i);

    public DataListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(R.string.missing_name);
    }

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon().appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true").build();
    }

    public String getContactDisplayName(int position) {
        if (((Cursor) getItem(position)) == null) {
            return null;
        }
        return ((Cursor) getItem(position)).getString(this.mDisplayNameColumnIndex);
    }

    public void setContactNameDisplayOrder(int displayOrder) {
        super.setContactNameDisplayOrder(displayOrder);
        if (getContactNameDisplayOrder() == 1) {
            this.mDisplayNameColumnIndex = 2;
        } else {
            this.mDisplayNameColumnIndex = 3;
        }
    }

    public Uri getContactUri(int position) {
        int partitionIndex = getPartitionForPosition(position);
        Cursor item = (Cursor) getItem(position);
        if (item != null) {
            return getContactUri(partitionIndex, item);
        }
        return null;
    }

    public Uri getContactUri(int partitionIndex, Cursor cursor) {
        Uri uri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(6));
        long directoryId = ((DirectoryPartition) getPartition(partitionIndex)).getDirectoryId();
        if (directoryId != 0) {
            return uri.buildUpon().appendQueryParameter("directory", String.valueOf(directoryId)).build();
        }
        return uri;
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        view.setActivatedStateSupported(isSelectionVisible());
        return view;
    }

    protected void bindCheckBox(ContactListItemView view) {
        view.showCheckBox();
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, this.mDisplayNameColumnIndex, getContactNameDisplayOrder());
    }

    public void changeCursor(int partitionIndex, Cursor cursor) {
        super.changeCursor(partitionIndex, cursor);
    }
}
