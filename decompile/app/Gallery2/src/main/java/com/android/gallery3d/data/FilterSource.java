package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.burst.BurstPhotoSet;

public class FilterSource extends MediaSource {
    private GalleryApp mApplication;
    private MediaItem mCameraShortcutItem;
    private MediaItem mEmptyItem;
    private PathMatcher mMatcher = new PathMatcher();

    public FilterSource(GalleryApp application) {
        super("filter");
        this.mApplication = application;
        this.mMatcher.add("/filter/mediatype/*/*", 0);
        this.mMatcher.add("/filter/delete/*", 1);
        this.mMatcher.add("/filter/empty/*", 2);
        this.mMatcher.add("/filter/empty_prompt", 3);
        this.mMatcher.add("/filter/camera_shortcut", 4);
        this.mMatcher.add("/filter/camera_shortcut_item", 5);
        this.mMatcher.add("/filter/*/bucket/*/burst/*", 13);
        this.mMatcher.add("/filter/keyguard/*", 14);
        this.mEmptyItem = new EmptyAlbumImage(GalleryUtils.getCleanPath("/filter/empty_prompt"), this.mApplication);
        this.mCameraShortcutItem = new CameraShortcutImage(GalleryUtils.getCleanPath("/filter/camera_shortcut_item"), this.mApplication);
    }

    public MediaObject createMediaObject(Path path) {
        int matchType = this.mMatcher.match(path);
        DataManager dataManager = this.mApplication.getDataManager();
        switch (matchType) {
            case 0:
                return new FilterTypeSet(path, dataManager, dataManager.getMediaSetsFromString(this.mMatcher.getVar(1))[0], this.mMatcher.getIntVar(0));
            case 1:
                return new FilterDeleteSet(path, dataManager.getMediaSetsFromString(this.mMatcher.getVar(0))[0]);
            case 2:
                return new FilterEmptyPromptSet(path, dataManager.getMediaSetsFromString(this.mMatcher.getVar(0))[0], this.mEmptyItem);
            case 3:
                return this.mEmptyItem;
            case 4:
                return new SingleItemAlbum(path, this.mCameraShortcutItem);
            case 5:
                return this.mCameraShortcutItem;
            case 13:
                return new BurstPhotoSet(path, this.mApplication, "localBurst".equalsIgnoreCase(this.mMatcher.getVar(0)), this.mMatcher.getIntVar(1), this.mMatcher.getVar(2));
            case 14:
                return new FilterKeyguardSet(path, this.mApplication, dataManager.getMediaSetsFromString(this.mMatcher.getVar(0))[0]);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    public static Path getSetPath(int bucketId, String burstId, boolean isLocal) {
        return Path.fromString("/filter/" + (isLocal ? "localBurst" : "galleryBurst") + "/bucket/" + bucketId + "/burst/" + burstId);
    }
}
