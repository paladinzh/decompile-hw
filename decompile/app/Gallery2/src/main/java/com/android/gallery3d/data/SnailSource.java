package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;

public class SnailSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public SnailSource(GalleryApp application) {
        super("snail");
        this.mApplication = application;
        this.mMatcher.add("/snail/set/*", 0);
        this.mMatcher.add("/snail/item/*", 1);
    }

    public MediaObject createMediaObject(Path path) {
        DataManager dataManager = this.mApplication.getDataManager();
        switch (this.mMatcher.match(path)) {
            case 0:
                return new SnailAlbum(path, (SnailItem) dataManager.getMediaObject("/snail/item/" + this.mMatcher.getVar(0)));
            case 1:
                int id = this.mMatcher.getIntVar(0);
                return new SnailItem(path);
            default:
                return null;
        }
    }
}
