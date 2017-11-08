package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;

public class ContactsMissingItemsDetailFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    private MenuItem mActionMenu;
    ContactListAdapter mAdapter;
    private Uri mCurSelectionUri;

    public ContactsMissingItemsDetailFragment() {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(false);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
    }

    public ContactsMissingItemsDetailFragment(int missingItemIndex) {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(false);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
        this.mMissingItemIndex = missingItemIndex;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View lView = inflater.inflate(R.layout.contacts_missing_items_detail_content, null);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        setTitle();
        return lView;
    }

    protected ContactEntryListAdapter createListAdapter() {
        Activity activity = getActivity();
        this.mAdapter = new ContactsMissingItemsDetailAdapter(activity, this.mMissingItemIndex);
        this.mAdapter.setFilter(ContactListFilter.restoreDefaultPreferences(SharePreferenceUtil.getDefaultSp_de(activity)));
        this.mAdapter.setSectionHeaderDisplayEnabled(true);
        this.mAdapter.setDisplayPhotos(true);
        this.mAdapter.setSelectedContactUri(this.mCurSelectionUri);
        return this.mAdapter;
    }

    protected void configureListView(ListView aListView) {
        super.configureListView(aListView);
        aListView.setOnFocusChangeListener(null);
        aListView.setOnTouchListener(null);
        CommonUtilMethods.addFootEmptyViewPortrait(aListView, getContext());
    }

    protected void onItemClick(int position, long id) {
        Cursor lCursor = (Cursor) getAdapter().getItem(((getListView().getHeaderViewsCount() + position) + 0) + 0);
        if (lCursor == null) {
            HwLog.e("ContactsMissingItemsDetailFragment", "lCursor is null in onItemClick");
            return;
        }
        long contactId = -1;
        int idIndex = lCursor.getColumnIndex("_id");
        if (idIndex != -1) {
            contactId = lCursor.getLong(idIndex);
        }
        int lookupKeyIndex = lCursor.getColumnIndex("lookup");
        String lookupKey = null;
        if (lookupKeyIndex != -1) {
            lookupKey = lCursor.getString(lookupKeyIndex);
        }
        if (contactId <= 0 || lookupKey == null) {
            HwLog.e("ContactsMissingItemsDetailFragment", "contactId=" + contactId + ", lookupKey=" + lookupKey);
        } else {
            Uri lLookupUri = Contacts.getLookupUri(contactId, lookupKey);
            this.mAdapter.setSelectedContactUri(lLookupUri);
            this.mCurSelectionUri = lLookupUri;
            Intent intent = new Intent("android.intent.action.VIEW", lLookupUri);
            intent.setClass(getActivity(), ContactDetailActivity.class);
            intent.setFlags(67108864);
            startActivity(intent);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Activity activity = getActivity();
        this.mSelectAllItem = menu.findItem(R.id.menu_action_selectall);
        this.mSelectAllItem.setVisible(false);
        this.mActionMenu = menu.findItem(R.id.menu_action_operation);
        this.mActionMenu.setTitle(R.string.menu_deleteContact);
        this.mActionMenu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.ic_trash_normal_light, R.drawable.ic_trash_normal));
        ViewUtil.setMenuItemStateListIcon(activity, this.mActionMenu);
        this.mActionMenu.setEnabled(true);
        if (!EmuiVersion.isSupportEmui()) {
            MenuItem cancelItem = menu.findItem(R.id.menu_action_cancel);
            if (cancelItem != null) {
                cancelItem.setVisible(true);
            }
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (this.mAdapter == null || this.mAdapter.getCount() <= 0) {
            this.mActionMenu.setVisible(false);
        } else {
            this.mActionMenu.setVisible(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                getActivity().finish();
                return true;
            case R.id.menu_action_operation:
                Intent lIntent = new Intent();
                lIntent.setAction("android.intent.action.HAP_DELETE_CONTACTS");
                lIntent.putExtra("missingItemIndex", this.mMissingItemIndex);
                startActivity(lIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleEmptyList(int aCount) {
        if (aCount == 0 && this.mEmptyTextView != null) {
            this.mEmptyTextView.setText(R.string.noContacts);
            this.mEmptyTextView.setVisibility(0);
        }
        super.handleEmptyList(aCount);
    }

    private void setTitle() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar mActionBar = activity.getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
            String title = "";
            switch (this.mMissingItemIndex) {
                case 0:
                    title = activity.getString(R.string.contacts_missing_name);
                    break;
                case 1:
                    title = activity.getString(R.string.contacts_missing_number);
                    break;
                case 2:
                    title = activity.getString(R.string.contacts_missing_number_and_mail);
                    break;
                default:
                    title = activity.getString(R.string.contacts_missing_items);
                    break;
            }
            mActionBar.setTitle(title);
        }
    }
}
