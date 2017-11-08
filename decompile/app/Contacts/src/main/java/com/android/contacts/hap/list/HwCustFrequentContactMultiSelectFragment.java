package com.android.contacts.hap.list;

import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.widget.ListView;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.list.ContactListItemView;
import java.util.ArrayList;
import java.util.Set;

public class HwCustFrequentContactMultiSelectFragment {
    protected static final String TAG = "HwCustFrequentContactMultiSelectFragment";
    Context mContext;

    public HwCustFrequentContactMultiSelectFragment(Context context) {
        this.mContext = context;
    }

    public boolean getEnableEmailContactInMms() {
        return false;
    }

    public int getNoPhoneNumbersOrEmailsTextId() {
        return 0;
    }

    public boolean ifContainsSelectedUri(Set<Uri> set, Uri selectedUri) {
        return false;
    }

    public int setFilterTypeForCustomizations(int switchKey) {
        return 0;
    }

    public boolean isContainNumber(ArrayList<String> arrayList, Uri uri, ContactMultiSelectionActivity activity) {
        return false;
    }

    public void setCheckBoxStateAndPosition(ContactListItemView v, ListView listView, int position) {
    }

    public void updateSelectAllMenuState(MenuItem mSelectAllItem, int mSelectedItemCount) {
    }

    public int setSelectedItemCount(int mSelectedItemCount, int position, boolean isChecked, ListView mListView) {
        return 0;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean isIgnoreOnItemClick(Context context, Uri dataUri) {
        return false;
    }

    public void addExsitedDataFrequentUris(Context context, FrequentContactSelectAdapter adapter) {
    }
}
