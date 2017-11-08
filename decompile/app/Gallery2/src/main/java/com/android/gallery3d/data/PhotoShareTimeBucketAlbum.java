package com.android.gallery3d.data;

import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoGroup;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareTimeBucketAlbum extends PhotoShareAlbum implements IGroupAlbum {
    private final GalleryApp mApplication;
    private ArrayList<CloudShareGroupData> mGroupDatas = new ArrayList();

    public static class CloudShareGroupData extends AbsGroupData {
        public int batch;
        public long batchCreateTime;
        public String createrNickName;
        public int videoCount;
    }

    public PhotoShareTimeBucketAlbum(Path path, GalleryApp galleryApp, int mediaType, PhotoShareAlbumInfo albumInfo) {
        super(path, galleryApp, mediaType, 1, albumInfo);
        this.mApplication = galleryApp;
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1) {
            reloadGroupData();
        }
        printExcuteInfo(startTime, "CloudShareAlbum getMediaItemCount");
        return this.mCachedCount;
    }

    public ArrayList<AbsGroupData> getGroupData() {
        ArrayList<AbsGroupData> result = new ArrayList();
        result.addAll(this.mGroupDatas);
        return result;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        try {
            List<FileInfo> fileInfoList = new ArrayList();
            int end = start + count;
            while (start < end) {
                int batchCount = Math.min(SmsCheckResult.ESCT_200, end - start);
                List<FileInfo> tempList = PhotoShareUtils.getServer().getFileInfoListByGroupLimit(this.mAlbumInfo.getId(), start, batchCount);
                if (tempList != null && tempList.size() > 0) {
                    fileInfoList.addAll(tempList);
                }
                start += batchCount;
            }
            if (fileInfoList.size() > 0) {
                for (FileInfo fileInfo : fileInfoList) {
                    boolean isImage = 4 != fileInfo.getFileType();
                    list.add(PhotoShareAlbum.loadOrUpdateItem(PhotoShareAlbum.getItemPath(this.mPath, isImage, fileInfo.getHash()), fileInfo, this.mApplication, getAlbumType(), this.mAlbumInfo.getName(), isImage));
                }
            }
        } catch (RemoteException e) {
            GalleryLog.w("CloudShareAlbum", "getMediaItem e:" + e);
            PhotoShareUtils.dealRemoteException(e);
        }
        printExcuteInfo(startTime, "CloudShareAlbum getMediaItem");
        return list;
    }

    public int getBatchSize() {
        return SmsCheckResult.ESCT_200;
    }

    public List<MediaItem> getMediaItem(int start, int count, AbsGroupData groupSpec) {
        if (groupSpec instanceof CloudShareGroupData) {
            return getMediaItem(start, count, ((CloudShareGroupData) groupSpec).batch);
        }
        return new ArrayList(0);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count, int batch) {
        long startTime = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        try {
            List<FileInfo> fileInfoList = PhotoShareUtils.getServer().getFileInfoListByGroupBatchLimit(this.mAlbumInfo.getId(), batch, start, count);
            if (fileInfoList != null && fileInfoList.size() > 0) {
                for (FileInfo fileInfo : fileInfoList) {
                    boolean isImage = 4 != fileInfo.getFileType();
                    list.add(PhotoShareAlbum.loadOrUpdateItem(PhotoShareAlbum.getItemPath(this.mPath, isImage, fileInfo.getHash()), fileInfo, this.mApplication, getAlbumType(), this.mAlbumInfo.getName(), isImage));
                }
            }
        } catch (RemoteException e) {
            GalleryLog.w("CloudShareAlbum", "getMediaItem e:" + e);
            PhotoShareUtils.dealRemoteException(e);
        }
        printExcuteInfo(startTime, "CloudShareAlbum getMediaItem");
        return list;
    }

    private ArrayList<CloudShareGroupData> getCloudShareGroupData() {
        ArrayList<CloudShareGroupData> cloudShareGroupDataArrayList = new ArrayList();
        try {
            String shareId = this.mAlbumInfo.getId();
            int total = PhotoShareUtils.getServer().getFileInfoGroupListCount(shareId);
            int start = 0;
            while (start < total) {
                int count = Math.min(SmsCheckResult.ESCT_200, total - start);
                List<FileInfoGroup> fileInfoGroupList = PhotoShareUtils.getServer().getFileInfoGroupListLimit(shareId, start, count);
                if (fileInfoGroupList != null && fileInfoGroupList.size() > 0) {
                    for (FileInfoGroup fileInfoGroup : fileInfoGroupList) {
                        CloudShareGroupData cloudShareGroupData = new CloudShareGroupData();
                        cloudShareGroupData.batchCreateTime = fileInfoGroup.getBatchCtime();
                        cloudShareGroupData.dateTaken = cloudShareGroupData.batchCreateTime;
                        cloudShareGroupData.count = fileInfoGroup.getPhotoNum();
                        if (!TextUtils.isEmpty(fileInfoGroup.getCreaterNickName())) {
                            cloudShareGroupData.createrNickName = fileInfoGroup.getCreaterNickName();
                        } else if (TextUtils.isEmpty(fileInfoGroup.getCreaterAccount())) {
                            cloudShareGroupData.createrNickName = this.mAlbumInfo.getOwnerAcc();
                        } else {
                            cloudShareGroupData.createrNickName = fileInfoGroup.getCreaterAccount();
                        }
                        cloudShareGroupData.videoCount = fileInfoGroup.getVideoNum();
                        cloudShareGroupData.batch = fileInfoGroup.getBatchId();
                        if (cloudShareGroupData.batch == 0) {
                            cloudShareGroupData.isCloudHistroyData = true;
                        }
                        String formatDate = new SimpleDateFormat("yyyyMMdd").format(new Date(cloudShareGroupData.batchCreateTime));
                        if (cloudShareGroupData.batch == 0) {
                            cloudShareGroupData.defaultTitle = "";
                        } else {
                            cloudShareGroupData.defaultTitle = formatDate + "-" + formatDate;
                        }
                        cloudShareGroupDataArrayList.add(cloudShareGroupData);
                    }
                    start += count;
                }
            }
        } catch (RemoteException e) {
            GalleryLog.w("CloudShareAlbum", "getCloudShareGroupData e:" + e);
            PhotoShareUtils.dealRemoteException(e);
        }
        return cloudShareGroupDataArrayList;
    }

    private void reloadGroupData() {
        this.mGroupDatas = getCloudShareGroupData();
        int totalCount = 0;
        int videoCount = 0;
        for (CloudShareGroupData cloudShareGroupData : this.mGroupDatas) {
            totalCount += cloudShareGroupData.count;
            videoCount += cloudShareGroupData.videoCount;
        }
        this.mCachedCount = totalCount;
        this.mVideoCount = videoCount;
    }

    public int getTotalVideoCount() {
        if (this.mMediaType == 1) {
            return 0;
        }
        if (this.mVideoCount == -1) {
            reloadGroupData();
        }
        return this.mVideoCount;
    }
}
