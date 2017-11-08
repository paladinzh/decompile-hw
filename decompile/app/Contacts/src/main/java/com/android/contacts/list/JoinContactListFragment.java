package com.android.contacts.list;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.list.JoinContactLoader.JoinContactLoaderResult;
import com.google.android.gms.R;

public class JoinContactListFragment extends ContactEntryListFragment<JoinContactListAdapter> {
    private ImageView mEmptyImageView;
    private OnContactPickerActionListener mListener;
    private final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case -2:
                    return new CursorLoader(JoinContactListFragment.this.getActivity(), ContentUris.withAppendedId(Contacts.CONTENT_URI, JoinContactListFragment.this.mTargetContactId), new String[]{"display_name"}, null, null, null);
                case 1:
                    JoinContactLoader loader = new JoinContactLoader(JoinContactListFragment.this.getActivity());
                    JoinContactListAdapter adapter = (JoinContactListAdapter) JoinContactListFragment.this.getAdapter();
                    if (adapter != null) {
                        adapter.configureLoader(loader, 0);
                    }
                    return loader;
                default:
                    throw new IllegalArgumentException("No loader for ID=" + id);
            }
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!(data == null || loader == null)) {
                switch (loader.getId()) {
                    case -2:
                        if (data.moveToFirst()) {
                            JoinContactListFragment.this.showTargetContactName(data.getString(0));
                            break;
                        }
                        break;
                    case 1:
                        JoinContactListFragment.this.onContactListLoaded(((JoinContactLoaderResult) data).suggestionCursor, data);
                        break;
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private long mTargetContactId;

    public JoinContactListFragment() {
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(false);
        setQuickContactEnabled(false);
    }

    public void setOnContactPickerActionListener(OnContactPickerActionListener listener) {
        this.mListener = listener;
    }

    protected void startLoading() {
        configureAdapter();
        Bundle bundle = new Bundle();
        getLoaderManager().initLoader(-2, bundle, this.mLoaderCallbacks);
        getLoaderManager().restartLoader(1, bundle, this.mLoaderCallbacks);
    }

    private void onContactListLoaded(Cursor suggestionsCursor, Cursor allContactsCursor) {
        onLoadCompleted(1, allContactsCursor, suggestionsCursor);
        ((JoinContactListAdapter) getAdapter()).setSuggestionsCursor(suggestionsCursor);
        setVisibleScrollbarEnabled(true);
        onPartitionLoaded(1, allContactsCursor);
    }

    private void showTargetContactName(String displayName) {
        Activity activity = getActivity();
        if (displayName == null) {
            displayName = activity.getString(R.string.missing_name);
        }
        ((TextView) activity.findViewById(R.id.join_contact_blurb)).setText(activity.getString(R.string.blurbJoinContactDataWith, new Object[]{displayName}));
    }

    public void setTargetContactId(long targetContactId) {
        this.mTargetContactId = targetContactId;
    }

    public JoinContactListAdapter createListAdapter() {
        return new JoinContactListAdapter(getActivity());
    }

    protected void configureAdapter() {
        super.configureAdapter();
        ((JoinContactListAdapter) getAdapter()).setTargetContactId(this.mTargetContactId);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.join_contact_picker_list_content, null);
        this.mEmptyTextView = (TextView) view.findViewById(R.id.list_empty);
        this.mEmptyImageView = (ImageView) view.findViewById(R.id.empty_contacts_icon);
        return view;
    }

    protected void onItemClick(int position, long id) {
        Uri contactUri = ((JoinContactListAdapter) getAdapter()).getContactUri(position);
        if (contactUri != null) {
            this.mListener.onPickContactAction(contactUri);
        }
    }

    public void onPickerResult(Intent data) {
        Uri contactUri = data.getData();
        if (contactUri != null) {
            this.mListener.onPickContactAction(contactUri);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("targetContactId", this.mTargetContactId);
    }

    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);
        if (savedState != null) {
            this.mTargetContactId = savedState.getLong("targetContactId");
        }
    }

    protected void onLoadCompleted(int partitionIndex, Cursor data, Cursor suggestionCursor) {
        Activity activity = getActivity();
        if (data.getCount() == 0 && suggestionCursor.getCount() == 0) {
            activity.findViewById(R.id.join_contact_blurb).setVisibility(8);
            getListView().setVisibility(8);
            this.mEmptyTextView.setVisibility(0);
            this.mEmptyImageView.setVisibility(0);
            this.mEmptyTextView.setText(R.string.noContactsToJoin);
        } else {
            this.mEmptyTextView.setVisibility(8);
            this.mEmptyImageView.setVisibility(8);
            getListView().setVisibility(0);
            activity.findViewById(R.id.join_contact_blurb).setVisibility(0);
        }
        handleEmptyList(data.getCount());
    }

    public void setQueryString(String queryString, boolean delaySelection) {
        super.setQueryString(queryString, delaySelection);
        setSearchMode(!TextUtils.isEmpty(queryString));
    }

    public void handleEmptyList(int aCount) {
        int i;
        TextView emptyTextView = getEmptyTextView();
        if (isSearchMode()) {
            i = R.string.contact_list_FoundAllContactsZero;
        } else {
            i = R.string.noContactsToJoin;
        }
        emptyTextView.setText(i);
        super.handleEmptyList(aCount);
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        super.onPartitionLoaded(partitionIndex, data);
        if (isSearchMode() && data != null) {
            handleEmptyList(data.getCount());
        }
    }
}
