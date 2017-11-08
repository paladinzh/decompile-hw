package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;

public class KeyguardSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    protected KeyguardSource(GalleryApp application) {
        super("keyguard");
        this.mApplication = application;
        this.mMatcher.add("/keyguard/download", 0);
        this.mMatcher.add("/keyguard/custom", 1);
        this.mMatcher.add("/keyguard/item/*", 2);
    }

    public MediaObject createMediaObject(Path path) {
        switch (this.mMatcher.match(path)) {
            case 0:
            case 1:
                return new KeyguardSet(path, this.mApplication);
            case 2:
                return new KeyguardItem(path, this.mApplication, this.mMatcher.getIntVar(0));
            default:
                throw new RuntimeException("bad path : " + path);
        }
    }
}
