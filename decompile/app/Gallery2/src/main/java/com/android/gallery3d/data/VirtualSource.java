package com.android.gallery3d.data;

import com.amap.api.services.core.AMapException;
import com.android.gallery3d.app.GalleryApp;

class VirtualSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public VirtualSource(GalleryApp context) {
        super("virtual");
        this.mApplication = context;
        this.mMatcher.add("/virtual/all", 0);
        this.mMatcher.add("/virtual/all/all", 14);
        this.mMatcher.add("/virtual/all/empty", 10);
        this.mMatcher.add("/virtual/all/other", 11);
        this.mMatcher.add("/virtual/all/camera", 12);
        this.mMatcher.add("/virtual/all/favorite", 13);
        this.mMatcher.add("/virtual/all/camera_video", 16);
        this.mMatcher.add("/virtual/all/photoshare", 25);
        this.mMatcher.add("/virtual/all/3d_panorama", 27);
        this.mMatcher.add("/virtual/other", 7);
        this.mMatcher.add("/virtual/empty", 6);
        this.mMatcher.add("/virtual/favorite", 9);
        this.mMatcher.add("/virtual/camera_video", 15);
        this.mMatcher.add("/virtual/photoshare", 26);
        this.mMatcher.add("/virtual/3d_panorama", 28);
        this.mMatcher.add("/virtual/image", 1);
        this.mMatcher.add("/virtual/image/camera", 17);
        this.mMatcher.add("/virtual/image/favorite", 18);
        this.mMatcher.add("/virtual/image/album/favorite", 19);
        this.mMatcher.add("/virtual/video/favorite", 20);
        this.mMatcher.add("/virtual/video/album/favorite", 5);
        this.mMatcher.add("/virtual/video", 2);
        this.mMatcher.add("/virtual/all/*", 3);
        this.mMatcher.add("/virtual/image/*", 4);
        this.mMatcher.add("/virtual/video/*", 5);
        this.mMatcher.add("/virtual/image/3d_panorama", 29);
        this.mMatcher.add("/virtual/all/screenshots", 21);
        this.mMatcher.add("/virtual/all/screenshots_video", 24);
        this.mMatcher.add("/virtual/image/screenshots", 23);
        this.mMatcher.add("/virtual/screenshots_video", 22);
        this.mMatcher.add("/virtual/all/doc_rectify", 30);
        this.mMatcher.add("/virtual/image/doc_rectify", 31);
        this.mMatcher.add("/virtual/doc_rectify", 32);
        this.mMatcher.add("/virtual/all/3d_model_image", 33);
        this.mMatcher.add("/virtual/image/3d_model_image", 34);
        this.mMatcher.add("/virtual/3d_model_image", 35);
        this.mMatcher.add("/virtual/all/recycle", 36);
        this.mMatcher.add("/virtual/recycle", 37);
        this.mMatcher.add("/virtual/image/wallpaper", 121);
    }

    public MediaObject createMediaObject(Path path) {
        int type = this.mMatcher.match(path);
        switch (type) {
            case 0:
            case 1:
            case 2:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 20:
            case 21:
            case 23:
            case 24:
            case 25:
            case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
            case AMapException.ERROR_CODE_PROTOCOL /*29*/:
            case 30:
            case 31:
            case 33:
            case AMapException.ERROR_CODE_SERVER /*34*/:
            case AMapException.ERROR_CODE_REQUEST /*36*/:
                return new VirtualAlbumSet(path, this.mApplication);
            case 3:
            case 4:
            case 5:
            case 9:
            case 15:
            case 19:
            case 22:
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
            case 32:
            case AMapException.ERROR_CODE_QUOTA /*35*/:
                return new VirtualAlbum(path, this.mApplication, type);
            case 6:
                return new VirtualEmptyAlbum(path, this.mApplication);
            case 7:
                return new VirtualOtherAlbum(path, this.mApplication);
            case AMapException.ERROR_CODE_URL /*26*/:
                return new VirtualPhotoShareAlbum(path, this.mApplication);
            case 37:
                return new GalleryRecycleAlbum(path, this.mApplication);
            case 121:
                return new WallpaperImage(path, this.mApplication);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
