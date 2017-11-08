package com.android.contacts.hap.list;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;

public class FavoritesFrequentDataMultiSelectFragment extends ContactDataMultiSelectFragment {
    public CursorLoader createCursorLoader(int id) {
        return new FavoritesFrequentDataMultiSelectLoader(getActivity(), null, null, null, null, null);
    }

    protected DataListAdapter getListAdapter() {
        FavoritesFrequentDataMultiSelectAdapter adapter = new FavoritesFrequentDataMultiSelectAdapter(getActivity(), getListView());
        adapter.setDataFilter(getFilterForRequest());
        adapter.setFrequentContactIds(getActivity().getIntent().getIntegerArrayListExtra("extra_frequent_contact_ids"));
        return adapter;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
        setVisibleScrollbarEnabled(false);
    }
}
