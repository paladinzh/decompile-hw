package com.android.contacts.hap.list;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.list.RawContactsPhotoFetcher.RawContactsPhotoFetchListener;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RawContactMultiselectionAdapter extends ContactListAdapter implements RawContactsPhotoFetchListener {
    private Cursor mCursor;
    private HashMap<Long, Long> mRawContactPhotoMap;
    private DefaultImageRequest mRequest = new DefaultImageRequest();

    public RawContactMultiselectionAdapter(Context aContext) {
        super(aContext);
    }

    public void configureLoader(CursorLoader aLoader, long aDirectoryId) {
        ContactListFilter filter = getFilter();
        configureUri(aLoader, aDirectoryId, filter);
        configureProjection(aLoader, aDirectoryId, filter);
        configureSelection(aLoader, aDirectoryId, filter);
        aLoader.setSortOrder("sort_key");
    }

    protected void bindView(View aItemView, int aPartition, Cursor aCursor, int aPosition) {
        super.bindView(aItemView, aPartition, aCursor, aPosition);
        ContactListItemView view = (ContactListItemView) aItemView;
        bindSectionHeaderAndDivider(view, aPosition, aCursor);
        bindName(view, aCursor);
        bindPhoto(view, aPartition, aCursor);
        bindCheckBox(view);
        view.setSnippet(null);
        view.setCompany(null);
    }

    private void configureUri(CursorLoader aLoader, long aDirectoryId, ContactListFilter aFilter) {
        Uri uri = RawContacts.CONTENT_URI;
        if (aDirectoryId == 0 && isSectionHeaderDisplayEnabled()) {
            uri = ContactListAdapter.buildSectionIndexerUri(uri);
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext)) {
            uri = uri.buildUpon().appendQueryParameter("directory", String.valueOf(0)).build();
        }
        aLoader.setUri(uri);
    }

    private void configureProjection(CursorLoader aLoader, long aDirectoryId, ContactListFilter aFilter) {
        aLoader.setProjection(getRawContactsProjection());
    }

    private void configureSelection(CursorLoader aLoader, long aDirectoryId, ContactListFilter aFilter) {
        if (aFilter != null && aDirectoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            selection.append("deleted=0 AND ");
            selection.append("account_type=? AND account_name=?");
            selectionArgs.add(aFilter.accountType);
            selectionArgs.add(aFilter.accountName);
            if (aFilter.dataSet != null) {
                selection.append(" AND data_set=?");
                selectionArgs.add(aFilter.dataSet);
            } else {
                selection.append(" AND data_set IS NULL");
            }
            aLoader.setSelection(selection.toString());
            aLoader.setSelectionArgs((String[]) selectionArgs.toArray(new String[0]));
        }
    }

    protected void bindPhoto(ContactListItemView view, int partitionIndex, Cursor cursor) {
        if (!isPhotoSupported(partitionIndex) || ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId;
        int i;
        ContactListFilter filter = getFilter();
        if ("com.android.huawei.sim".equals(filter.accountType)) {
            photoId = -2;
        } else if ("com.android.huawei.secondsim".equals(filter.accountType)) {
            photoId = -3;
        } else {
            photoId = getPhotoIdForRawContactId(cursor.getLong(6));
        }
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        DefaultImageRequest request = null;
        if (photoId <= 0) {
            this.mRequest.displayName = cursor.getString(1);
            this.mRequest.identifier = cursor.getString(0);
            this.mRequest.isCircular = true;
            request = this.mRequest;
        }
        ContactPhotoManager photoLoader = getPhotoLoader();
        ImageView photoView = view.getPhotoView(photoId);
        long j = cursor.getLong(0);
        if (lIsPrivateContact) {
            i = 4;
        } else {
            i = 0;
        }
        photoLoader.loadThumbnail(photoView, photoId, false, request, j, i);
    }

    protected void bindName(ContactListItemView aView, Cursor aCursor) {
        aView.showDisplayName(aCursor, 1, getContactNameDisplayOrder());
    }

    protected void bindCheckBox(ContactListItemView view) {
        view.showCheckBox();
    }

    public void changeCursor(int aPartitionIndex, Cursor aCursor) {
        super.changeCursor(aPartitionIndex, aCursor);
        this.mCursor = aCursor;
    }

    public Uri getSelectedContactUri(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        return Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(this.mCursor.getLong(6)));
    }

    public Uri getSelectedContactLookupUri(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        return Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(this.mCursor.getLong(0)));
    }

    public void onPhotoFetchComplete(HashMap<Long, Long> aPhotoIdForRawContactIdMap) {
        this.mRawContactPhotoMap = aPhotoIdForRawContactIdMap;
    }

    private long getPhotoIdForRawContactId(long aRawContactId) {
        if (this.mRawContactPhotoMap == null) {
            return 0;
        }
        Long photoId = (Long) this.mRawContactPhotoMap.get(Long.valueOf(aRawContactId));
        if (photoId == null) {
            return 0;
        }
        return photoId.longValue();
    }

    public void startFetchingPhotoIdsInBackground(ContentResolver aResolver) {
        ContactListFilter filter = getFilter();
        if (!TextUtils.isEmpty(filter.accountName) && !TextUtils.isEmpty(filter.accountType)) {
            RawContactsPhotoFetcher photoFetcher = new RawContactsPhotoFetcher(new AccountWithDataSet(filter.accountName, filter.accountType, filter.dataSet), aResolver);
            photoFetcher.setRawContactsPhotoFetchListener(this);
            photoFetcher.start();
        }
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView aView, int aPosition, Cursor aCursor) {
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(aPosition);
            if (aPosition == 0 && aCursor.getInt(5) == 1) {
                aView.setCountView(getContactsCount());
            } else {
                aView.setCountView(null);
            }
            aView.setSectionHeader(placement.sectionHeader);
            return;
        }
        aView.setSectionHeader(null);
        aView.setCountView(null);
    }

    protected int getProfileColumnIndex() {
        return 5;
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        return (ContactListItemView) super.newView(context, partition, cursor, position, parent);
    }
}
