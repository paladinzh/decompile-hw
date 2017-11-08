package com.android.gallery3d.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaFile;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareVoHelper;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class PhotoShareMediaItem extends MediaItem {
    protected boolean isPreViewItem = false;
    protected boolean isUploadFailItem = false;
    protected double latitude = 0.0d;
    protected double longitude = 0.0d;
    protected String mAlbumName;
    protected GalleryApp mApplication;
    protected String mCreateID = null;
    protected long mDateTakenInMs;
    private long mFileCreateTime;
    protected FileInfo mFileInfo;
    protected String mFilePath;
    private int mFilePathType;
    protected long mFileSize;
    protected int mFolderType;
    protected int mHeight;
    protected String mName;
    protected String mOwnerID = null;
    protected int mRotation = 0;
    protected int mWidth;
    protected String mimeType;

    protected static class LastModifyInfo {
        protected String filePath;
        protected long timeModified;

        protected LastModifyInfo() {
        }
    }

    public abstract boolean isThumbNail();

    protected abstract void setFileSizeAndLastModify();

    protected abstract void setPhotoSharePreView();

    public PhotoShareMediaItem(Path path, GalleryApp app, FileInfo fileInfo, int folderType, String albumName) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = app;
        this.mFileInfo = fileInfo;
        this.mFolderType = folderType;
        this.mAlbumName = albumName;
        setName(fileInfo.getFileName());
        this.mFileCreateTime = fileInfo.getCreateTime();
        this.mFilePath = getLocalFilePath(this.mAlbumName, this.mFileInfo);
        this.mimeType = MediaFile.getMimeTypeForFile(this.mFilePath);
        setPhotoSharePreView();
        this.mRotation = 0;
        if (isPhotoSharePreView()) {
            this.mRotation = PhotoShareUtils.getRotationFromExif(this.mFilePath);
        }
        parseExpand(this.mFileInfo.getExpand());
        setFileSizeAndLastModify();
    }

    protected void setLocation(String location) {
        this.mFilePath = location;
        this.mimeType = MediaFile.getMimeTypeForFile(this.mFilePath);
    }

    protected void parseExpand(String expand) {
        if (!TextUtils.isEmpty(expand)) {
            try {
                JSONObject expandJson = new JSONObject(expand);
                if (expandJson.has("position")) {
                    JSONObject positionJson = new JSONObject(expandJson.getString("position"));
                    if (positionJson.has("y")) {
                        this.longitude = positionJson.getDouble("y");
                    }
                    if (positionJson.has("x")) {
                        this.latitude = positionJson.getDouble("x");
                    }
                }
                if (expandJson.has("rotate")) {
                    this.mRotation = PhotoShareUtils.getOrientation(Integer.parseInt(expandJson.getString("rotate")));
                }
                if (expandJson.has("createrId")) {
                    this.mCreateID = expandJson.getString("createrId");
                }
                if (expandJson.has("ownerId")) {
                    this.mOwnerID = expandJson.getString("ownerId");
                }
                if (expandJson.has("fileStatus")) {
                    this.isUploadFailItem = expandJson.getString("fileStatus").equals(String.valueOf(32));
                } else {
                    this.isUploadFailItem = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getLocalFilePath(String albumName, FileInfo fileInfo) {
        String realPath = PhotoShareAlbum.getLocalRealPath(albumName, fileInfo);
        if (realPath != null) {
            this.mFilePathType = 1;
            return realPath;
        } else if (PhotoShareUtils.isFilePathValid(fileInfo.getLocalBigThumbPath())) {
            this.mFilePathType = 2;
            return fileInfo.getLocalBigThumbPath();
        } else if (!PhotoShareUtils.isFilePathValid(fileInfo.getLocalThumbPath())) {
            return "";
        } else {
            this.mFilePathType = 3;
            return fileInfo.getLocalThumbPath();
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public boolean deletePhotoShareFile() {
        GalleryUtils.assertNotInRenderThread();
        ArrayList<FileInfo> deleteList = new ArrayList();
        deleteList.add(this.mFileInfo);
        List result = null;
        try {
            if (isPhotoSharePreView()) {
                PhotoShareUtils.getServer().deleteUploadHistory(new FileInfoDetail[]{PhotoShareVoHelper.getFileInfoDetail(this.mFileInfo)});
                PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                PhotoShareUtils.updateNotify();
                PhotoShareUtils.refreshStatusBar(false);
            } else if (1 != this.mFolderType) {
                if (!(3 == this.mFolderType || 2 == this.mFolderType)) {
                    if (7 == this.mFolderType) {
                    }
                }
                result = PhotoShareUtils.getServer().deleteShareFile(this.mPath.getParent().getSuffix(), deleteList);
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (result == null || result.size() == 0) {
            GalleryLog.v("PhotoShareMediaItem", "delete file " + this.mName + " success");
            return true;
        }
        new Handler(this.mApplication.getAndroidContext().getMainLooper()).post(new Runnable() {
            public void run() {
                ContextedUtils.showToastQuickly(PhotoShareMediaItem.this.mApplication.getAndroidContext(), String.format(PhotoShareMediaItem.this.mApplication.getResources().getQuantityString(R.plurals.photoshare_toast_delete_file_fail, 1), new Object[]{PhotoShareMediaItem.this.mApplication.getResources().getString(R.string.photoshare_toast_fail_common_Toast)}), 0);
            }
        });
        GalleryLog.v("PhotoShareMediaItem", "delete file " + this.mName + " failed");
        return false;
    }

    public int getFolderType() {
        return this.mFolderType;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public long getSize() {
        return this.mFileSize;
    }

    public long getDateInMs() {
        return this.mDateTakenInMs;
    }

    public String getName() {
        return this.mName;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Uri getContentUri() {
        if (canShare()) {
            return Uri.fromFile(new File(this.mFilePath));
        }
        return null;
    }

    public long getDateModifiedInSec() {
        return this.mDateTakenInMs;
    }

    public Bitmap getScreenNailBitmap(int type) {
        if (type != 1) {
            return null;
        }
        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
        try {
            if (this.mApplication.getImageCacheService().getImageData(this.mPath, getLastModifyInfo().timeModified, type, buffer)) {
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                if (this.mWidth == 0 || this.mHeight == 0) {
                    updateWidthAndHeight();
                }
                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(buffer.data, buffer.offset, buffer.length, options);
                return decodeByteArray;
            }
            MediaItem.getBytesBufferPool().recycle(buffer);
            return null;
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
    }

    public MediaDetails getDetails() {
        MediaDetails details = new MediaDetails();
        details.addDetail(1, this.mName);
        details.addDetail(3, Long.valueOf(this.mFileCreateTime));
        if (this.mFileSize > 0) {
            details.addDetail(5, Long.valueOf(this.mFileSize));
        }
        if (!(this.mWidth == 0 || this.mHeight == 0)) {
            details.addDetail(6, Integer.valueOf(this.mWidth));
            details.addDetail(7, Integer.valueOf(this.mHeight));
        }
        if (!(!PhotoShareUtils.isFileExists(this.mFilePath) || this.mFilePath.equals(this.mFileInfo.getLocalThumbPath()) || this.mFilePath.equals(this.mFileInfo.getLocalBigThumbPath()))) {
            details.addDetail(SmsCheckResult.ESCT_200, this.mFilePath);
        }
        return details;
    }

    protected void setName(String name) {
        if (TextUtils.isEmpty(name) || name.lastIndexOf(".") == -1) {
            this.mName = name;
        } else {
            this.mName = name.substring(0, name.lastIndexOf("."));
        }
    }

    public FileInfo getFileInfo() {
        return this.mFileInfo;
    }

    protected void setFileInfo(FileInfo fileInfo) {
        if (this.mFileInfo.getFileId() == null && fileInfo.getFileId() != null) {
            updateVersion();
        }
        this.mFileInfo = fileInfo;
    }

    protected void updateVersion() {
        this.mDataVersion = MediaObject.nextVersionNumber();
    }

    protected void updateWidthAndHeight() {
    }

    public boolean photoShareDownLoadOperation(Context context, boolean canUseMobileNetWork) {
        FileInfo[] downLoadList = new FileInfo[]{this.mFileInfo};
        int addDownLoadResult = 1;
        try {
            GalleryLog.v("PhotoShareMediaItem", "photoShareDownLoadOperation  DownLoad getFileName = " + this.mFileInfo.getFileName() + " , " + "type = ORIGIN");
            if (1 == this.mFolderType) {
                addDownLoadResult = PhotoShareUtils.getServer().downloadPhotoThumb(downLoadList, 0, 1, canUseMobileNetWork);
            } else {
                addDownLoadResult = PhotoShareUtils.getServer().downloadSharePhotoThumb(downLoadList, 0, 1, canUseMobileNetWork);
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (addDownLoadResult != 0) {
            ContextedUtils.showToastQuickly(this.mApplication.getAndroidContext(), (int) R.string.photoshare_toast_nonetwork, 0);
        }
        if (addDownLoadResult == 0) {
            return true;
        }
        return false;
    }

    public boolean isPhotoSharePreView() {
        return this.isPreViewItem;
    }

    public boolean isPhotoShareUploadFailItem() {
        return this.isUploadFailItem;
    }

    public File getDestinationDirectory() {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null && "default-album-1".equalsIgnoreCase(this.mAlbumName)) {
            return new File(innerGalleryStorage.getPath() + Constant.CAMERA_PATH);
        }
        if (innerGalleryStorage == null || !"default-album-2".equalsIgnoreCase(this.mAlbumName)) {
            return new File(PhotoShareUtils.PHOTOSHARE_DOWNLOAD_PATH + "/" + this.mAlbumName);
        }
        return new File(innerGalleryStorage.getPath() + "/Pictures/Screenshots");
    }

    protected LastModifyInfo getLastModifyInfo() {
        LastModifyInfo info = new LastModifyInfo();
        if (PhotoShareUtils.isFileExists(this.mFilePath)) {
            info.filePath = this.mFilePath;
        } else if (PhotoShareUtils.isFileExists(this.mFileInfo.getLocalRealPath())) {
            info.filePath = this.mFileInfo.getLocalRealPath();
        } else if (PhotoShareUtils.isFileExists(this.mFileInfo.getLocalBigThumbPath())) {
            info.filePath = this.mFileInfo.getLocalBigThumbPath();
        } else {
            info.filePath = this.mFileInfo.getLocalThumbPath();
        }
        if (info.filePath == null) {
            info.filePath = "";
        }
        File file = new File(info.filePath);
        if (file.exists()) {
            info.timeModified = file.lastModified() + ((long) this.mFilePathType);
        } else {
            info.timeModified = 0;
        }
        return info;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = this.latitude;
        latLong[1] = this.longitude;
    }

    public void updateFromFileInfo(FileInfo fileInfo) {
        setName(fileInfo.getFileName());
        setFileInfo(fileInfo);
        boolean oldIsPreViewItem = this.isPreViewItem;
        boolean oldIsUploadFailItem = this.isUploadFailItem;
        setPhotoSharePreView();
        String filePath = getLocalFilePath(this.mAlbumName, fileInfo);
        parseExpand(fileInfo.getExpand());
        if (this.mFilePath.equalsIgnoreCase(filePath) && oldIsPreViewItem == this.isPreViewItem) {
            if (oldIsUploadFailItem != this.isUploadFailItem) {
            }
            setFileSizeAndLastModify();
        }
        updateVersion();
        setLocation(filePath);
        updateWidthAndHeight();
        setFileSizeAndLastModify();
    }

    public boolean isCloudPlaceholder() {
        if (TextUtils.isEmpty(this.mFilePath)) {
            return true;
        }
        return false;
    }

    public int getFilePathType() {
        return this.mFilePathType;
    }

    public boolean isLCDDownloaded() {
        return canShare();
    }
}
