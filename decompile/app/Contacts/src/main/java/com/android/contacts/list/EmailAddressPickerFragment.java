package com.android.contacts.list;

import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class EmailAddressPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    private EditText mContactsSearchView;
    private OnEmailAddressPickerActionListener mListener;
    private LinearLayout mSearchLayout;

    public EmailAddressPickerFragment() {
        setQuickContactEnabled(false);
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
    }

    public void setOnEmailAddressPickerActionListener(OnEmailAddressPickerActionListener listener) {
        this.mListener = listener;
    }

    protected void onItemClick(int position, long id) {
        pickEmailAddress(((EmailAddressListAdapter) getAdapter()).getDataUri(position));
    }

    protected ContactEntryListAdapter createListAdapter() {
        EmailAddressListAdapter adapter = new EmailAddressListAdapter(getActivity());
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        return adapter;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contact_list_content, null);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
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

    private void pickEmailAddress(Uri uri) {
        this.mListener.onPickEmailAddressAction(uri);
    }

    public void handleEmptyList(int aCount) {
        getEmptyTextView().setText(R.string.contact_noEmails);
        super.handleEmptyList(aCount);
    }

    public void onResume() {
        this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
        this.mContactsSearchView.setCursorVisible(false);
        super.onResume();
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        if (isSearchMode() || data.getCount() != 0) {
            this.mSearchLayout.setVisibility(0);
        } else {
            this.mSearchLayout.setVisibility(8);
        }
        super.onPartitionLoaded(partitionIndex, data);
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
