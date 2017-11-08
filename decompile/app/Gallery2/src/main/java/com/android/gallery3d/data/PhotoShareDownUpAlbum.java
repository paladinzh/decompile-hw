package com.android.gallery3d.data;

import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareDownUpAlbum extends MediaSet {
    private final GalleryApp mApplication;
    private int mCachedCount = -1;
    private final boolean mIsDownLoad;
    private final int mParameterType;
    private final PhotoShareDownUpNotifier mPhotoShareDownUpNotifier;
    private final int mType;

    public PhotoShareDownUpAlbum(Path path, GalleryApp app) {
        super(path, MediaObject.nextVersionNumber());
        this.mType = Integer.valueOf(path.getSuffix()).intValue();
        this.mParameterType = getDataType(this.mType);
        this.mIsDownLoad = isDownload(Integer.valueOf(path.getSuffix()).intValue());
        this.mPhotoShareDownUpNotifier = new PhotoShareDownUpNotifier(this);
        this.mApplication = app;
    }

    private boolean isDownload(int albumType) {
        return 1 == albumType || 2 == albumType;
    }

    public static int getDataType(int albumType) {
        switch (albumType) {
            case 1:
                return 47;
            case 2:
                return 16;
            case 3:
                return 47;
            case 4:
                return 16;
            default:
                return 0;
        }
    }

    public String getName() {
        return null;
    }

    public long reload() {
        if (this.mPhotoShareDownUpNotifier.isDirty()) {
            this.mCachedCount = -1;
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public int getMediaItemCount() {
        if (-1 == this.mCachedCount) {
            try {
                if (this.mIsDownLoad) {
                    this.mCachedCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(this.mParameterType);
                } else {
                    this.mCachedCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(this.mParameterType);
                }
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
                this.mCachedCount = 0;
            } catch (Exception e2) {
                GalleryLog.i("PhotoShareDownUpAlbum", "getMediaItemCount() failed." + e2.getMessage());
                this.mCachedCount = 0;
            }
        }
        return this.mCachedCount;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> list = new ArrayList();
        List<FileInfoDetail> data = getItemInfo(start, count);
        if (data.size() == 0) {
            return list;
        }
        for (int i = 0; i < data.size(); i++) {
            FileInfoDetail itemInfo = (FileInfoDetail) data.get(i);
            list.add(loadOrUpdateItem(getItemPath(TextUtils.isEmpty(itemInfo.getAlbumId()) ? itemInfo.getShareId() : itemInfo.getAlbumId(), itemInfo.getHash(), itemInfo.getUniqueId()), itemInfo));
        }
        return list;
    }

    private Path getItemPath(String albumId, String itemId, String uniqueId) {
        if (this.mType != 1 && this.mType != 2) {
            return Path.fromString("/photoshare/up/").getChild(albumId).getChild(itemId);
        }
        if (PhotoShareUtils.isGUIDSupport()) {
            return Path.fromString("/photoshare/guid/down/").getChild(uniqueId);
        }
        return Path.fromString("/photoshare/down/").getChild(albumId).getChild(itemId);
    }

    private MediaItem loadOrUpdateItem(Path path, FileInfoDetail itemInfo) {
        PhotoShareDownUpItem item;
        DataManager dataManager = this.mApplication.getDataManager();
        synchronized (DataManager.LOCK) {
            item = (PhotoShareDownUpItem) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new PhotoShareDownUpItem(path, this.mIsDownLoad, itemInfo, this.mApplication);
            } else {
                item.updateFileInfo(itemInfo);
            }
        }
        return item;
    }

    private List<FileInfoDetail> getItemInfo(int start, int count) {
        List<FileInfoDetail> result = new ArrayList();
        try {
            List<FileInfoDetail> tempList;
            int batch = count / SmsCheckResult.ESCT_200;
            for (int i = 0; i < batch; i++) {
                if (this.mIsDownLoad) {
                    tempList = PhotoShareUtils.getServer().getDownloadFileInfoListLimit(this.mParameterType, start, SmsCheckResult.ESCT_200);
                } else {
                    tempList = PhotoShareUtils.getServer().getUploadFileInfoListLimit(this.mParameterType, start, SmsCheckResult.ESCT_200);
                }
                if (tempList != null) {
                    result.addAll(tempList);
                }
                start += SmsCheckResult.ESCT_200;
            }
            int left = count % SmsCheckResult.ESCT_200;
            if (left > 0) {
                if (this.mIsDownLoad) {
                    tempList = PhotoShareUtils.getServer().getDownloadFileInfoListLimit(this.mParameterType, start, left);
                } else {
                    tempList = PhotoShareUtils.getServer().getUploadFileInfoListLimit(this.mParameterType, start, left);
                }
                if (tempList != null) {
                    result.addAll(tempList);
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        } catch (Exception e2) {
            GalleryLog.i("PhotoShareDownUpAlbum", "An exception has occurred in getItemInfo() method." + e2.getMessage());
        }
        return result;
    }
}
