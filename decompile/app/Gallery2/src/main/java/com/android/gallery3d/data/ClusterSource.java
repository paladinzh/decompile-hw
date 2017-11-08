package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.map.data.MapAlbumSet;

class ClusterSource extends MediaSource {
    GalleryApp mApplication;
    PathMatcher mMatcher = new PathMatcher();

    public ClusterSource(GalleryApp application) {
        super("cluster");
        this.mApplication = application;
        this.mMatcher.add("/cluster/*/time", 0);
        this.mMatcher.add("/cluster/*/location", 1);
        this.mMatcher.add("/cluster/*/tag", 2);
        this.mMatcher.add("/cluster/*/size", 3);
        this.mMatcher.add("/cluster/*/map", 5);
        this.mMatcher.add("/cluster/*/time/*", 256);
        this.mMatcher.add("/cluster/*/location/*", 257);
        this.mMatcher.add("/cluster/*/tag/*", 258);
        this.mMatcher.add("/cluster/*/size/*", 259);
        this.mMatcher.add("/cluster/*/map/*", 261);
    }

    public MediaObject createMediaObject(Path path) {
        int matchType = this.mMatcher.match(path);
        String setsName = this.mMatcher.getVar(0);
        DataManager dataManager = this.mApplication.getDataManager();
        MediaSet[] sets = dataManager.getMediaSetsFromString(setsName);
        switch (matchType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return new ClusterAlbumSet(path, this.mApplication, sets[0], matchType);
            case 5:
                return new MapAlbumSet(this.mApplication, path, sets[0], -1);
            case 256:
            case 257:
            case 258:
            case 259:
                return new ClusterAlbum(path, dataManager, dataManager.getMediaSet(path.getParent()));
            case 261:
                return new MapAlbum(this.mApplication, path, -1);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
