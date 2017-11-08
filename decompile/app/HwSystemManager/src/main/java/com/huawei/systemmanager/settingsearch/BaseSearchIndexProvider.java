package com.huawei.systemmanager.settingsearch;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.google.common.collect.Lists;
import java.util.List;

public class BaseSearchIndexProvider {
    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
        return null;
    }

    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
        return Lists.newArrayList();
    }

    public List<String> getNonIndexableKeys(Context context) {
        return Lists.newArrayList();
    }
}
