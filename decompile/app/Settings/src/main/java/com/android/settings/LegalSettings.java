package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegalSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230720;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (!checkIntentAction(context, "android.settings.TERMS")) {
                keys.add("terms");
            }
            if (!checkIntentAction(context, "android.settings.LICENSE")) {
                keys.add("license");
            }
            if (!checkIntentAction(context, "android.settings.COPYRIGHT")) {
                keys.add("copyright");
            }
            if (!checkIntentAction(context, "android.settings.WEBVIEW_LICENSE")) {
                keys.add("webview_license");
            }
            return keys;
        }

        private boolean checkIntentAction(Context context, String action) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(new Intent(action), 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((((ResolveInfo) list.get(i)).activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230720);
        Activity act = getActivity();
        PreferenceGroup parentPreference = getPreferenceScreen();
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "terms", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "license", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "copyright", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "webview_license", 1);
    }

    protected int getMetricsCategory() {
        return 225;
    }
}
