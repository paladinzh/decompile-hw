package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.util.ContactDisplayUtils;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cspcommon.util.SearchContract$EmailQuery;
import java.util.Locale;

public class EmailAddressListAdapter extends ContactEntryListAdapter {
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private final CharSequence mUnknownNameText;

    protected static class EmailQuery {
        private static final String[] PROJECTION_ALTERNATIVE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name_alt"};
        private static final String[] PROJECTION_PRIMARY = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name"};

        protected EmailQuery() {
        }
    }

    public EmailAddressListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        Builder builder;
        if (isSearchMode()) {
            builder = Email.CONTENT_FILTER_URI.buildUpon();
            String query = getQueryString();
            if (TextUtils.isEmpty(query)) {
                query = "";
            }
            builder.appendPath(query);
        } else {
            builder = Email.CONTENT_URI.buildUpon();
            if (isSectionHeaderDisplayEnabled()) {
                builder.appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true");
            }
        }
        builder.appendQueryParameter("directory", String.valueOf(directoryId));
        builder.appendQueryParameter("remove_duplicate_entries", "true");
        loader.setUri(builder.build());
        if (getContactNameDisplayOrder() == 1) {
            loader.setProjection(EmailQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(EmailQuery.PROJECTION_ALTERNATIVE);
        }
        if (getSortOrder() == 1) {
            loader.setSortOrder("sort_key");
        } else {
            loader.setSortOrder("sort_key_alt");
        }
    }

    public Uri getDataUri(int position) {
        return ContentUris.withAppendedId(Data.CONTENT_URI, ((Cursor) getItem(position)).getLong(1));
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        return view;
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        bindSectionHeaderAndDivider(view, position);
        bindName(view, cursor);
        bindPhoto(view, cursor);
        bindEmailAddress(view, cursor);
    }

    protected void bindEmailAddress(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(2)) {
            label = Email.getTypeLabel(getContext().getResources(), cursor.getInt(2), cursor.getString(3));
        }
        view.setLabel(label);
        view.showData(cursor, 4);
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position) {
        int section = getSectionForPosition(position);
        if (getPositionForSection(section) == position) {
            view.setSectionHeader(getSections()[section]);
        } else {
            view.setSectionHeader(null);
        }
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 6, getContactNameDisplayOrder());
    }

    protected void bindPhoto(ContactListItemView view, Cursor cursor) {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = 0;
        if (!cursor.isNull(5)) {
            photoId = cursor.getLong(5);
        }
        DefaultImageRequest request = null;
        if (photoId <= 0) {
            this.mRequest.displayName = cursor.getString(6);
            this.mRequest.identifier = cursor.getString(0);
            this.mRequest.isCircular = true;
            request = this.mRequest;
        }
        getPhotoLoader().loadThumbnail(view.getPhotoView(photoId), photoId, false, request);
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader, directoryId);
        configHwSearchProjection(loader);
        configHwSearchSortOrder(loader);
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId) {
        loader.setUri(getHwSearchBaseUri(SearchContract$DataSearch.EMAIL_CONTENT_FILTER_URI, "search_type", "search_email"));
    }

    protected void configHwSearchProjection(CursorLoader loader) {
        String[] projection;
        if (getContactNameDisplayOrder() == 1) {
            projection = SearchContract$EmailQuery.PROJECTION_PRIMARY;
        } else {
            projection = SearchContract$EmailQuery.PROJECTION_ALTERNATIVE;
        }
        loader.setProjection(projection);
    }

    protected void configHwSearchSortOrder(CursorLoader loader) {
        String sortOrder;
        if (getSortOrder() != 1) {
            sortOrder = "sort_key_alt";
        } else if (TextUtils.isEmpty(getQueryString()) || !Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "pinyin_name";
        }
        loader.setSortOrder(sortOrder);
    }
}
