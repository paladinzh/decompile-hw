package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cspcommon.util.SearchContract$LegacyPhoneQuery;

public class LegacyPhoneNumberListAdapter extends ContactEntryListAdapter {
    private static final String[] PHONES_PROJECTION = new String[]{"_id", "type", "label", "number", "display_name", "phonetic_name"};
    private CharSequence mUnknownNameText;

    public LegacyPhoneNumberListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        loader.setUri(Phones.CONTENT_URI);
        loader.setProjection(PHONES_PROJECTION);
        loader.setSortOrder("display_name");
    }

    public Uri getPhoneUri(int position) {
        return ContentUris.withAppendedId(Phones.CONTENT_URI, ((Cursor) getItem(position)).getLong(0));
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
        bindPhoneNumber(view, cursor);
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 4, getContactNameDisplayOrder());
        view.showPhoneticName(cursor, 5);
    }

    protected void bindPhoneNumber(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(1)) {
            label = Phone.getTypeLabel(getContext().getResources(), cursor.getInt(1), cursor.getString(2));
        }
        view.setLabel(label);
        view.showData(cursor, 3);
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader);
        loader.setProjection(SearchContract$LegacyPhoneQuery.PHONES_PROJECTION);
        loader.setSortOrder("display_name");
    }

    protected void configHwSearchUri(CursorLoader loader) {
        loader.setUri(getHwSearchBaseUri(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, "search_type", "search_phone"));
    }
}
