package com.android.contacts.list;

import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.list.ContactListItemView.PhotoPosition;
import com.android.contacts.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class PhoneNumberPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter> implements OnShortcutIntentCreatedListener {
    private static final String TAG = PhoneNumberPickerFragment.class.getSimpleName();
    private EditText mContactsSearchView;
    private ContactListFilter mFilter;
    private OnClickListener mFilterHeaderClickListener = new FilterHeaderClickListener();
    private final Handler mHandler;
    private OnPhoneNumberPickerActionListener mListener;
    private boolean mLoaderStarted;
    private View mPaddingView;
    private PhotoPosition mPhotoPosition = ContactListItemView.DEFAULT_PHOTO_POSITION;
    private LinearLayout mSearchLayout;
    private String mShortcutAction;
    private boolean mUseCallableUri;

    private class FilterHeaderClickListener implements OnClickListener {
        private FilterHeaderClickListener() {
        }

        public void onClick(View view) {
            AccountFilterUtil.startAccountFilterActivityForResult(PhoneNumberPickerFragment.this, 1, PhoneNumberPickerFragment.this.mFilter);
        }
    }

    public PhoneNumberPickerFragment() {
        setQuickContactEnabled(false);
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setHasOptionsMenu(true);
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void setOnPhoneNumberPickerActionListener(OnPhoneNumberPickerActionListener listener) {
        this.mListener = listener;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        View paddingView = inflater.inflate(R.layout.contact_detail_list_padding, null, false);
        this.mPaddingView = paddingView.findViewById(R.id.contact_detail_list_padding);
        getListView().addHeaderView(paddingView);
        setVisibleScrollbarEnabled(!isLegacyCompatibilityMode());
        this.mSearchLayout = (LinearLayout) getView().findViewById(R.id.contactListsearchlayout);
        this.mInnerSearchLayout = getView().findViewById(R.id.inner_contactListsearchlayout);
        this.mSearchLayout.setVisibility(0);
        this.mContactsSearchView = (EditText) getView().findViewById(R.id.search_view);
        ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
        if (getActivity() instanceof ContactSelectionActivity) {
            ((ContactSelectionActivity) getActivity()).setUpFragment();
        }
    }

    public void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
    }

    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);
        if (savedState != null) {
            this.mFilter = (ContactListFilter) savedState.getParcelable("filter");
            this.mShortcutAction = savedState.getString("shortcutAction");
            if (PhotoPosition.RIGHT.toString().equals(savedState.getString("photoPosition"))) {
                this.mPhotoPosition = PhotoPosition.RIGHT;
            } else {
                this.mPhotoPosition = PhotoPosition.LEFT;
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("filter", this.mFilter);
        outState.putString("shortcutAction", this.mShortcutAction);
        outState.putString("photoPosition", this.mPhotoPosition.toString());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        if (this.mListener != null) {
            this.mListener.onHomeInActionBarSelected();
        }
        return true;
    }

    public void setShortcutAction(String shortcutAction) {
        this.mShortcutAction = shortcutAction;
    }

    protected void onItemClick(int position, long id) {
        Uri phoneUri;
        if (isLegacyCompatibilityMode()) {
            phoneUri = ((LegacyPhoneNumberListAdapter) getAdapter()).getPhoneUri(position);
        } else {
            phoneUri = ((PhoneNumberListAdapter) getAdapter()).getDataUri(position);
        }
        if (phoneUri != null) {
            pickPhoneNumber(phoneUri);
        } else {
            HwLog.w(TAG, "Item at " + position + " was clicked before adapter is ready. Ignoring");
        }
    }

    protected void startLoading() {
        this.mLoaderStarted = true;
        super.startLoading();
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            super.onLoadFinished((Loader) loader, data);
            if (!(loader == null || loader.getId() == -1)) {
                if (data.getCount() == 0) {
                    if (this.contactsAvailableView != null) {
                        this.contactsAvailableView.setVisibility(0);
                    }
                    getEmptyTextView().setVisibility(0);
                    getListView().setVisibility(8);
                } else {
                    getEmptyTextView().setVisibility(8);
                    getListView().setVisibility(0);
                }
            }
        }
    }

    protected ContactEntryListAdapter createListAdapter() {
        if (isLegacyCompatibilityMode()) {
            LegacyPhoneNumberListAdapter adapter = new LegacyPhoneNumberListAdapter(getActivity());
            adapter.setDisplayPhotos(true);
            return adapter;
        }
        PhoneNumberListAdapter adapter2 = new PhoneNumberListAdapter(getActivity());
        adapter2.setDisplayPhotos(true);
        adapter2.setExcludePrivateContacts(getIfExcludePrivateContacts());
        adapter2.setUseCallableUri(this.mUseCallableUri);
        return adapter2;
    }

    protected void configureAdapter() {
        super.configureAdapter();
        ContactEntryListAdapter adapter = getAdapter();
        if (adapter != null) {
            if (!(isSearchMode() || this.mFilter == null)) {
                adapter.setFilter(this.mFilter);
            }
            if (!isLegacyCompatibilityMode()) {
                ((PhoneNumberListAdapter) adapter).setPhotoPosition(this.mPhotoPosition);
            }
        }
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contact_list_content, null);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    public void pickPhoneNumber(Uri uri) {
        if (this.mShortcutAction == null) {
            this.mListener.onPickPhoneNumberAction(uri);
        } else if (isLegacyCompatibilityMode()) {
            throw new UnsupportedOperationException();
        } else {
            new ShortcutIntentBuilder(getActivity(), this).createPhoneNumberShortcutIntent(uri, this.mShortcutAction);
        }
    }

    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
        this.mListener.onShortcutIntentCreated(shortcutIntent);
    }

    public void onPickerResult(Intent data) {
        this.mListener.onPickPhoneNumberAction(data.getData());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            return;
        }
        if (getActivity() != null) {
            AccountFilterUtil.handleAccountFilterResult(ContactListFilterController.getInstance(getActivity()), resultCode, data);
        } else {
            HwLog.e(TAG, "getActivity() returns null during Fragment#onActivityResult()");
        }
    }

    public void handleEmptyList(int aCount) {
        int i;
        TextView emptyTextView = getEmptyTextView();
        if (isSearchMode()) {
            i = R.string.no_matching_results_with_number;
        } else {
            i = R.string.contact_noNumbers;
        }
        emptyTextView.setText(i);
        super.handleEmptyList(aCount);
    }

    public void onResume() {
        this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
        this.mContactsSearchView.setCursorVisible(false);
        super.onResume();
        ContactEntryListAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.upateSimpleDisplayMode();
        }
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        if (isSearchMode() || data.getCount() != 0) {
            this.mSearchLayout.setVisibility(0);
            refreshListViewDelayed();
        } else {
            this.mSearchLayout.setVisibility(8);
        }
        super.onPartitionLoaded(partitionIndex, data);
    }

    private void refreshListViewDelayed() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ContactEntryListAdapter adapter = PhoneNumberPickerFragment.this.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }, 50);
    }

    protected void checkAlphaScrollerAndUpdateViews() {
        if (this.mSearchLayout != null && this.mSearchLayout.getVisibility() == 0) {
            LayoutParams lParams = this.mSearchLayout.getLayoutParams();
            if (lParams != null && (lParams instanceof LinearLayout.LayoutParams)) {
                int i;
                LinearLayout.LayoutParams lLP = (LinearLayout.LayoutParams) lParams;
                Resources resources = getResources();
                if (isAlphaScrollerVisible()) {
                    i = R.dimen.searchLayout_margin_right_default;
                } else {
                    i = R.dimen.searchLayout_margin_left_right;
                }
                lLP.rightMargin = resources.getDimensionPixelOffset(i);
                this.mSearchLayout.setLayoutParams(lLP);
            }
        }
    }
}
