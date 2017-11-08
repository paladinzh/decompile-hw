package com.android.settings.search;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.Collections;
import java.util.List;

public class BaseSearchIndexProvider implements SearchIndexProvider {
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
        return null;
    }

    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
        return null;
    }

    public List<String> getNonIndexableKeys(Context context) {
        return EMPTY_LIST;
    }
}
