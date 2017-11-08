package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;

class ComboSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public ComboSource(GalleryApp application) {
        super("combo");
        this.mApplication = application;
        this.mMatcher.add("/combo/*", 0);
        this.mMatcher.add("/combo/*/*", 1);
    }

    public MediaObject createMediaObject(Path path) {
        String[] segments = path.split();
        if (segments.length < 2) {
            throw new RuntimeException("bad path: " + path);
        }
        DataManager dataManager = this.mApplication.getDataManager();
        switch (this.mMatcher.match(path)) {
            case 0:
                return new ComboAlbumSet(path, this.mApplication, dataManager.getMediaSetsFromString(segments[1]));
            case 1:
                return new ComboAlbum(path, dataManager.getMediaSetsFromString(segments[2]), segments[1]);
            default:
                return null;
        }
    }
}
