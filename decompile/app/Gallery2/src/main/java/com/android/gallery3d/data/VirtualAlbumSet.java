package com.android.gallery3d.data;

import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.photorectify.RectifyUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.threedmodel.ThreeDModelImageUtils;
import com.huawei.watermark.manager.parse.WMConfig;
import java.util.ArrayList;

public class VirtualAlbumSet extends MediaSet implements ContentListener, FutureListener<ArrayList<MediaSet>> {
    private static boolean mIsSupportAllVideo = true;
    private static boolean mIsSupportMyFavorite = GalleryUtils.isSupportMyFavorite();
    private static final ArrayList<VirtualAlbumType> mVirtualAlbumTypeSets = new ArrayList();
    private static final Uri[] mWatchUris = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI, Files.getContentUri("external"), Constant.MYFAVORITE_URI, Constant.MOVE_OUT_IN_URI};
    private final GalleryApp mApplication;
    private final int mDisplayType;
    private final Handler mHandler;
    private boolean mIsDirty = false;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private Object mLock = new Object();
    private final ChangeNotifier mNotifier;
    private ArrayList<MediaSet> mOriginSets = new ArrayList();
    private int mReloadType = 6;
    private final ReloadNotifier mReloader;
    private ArrayList<MediaSet> mVirtualAlbumSets = new ArrayList();

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private int rtype;

        public AlbumsLoader(int reloadType) {
            this.rtype = reloadType;
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            ArrayList<MediaSet> albums;
            TraceController.beginSection("run VirtualAlbum$AlbumsLoader " + VirtualAlbumSet.this.mPath);
            boolean showCreateEmptyFunc = true;
            if ((this.rtype & 32768) != 0) {
                showCreateEmptyFunc = false;
            }
            synchronized (VirtualAlbumSet.this.mLock) {
                albums = new ArrayList();
                albums.addAll(VirtualAlbumSet.this.mOriginSets);
                for (MediaSet virtualAlbum : albums) {
                    virtualAlbum.reload();
                }
                for (int index = albums.size() - 1; index >= 0; index--) {
                    MediaSet set = (MediaSet) albums.get(index);
                    if (set.getMediaItemCount() == 0 && !(set instanceof GalleryRecycleAlbum)) {
                        albums.remove(index);
                    } else if ("empty".equalsIgnoreCase(((MediaSet) albums.get(index)).getLabel()) && !showCreateEmptyFunc) {
                        albums.remove(index);
                    }
                }
                TraceController.endSection();
            }
            return albums;
        }

        public String workContent() {
            return "reload virtual album. reloadType: " + this.rtype;
        }
    }

    public enum VirtualAlbumType {
        VIRTUAL_CAMERA_PHOTO,
        VIRTUAL_MY_FAVORITE,
        VIRTUAL_ALL_VIDEO,
        VIRTUAL_OTHER_ALBUM,
        VIRTUAL_EMPTY_ALBUM,
        VIRTUAL_CAMERA_3D_PANORAMA,
        VIRTUAL_SCREENSHOTS_PHOTO,
        VIRTUAL_SCREENSHOTS_VIDEO,
        VIRTUAL_PHOTOSHARE,
        VIRTUAL_DOC_RECTIFY,
        VIRTUAL_THREE_D_MODEL_IMAGE,
        VIRTUAL_RECYCLE
    }

    static {
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_CAMERA_PHOTO"));
        if (mIsSupportMyFavorite) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_MY_FAVORITE"));
        }
        if (FyuseFile.isSupport3DPanorama()) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_CAMERA_3D_PANORAMA"));
        }
        if (mIsSupportAllVideo) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_ALL_VIDEO"));
        }
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_OTHER_ALBUM"));
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_EMPTY_ALBUM"));
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_SCREENSHOTS_PHOTO"));
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_SCREENSHOTS_VIDEO"));
        mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_PHOTOSHARE"));
        if (RectifyUtils.isRectifyNativeSupport()) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_DOC_RECTIFY"));
        }
        if (ThreeDModelImageUtils.isThreeDModelImageNativeSupport()) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_THREE_D_MODEL_IMAGE"));
        }
        if (RecycleUtils.supportRecycle()) {
            mVirtualAlbumTypeSets.add(VirtualAlbumType.valueOf("VIRTUAL_RECYCLE"));
        }
    }

    public VirtualAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mHandler = new Handler(application.getMainLooper());
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUMSET, application);
        this.mDisplayType = getDisplayTypeFromPath(path);
        boolean isImage = MediaObject.isImageTypeFromPath(path);
        boolean isVideo = MediaObject.isVideoTypeFromPath(path);
        DataManager dataManager = this.mApplication.getDataManager();
        MediaSet album = null;
        for (VirtualAlbumType albums : mVirtualAlbumTypeSets) {
            String id = albums.name();
            if ((this.mDisplayType == 10 || this.mDisplayType == 18) && VirtualAlbumType.VIRTUAL_PHOTOSHARE.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/photoshare");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 13) && VirtualAlbumType.VIRTUAL_CAMERA_PHOTO.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject(isImage ? "/local/image/camera" : "/local/camera");
            } else if (this.mDisplayType == 11 && VirtualAlbumType.VIRTUAL_EMPTY_ALBUM.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/empty");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 12) && VirtualAlbumType.VIRTUAL_OTHER_ALBUM.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/other");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 14) && VirtualAlbumType.VIRTUAL_MY_FAVORITE.name().equalsIgnoreCase(id)) {
                String favoritePath = isImage ? "/virtual/image/album/favorite" : isVideo ? "/virtual/video/album/favorite" : "/virtual/favorite";
                album = (MediaSet) dataManager.getMediaObject(favoritePath);
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 15) && VirtualAlbumType.VIRTUAL_ALL_VIDEO.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/camera_video");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 19) && VirtualAlbumType.VIRTUAL_CAMERA_3D_PANORAMA.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/3d_panorama");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 16) && VirtualAlbumType.VIRTUAL_SCREENSHOTS_PHOTO.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject(isImage ? "/local/image/screenshots" : "/local/screenshots");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 17) && VirtualAlbumType.VIRTUAL_SCREENSHOTS_VIDEO.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/screenshots_video");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 20) && VirtualAlbumType.VIRTUAL_DOC_RECTIFY.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/doc_rectify");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 21) && VirtualAlbumType.VIRTUAL_THREE_D_MODEL_IMAGE.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/3d_model_image");
            } else if ((this.mDisplayType == 10 || this.mDisplayType == 22) && VirtualAlbumType.VIRTUAL_RECYCLE.name().equalsIgnoreCase(id)) {
                album = (MediaSet) dataManager.getMediaObject("/virtual/recycle");
            }
            if (album != null) {
                album.addContentListener(this);
                this.mOriginSets.add(album);
                album = null;
            }
        }
    }

    private static int getDisplayTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length < 3) {
            throw new IllegalArgumentException(path.toString());
        } else if (WMConfig.SUPPORTALL.equalsIgnoreCase(name[2])) {
            return 10;
        } else {
            if ("empty".equalsIgnoreCase(name[2])) {
                return 11;
            }
            if ("other".equalsIgnoreCase(name[2])) {
                return 12;
            }
            if ("camera".equalsIgnoreCase(name[2])) {
                return 13;
            }
            if ("favorite".equalsIgnoreCase(name[2])) {
                return 14;
            }
            if ("camera_video".equalsIgnoreCase(name[2])) {
                return 15;
            }
            if ("screenshots".equalsIgnoreCase(name[2])) {
                return 16;
            }
            if ("screenshots_video".equalsIgnoreCase(name[2])) {
                return 17;
            }
            if ("3d_panorama".equalsIgnoreCase(name[2])) {
                return 19;
            }
            if ("photoshare".equalsIgnoreCase(name[2])) {
                return 18;
            }
            if ("doc_rectify".equalsIgnoreCase(name[2])) {
                return 20;
            }
            if ("3d_model_image".equalsIgnoreCase(name[2])) {
                return 21;
            }
            if ("recycle".equals(name[2])) {
                return 22;
            }
            return 10;
        }
    }

    public MediaSet getSubMediaSet(int index) {
        MediaSet mediaSet;
        synchronized (this.mLock) {
            mediaSet = index < this.mVirtualAlbumSets.size() ? (MediaSet) this.mVirtualAlbumSets.get(index) : null;
        }
        return mediaSet;
    }

    public int getSubMediaSetCount() {
        int size;
        synchronized (this.mLock) {
            size = this.mVirtualAlbumSets.size();
        }
        return size;
    }

    public String getName() {
        return "VirtualAlbumSet";
    }

    public void onContentDirty() {
        synchronized (this) {
            this.mIsDirty = true;
        }
        notifyContentChanged();
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized long reload() {
        boolean reloadFlag = this.mReloader.isDirty();
        int oldType = this.mReloadType;
        if (reloadFlag) {
            this.mReloadType = this.mReloader.getReloadType();
        }
        if (this.mNotifier.isDirty() || reloadFlag || this.mIsDirty) {
            this.mIsDirty = false;
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            TraceController.printDebugInfo("submit AlbumsLoader " + this.mPath);
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(this.mReloadType), this, 6);
            if (oldType != this.mReloadType) {
                this.mLoadBuffer = null;
                this.mVirtualAlbumSets.clear();
            }
        }
        if (this.mLoadBuffer != null) {
            this.mVirtualAlbumSets = this.mLoadBuffer;
            this.mLoadBuffer = null;
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (this.mLoadTask == future) {
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                GalleryLog.v("VirtualAlbumSet", "loadBuffer is null, create new");
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    VirtualAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }
}
