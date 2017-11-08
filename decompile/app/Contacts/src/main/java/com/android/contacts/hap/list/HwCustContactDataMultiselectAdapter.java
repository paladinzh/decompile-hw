package com.android.contacts.hap.list;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.widget.ListView;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.list.ContactListItemView;
import java.util.List;

public class HwCustContactDataMultiselectAdapter {
    protected static final String TAG = "HwCustContactDataMultiselectAdapter";
    Context mContext;

    public HwCustContactDataMultiselectAdapter(Context context) {
        this.mContext = context;
    }

    public boolean getEnableEmailContactInMms() {
        return false;
    }

    public void setSelectionQueryArgs(StringBuilder selectionBuilder, List<String> list) {
    }

    public void initService() {
    }

    public void setSelectionAndSelectionArgsForCustomizations(int filterType, StringBuilder selection, List<String> list) {
    }

    public void setCheckStateAndRestoreData(ListView listView, ContactMultiSelectionActivity activity, ContactListItemView contactListItemView, int position, ContactDataMultiselectAdapter contactDataMultiselectAdapter) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public Uri configRCSSearchUri(String searchType, String searchValue, String keywords, int filter) {
        return null;
    }

    public void configRCSBuilder(String parameter, Builder builder) {
    }
}
