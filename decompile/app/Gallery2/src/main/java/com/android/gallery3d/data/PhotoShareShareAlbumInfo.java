package com.android.gallery3d.data;

import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareShareAlbumInfo extends PhotoShareAlbumInfo {
    private final ShareInfo mShareInfo;

    public PhotoShareShareAlbumInfo(ShareInfo shareInfo) {
        this.mShareInfo = shareInfo;
    }

    public String getName() {
        return this.mShareInfo.getShareName();
    }

    public String getId() {
        return this.mShareInfo.getShareId();
    }

    public boolean delete() {
        int result = 1;
        try {
            result = PhotoShareUtils.getServer().deleteShareAlbum(this.mShareInfo.getShareId());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        PhotoShareUtils.notifyPhotoShareFolderChanged(1);
        if (result == 0) {
            return true;
        }
        return false;
    }

    public int addFileToAlbum(String[] fileNames) {
        int result = 1;
        try {
            result = PhotoShareUtils.getServer().addFileToShare(this.mShareInfo.getShareId(), fileNames);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public int modifyName(String name) {
        int result = 1;
        try {
            result = PhotoShareUtils.getServer().modifyShareAlbum(this.mShareInfo.getShareId(), name);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public String getOwnerAcc() {
        if (this.mShareInfo.getType() == 2) {
            try {
                List<ShareReceiver> list = PhotoShareUtils.getServer().getAlbumLocalHeadPic(this.mShareInfo.getShareId());
                if (list == null || list.size() == 0) {
                    return this.mShareInfo.getOwnerAcc();
                }
                ShareReceiver owner = null;
                for (ShareReceiver receiver : list) {
                    if (receiver.getReceiverId().equals(this.mShareInfo.getOwnerId())) {
                        owner = receiver;
                        break;
                    }
                }
                if (owner != null) {
                    String nickName = PhotoShareUtils.getValueFromJson(owner.getShareId(), "receiverName");
                    if (!TextUtils.isEmpty(nickName)) {
                        return nickName;
                    }
                }
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        }
        return this.mShareInfo.getOwnerAcc();
    }

    public int getItemCount(int fileType) {
        int result = 0;
        try {
            result = PhotoShareUtils.getServer().getShareFileInfoListLimitCount(this.mShareInfo.getShareId(), fileType);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public List<FileInfo> getMediaItems(int fileType, int start, int count) {
        List<FileInfo> result = new ArrayList();
        try {
            List<FileInfo> tempList;
            int batch = count / SmsCheckResult.ESCT_200;
            for (int i = 0; i < batch; i++) {
                tempList = PhotoShareUtils.getServer().getShareFileInfoListLimit(this.mShareInfo.getShareId(), fileType, start, SmsCheckResult.ESCT_200);
                if (tempList != null) {
                    result.addAll(tempList);
                }
                start += SmsCheckResult.ESCT_200;
            }
            int left = count % SmsCheckResult.ESCT_200;
            if (left > 0) {
                tempList = PhotoShareUtils.getServer().getShareFileInfoListLimit(this.mShareInfo.getShareId(), fileType, start, left);
                if (tempList != null) {
                    result.addAll(tempList);
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public int getReceiverCount() {
        List<ShareReceiver> receiverList = this.mShareInfo.getReceiverList();
        if (receiverList == null) {
            return 0;
        }
        int count = 0;
        for (ShareReceiver receiver : receiverList) {
            if (receiver.getStatus() != 2) {
                count++;
            }
        }
        return count;
    }

    public int getPreItemCount(int fileType) {
        int result = 0;
        try {
            result = PhotoShareUtils.getServer().getSharePreFileInfoListLimitCount(this.mShareInfo.getShareId(), fileType);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public List<FileInfo> getPreMediaItems(int fileType, int start, int count) {
        List<FileInfo> result = new ArrayList();
        try {
            List<FileInfo> tempList;
            int batch = count / SmsCheckResult.ESCT_200;
            for (int i = 0; i < batch; i++) {
                tempList = PhotoShareUtils.getServer().getSharePreFileInfoListLimit(this.mShareInfo.getShareId(), fileType, start, SmsCheckResult.ESCT_200);
                if (tempList != null) {
                    result.addAll(tempList);
                }
                start += SmsCheckResult.ESCT_200;
            }
            int left = count % SmsCheckResult.ESCT_200;
            if (left > 0) {
                tempList = PhotoShareUtils.getServer().getSharePreFileInfoListLimit(this.mShareInfo.getShareId(), fileType, start, left);
                if (tempList != null) {
                    result.addAll(tempList);
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    public long getCloudSize() {
        long size = 0;
        try {
            size = PhotoShareUtils.getServer().getShareFileInfoTotalSize(this.mShareInfo.getShareId());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return size;
    }

    public ShareInfo getShareInfo() {
        return this.mShareInfo;
    }
}
