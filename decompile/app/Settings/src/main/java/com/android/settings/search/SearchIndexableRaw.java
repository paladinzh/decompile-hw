package com.android.settings.search;

import android.content.Context;
import android.provider.SearchIndexableData;

public class SearchIndexableRaw extends SearchIndexableData {
    public String entries;
    public String keywords;
    public String screenTitle;
    public String summaryOff;
    public String summaryOn;
    public String title;

    public SearchIndexableRaw(Context context) {
        super(context);
    }
}
