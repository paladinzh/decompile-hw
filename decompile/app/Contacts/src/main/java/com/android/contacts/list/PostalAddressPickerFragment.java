package com.android.contacts.list;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class PostalAddressPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    private OnPostalAddressPickerActionListener mListener;

    public PostalAddressPickerFragment() {
        setQuickContactEnabled(false);
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
    }

    public void setOnPostalAddressPickerActionListener(OnPostalAddressPickerActionListener listener) {
        this.mListener = listener;
    }

    protected void onItemClick(int position, long id) {
        if (isLegacyCompatibilityMode()) {
            pickPostalAddress(((LegacyPostalAddressListAdapter) getAdapter()).getContactMethodUri(position));
        } else {
            pickPostalAddress(((PostalAddressListAdapter) getAdapter()).getDataUri(position));
        }
    }

    protected ContactEntryListAdapter createListAdapter() {
        if (isLegacyCompatibilityMode()) {
            LegacyPostalAddressListAdapter adapter = new LegacyPostalAddressListAdapter(getActivity());
            adapter.setSectionHeaderDisplayEnabled(false);
            adapter.setDisplayPhotos(false);
            return adapter;
        }
        PostalAddressListAdapter adapter2 = new PostalAddressListAdapter(getActivity());
        adapter2.setSectionHeaderDisplayEnabled(true);
        adapter2.setDisplayPhotos(true);
        return adapter2;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        setVisibleScrollbarEnabled(!isLegacyCompatibilityMode());
    }

    public void onResume() {
        super.onResume();
        ContactEntryListAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.upateSimpleDisplayMode();
        }
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contact_list_content, null);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        lView.findViewById(R.id.contactListsearchlayout).setVisibility(8);
        return lView;
    }

    private void pickPostalAddress(Uri uri) {
        this.mListener.onPickPostalAddressAction(uri);
    }

    public void handleEmptyList(int aCount) {
        getEmptyTextView().setText(isSearchMode() ? R.string.contact_list_FoundAllContactsZero : R.string.noContacts);
        super.handleEmptyList(aCount);
    }
}
