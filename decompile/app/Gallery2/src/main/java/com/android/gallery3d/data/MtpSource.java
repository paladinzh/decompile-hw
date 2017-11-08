package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;

public class MtpSource extends MediaSource {
    GalleryApp mApplication;
    PathMatcher mMatcher = new PathMatcher();
    MtpContext mMtpContext;

    public MtpSource(GalleryApp application) {
        super("mtp");
        this.mApplication = application;
        this.mMatcher.add("/mtp", 0);
        this.mMatcher.add("/mtp/*", 1);
        this.mMatcher.add("/mtp/item/*/*", 2);
        this.mMtpContext = new MtpContext(this.mApplication.getAndroidContext());
    }

    public MediaObject createMediaObject(Path path) {
        switch (this.mMatcher.match(path)) {
            case 0:
                return new MtpDeviceSet(path, this.mApplication, this.mMtpContext);
            case 1:
                return new MtpDevice(path, this.mApplication, this.mMatcher.getIntVar(0), this.mMtpContext);
            case 2:
                return new MtpImage(path, this.mApplication, this.mMatcher.getIntVar(0), this.mMatcher.getIntVar(1), this.mMtpContext);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    public void pause() {
        this.mMtpContext.pause();
    }

    public void resume() {
        this.mMtpContext.resume();
    }

    public static boolean isMtpPath(String s) {
        return s != null ? Path.fromString(s).getPrefix().equals("mtp") : false;
    }
}
