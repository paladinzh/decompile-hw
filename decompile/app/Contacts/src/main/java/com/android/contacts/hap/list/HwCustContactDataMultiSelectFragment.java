package com.android.contacts.hap.list;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import java.util.ArrayList;

public class HwCustContactDataMultiSelectFragment {
    protected static final String TAG = "HwCustContactDataMultiSelectFragment";
    Context mContext;

    public HwCustContactDataMultiSelectFragment(Context context) {
        this.mContext = context;
    }

    public boolean handleRemoveGrpMemOperationCust(ContactDataMultiSelectFragment aFragment, FragmentManager aFragmentManager) {
        return false;
    }

    public boolean getEnableEmailContactInMms() {
        return false;
    }

    public int getPickEmailNumber(boolean isSearchMode) {
        return 0;
    }

    public boolean getRcsEnableStatus() {
        return false;
    }

    public boolean ifGroupMemberSizeValid(ContactMultiSelectionActivity activity) {
        return true;
    }

    public boolean isIgnoreOnItemClick(ArrayList<String> arrayList, Uri selectDataUri) {
        return false;
    }

    public void handleCustomizationsOnSaveInstanceState(Bundle outState, Activity activity) {
    }

    public void handleCustomizationsOnCreate(Bundle savedState, Activity activity) {
    }

    public void addSelectedNameKey(String key) {
    }

    public void addSelectedName(String selectedData, String selectedName, Activity activity) {
    }

    public void rmSelectedName(String selectedData, Activity activity) {
    }

    public int getFilterForCustomizationsRequest(int actionCode, int filterType) {
        return filterType;
    }

    public void doOperationForCustomizations(int actionCode, Activity activity, Fragment fragment) {
    }

    public void updateSelectAllMenuState(MenuItem mSelectAllItem, ContactMultiSelectionActivity mParentActivity) {
    }

    public int resetSelectedDataInSearch(ListView mListView, DataListAdapter mAdapter, ContactMultiSelectionActivity localActivityRef, int mSelectedCountInSearch) {
        return 0;
    }

    public void resetSelectedDataInNotSearch(ListView mListView, DataListAdapter mAdapter, ContactMultiSelectionActivity localActivityRef) {
    }

    public boolean isFromActivityForGroupChat(ContactMultiSelectionActivity activity) {
        return false;
    }
}
