package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.hardware.usb.UsbDevice;
import android.mtp.MtpObjectInfo;
import android.mtp.MtpStorageInfo;
import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.List;

@TargetApi(12)
public class MtpDevice extends MediaSet {
    private final GalleryApp mApplication;
    private final int mDeviceId;
    private final String mDeviceName;
    private final Path mItemPath;
    private List<MtpObjectInfo> mJpegChildren;
    private final MtpContext mMtpContext;
    private final String mName;
    private final ChangeNotifier mNotifier;

    public MtpDevice(Path path, GalleryApp application, int deviceId, String name, MtpContext mtpContext) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mDeviceId = deviceId;
        this.mDeviceName = UsbDevice.getDeviceName(deviceId);
        this.mMtpContext = mtpContext;
        this.mName = name;
        this.mNotifier = new ChangeNotifier((MediaSet) this, Uri.parse("mtp://"), application);
        this.mItemPath = Path.fromString("/mtp/item/" + String.valueOf(deviceId));
        this.mJpegChildren = new ArrayList();
    }

    public MtpDevice(Path path, GalleryApp application, int deviceId, MtpContext mtpContext) {
        this(path, application, deviceId, MtpDeviceSet.getDeviceName(mtpContext, deviceId), mtpContext);
    }

    private List<MtpObjectInfo> loadItems() {
        ArrayList<MtpObjectInfo> result = new ArrayList();
        List<MtpStorageInfo> storageList = this.mMtpContext.getMtpClient().getStorageList(this.mDeviceName);
        if (storageList == null) {
            return result;
        }
        for (MtpStorageInfo info : storageList) {
            collectJpegChildren(info.getStorageId(), 0, result);
        }
        return result;
    }

    private void collectJpegChildren(int storageId, int objectId, ArrayList<MtpObjectInfo> result) {
        ArrayList<MtpObjectInfo> dirChildren = new ArrayList();
        queryChildren(storageId, objectId, result, dirChildren);
        int n = dirChildren.size();
        for (int i = 0; i < n; i++) {
            collectJpegChildren(storageId, ((MtpObjectInfo) dirChildren.get(i)).getObjectHandle(), result);
        }
    }

    private void queryChildren(int storageId, int objectId, ArrayList<MtpObjectInfo> jpeg, ArrayList<MtpObjectInfo> dir) {
        List<MtpObjectInfo> children = this.mMtpContext.getMtpClient().getObjectList(this.mDeviceName, storageId, objectId);
        if (children != null) {
            for (MtpObjectInfo obj : children) {
                int format = obj.getFormat();
                switch (format) {
                    case 12289:
                        dir.add(obj);
                        break;
                    case 14337:
                    case 14344:
                        jpeg.add(obj);
                        break;
                    default:
                        GalleryLog.w("MtpDevice", "other type: name = " + obj.getName() + ", format = " + format);
                        break;
                }
            }
        }
    }

    public static MtpObjectInfo getObjectInfo(MtpContext mtpContext, int deviceId, int objectId) {
        return mtpContext.getMtpClient().getObjectInfo(UsbDevice.getDeviceName(deviceId), objectId);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> result = new ArrayList();
        int begin = start;
        int end = Math.min(start + count, this.mJpegChildren.size());
        DataManager dataManager = this.mApplication.getDataManager();
        for (int i = start; i < end; i++) {
            MtpObjectInfo child = (MtpObjectInfo) this.mJpegChildren.get(i);
            Path childPath = this.mItemPath.getChild(child.getObjectHandle());
            synchronized (DataManager.LOCK) {
                MtpImage image = (MtpImage) dataManager.peekMediaObject(childPath);
                if (image == null) {
                    image = new MtpImage(childPath, this.mApplication, this.mDeviceId, child, this.mMtpContext);
                } else {
                    image.updateContent(child);
                }
                result.add(image);
            }
        }
        return result;
    }

    public int getMediaItemCount() {
        return this.mJpegChildren.size();
    }

    public String getName() {
        return this.mName;
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mJpegChildren = loadItems();
        }
        return this.mDataVersion;
    }

    public int getSupportedOperations() {
        return 3072;
    }

    public boolean Import() {
        return this.mMtpContext.copyAlbum(this.mDeviceName, this.mName, this.mJpegChildren);
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
