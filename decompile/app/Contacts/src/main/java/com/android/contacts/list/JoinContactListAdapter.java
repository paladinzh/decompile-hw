package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.R;

public class JoinContactListAdapter extends ContactListAdapter {
    private long mTargetContactId;

    public JoinContactListAdapter(Context context) {
        super(context);
        setPinnedPartitionHeadersEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setIndexedPartition(1);
        setDirectorySearchMode(0);
    }

    protected void addPartitions() {
        addPartition(false, true);
        addPartition(createDefaultDirectoryPartition());
    }

    public void setTargetContactId(long targetContactId) {
        this.mTargetContactId = targetContactId;
    }

    public void configureLoader(CursorLoader cursorLoader, long directoryId) {
        Uri allContactsUri;
        JoinContactLoader loader = (JoinContactLoader) cursorLoader;
        Builder builder = Contacts.CONTENT_URI.buildUpon();
        builder.appendEncodedPath(String.valueOf(this.mTargetContactId));
        builder.appendEncodedPath("suggestions");
        String filter = getQueryString();
        if (!TextUtils.isEmpty(filter)) {
            builder.appendEncodedPath(Uri.encode(filter));
        }
        builder.appendQueryParameter("limit", String.valueOf(4));
        loader.setSuggestionUri(builder.build());
        loader.setProjection(getProjection());
        if (TextUtils.isEmpty(filter)) {
            allContactsUri = ContactListAdapter.buildSectionIndexerUri(Contacts.CONTENT_URI).buildUpon().appendQueryParameter("directory", String.valueOf(0)).build();
        } else {
            allContactsUri = ContactListAdapter.buildSectionIndexerUri(Contacts.CONTENT_FILTER_URI).buildUpon().appendEncodedPath(Uri.encode(filter)).appendQueryParameter("directory", String.valueOf(0)).build();
        }
        loader.setUri(allContactsUri);
        StringBuilder selection = new StringBuilder();
        selection.append("_id IN (").append("SELECT contact_id FROM view_raw_contacts WHERE account_type !=? AND account_type !=?)").append(" AND _id!=?");
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(new String[]{"com.android.huawei.secondsim", "com.android.huawei.sim", String.valueOf(this.mTargetContactId)});
        if (getSortOrder() == 1) {
            loader.setSortOrder("sort_key");
        } else {
            loader.setSortOrder("sort_key_alt");
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public void setSuggestionsCursor(Cursor cursor) {
        changeCursor(0, cursor);
    }

    public void configureDefaultPartition(boolean showIfEmpty, boolean hasHeader) {
        super.configureDefaultPartition(false, true);
    }

    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    public int getItemViewType(int partition, int position) {
        return super.getItemViewType(partition, position);
    }

    protected View newHeaderView(Context context, int partition, Cursor cursor, ViewGroup parent) {
        View view;
        switch (partition) {
            case 0:
                view = inflate(R.layout.join_contact_picker_section_header, parent);
                ((TextView) view.findViewById(R.id.text)).setText(R.string.separatorJoinAggregateSuggestions);
                return view;
            case 1:
                view = inflate(R.layout.join_contact_picker_section_header, parent);
                ((TextView) view.findViewById(R.id.text)).setText(R.string.separatorJoinAggregateAll);
                return view;
            default:
                return null;
        }
    }

    protected void bindHeaderView(View view, int partitionIndex, Cursor cursor) {
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        switch (partition) {
            case 0:
            case 1:
                return super.newView(context, partition, cursor, position, parent);
            default:
                return null;
        }
    }

    private View inflate(int layoutId, ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        char[] cArr = null;
        ContactListItemView view;
        switch (partition) {
            case 0:
                view = (ContactListItemView) itemView;
                if (isSearchMode()) {
                    view.setSearchMatchType(cursor);
                    view.setHighlightedPrefix(getLowerCaseQueryString());
                }
                view.setSectionHeader(null);
                bindPhoto(view, partition, cursor);
                bindName(view, cursor);
                if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
                    bindAccountInfo(view, cursor);
                    return;
                }
                return;
            case 1:
                view = (ContactListItemView) itemView;
                if (isSearchMode()) {
                    view.setSearchMatchType(cursor);
                }
                if (isSearchMode()) {
                    cArr = getLowerCaseQueryString();
                }
                view.setHighlightedPrefix(cArr);
                bindSectionHeaderAndDivider(view, position, cursor, position == cursor.getCount() + -1);
                bindPhoto(view, partition, cursor);
                bindName(view, cursor);
                if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
                    bindAccountInfo(view, cursor);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public Uri getContactUri(int partitionIndex, Cursor cursor) {
        return Contacts.getLookupUri(cursor.getLong(0), cursor.getString(6));
    }
}
