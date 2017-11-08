package com.huawei.systemmanager.settingsearch;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesContract;
import android.provider.SearchIndexablesProvider;
import com.huawei.netassistant.ui.NetAssistantMainActivity;
import com.huawei.permissionmanager.ui.MainFragment;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity;
import java.util.Iterator;
import java.util.List;

public class SettingSearchProvider extends SearchIndexablesProvider {
    private static final String TAG = "SettingSearchProvider";
    private static final BaseSearchIndexProvider[] sSearchProviders = new BaseSearchIndexProvider[]{NetAssistantMainActivity.SEARCH_INDEX_DATA_PROVIDER, MainFragment.SEARCH_INDEX_DATA_PROVIDER, HwPowerManagerActivity.SEARCH_INDEX_DATA_PROVIDER};

    public boolean onCreate() {
        return true;
    }

    public Cursor queryXmlResources(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
        for (BaseSearchIndexProvider provider : sSearchProviders) {
            List<SearchIndexableResource> values = provider.getXmlResourcesToIndex(getContext(), true);
            if (!HsmCollections.isEmpty(values)) {
                for (SearchIndexableResource val : values) {
                    cursor.addRow(new Object[]{Integer.valueOf(val.rank), Integer.valueOf(val.xmlResId), val.className, Integer.valueOf(val.iconResId), val.intentAction, val.intentTargetPackage, val.intentTargetClass});
                }
            }
        }
        return cursor;
    }

    public Cursor queryRawData(String[] projection) {
        MatrixCursor result = new MatrixCursor(SearchIndexablesContract.INDEXABLES_RAW_COLUMNS);
        for (BaseSearchIndexProvider provider : sSearchProviders) {
            List<SearchIndexableRaw> values = provider.getRawDataToIndex(getContext(), true);
            if (!HsmCollections.isEmpty(values)) {
                for (SearchIndexableRaw val : values) {
                    result.addRow(new Object[]{Integer.valueOf(val.rank), val.title, val.summaryOn, val.summaryOff, val.entries, val.keywords, val.screenTitle, val.className, Integer.valueOf(val.iconResId), val.intentAction, val.intentTargetPackage, val.intentTargetClass, val.key, Integer.valueOf(val.userId)});
                }
            }
        }
        return result;
    }

    public Cursor queryNonIndexableKeys(String[] projection) {
        MatrixCursor cursor = new MatrixCursor(SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
        for (BaseSearchIndexProvider provider : sSearchProviders) {
            List<String> values = provider.getNonIndexableKeys(getContext());
            if (!HsmCollections.isEmpty(values)) {
                Iterator val$iterator = values.iterator();
                while (val$iterator.hasNext()) {
                    cursor.addRow(new Object[]{(String) val$iterator.next()});
                }
            }
        }
        return cursor;
    }
}
