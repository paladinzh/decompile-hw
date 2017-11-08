package com.huawei.gallery.photoshare.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobBulk {
    private static final MyPrinter LOG = new MyPrinter("JobBulk", "DownloadServiceLogic ");
    private Set<Path> mActiveItemLists = new HashSet(100);
    private ContentResolver mContentResolver;
    private int mDownloadType;
    private Handler mHandler;
    private Set<Path> mItemsSendTOSDK = new HashSet(100);
    private Set<Path> mItemsSubmited = new HashSet(100);
    private boolean mSupportPhotoShare;
    private int mThumbType;

    public JobBulk(ContentResolver contentResolver, Looper looper, int thumbType) {
        this.mContentResolver = contentResolver;
        this.mSupportPhotoShare = PhotoShareUtils.isSupportPhotoShare();
        LOG.d("Suport Photoshare ? " + this.mSupportPhotoShare);
        if (this.mSupportPhotoShare) {
            this.mHandler = new Handler(looper) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            JobBulk.this.checkAcitveDataChanges((Set) message.obj);
                            return;
                        case 2:
                            JobBulk.this.submitNewJob((Set) message.obj);
                            return;
                        default:
                            return;
                    }
                }
            };
        }
        this.mThumbType = thumbType;
        if (thumbType == 1) {
            this.mDownloadType = 2;
        } else {
            this.mDownloadType = -1;
        }
    }

    public void beginUpdateActiveList() {
        if (this.mSupportPhotoShare) {
            this.mActiveItemLists.clear();
        }
    }

    public void addItem(Path job) {
        if (this.mSupportPhotoShare) {
            this.mActiveItemLists.add(job);
        }
    }

    public void endUpdateActiveList() {
        if (this.mSupportPhotoShare) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.obj = new HashSet(this.mActiveItemLists);
            this.mHandler.sendMessage(msg);
        }
    }

    private void checkAcitveDataChanges(Set<Path> newData) {
        if (compareSet(this.mItemsSubmited, newData)) {
            this.mItemsSubmited.clear();
            this.mItemsSubmited.addAll(newData);
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, newData), 200);
        }
    }

    private void submitNewJob(Set<Path> newData) {
        if (compareSet(this.mItemsSendTOSDK, newData)) {
            List<FileInfo> jobToDo = new ArrayList(newData.size());
            for (Path path : newData) {
                addJobToDoList(path, jobToDo);
            }
            if (!jobToDo.isEmpty()) {
                FileInfo[] data = new FileInfo[jobToDo.size()];
                jobToDo.toArray(data);
                try {
                    PhotoShareUtils.getServer().downloadPhotoThumb(data, this.mDownloadType, 1, false);
                    LOG.d("download photo thumbnail " + this.mDownloadType + ",  count: " + data.length);
                } catch (RemoteException e) {
                    LOG.w(" download photo thumb failed ." + e.getMessage());
                }
                this.mItemsSendTOSDK.clear();
                this.mItemsSendTOSDK.addAll(newData);
            }
        }
    }

    private void addJobToDoList(Path path, List<FileInfo> jobToDo) {
        if (path == null) {
            LOG.d(false, "invalid path");
            return;
        }
        MediaObject mo = path.getObject();
        if (mo instanceof GalleryMediaItem) {
            GalleryMediaItem item = (GalleryMediaItem) mo;
            if (item.getLocalMediaId() == -1) {
                FileInfo info;
                if (item.getThumbType() < this.mThumbType) {
                    info = item.getFileInfo();
                } else if (!PhotoShareUtils.isFileExists(item.getFilePath())) {
                    updateGalleryDataBase(this.mContentResolver, item);
                    info = item.getFileInfo();
                } else {
                    return;
                }
                if (info != null) {
                    LOG.d(false, "file name " + info.getFileName());
                    jobToDo.add(info);
                } else {
                    LOG.d(false, " cant get file info from item: " + item.getFilePath());
                }
            } else {
                LOG.d(false, "has local media id " + item.getLocalMediaId() + ", thumbnailType: " + item.getThumbType() + ", " + item.filePath);
            }
        } else {
            LOG.d(false, "not gallery media item " + path);
        }
    }

    private void updateGalleryDataBase(ContentResolver resolver, GalleryMediaItem item) {
        FileInfo info = item.getFileInfo();
        if (info != null) {
            String str = null;
            String str2 = null;
            String data = CloudTableOperateHelper.genDefaultFilePath(info.getAlbumId(), info.getHash());
            int thumbType = 0;
            if (PhotoShareUtils.isFileExists(info.getLocalThumbPath())) {
                str2 = info.getLocalThumbPath();
                data = str2;
                thumbType = 1;
            }
            if (PhotoShareUtils.isFileExists(info.getLocalBigThumbPath())) {
                str = info.getLocalBigThumbPath();
                data = str;
                thumbType = 2;
            }
            ContentValues cloudFileValues = new ContentValues();
            cloudFileValues.put("localBigThumbPath", str);
            cloudFileValues.put("localThumbPath", str2);
            resolver.update(StorageService.sCloudFileUri, cloudFileValues, "id = ?", new String[]{String.valueOf(item.getCloudMediaId())});
            ContentValues galleryMediaValues = new ContentValues();
            galleryMediaValues.put("localBigThumbPath", str);
            galleryMediaValues.put("localThumbPath", str2);
            galleryMediaValues.put("_data", data);
            galleryMediaValues.put("thumbType", Integer.valueOf(thumbType));
            try {
                resolver.update(GalleryMedia.URI, galleryMediaValues, "_id = ?", new String[]{String.valueOf(item.id)});
            } catch (Exception e) {
                LOG.d("update gallery media exception: " + e.getMessage());
            }
        }
    }

    private boolean compareSet(Set<Path> oldData, Set<Path> newData) {
        if (oldData.size() != newData.size()) {
            return true;
        }
        for (Path path : newData) {
            if (!oldData.contains(path)) {
                return true;
            }
        }
        return false;
    }
}
