package com.android.contacts.hap.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.android.contacts.hap.list.ContactDataMultiSelectFragment;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.google.android.gms.R;
import java.util.ArrayList;

public class ContactsMultiSelectMessageActivitySimplified extends ContactMultiSelectionActivity {
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_action_done:
                setResultForMessaging();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setResultForMessaging() {
        Intent intent = new Intent();
        intent.putExtra("SelItemData_KeyValue", new ArrayList(this.mSelectedDataUris));
        setResult(-1, intent);
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(16777216, 16777216);
        super.onCreate(savedInstanceState);
        setTheme(R.style.MultiSelectTheme);
    }

    protected boolean configureListFragment(Bundle args) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        this.mMultiSelectFragment = (ContactEntryListFragment) fragmentManager.findFragmentByTag("multiselect-tag-simplified");
        if (this.mMultiSelectFragment == null) {
            this.mMultiSelectFragment = getFragmentToLoad();
        }
        this.mMultiSelectFragment.setContactsRequest(this.mRequest);
        transaction.replace(R.id.list_container, this.mMultiSelectFragment, "multiselect-tag-simplified");
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        return true;
    }

    protected int getViewToUse() {
        return R.layout.simple_frame_layout;
    }

    public ContactEntryListFragment<ContactEntryListAdapter> getFragmentToLoad() {
        return new ContactDataMultiSelectFragment();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
}
