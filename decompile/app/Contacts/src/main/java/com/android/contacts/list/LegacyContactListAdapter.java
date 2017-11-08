package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.view.View;
import android.view.ViewGroup;

public class LegacyContactListAdapter extends ContactEntryListAdapter {
    static final String[] PEOPLE_PROJECTION = new String[]{"_id", "display_name", "phonetic_name", "starred", "mode"};
    private CharSequence mUnknownNameText;

    public LegacyContactListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        loader.setUri(People.CONTENT_URI);
        loader.setProjection(PEOPLE_PROJECTION);
        loader.setSortOrder("display_name");
    }

    public Uri getPersonUri(int position) {
        return ContentUris.withAppendedId(People.CONTENT_URI, ((Cursor) getItem(position)).getLong(0));
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
        bindPresence(view, cursor);
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 1, getContactNameDisplayOrder());
        view.showPhoneticName(cursor, 2);
    }

    protected void bindPresence(ContactListItemView view, Cursor cursor) {
        view.showPresenceAndStatusMessage(cursor, 4, 0);
    }
}
