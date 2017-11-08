package com.android.gallery3d.data;

import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.BucketHelper.BucketEntry;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import java.util.ArrayList;
import tmsdk.fg.module.spacemanager.SpaceManager;

public class LocalAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    public static final Path PATH_ALL = Path.fromString("/local/all");
    public static final Path PATH_ALL_INSIDE = Path.fromString("/local/all/inside");
    public static final Path PATH_ALL_OUTSIDE = Path.fromString("/local/all/outside");
    public static final Path PATH_IAMGE_OUTSIDE = Path.fromString("/local/image/outside");
    public static final Path PATH_IMAGE = Path.fromString("/local/image");
    public static final Path PATH_IMAGE_INSIDE = Path.fromString("/local/image/inside");
    public static final Path PATH_VIDEO = Path.fromString("/local/video");
    public static final Path PATH_VIDEO_INSIDE = Path.fromString("/local/video/inside");
    public static final Path PATH_VIDEO_OUTSIDE = Path.fromString("/local/video/outside");
    private static final Uri[] mWatchUris = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI, Constant.SETTIGNS_URI, Files.getContentUri("external"), Constant.MOVE_OUT_IN_URI};
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private final GalleryApp mApplication;
    private final int mDisplayType;
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private final String mName;
    private final ChangeNotifier mNotifier;
    private int mReloadType = 6;
    private final ReloadNotifier mReloader;
    private final int mType;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private int rtype;

        public AlbumsLoader(int reloadType) {
            this.rtype = reloadType;
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            TraceController.beginSection("LocalAlbumSet$AlbumsLoader " + LocalAlbumSet.this.mPath);
            BucketHelper.setGalleryApp(LocalAlbumSet.this.mApplication);
            BucketEntry[] entries = BucketHelper.loadBucketEntries(jc, LocalAlbumSet.this.mApplication.getContentResolver(), (LocalAlbumSet.this.mType | SpaceManager.ERROR_CODE_UNKNOW) & this.rtype, LocalAlbumSet.this.mDisplayType);
            if (jc.isCancelled()) {
                TraceController.endSection();
                return null;
            }
            boolean isSupportEmptyAlbums = (this.rtype & 16384) == 0;
            ArrayList<MediaSet> albums = new ArrayList();
            DataManager dataManager = LocalAlbumSet.this.mApplication.getDataManager();
            for (BucketEntry entry : entries) {
                MediaSet album = LocalAlbumSet.this.getLocalAlbum(dataManager, LocalAlbumSet.this.mType, LocalAlbumSet.this.mPath, entry.bucketId, entry.bucketName);
                album.setHidden(entry.isHidden);
                album.setBucketPath(entry.bucketPath);
                if (isSupportEmptyAlbums || entry.mediaCount != 0) {
                    albums.add(album);
                }
            }
            TraceController.endSection();
            return albums;
        }

        public String workContent() {
            return "reload albums see type: " + this.rtype;
        }
    }

    public LocalAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mHandler = new Handler(application.getMainLooper());
        this.mType = getTypeFromPath(path);
        this.mDisplayType = getDisplayTypeFromPath(path);
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mName = application.getResources().getString(R.string.set_label_local_albums);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUMSET, application);
    }

    private static int getTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length >= 2) {
            return MediaObject.getTypeFromString(name[1]);
        }
        throw new IllegalArgumentException(path.toString());
    }

    private static int getDisplayTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length < 3) {
            return 23;
        }
        if ("outside".equals(name[2])) {
            return 20;
        }
        if ("inside".equals(name[2])) {
            return 21;
        }
        if ("camerapaste".equals(name[2])) {
            return 22;
        }
        if ("screenshotspaste".equals(name[2])) {
            return 24;
        }
        return 23;
    }

    public MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    public int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    public String getName() {
        return this.mName;
    }

    private MediaSet getLocalAlbum(DataManager manager, int type, Path parent, int id, String name) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(id);
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) {
                MediaSet mediaSet = (MediaSet) object;
                return mediaSet;
            }
            MediaSet localAlbum;
            switch (type) {
                case 2:
                    localAlbum = new LocalAlbum(path, this.mApplication, id, true, name);
                    return localAlbum;
                case 4:
                    localAlbum = new LocalAlbum(path, this.mApplication, id, false, name);
                    return localAlbum;
                case 6:
                    localAlbum = new LocalBucketAlbum(path, this.mApplication, id, name);
                    return localAlbum;
                default:
                    throw new IllegalArgumentException(String.valueOf(type));
            }
        }
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
        if (this.mNotifier.isDirty() || reloadFlag) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            TraceController.printDebugInfo("submit AlbumsLoader " + this.mPath);
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(this.mReloadType), this, 4);
            if (oldType != this.mReloadType) {
                this.mLoadBuffer = null;
                this.mAlbums.clear();
            }
        }
        if (this.mLoadBuffer != null) {
            this.mAlbums = this.mLoadBuffer;
            this.mLoadBuffer = null;
            for (MediaSet album : this.mAlbums) {
                album.reload();
            }
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (this.mLoadTask == future) {
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    LocalAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }
}
