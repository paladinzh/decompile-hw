package com.android.settings.applications;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SpecialAccessSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230902;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new LinkedList();
            if (Utils.isWifiOnly(context)) {
                keys.add("data_saver");
            }
            return keys;
        }
    };
    private PreferenceScreen mDataSaver = null;

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(2131230902);
    }

    protected int getMetricsCategory() {
        return 351;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDataSaver = (PreferenceScreen) findPreference("data_saver");
    }

    public void onResume() {
        super.onResume();
        if (this.mDataSaver != null && Utils.isWifiOnly(getContext())) {
            removePreference("data_saver");
        }
    }
}
