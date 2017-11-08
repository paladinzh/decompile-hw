package com.android.gallery3d.data;

import android.os.Handler;
import android.os.RemoteException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.CategoryInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.ArrayList;

public class PhotoShareCategoryAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private final GalleryApp mApplication;
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private PhotoShareChangeNotifier mPhotoShareChangeNotifier;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            DataManager dataManager = PhotoShareCategoryAlbumSet.this.mApplication.getDataManager();
            ArrayList<MediaSet> albums = new ArrayList();
            Iterable categoryInfoList = null;
            try {
                categoryInfoList = PhotoShareUtils.getServer().getCategoryInfoList();
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            if (r1 != null && r1.size() > 0) {
                for (CategoryInfo info : r1) {
                    if (info.getPhotoNum() > 0) {
                        albums.add(PhotoShareCategoryAlbumSet.this.getCategoryAlbum(dataManager, info));
                    }
                }
            }
            return albums;
        }

        public String workContent() {
            return "PhotoShareCategoryAlbumSet reload albums.";
        }
    }

    PhotoShareCategoryAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mHandler = new Handler(application.getMainLooper());
        this.mPhotoShareChangeNotifier = new PhotoShareChangeNotifier(this, 2, 1);
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    private MediaSet getCategoryAlbum(DataManager dataManager, CategoryInfo categoryInfo) {
        MediaSet mediaSet;
        Path path = this.mPath.getChild(categoryInfo.getCategoryId());
        synchronized (DataManager.LOCK) {
            mediaSet = (MediaSet) dataManager.peekMediaObject(path);
            if (mediaSet == null) {
                mediaSet = new PhotoShareCategoryAlbum(path, this.mApplication, categoryInfo);
            } else {
                ((PhotoShareCategoryAlbum) mediaSet).updateCategoryInfo(categoryInfo);
            }
        }
        return mediaSet;
    }

    public String getName() {
        return "Cloud Classify";
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized long reload() {
        if (this.mPhotoShareChangeNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        GalleryLog.printDFXLog("PhotoShareCategoryAlbumSet reload call for DFX");
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
                GalleryLog.printDFXLog("PhotoShareCategoryAlbumSet mLoadBuffer is NULL for DFX");
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhotoShareCategoryAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }

    public MediaItem getCoverMediaItem() {
        return null;
    }
}
