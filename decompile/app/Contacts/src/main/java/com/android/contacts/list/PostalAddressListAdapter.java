package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.ContactDisplayUtils;

public class PostalAddressListAdapter extends ContactEntryListAdapter {
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private final CharSequence mUnknownNameText;

    protected static class PostalQuery {
        private static final String[] PROJECTION_ALTERNATIVE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name_alt", "company"};
        private static final String[] PROJECTION_ALTERNATIVE_PRIVATE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name_alt", "is_private", "company"};
        private static final String[] PROJECTION_PRIMARY = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name", "company"};
        private static final String[] PROJECTION_PRIMARY_PRIVATE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "photo_id", "display_name", "is_private", "company"};

        protected PostalQuery() {
        }
    }

    public PostalAddressListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        Builder builder = StructuredPostal.CONTENT_URI.buildUpon().appendQueryParameter("remove_duplicate_entries", "true");
        if (isSectionHeaderDisplayEnabled()) {
            builder.appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true");
        }
        loader.setUri(builder.build());
        if (getContactNameDisplayOrder() == 1) {
            loader.setProjection(EmuiFeatureManager.isPrivacyFeatureEnabled() ? PostalQuery.PROJECTION_PRIMARY_PRIVATE : PostalQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(EmuiFeatureManager.isPrivacyFeatureEnabled() ? PostalQuery.PROJECTION_ALTERNATIVE_PRIVATE : PostalQuery.PROJECTION_ALTERNATIVE);
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
        bindPostalAddress(view, cursor);
        if (isSearchMode()) {
            view.showCompany(cursor, cursor.getColumnIndex("company"), -1);
        } else {
            view.setCompany(null);
        }
    }

    protected void bindPostalAddress(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(2)) {
            label = StructuredPostal.getTypeLabel(getContext().getResources(), cursor.getInt(2), cursor.getString(3));
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
}
