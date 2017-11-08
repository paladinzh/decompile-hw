package com.android.gallery3d.data;

import android.content.Context;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoShareAlbum extends MediaSet {
    protected PhotoShareAlbumInfo mAlbumInfo;
    private int mAlbumType;
    private final GalleryApp mApplication;
    protected int mCachedCount = -1;
    protected final int mMediaType;
    private String mName;
    private int mNewPictureCount = 0;
    protected PhotoShareChangeNotifier mPhotoShareChangeNotifier;
    private int mPreViewCount = 0;
    private int mResId;
    private final int mStateType;
    protected int mVideoCount = -1;

    public PhotoShareAlbum(Path path, GalleryApp galleryApp, int mediaType, int stateType, PhotoShareAlbumInfo albumInfo) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = galleryApp;
        this.mAlbumInfo = albumInfo;
        this.mResId = PhotoShareUtils.getResId(this.mAlbumInfo.getId());
        this.mName = this.mAlbumInfo.getName();
        this.mMediaType = mediaType;
        this.mStateType = stateType;
        this.mPhotoShareChangeNotifier = new PhotoShareChangeNotifier(this, 1);
    }

    private int convertMediaType(int type) {
        switch (type) {
            case 1:
                return 10;
            case 2:
                return 4;
            default:
                return 0;
        }
    }

    protected static Path getItemPath(Path parent, boolean isImage, String itemId) {
        String itemPath;
        String albumId = parent.getSuffix();
        if (isImage) {
            itemPath = "/photoshare/item/image/*/*";
        } else {
            itemPath = "/photoshare/item/video/*/*";
        }
        return Path.fromString(itemPath.replace("*/*", albumId + "/" + itemId));
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        ArrayList<FileInfo> fileInfoList = new ArrayList();
        if (start < this.mPreViewCount) {
            List<FileInfo> preItems = getAlbumInfo().getPreMediaItems(convertMediaType(this.mMediaType), start, start + count <= this.mPreViewCount ? count : this.mPreViewCount - start);
            if (!(preItems == null || preItems.isEmpty())) {
                fileInfoList.addAll(preItems);
                count -= preItems.size();
            }
            start = 0;
        } else {
            start -= this.mPreViewCount;
        }
        if (count > 0) {
            List<FileInfo> fileItems = getAlbumInfo().getMediaItems(convertMediaType(this.mMediaType), start, count);
            if (!(fileItems == null || fileItems.isEmpty())) {
                fileInfoList.addAll(fileItems);
            }
        }
        if (fileInfoList.isEmpty()) {
            return list;
        }
        for (int i = 0; i < fileInfoList.size(); i++) {
            FileInfo fileInfo = (FileInfo) fileInfoList.get(i);
            boolean isImage = 4 != fileInfo.getFileType();
            list.add(loadOrUpdateItem(getItemPath(this.mPath, isImage, fileInfo.getHash()), fileInfo, this.mApplication, this.mAlbumType, this.mAlbumInfo.getName(), isImage));
        }
        printExcuteInfo(startTime, "getMediaItem");
        return list;
    }

    public void setAlbumType(int albumType) {
        this.mAlbumType = albumType;
    }

    public int getAlbumType() {
        return this.mAlbumType;
    }

    protected static MediaItem loadOrUpdateItem(Path path, FileInfo fileInfo, GalleryApp app, int folderType, String albumName, boolean isImage) {
        PhotoShareMediaItem item;
        DataManager dataManager = app.getDataManager();
        synchronized (DataManager.LOCK) {
            item = (PhotoShareMediaItem) dataManager.peekMediaObject(path);
            if (item != null) {
                item.updateFromFileInfo(fileInfo);
            } else if (isImage) {
                item = new PhotoShareImage(path, app, fileInfo, folderType, albumName);
            } else {
                item = new PhotoShareVideo(path, app, fileInfo, folderType, albumName);
            }
        }
        return item;
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mStateType == 2 && this.mPhotoShareChangeNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mCachedCount = -1;
            this.mVideoCount = -1;
        }
        if (this.mCachedCount == -1) {
            if (this.mStateType == 1) {
                this.mPreViewCount = getAlbumInfo().getPreItemCount(convertMediaType(this.mMediaType));
            } else {
                this.mPreViewCount = 0;
            }
            this.mCachedCount = getAlbumInfo().getItemCount(convertMediaType(this.mMediaType)) + this.mPreViewCount;
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public long reload() {
        if (this.mPhotoShareChangeNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mCachedCount = -1;
            this.mVideoCount = -1;
        }
        return this.mDataVersion;
    }

    public String getName() {
        if (this.mResId != 0) {
            return this.mApplication.getResources().getString(this.mResId);
        }
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        this.mAlbumInfo.delete();
    }

    public void editPhotoShare(Context context) {
    }

    public PhotoShareAlbumInfo getAlbumInfo() {
        return this.mAlbumInfo;
    }

    public void setAlbumInfo(PhotoShareAlbumInfo albumInfo) {
        this.mAlbumInfo = albumInfo;
        this.mName = this.mAlbumInfo.getName();
    }

    public boolean isEmptyAlbum() {
        return getMediaItemCount() == 0;
    }

    public int getNewPictureCount() {
        return this.mNewPictureCount;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public int getPreViewCount() {
        return this.mPreViewCount;
    }

    public int getTotalVideoCount() {
        if (this.mMediaType == 1) {
            return 0;
        }
        if (this.mVideoCount == -1) {
            this.mVideoCount = getAlbumInfo().getItemCount(4);
            if (this.mStateType == 1) {
                this.mVideoCount += getAlbumInfo().getPreItemCount(4);
            }
        }
        return this.mVideoCount;
    }

    public static String getLocalRealPath(String name, FileInfo fileInfo) {
        if (!TextUtils.isEmpty(fileInfo.getLocalRealPath())) {
            File file = new File(fileInfo.getLocalRealPath());
            if (file.exists() && fileInfo.getSize() == file.length()) {
                return fileInfo.getLocalRealPath();
            }
        }
        if ("default-album-1".equalsIgnoreCase(name)) {
            if (isFileExists(PhotoShareUtils.INNER_CAMERA_PATH, fileInfo)) {
                return fileInfo.getLocalRealPath();
            }
            for (GalleryStorage galleryStorage : GalleryStorageManager.getInstance().getOuterGalleryStorageList()) {
                if (isFileExists(galleryStorage.getPath() + Constant.CAMERA_PATH, fileInfo)) {
                    return fileInfo.getLocalRealPath();
                }
            }
        } else if ("default-album-2".equalsIgnoreCase(name)) {
            if (isFileExists(PhotoShareUtils.INNER_SCREEN_SHOT_PATH, fileInfo)) {
                return fileInfo.getLocalRealPath();
            }
            for (GalleryStorage galleryStorage2 : GalleryStorageManager.getInstance().getOuterGalleryStorageList()) {
                if (isFileExists(galleryStorage2.getPath() + "/Pictures/Screenshots", fileInfo)) {
                    return fileInfo.getLocalRealPath();
                }
            }
        } else if (isFileExists(PhotoShareUtils.PHOTOSHARE_DOWNLOAD_PATH + "/" + name, fileInfo)) {
            return fileInfo.getLocalRealPath();
        }
        return null;
    }

    private static boolean isFileExists(String directory, FileInfo fileInfo) {
        if (directory == null) {
            return false;
        }
        String path = directory + "/" + fileInfo.getFileName();
        File file = new File(path);
        if (!file.exists() || fileInfo.getSize() != file.length()) {
            return false;
        }
        fileInfo.setLocalRealPath(path);
        return true;
    }

    public MediaDetails getDetails() {
        MediaDetails details = new MediaDetails();
        details.addDetail(1, getName());
        details.addDetail(150, Integer.valueOf(getMediaItemCount()));
        long localSize = (0 + PhotoShareUtils.getFileSize(new File(PhotoShareUtils.PHOTOSHARE_LCD_PATH + "/" + this.mAlbumInfo.getId()))) + PhotoShareUtils.getFileSize(new File(PhotoShareUtils.PHOTOSHARE_THUMB_PATH + "/" + this.mAlbumInfo.getId()));
        details.addDetail(300, new long[]{this.mAlbumInfo.getCloudSize(), localSize});
        return details;
    }
}
