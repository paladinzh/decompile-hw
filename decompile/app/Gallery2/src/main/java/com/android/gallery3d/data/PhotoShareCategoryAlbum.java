package com.android.gallery3d.data;

import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.CategoryInfo;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareCategoryAlbum extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private ArrayList<MediaSet> mAlbums;
    private final GalleryApp mApplication;
    private Category mCategory;
    private String mCategoryID;
    private CategoryInfo mCategoryInfo;
    private String mExcludeAlbumID;
    private final Handler mHandler;
    private boolean mIsExcludeAlbum;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private String mName;
    private PhotoShareChangeNotifier mPhotoShareChangeNotifier;
    private int mResId;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            if (!PhotoShareUtils.isClassifySwitchOpen() || !PhotoShareUtils.isCloudPhotoSwitchOpen()) {
                return null;
            }
            long categoryAlbumReloadStart = System.currentTimeMillis();
            ArrayList<MediaSet> albums = new ArrayList();
            DataManager dataManager = PhotoShareCategoryAlbum.this.mApplication.getDataManager();
            ArrayList<TagInfo> tagInfoList = new ArrayList();
            try {
                List<TagInfo> temp;
                int count = PhotoShareUtils.getServer().getTagInfoListCount(PhotoShareCategoryAlbum.this.mCategoryID);
                GalleryLog.v("PhotoShareCategoryAlbum", " getTagInfoListCount " + count + ", path " + PhotoShareCategoryAlbum.this.mPath);
                int start = 0;
                int group = count / SmsCheckResult.ESCT_200;
                for (int i = 0; i < group; i++) {
                    temp = PhotoShareUtils.getServer().getTagInfoListLimit(PhotoShareCategoryAlbum.this.mCategoryID, start, SmsCheckResult.ESCT_200);
                    if (!(temp == null || temp.isEmpty())) {
                        tagInfoList.addAll(temp);
                    }
                    start += SmsCheckResult.ESCT_200;
                }
                int left = count % SmsCheckResult.ESCT_200;
                if (left != 0) {
                    temp = PhotoShareUtils.getServer().getTagInfoListLimit(PhotoShareCategoryAlbum.this.mCategoryID, start, left);
                    if (!(temp == null || temp.isEmpty())) {
                        tagInfoList.addAll(temp);
                    }
                }
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
            if (tagInfoList.size() > 0) {
                GalleryLog.v("PhotoShareCategoryAlbum", " Reload Size " + tagInfoList.size());
                if (PhotoShareCategoryAlbum.this.mIsExcludeAlbum) {
                    PhotoShareCategoryAlbum.this.removeAlbum(tagInfoList, PhotoShareCategoryAlbum.this.mExcludeAlbumID);
                }
                PhotoShareCategoryAlbum.this.removeAlbum(tagInfoList, "-1");
                for (TagInfo info : tagInfoList) {
                    albums.add(PhotoShareCategoryAlbum.this.getTagAlbum(dataManager, info));
                }
            }
            PhotoShareCategoryAlbum.this.printExcuteInfo(categoryAlbumReloadStart, "PhotoShareCategoryAlbum reload");
            return albums;
        }

        public String workContent() {
            return "PhotoShareCategoryAlbum reload albums.";
        }
    }

    public enum Category {
        PEOPLE("0", R.string.photoshare_classify_people),
        FOOD("1", R.string.photoshare_classify_food),
        LANDSCAPE("Landscape", R.string.photoshare_classify_landscape),
        CAT("Pet_cat", R.string.photoshare_classify_cat),
        DOG("Pet_dog", R.string.photoshare_classify_dog),
        IDENTIFY_CARD("File_idcard", R.string.photoshare_classify_document),
        PASSPORT_CARD("File_passport", R.string.photoshare_classify_document),
        BUSINESS_CARD("File_namecard", R.string.photoshare_classify_document),
        DOCUMENT_CARD("File_document", R.string.photoshare_classify_document);
        
        public final String categoryId;
        public final int resId;

        private Category(String categoryId, int resId) {
            this.categoryId = categoryId;
            this.resId = resId;
        }

        public static Category getName(String categoryId) {
            for (Category c : values()) {
                if (c.categoryId.equalsIgnoreCase(categoryId)) {
                    return c;
                }
            }
            return null;
        }
    }

    public PhotoShareCategoryAlbum(Path path, GalleryApp galleryApp, CategoryInfo categoryInfo) {
        this(path, galleryApp, categoryInfo, false, null);
    }

    public PhotoShareCategoryAlbum(Path path, GalleryApp galleryApp, CategoryInfo categoryInfo, boolean isExcludeAlbum, String excludeAlbumId) {
        super(path, MediaObject.nextVersionNumber());
        this.mAlbums = new ArrayList();
        this.mApplication = galleryApp;
        this.mCategoryInfo = categoryInfo;
        this.mCategoryID = this.mCategoryInfo.getCategoryId();
        this.mCategory = Category.getName(this.mCategoryID);
        this.mResId = this.mCategory != null ? this.mCategory.resId : 0;
        this.mName = this.mCategoryInfo.getCategoryName();
        this.mPhotoShareChangeNotifier = new PhotoShareChangeNotifier(this, 2, 2);
        this.mHandler = new Handler(this.mApplication.getMainLooper());
        this.mIsExcludeAlbum = isExcludeAlbum;
        this.mExcludeAlbumID = excludeAlbumId;
    }

    public void updateCategoryInfo(CategoryInfo info) {
        this.mCategoryInfo = info;
        this.mName = this.mCategoryInfo.getCategoryName();
    }

    public synchronized MediaSet getSubMediaSet(int index) {
        if (index >= 0) {
            if (index < this.mAlbums.size()) {
                return (MediaSet) this.mAlbums.get(index);
            }
        }
        return null;
    }

    public synchronized int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    public int getTotalMediaItemCount() {
        return this.mCategoryInfo.getPhotoNum();
    }

    private boolean isIllegality() {
        return this.mCategoryInfo.getAlbumList() == null || this.mCategoryInfo.getAlbumList().size() == 0;
    }

    public MediaItem getCoverMediaItem() {
        if (isIllegality()) {
            return null;
        }
        String hash = this.mCategoryInfo.getHash();
        String albumID = null;
        String name = null;
        FileInfo fileInfo = null;
        int folderType = 1;
        if (this.mCategoryInfo.getAlbumList() == null) {
            return null;
        }
        int albumListSize = this.mCategoryInfo.getAlbumList().size();
        for (int i = 0; i < albumListSize; i++) {
            albumID = (String) this.mCategoryInfo.getAlbumList().get(i);
            if (!TextUtils.isEmpty(albumID)) {
                try {
                    if (!albumID.equalsIgnoreCase("default-album-1") && !albumID.equalsIgnoreCase("default-album-2")) {
                        ShareInfo shareInfo = PhotoShareUtils.getServer().getShare(albumID);
                        if (shareInfo != null) {
                            name = shareInfo.getShareName();
                        }
                        folderType = 2;
                        fileInfo = PhotoShareUtils.getServer().getShareFileInfo(albumID, hash);
                        if (fileInfo != null) {
                            break;
                        }
                    } else {
                        name = albumID;
                        folderType = 1;
                        fileInfo = PhotoShareTagAlbum.getFileInfo(this.mApplication, albumID, hash);
                        if (fileInfo != null) {
                            break;
                        }
                    }
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
            }
        }
        if (fileInfo == null) {
            printFileInfoLog(hash);
            return null;
        }
        PhotoShareMediaItem item;
        Path path = Path.fromString("/photoshare/categorycover/" + this.mCategoryID + "/" + albumID + "/" + hash);
        DataManager dataManager = this.mApplication.getDataManager();
        synchronized (DataManager.LOCK) {
            item = (PhotoShareMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new PhotoShareCategoryCover(path, this.mApplication, fileInfo, folderType, name);
            } else {
                item.updateFromFileInfo(fileInfo);
            }
        }
        return item;
    }

    private void printFileInfoLog(String hash) {
        GalleryLog.v("PhotoShareCategoryAlbum", " getCoverMediaItem is null. hash " + hash + " mCategoryID " + this.mCategoryID);
        for (String album : this.mCategoryInfo.getAlbumList()) {
            GalleryLog.v("PhotoShareCategoryAlbum", " album  " + album);
        }
    }

    public String getName() {
        if (this.mResId != 0) {
            return this.mApplication.getResources().getString(this.mResId);
        }
        return this.mName;
    }

    public synchronized long reload() {
        if (this.mPhotoShareChangeNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        GalleryLog.printDFXLog("PhotoShareCategoryAlbum reload call for DFX");
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

    private void removeAlbum(List<TagInfo> tagInfoList, String albumName) {
        for (TagInfo tagInfo : tagInfoList) {
            if (albumName.equalsIgnoreCase(tagInfo.getTagId())) {
                tagInfoList.remove(tagInfo);
                return;
            }
        }
    }

    private MediaSet getTagAlbum(DataManager dataManager, TagInfo tagInfo) {
        MediaSet mediaSet;
        Path path = this.mPath.getChild(tagInfo.getTagId());
        synchronized (DataManager.LOCK) {
            mediaSet = (MediaSet) dataManager.peekMediaObject(path);
            if (mediaSet == null) {
                mediaSet = new PhotoShareTagAlbum(path, this.mApplication, tagInfo);
            } else {
                ((PhotoShareTagAlbum) mediaSet).updateTagInfo(tagInfo);
            }
        }
        return mediaSet;
    }

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (this.mLoadTask == future) {
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                GalleryLog.printDFXLog("mLoadBuffer is NULL for DFX");
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhotoShareCategoryAlbum.this.notifyContentChanged();
                }
            });
        }
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }
}
