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

public class PressureContentPreviewFragment extends PagerHelperPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (context == null || !PressureUtil.isSupportPressureHabit(context) || !context.getResources().getBoolean(2131492915)) {
                return null;
            }
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230839;
            result.add(sir);
            return result;
        }
    };
    private CustomSwitchPreference mMessagePreview;
    private CustomSwitchPreference mThumbnailPreview;

    public int[] getDrawables() {
        return new int[]{2130838613, 2130838618};
    }

    public int[] getSummaries() {
        return new int[]{2131628444, 2131628445};
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230839);
        this.mMessagePreview = (CustomSwitchPreference) findPreference("message_preview");
        this.mMessagePreview.setOnPreferenceChangeListener(this);
        this.mThumbnailPreview = (CustomSwitchPreference) findPreference("thumbnail_preview");
        this.mThumbnailPreview.setOnPreferenceChangeListener(this);
    }

    public void onResume() {
        super.onResume();
        updateSwitchState();
    }

    private void updateSwitchState() {
        if (this.mMessagePreview != null) {
            this.mMessagePreview.setChecked(isChecked("pressure_preview_message_list"));
        }
        if (this.mThumbnailPreview != null) {
            this.mThumbnailPreview.setChecked(isChecked("pressure_preview_picture"));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mMessagePreview) {
            handleChecked("pressure_preview_message_list", ((Boolean) newValue).booleanValue(), preference);
            return true;
        } else if (preference != this.mThumbnailPreview) {
            return false;
        } else {
            handleChecked("pressure_preview_picture", ((Boolean) newValue).booleanValue(), preference);
            return true;
        }
    }

    private boolean isChecked(String key) {
        return System.getInt(getContentResolver(), key, 1) == 1;
    }

    private void handleChecked(String dbKey, boolean isChecked, Preference preference) {
        System.putInt(getContentResolver(), dbKey, isChecked ? 1 : 0);
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, Boolean.valueOf(isChecked));
    }
}
