package com.huawei.gallery.editor.filters;

import android.content.res.Resources;

public class FiltersManager extends BaseFiltersManager {
    public FiltersManager() {
        init();
    }

    public void setResources(Resources resources) {
        setFilterResources(resources);
    }
}
