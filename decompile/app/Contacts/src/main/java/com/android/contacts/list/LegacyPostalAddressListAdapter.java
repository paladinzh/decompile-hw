package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.ContactMethods;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.view.View;
import android.view.ViewGroup;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;

public class LegacyPostalAddressListAdapter extends ContactEntryListAdapter {
    static final String[] POSTALS_PROJECTION = new String[]{"_id", "type", "label", MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, "display_name", "phonetic_name"};
    private CharSequence mUnknownNameText;

    public LegacyPostalAddressListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        loader.setUri(ContactMethods.CONTENT_URI);
        loader.setProjection(POSTALS_PROJECTION);
        loader.setSortOrder("display_name");
        loader.setSelection("kind=2");
    }

    public Uri getContactMethodUri(int position) {
        return ContentUris.withAppendedId(ContactMethods.CONTENT_URI, ((Cursor) getItem(position)).getLong(0));
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        return view;
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        bindName(view, cursor);
        bindPostalAddress(view, cursor);
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 4, getContactNameDisplayOrder());
        view.showPhoneticName(cursor, 5);
    }

    protected void bindPostalAddress(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(1)) {
            label = StructuredPostal.getTypeLabel(getContext().getResources(), cursor.getInt(1), cursor.getString(2));
        }
        view.setLabel(label);
        view.showData(cursor, 3);
    }
}
