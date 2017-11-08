package com.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.List;

public class ChooseSingleHandSettings extends MoreSettings implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (context == null || Utils.isHideSingleHandOperation(context)) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230753;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (context == null || Utils.isHideSingleHandOperation(context)) {
                return keys;
            }
            if (Utils.isHideSingleHandScreen(context)) {
                keys.add("single_hand_screen_zoom_settings");
            }
            if (SystemProperties.getInt("ro.config.hw_singlehand", 0) <= 0) {
                keys.add("single_hand_keyboard");
            }
            return keys;
        }
    };
    private SingleHandEnabler mSingleHandEnabler;
    private SingleHandScreenZoomEnabler mSingleHandScreenZoomEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230753);
    }

    public void onResume() {
        super.onResume();
        if (this.mSingleHandScreenZoomEnabler != null) {
            this.mSingleHandScreenZoomEnabler.resume();
        }
        if (this.mSingleHandEnabler != null) {
            this.mSingleHandEnabler.resume();
        }
    }

    public void onPause() {
        if (this.mSingleHandScreenZoomEnabler != null) {
            this.mSingleHandScreenZoomEnabler.pause();
        }
        if (this.mSingleHandEnabler != null) {
            this.mSingleHandEnabler.pause();
        }
        super.onPause();
    }

    protected void updatePreferenceList() {
        Preference singleHandScreenZoomPreference = findPreference("single_hand_screen_zoom_settings");
        if (singleHandScreenZoomPreference != null) {
            if (Utils.isHideSingleHandScreen(getActivity())) {
                removePreference("single_hand_screen_zoom_settings");
            } else {
                this.mSingleHandScreenZoomEnabler = new SingleHandScreenZoomEnabler(getActivity(), singleHandScreenZoomPreference);
            }
        }
        Preference singleHandPreference = findPreference("single_hand_keyboard");
        if (singleHandPreference != null) {
            if (SystemProperties.getInt("ro.config.hw_singlehand", 0) > 0) {
                this.mSingleHandEnabler = new SingleHandEnabler(getActivity(), singleHandPreference);
            } else {
                removePreference("single_hand_keyboard");
            }
        }
        super.updatePreferenceList();
    }
}
