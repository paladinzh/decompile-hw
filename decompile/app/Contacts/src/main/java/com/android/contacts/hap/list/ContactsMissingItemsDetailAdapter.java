package com.android.contacts.hap.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.View;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.util.PhoneCapabilityTester;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class ContactsMissingItemsDetailAdapter extends ContactListAdapter {
    private final int mMissingItemsIndex;

    public ContactsMissingItemsDetailAdapter(Context context, int index) {
        super(context);
        this.mMissingItemsIndex = index;
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        String sortOrder;
        ContactListFilter filter = getFilter();
        configureUri(loader, directoryId, filter);
        configureProjection(loader, directoryId, filter);
        configureSelection(loader, directoryId, filter);
        if (getSortOrder() == 1) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "sort_key_alt";
        }
        loader.setSortOrder(sortOrder);
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        view.setHighlightedPrefix(null);
        bindSectionHeaderAndDivider(view, position, cursor, position == cursor.getCount() + -1);
        view.setAccountFilterText(null);
        bindPhoto(view, partition, cursor);
        bindName(view, cursor);
        bindSimIcon(view, cursor);
        hideCheckBox(view);
        if (view.isSimAccount()) {
            view.setAccountIcons(null);
        } else {
            bindAccountInfo(view, cursor);
        }
        bindPresenceAndStatusMessage(view, cursor);
        if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
            bindAccountInfo(view, cursor);
        }
        view.setSnippet(null);
        view.setCompany(null);
    }

    private void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;
        if (directoryId == 0) {
            uri = ContactListAdapter.buildSectionIndexerUri(uri);
        }
        if (!(!PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext) || filter == null || filter.filterType == -3)) {
            uri = uri.buildUpon().appendQueryParameter("directory", String.valueOf(0)).build();
        }
        loader.setUri(uri);
    }

    private void configureProjection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        loader.setProjection(getProjection());
    }

    private void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            switch (this.mMissingItemsIndex) {
                case 0:
                    selection.append("has_name").append("=0");
                    break;
                case 1:
                    selection.append("has_phone_number").append("=0");
                    break;
                case 2:
                    selection.append("has_phone_number").append("=0");
                    selection.append(" AND ").append("has_email").append("=0");
                    break;
            }
            if (!CommonUtilMethods.isPrivacyModeEnabled(this.mContext)) {
                if (!TextUtils.isEmpty(selection.toString())) {
                    selection.append(" AND ");
                }
                selection.append("is_private = 0");
            }
            loader.setSelection(selection.toString());
            loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
        }
    }
}
