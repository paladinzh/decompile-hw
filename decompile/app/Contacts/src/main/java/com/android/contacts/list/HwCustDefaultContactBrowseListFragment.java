package com.android.contacts.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.ArrayList;

public class HwCustDefaultContactBrowseListFragment {
    public boolean onOptionsItemSelectedForCust(MenuItem item, Context context) {
        return false;
    }

    public boolean isRemoveShareContacts() {
        return false;
    }

    public void makeMenuItemInVisible(Menu menu) {
    }

    public boolean supportReadOnly() {
        return false;
    }

    public boolean isReadOnlyContact(long contactID, Context context) {
        return false;
    }

    public void customizeListHeaderViews(DefaultContactBrowseListFragment fragment, LayoutInflater inflater, ArrayList<View> arrayList) {
    }

    public boolean isSupportICERequired() {
        return false;
    }
}
