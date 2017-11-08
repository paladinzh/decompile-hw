package com.android.contacts.hap.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.widget.ListView;
import com.android.contacts.list.ContactListItemView;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFrequentDataMultiSelectAdapter extends ContactDataMultiselectAdapter {
    private Context mContext;
    private ArrayList<Integer> mFrequentContactIds;

    public FavoritesFrequentDataMultiSelectAdapter(Context context, ListView listView) {
        super(context, listView);
        this.mContext = context;
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        super.configureLoader(loader, directoryId);
        if (!isSearchMode() && (loader instanceof FavoritesFrequentDataMultiSelectLoader)) {
            FavoritesFrequentDataMultiSelectLoader freLoader = (FavoritesFrequentDataMultiSelectLoader) loader;
            configFrequentUri(freLoader, directoryId);
            configFrequentSelectionAndArgs(freLoader, directoryId);
            configFrequentSortOrder(freLoader);
        }
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        super.configHwSearchLoader(loader, directoryId);
        if (loader instanceof FavoritesFrequentDataMultiSelectLoader) {
            ((FavoritesFrequentDataMultiSelectLoader) loader).setWetherLoadFrequent(false);
            configHwSearchSelectionAndArgs((FavoritesFrequentDataMultiSelectLoader) loader);
        }
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor) {
        Bundle cursorExtra = cursor.getExtras();
        if (cursorExtra != null) {
            int favCount = cursorExtra.getInt("favorites_count", 0);
            int freCount = cursorExtra.getInt("frequent_count", 0);
            int favHeaderPos = favCount == 0 ? -1 : 0;
            int freHeaderPos = freCount == 0 ? -1 : favCount;
            String title = null;
            CharSequence count = null;
            if (isSectionHeaderDisplayEnabled()) {
                if (position == favHeaderPos) {
                    title = this.mContext.getString(R.string.contacts_section_header_starred);
                    count = String.valueOf(favCount);
                } else if (position == freHeaderPos) {
                    title = this.mContext.getString(R.string.contact_favorites_frequent_label);
                    count = String.valueOf(freCount);
                }
            }
            view.setSectionHeader(title);
            view.setCountView(count);
        }
    }

    public void setFrequentContactIds(ArrayList<Integer> ids) {
        this.mFrequentContactIds = ids;
    }

    protected void configureSelection(CursorLoader loader, long directoryId) {
        DataListFilter dataFilter = getDataFilter();
        if (dataFilter != null && directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            setSelectionAndArgs(selection, selectionArgs, dataFilter);
            selection.append(" AND (contact_id IN (").append("SELECT _id FROM view_contacts WHERE starred = 1 )");
            if (isSearchMode() && this.mFrequentContactIds != null && this.mFrequentContactIds.size() > 0) {
                selection.append(" OR (").append("contact_id").append(" IN (");
                for (int i = 0; i < this.mFrequentContactIds.size(); i++) {
                    selection.append(this.mFrequentContactIds.get(i)).append(",");
                }
                selection.setLength(selection.length() - 1);
                selection.append("))");
            }
            selection.append(")");
            loader.setSelection(selection.toString());
            loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
        }
    }

    private void setSelectionAndArgs(StringBuilder selection, List<String> selectionArgs, DataListFilter dataFilter) {
        if (dataFilter.filterType == -2) {
            selection.append("mimetype IN (?)");
            selectionArgs.add("vnd.android.cursor.item/email_v2");
            return;
        }
        selection.append("mimetype IN (?)");
        selectionArgs.add("vnd.android.cursor.item/phone_v2");
    }

    private void configFrequentUri(FavoritesFrequentDataMultiSelectLoader loader, long directoryId) {
        loader.setFrequentUri(Data.CONTENT_URI);
    }

    private void configFrequentSelectionAndArgs(FavoritesFrequentDataMultiSelectLoader freLoader, long directoryId) {
        DataListFilter dataFilter = getDataFilter();
        if (dataFilter != null && directoryId == 0) {
            if (this.mFrequentContactIds == null || this.mFrequentContactIds.size() < 1) {
                freLoader.setWetherLoadFrequent(false);
            } else {
                freLoader.setWetherLoadFrequent(true);
                StringBuilder selection = new StringBuilder();
                List<String> selectionArgs = new ArrayList();
                setSelectionAndArgs(selection, selectionArgs, dataFilter);
                selection.append(" AND ").append("contact_id").append(" IN (");
                for (int i = 0; i < this.mFrequentContactIds.size(); i++) {
                    selection.append(this.mFrequentContactIds.get(i)).append(",");
                }
                selection.setLength(selection.length() - 1);
                selection.append(")");
                freLoader.setFrequentSelection(selection.toString());
                freLoader.setFrequentSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
            }
        }
    }

    private void configFrequentSortOrder(FavoritesFrequentDataMultiSelectLoader freLoader) {
        freLoader.setFrequentSortOrder("times_contacted DESC,last_time_contacted DESC");
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId) {
        Uri uri;
        switch (getDataFilter().filterType) {
            case -2:
                uri = getHwSearchBaseUri(SearchContract$DataSearch.EMAIL_CONTENT_FILTER_URI, "search_type", "search_email");
                break;
            case -1:
                uri = getHwSearchBaseUri(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, "search_type", "search_phone");
                break;
            default:
                super.configHwSearchUri(loader, directoryId);
                return;
        }
        loader.setUri(uri);
    }

    private void configHwSearchSelectionAndArgs(FavoritesFrequentDataMultiSelectLoader loader) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        selection.append("starred").append("=1");
        if (this.mFrequentContactIds != null && this.mFrequentContactIds.size() > 0) {
            selection.append(" OR ").append("contact_id").append(" IN (");
            for (int i = 0; i < this.mFrequentContactIds.size(); i++) {
                selection.append(this.mFrequentContactIds.get(i)).append(",");
            }
            selection.setLength(selection.length() - 1);
            selection.append(")");
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
    }
}
