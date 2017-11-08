package com.android.contacts.list;

import android.os.Bundle;
import java.util.ArrayList;

public class SpeedDialContactPickerFragment extends PhoneNumberPickerFragment {
    private static String DATA_ADDED = "data_added";
    private ArrayList<String> mDataAdded = new ArrayList();

    protected ContactEntryListAdapter createListAdapter() {
        if (isLegacyCompatibilityMode()) {
            LegacyPhoneNumberListAdapter adapter = new LegacyPhoneNumberListAdapter(getActivity());
            adapter.setDisplayPhotos(true);
            return adapter;
        }
        PhoneNumberListAdapter adapter2 = new PhoneNumberListAdapter(getActivity());
        adapter2.setDisplayPhotos(true);
        adapter2.setExcludeSim(false);
        adapter2.setUseSelectionInSearchMode(true);
        adapter2.setFilter(ContactListFilter.createFilterWithType(0));
        adapter2.setDataAdded(this.mDataAdded);
        return adapter2;
    }

    public void restoreSavedState(Bundle savedState) {
        if (savedState != null) {
            this.mDataAdded = savedState.getStringArrayList(DATA_ADDED);
        }
        super.restoreSavedState(savedState);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(DATA_ADDED, this.mDataAdded);
        super.onSaveInstanceState(outState);
    }

    public void setContactsAddedInSpeedDial(ArrayList<String> aDataAdded) {
        this.mDataAdded = aDataAdded;
    }
}
