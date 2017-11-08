package com.android.contacts.list;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.google.android.gms.R;

public class ContactPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter> implements OnShortcutIntentCreatedListener {
    private EditText mContactsSearchView;
    private boolean mCreateContactEnabled;
    private boolean mEditMode;
    private boolean mExcludePrivateContactsWithToast = false;
    private boolean mExcludeReadOnly = false;
    private boolean mExcludeSim1AndReadOnly = false;
    private boolean mExcludeSim2AndReadOnly = false;
    private boolean mExcludeSimAndReadOnly = false;
    private boolean mIgnoreShowSimContactsPref = false;
    private OnContactPickerActionListener mListener;
    private boolean mShortcutRequested;

    public ContactPickerFragment() {
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
        setQuickContactEnabled(false);
        this.mIgnoreShowSimContactsPref = false;
    }

    public void setIgnoreShowSimContactsPref(boolean ignore) {
        this.mIgnoreShowSimContactsPref = ignore;
    }

    public void setOnContactPickerActionListener(OnContactPickerActionListener listener) {
        this.mListener = listener;
    }

    public boolean isCreateContactEnabled() {
        return this.mCreateContactEnabled;
    }

    public void setEditMode(boolean flag) {
        this.mEditMode = flag;
    }

    public void setShortcutRequested(boolean flag) {
        this.mShortcutRequested = flag;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("editMode", this.mEditMode);
        outState.putBoolean("createContactEnabled", this.mCreateContactEnabled);
        outState.putBoolean("shortcutRequested", this.mShortcutRequested);
        outState.putBoolean("excludeSimAndReadOnly", this.mExcludeSimAndReadOnly);
        outState.putBoolean("excludeSim1AndReadOnly", this.mExcludeSim1AndReadOnly);
        outState.putBoolean("excludeSim2AndReadOnly", this.mExcludeSim2AndReadOnly);
        outState.putBoolean("excludeReadOnly", this.mExcludeReadOnly);
        outState.putBoolean("mIgnoreShowSimContactsPref", this.mIgnoreShowSimContactsPref);
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            outState.putBoolean("excludeprivacycontactswithtoast", this.mExcludePrivateContactsWithToast);
        }
    }

    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);
        if (savedState != null) {
            this.mEditMode = savedState.getBoolean("editMode");
            this.mCreateContactEnabled = savedState.getBoolean("createContactEnabled");
            this.mShortcutRequested = savedState.getBoolean("shortcutRequested");
            this.mExcludeSimAndReadOnly = savedState.getBoolean("excludeSimAndReadOnly");
            this.mExcludeSim1AndReadOnly = savedState.getBoolean("excludeSim1AndReadOnly");
            this.mExcludeSim2AndReadOnly = savedState.getBoolean("excludeSim2AndReadOnly");
            this.mExcludeReadOnly = savedState.getBoolean("excludeReadOnly");
            this.mIgnoreShowSimContactsPref = savedState.getBoolean("mIgnoreShowSimContactsPref");
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                this.mExcludePrivateContactsWithToast = savedState.getBoolean("excludeprivacycontactswithtoast");
            }
        }
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        if (this.mCreateContactEnabled) {
            getListView().addHeaderView(inflater.inflate(R.layout.create_new_contact, null, false));
        }
        this.mSearchLayout = (LinearLayout) getView().findViewById(R.id.contactListsearchlayout);
        this.mInnerSearchLayout = getView().findViewById(R.id.inner_contactListsearchlayout);
        this.mSearchLayout.setVisibility(0);
        this.mContactsSearchView = (EditText) getView().findViewById(R.id.search_view);
        ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
        if (getActivity() instanceof ContactSelectionActivity) {
            ((ContactSelectionActivity) getActivity()).setUpFragment();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0 && this.mCreateContactEnabled) {
            this.mListener.onCreateNewContactAction();
        } else {
            super.onItemClick(parent, view, position, id);
        }
    }

    protected void onItemClick(int position, long id) {
        Uri uri;
        if (isLegacyCompatibilityMode()) {
            uri = ((LegacyContactListAdapter) getAdapter()).getPersonUri(position);
        } else {
            uri = ((ContactListAdapter) getAdapter()).getContactUri(position);
        }
        if (this.mEditMode) {
            editContact(uri);
        } else if (this.mShortcutRequested) {
            new ShortcutIntentBuilder(getActivity(), this).createContactShortcutIntent(uri);
        } else {
            pickContact(uri);
        }
    }

    public void editContact(Uri contactUri) {
        this.mListener.onEditContactAction(contactUri);
    }

    public void pickContact(Uri uri) {
        this.mListener.onPickContactAction(uri);
    }

    protected ContactEntryListAdapter createListAdapter() {
        if (isLegacyCompatibilityMode()) {
            LegacyContactListAdapter adapter = new LegacyContactListAdapter(getActivity());
            adapter.setSectionHeaderDisplayEnabled(false);
            adapter.setDisplayPhotos(false);
            return adapter;
        }
        DefaultContactListAdapter adapter2 = new DefaultContactListAdapter(getActivity());
        adapter2.setShowCompany(false);
        if (this.mExcludeSimAndReadOnly || (this.mExcludeSim1AndReadOnly && this.mExcludeSim2AndReadOnly)) {
            adapter2.setFilter(ContactListFilter.createFilterWithType(-12));
        } else if (this.mExcludeSim1AndReadOnly) {
            adapter2.setFilter(ContactListFilter.createFilterWithType(-15));
        } else if (this.mExcludeSim2AndReadOnly) {
            adapter2.setFilter(ContactListFilter.createFilterWithType(-16));
        } else if (this.mExcludeReadOnly) {
            adapter2.setFilter(ContactListFilter.createFilterWithType(-13));
        } else {
            adapter2.setFilter(ContactListFilter.createFilterWithType(-2));
        }
        adapter2.setSectionHeaderDisplayEnabled(true);
        adapter2.setDisplayPhotos(true);
        adapter2.setQuickContactEnabled(false);
        adapter2.setExcludePrivateContacts(getIfExcludePrivateContacts());
        adapter2.setIgnoreShowSimContactsPref(this.mIgnoreShowSimContactsPref);
        return adapter2;
    }

    protected void configureAdapter() {
        boolean z;
        super.configureAdapter();
        ContactEntryListAdapter adapter = getAdapter();
        if (isCreateContactEnabled()) {
            z = false;
        } else {
            z = true;
        }
        adapter.setEmptyListEnabled(z);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
        this.mListener.onShortcutIntentCreated(shortcutIntent);
    }

    public void onPickerResult(Intent data) {
        this.mListener.onPickContactAction(data.getData());
    }

    public void setExcludeSimAndReadOnly(boolean aExcludeSimAndReadOnly) {
        this.mExcludeSimAndReadOnly = aExcludeSimAndReadOnly;
    }

    public void setExcludeSim1AndReadOnly(boolean aExcludeSimAndReadOnly) {
        this.mExcludeSim1AndReadOnly = aExcludeSimAndReadOnly;
    }

    public void setExcludeSim2AndReadOnly(boolean aExcludeSimAndReadOnly) {
        this.mExcludeSim2AndReadOnly = aExcludeSimAndReadOnly;
    }

    public void setExcludeReadOnly(boolean aExcludeReadOnly) {
        this.mExcludeReadOnly = aExcludeReadOnly;
    }

    public void setExcludePrivateContactsWithToast(boolean aExcludePrivateContactsWithToast) {
        this.mExcludePrivateContactsWithToast = aExcludePrivateContactsWithToast;
    }

    public void handleEmptyList(int aCount) {
        int i;
        TextView emptyTextView = getEmptyTextView();
        if (isSearchMode()) {
            i = R.string.contact_list_FoundAllContactsZero;
        } else {
            i = R.string.noContacts;
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
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ContactEntryListAdapter adapter = ContactPickerFragment.this.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }, 50);
    }
}
