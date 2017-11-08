package com.huawei.gallery.map.app;

import com.huawei.gallery.map.app.MapFragmentBase.ClusterRadiusAndDate;

public class MapManager {
    private static final MapManager sInstance = new MapManager();
    private ClusterRadiusAndDate mClusterRadiusAndDate;
    private MapFragmentBase mShowingFragment;

    private MapManager() {
    }

    public static MapManager getInstance() {
        return sInstance;
    }

    void onStart(MapFragmentBase showingFragment) {
        this.mShowingFragment = showingFragment;
    }

    void onStop() {
        this.mShowingFragment = null;
    }

    public ClusterRadiusAndDate getClusterRadius() {
        if (this.mShowingFragment != null) {
            this.mClusterRadiusAndDate = this.mShowingFragment.getClusterRadiusAndDate();
        }
        return this.mClusterRadiusAndDate;
    }
}
