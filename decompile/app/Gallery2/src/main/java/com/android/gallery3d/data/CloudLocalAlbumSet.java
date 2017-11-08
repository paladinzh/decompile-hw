package com.android.gallery3d.data;

import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;

public class CloudLocalAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private static final Uri[] WATCH_URIS = new Uri[]{GalleryMedia.URI, PhotoShareConstants.CLOUD_ALBUM_TABLE_URI};
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private final GalleryApp mApplication;
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private final ChangeNotifier mNotifier;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            ArrayList<MediaSet> albums = new ArrayList();
            if (!PhotoShareUtils.isCloudPhotoSwitchOpen() || PhotoShareUtils.hasNeverSynchronizedCloudData()) {
                return albums;
            }
            ArrayList<LocalCloudAlbumEntry> albumList = new ArrayList();
            ArrayList<String> notEmptyCloudAlbumIds = CloudLocalAlbumSet.this.getNotEmptyCloudAlbumIds();
            Closeable closeable = null;
            Object cameraAlbumEntry = null;
            LocalCloudAlbumEntry screenShotAlbumEntry = null;
            try {
                closeable = CloudLocalAlbumSet.this.mApplication.getContentResolver().query(PhotoShareConstants.CLOUD_ALBUM_TABLE_URI, new String[]{"albumId", "albumName", "lpath", "newName", "newPath"}, "deleteFlag=0", null, "createTime DESC");
                if (closeable == null) {
                    GalleryLog.w("photoshareLogTag", "AlbumsLoader query fail");
                    return albums;
                }
                while (closeable.moveToNext()) {
                    LocalCloudAlbumEntry localCloudAlbumEntry = new LocalCloudAlbumEntry(TextUtils.isEmpty(closeable.getString(3)) ? closeable.getString(1) : closeable.getString(3), closeable.getString(0), TextUtils.isEmpty(closeable.getString(4)) ? closeable.getString(2) : closeable.getString(4));
                    if ("default-album-1".equalsIgnoreCase(localCloudAlbumEntry.albumId)) {
                        cameraAlbumEntry = localCloudAlbumEntry;
                    } else if ("default-album-2".equalsIgnoreCase(localCloudAlbumEntry.albumId)) {
                        screenShotAlbumEntry = localCloudAlbumEntry;
                    } else if (!"default-album-3".equalsIgnoreCase(localCloudAlbumEntry.albumId) && notEmptyCloudAlbumIds.contains(localCloudAlbumEntry.albumId)) {
                        albumList.add(localCloudAlbumEntry);
                    }
                }
                if (screenShotAlbumEntry != null) {
                    if (notEmptyCloudAlbumIds.contains("default-album-2")) {
                        albumList.add(0, screenShotAlbumEntry);
                    }
                }
                if (cameraAlbumEntry != null && notEmptyCloudAlbumIds.contains("default-album-1")) {
                    albumList.add(0, cameraAlbumEntry);
                }
                Utils.closeSilently(closeable);
                DataManager dataManager = CloudLocalAlbumSet.this.mApplication.getDataManager();
                int size = albumList.size();
                for (int i = 0; i < size; i++) {
                    LocalCloudAlbumEntry entry = (LocalCloudAlbumEntry) albumList.get(i);
                    albums.add(CloudLocalAlbumSet.this.getLocalAlbum(dataManager, CloudLocalAlbumSet.this.mPath, entry.albumId, entry.albumName, entry.relativePath));
                }
                return albums;
            } catch (Exception e) {
                GalleryLog.d("photoshareLogTag", "AlbumsLoader SQLiteException  " + e.toString());
            } finally {
                Utils.closeSilently(closeable);
            }
        }

        public String workContent() {
            return "CloudLocalAlbumSet reload albums.";
        }
    }

    public static class LocalCloudAlbumEntry implements Serializable {
        private static final long serialVersionUID = 1;
        private String albumId;
        public String albumName;
        private String relativePath;

        public LocalCloudAlbumEntry(String name, String id, String lPath) {
            this.albumName = name;
            this.albumId = id;
            this.relativePath = lPath;
        }

        public int hashCode() {
            return this.albumId.hashCode();
        }

        public boolean equals(Object object) {
            if (!(object instanceof LocalCloudAlbumEntry)) {
                return false;
            }
            LocalCloudAlbumEntry entry = (LocalCloudAlbumEntry) object;
            if (this.albumId == null) {
                return false;
            }
            return this.albumId.equalsIgnoreCase(entry.albumId);
        }
    }

    public CloudLocalAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mNotifier = new ChangeNotifier((MediaSet) this, WATCH_URIS, application);
        this.mHandler = new Handler(application.getMainLooper());
        this.mApplication = application;
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.hicloud_gallery_new);
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    private ArrayList<String> getNotEmptyCloudAlbumIds() {
        Closeable closeable = null;
        ArrayList<String> cloudAlbumIds = new ArrayList();
        try {
            closeable = this.mApplication.getContentResolver().query(GalleryMedia.URI, new String[]{"DISTINCT cloud_bucket_id"}, "cloud_bucket_id IS NOT NULL AND cloud_media_id != -1", null, null);
            if (closeable == null) {
                GalleryLog.w("photoshareLogTag", "getNotEmptyCloudAlbumIds cursor is null");
                return cloudAlbumIds;
            }
            while (closeable.moveToNext()) {
                cloudAlbumIds.add(closeable.getString(0));
            }
            Utils.closeSilently(closeable);
            return cloudAlbumIds;
        } catch (SQLiteException e) {
            GalleryLog.d("photoshareLogTag", "getNotEmptyCloudAlbumIds SQLiteException " + e.toString());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private MediaSet getLocalAlbum(DataManager manager, Path parent, String albumId, String name, String relativePath) {
        synchronized (DataManager.LOCK) {
            Path path = parent.getChild(albumId);
            MediaObject object = manager.peekMediaObject(path);
            if (object != null) {
                CloudLocalAlbum album = (CloudLocalAlbum) object;
                album.setName(name);
                album.setRelativePath(relativePath);
                return album;
            }
            MediaSet cloudLocalAlbum = new CloudLocalAlbum(path, this.mApplication, albumId, name, relativePath);
            return cloudLocalAlbum;
        }
    }

    public synchronized long reload() {
        if (this.mNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        if (this.mLoadBuffer != null) {
            GalleryLog.printDFXLog("CloudLocalAlbumSet.reload mLoadBuffer != null");
            this.mAlbums = this.mLoadBuffer;
            this.mLoadBuffer = null;
            int size = this.mAlbums.size();
            for (int i = 0; i < size; i++) {
                ((MediaSet) this.mAlbums.get(i)).reload();
            }
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (this.mLoadTask == future) {
            GalleryLog.printDFXLog("CloudLocalAlbumSet.onFutureDone");
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    CloudLocalAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }

    public int getTotalVideoCount() {
        int videoCount = 0;
        for (int i = 0; i < getSubMediaSetCount(); i++) {
            videoCount += getSubMediaSet(i).getTotalVideoCount();
        }
        return videoCount;
    }
}
