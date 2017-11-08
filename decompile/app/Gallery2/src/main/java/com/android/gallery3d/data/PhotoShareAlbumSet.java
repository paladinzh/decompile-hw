package com.android.gallery3d.data;

import android.os.Handler;
import android.os.RemoteException;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.watermark.manager.parse.WMConfig;
import java.util.ArrayList;
import java.util.List;

public class PhotoShareAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    public static final ArrayList<String> AUTO_UPLOAD_ALBUM_NAME = new ArrayList();
    public static final Path PATH_ALL = Path.fromString("/photoshare/all");
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private final GalleryApp mApplication;
    private String mExcludeAlbumID;
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private boolean mMyShareOnly;
    private final String mName;
    private PhotoShareChangeNotifier mPhotoShareChangeNotifier;
    private final int mType;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            ArrayList<MediaSet> albums = new ArrayList();
            DataManager dataManager = PhotoShareAlbumSet.this.mApplication.getDataManager();
            if (PhotoShareAlbumSet.this.mMyShareOnly) {
                albums.addAll(PhotoShareAlbumSet.this.getOnlyMyShareAlbum(dataManager));
            } else if (PhotoShareUtils.isShareSwitchOpen() && !PhotoShareUtils.isCloudNormandyVersion()) {
                List<ShareInfo> shareInfoList = PhotoShareAlbumSet.this.getShareInfoList();
                albums.addAll(PhotoShareAlbumSet.this.getFamilyShareAlbum(dataManager));
                if (shareInfoList != null && shareInfoList.size() > 0) {
                    albums.addAll(PhotoShareAlbumSet.this.getShareAlbum(dataManager, shareInfoList));
                }
            }
            return albums;
        }

        public String workContent() {
            return "PhotoShareAlbum reload albums. mMyShareOnly: " + PhotoShareAlbumSet.this.mMyShareOnly;
        }
    }

    static {
        AUTO_UPLOAD_ALBUM_NAME.add("default-album-101");
        AUTO_UPLOAD_ALBUM_NAME.add("default-album-102");
        AUTO_UPLOAD_ALBUM_NAME.add("default-album-103");
    }

    public PhotoShareAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mHandler = new Handler(application.getMainLooper());
        if ("myshare".equalsIgnoreCase(path.getParent().getSuffix())) {
            this.mMyShareOnly = true;
            this.mExcludeAlbumID = path.getSuffix();
        }
        this.mType = this.mMyShareOnly ? 6 : getTypeFromPath(path);
        this.mName = application.getResources().getString(R.string.tab_cloud);
        this.mPhotoShareChangeNotifier = new PhotoShareChangeNotifier(this, 3);
    }

    private int getTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length >= 2) {
            return getPhotoShareAlbumType(name[1]);
        }
        throw new IllegalArgumentException(path.toString());
    }

    private int getPhotoShareAlbumType(String s) {
        if (WMConfig.SUPPORTALL.equals(s)) {
            return 3;
        }
        if ("image".equals(s)) {
            return 1;
        }
        if ("video".equals(s)) {
            return 2;
        }
        return 3;
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

    private static String getAlbumPath(int mediaType) {
        if (mediaType == 2) {
            return "/photoshare/image/share/preview/*";
        }
        if (mediaType == 4) {
            return "/photoshare/video/share/preview/*";
        }
        return "/photoshare/all/share/preview/*";
    }

    private List<ShareInfo> getShareInfoList() {
        List<ShareInfo> shareList = null;
        try {
            shareList = PhotoShareUtils.getServer().getShareList(0);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return shareList;
    }

    private ArrayList<MediaSet> getShareAlbum(DataManager dataManager, List<ShareInfo> shareInfoList) {
        ArrayList<MediaSet> shareAlbums = new ArrayList();
        for (ShareInfo share : shareInfoList) {
            int i;
            String albumPath = getAlbumPath(this.mType);
            PhotoShareAlbumInfo photoShareShareAlbumInfo = new PhotoShareShareAlbumInfo(share);
            if (share.getType() == 1) {
                i = 2;
            } else {
                i = 3;
            }
            shareAlbums.add(getPhotoShareAlbum(dataManager, albumPath, photoShareShareAlbumInfo, i));
        }
        return shareAlbums;
    }

    private ArrayList<MediaSet> getFamilyShareAlbum(DataManager dataManager) {
        ArrayList<MediaSet> shareAlbums = new ArrayList();
        Iterable shareList = null;
        boolean z = false;
        try {
            z = PhotoShareUtils.getServer().isSupportSns();
            GalleryLog.v("PhotoShareAlbumSet", "isSupportSns" + z);
            if (!z) {
                return shareAlbums;
            }
            shareList = PhotoShareUtils.getServer().getShareGroupList();
            if (r7 != null && r7.size() > 0) {
                for (ShareInfo share : r7) {
                    shareAlbums.add(getPhotoShareAlbum(dataManager, getAlbumPath(this.mType), new PhotoShareShareAlbumInfo(share), 7));
                }
                GalleryLog.v("PhotoShareAlbumSet", "ALBUM_TYPE_FAMILY_SHARE size " + shareAlbums.size());
            }
            if (shareAlbums.size() == 0 && r1 && !this.mMyShareOnly) {
                Path path = this.mPath.getChild("virture").getChild("add");
                MediaSet set = (MediaSet) dataManager.peekMediaObject(path);
                if (set == null) {
                    set = new VirtualEmptyAlbum(path, this.mApplication);
                }
                shareAlbums.add(set);
            }
            return shareAlbums;
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    private ArrayList<MediaSet> getOnlyMyShareAlbum(DataManager dataManager) {
        ArrayList<MediaSet> onlyMyShareAlbums = new ArrayList();
        Iterable onlyMyShareList = null;
        try {
            onlyMyShareList = PhotoShareUtils.getServer().getShareList(0);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        onlyMyShareAlbums.addAll(getFamilyShareAlbum(dataManager));
        if (r2 != null && r2.size() > 0) {
            for (ShareInfo share : r2) {
                if (share.getType() != 2 || "0".equalsIgnoreCase((String) share.getLocalThumbPath().get(0))) {
                    int i;
                    String albumPath = getAlbumPath(this.mType);
                    PhotoShareAlbumInfo photoShareShareAlbumInfo = new PhotoShareShareAlbumInfo(share);
                    if (share.getType() == 1) {
                        i = 2;
                    } else {
                        i = 3;
                    }
                    onlyMyShareAlbums.add(getPhotoShareAlbum(dataManager, albumPath, photoShareShareAlbumInfo, i));
                }
            }
        }
        if (onlyMyShareAlbums.size() > 0) {
            for (MediaSet set : onlyMyShareAlbums) {
                if (set.getPath().getSuffix().equalsIgnoreCase(this.mExcludeAlbumID)) {
                    onlyMyShareAlbums.remove(set);
                    break;
                }
            }
        }
        return onlyMyShareAlbums;
    }

    private MediaSet getPhotoShareAlbum(DataManager manager, String parent, PhotoShareAlbumInfo albumInfo, int albumType) {
        String id = albumInfo.getId();
        synchronized (DataManager.LOCK) {
            Path path = Path.fromString(parent.replace("*", id));
            MediaSet object = (MediaSet) manager.peekMediaObject(path);
            if (object != null) {
                object.setAlbumInfo(albumInfo);
                object.setAlbumType(albumType);
                return object;
            }
            if (albumType == 7 || albumType == 3 || albumType == 2) {
                object = new PhotoShareTimeBucketAlbum(path, this.mApplication, this.mType, albumInfo);
            } else {
                object = new PhotoShareAlbum(path, this.mApplication, this.mType, 1, albumInfo);
            }
            object.setAlbumType(albumType);
            return object;
        }
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized long reload() {
        if (this.mPhotoShareChangeNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                GalleryLog.v("PhotoShareAlbumSet", "load task cancelled");
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(), this);
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
                    PhotoShareAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }

    public static String[] getFamilyShare() {
        if (PhotoShareUtils.getServer() == null) {
            return new String[0];
        }
        String[] group = null;
        try {
            group = PhotoShareUtils.getServer().getSnsGroupList();
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return group;
    }
}
