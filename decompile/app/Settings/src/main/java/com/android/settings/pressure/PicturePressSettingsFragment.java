package com.android.settings.pressure;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.views.pagerHelper.PagerHelperPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class PicturePressSettingsFragment extends PagerHelperPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!PressureUtil.isSupportPressureHabit(context)) {
                return null;
            }
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230832;
            result.add(sir);
            return result;
        }
    };

    protected int getMetricsCategory() {
        return 100000;
    }

    public int[] getDrawables() {
        return new int[]{2130838607};
    }

    public int[] getSummaries() {
        return new int[]{2131628476};
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230832);
    }

    public void onResume() {
        super.onResume();
        updateSwitchState();
    }

    private void updateSwitchState() {
        boolean z = true;
        CustomSwitchPreference picturePressPref = (CustomSwitchPreference) findPreference("picture_press_settings");
        picturePressPref.setOnPreferenceChangeListener(this);
        if (System.getInt(getContentResolver(), "picture_largen_type", 1) != 1) {
            z = false;
        }
        picturePressPref.setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("picture_press_settings".equals(preference.getKey())) {
            System.putInt(getContentResolver(), "picture_largen_type", ((Boolean) newValue).booleanValue() ? 1 : 0);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        return true;
    }
}
