package com.android.contacts.hap.optimize;

import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DummyContactBrowseListFragment extends DefaultContactBrowseListFragment {
    public boolean mIsChanged;
    public boolean mRestoreSelectedUri;

    public void onAttach(Activity activity) {
        if (HwLog.HWDBG) {
            HwLog.d("DummyContactBrowseListFragment", "DummyContactBrowseListFragment Attach to Activity");
        }
        super.onAttach(activity);
    }

    public void onDetach() {
        if (HwLog.HWDBG) {
            HwLog.d("DummyContactBrowseListFragment", "DummyContactBrowseListFragment Detach from Activity:" + getActivity());
        }
        super.onDetach();
    }

    public void onStart() {
        super.onStart();
    }

    protected void handleData(Loader<Cursor> loader, Cursor data) {
    }

    public void setQueryText(String s) {
        super.setQueryText(s);
    }

    protected void onItemClick(int position, long id) {
    }

    protected ContactListAdapter createListAdapter() {
        return super.createListAdapter();
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.dummy_list_layout, container, false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
    }

    public void onStop() {
        super.onStop();
    }

    public void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public void refreshSearchViewFocus() {
        super.refreshSearchViewFocus();
    }

    public void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
    }

    public void setFilter(ContactListFilter aFilter) {
    }

    public void setFilter(ContactListFilter aFilter, boolean aRestoreSelectedUri, boolean aIsChanged) {
        this.mRestoreSelectedUri = aRestoreSelectedUri;
        this.mIsChanged = aIsChanged;
    }

    protected void showCount(int partitionIndex, Cursor data) {
        super.showCount(partitionIndex, data);
    }

    protected void setProfileHeader() {
        super.setProfileHeader();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onPrepareOptionsMenu(Menu menu) {
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public EditText getContactsSearchView() {
        return null;
    }

    public int getContactsCount() {
        return 0;
    }

    public boolean isReplacable() {
        return true;
    }
}
