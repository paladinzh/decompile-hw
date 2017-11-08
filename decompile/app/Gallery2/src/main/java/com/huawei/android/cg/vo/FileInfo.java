package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;

public class FileInfo implements Parcelable {
    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
    protected long addTime;
    protected String albumId;
    protected long createTime;
    protected String deviceId;
    protected int duration;
    protected String expand;
    protected int fileAttribute;
    protected String fileId;
    protected String fileName;
    protected int fileType;
    protected String fileUrl;
    protected String fyuseAttach;
    protected String hash;
    protected double latitude;
    protected String localBigThumbPath;
    protected String localRealPath;
    protected String localThumbPath;
    protected double longtitude;
    protected int orientation;
    protected int otype;
    protected long oversion;
    protected String shareId;
    protected long size;
    protected String source;
    protected String thumbUrl;
    protected String uniqueId;
    protected String videoThumbId;

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getShareId() {
        return this.shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLocalThumbPath() {
        return this.localThumbPath;
    }

    public void setLocalThumbPath(String localThumbPath) {
        this.localThumbPath = localThumbPath;
    }

    public String getLocalBigThumbPath() {
        return this.localBigThumbPath;
    }

    public void setLocalBigThumbPath(String bigThumbPath) {
        this.localBigThumbPath = bigThumbPath;
    }

    public String getLocalRealPath() {
        return this.localRealPath;
    }

    public void setLocalRealPath(String localRealPath) {
        this.localRealPath = localRealPath;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setVideoThumbId(String videoThumbId) {
        this.videoThumbId = videoThumbId;
    }

    public int getFileType() {
        return this.fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getExpand() {
        return this.expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public void setFileAttribute(int fileAttribute) {
        this.fileAttribute = fileAttribute;
    }

    private FileInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel input) {
        this.fileId = input.readString();
        this.fileName = input.readString();
        this.createTime = input.readLong();
        this.albumId = input.readString();
        this.shareId = input.readString();
        this.hash = input.readString();
        this.size = input.readLong();
        this.source = input.readString();
        this.localThumbPath = input.readString();
        this.localBigThumbPath = input.readString();
        this.localRealPath = input.readString();
        this.videoThumbId = input.readString();
        this.fileType = input.readInt();
        this.expand = input.readString();
        this.oversion = input.readLong();
        this.orientation = input.readInt();
        this.fyuseAttach = input.readString();
        this.duration = input.readInt();
        this.latitude = input.readDouble();
        this.longtitude = input.readDouble();
        if (RecycleUtils.supportRecycle()) {
            this.uniqueId = input.readString();
            if (PhotoShareUtils.isGUIDSupport()) {
                this.fileAttribute = input.readInt();
                this.deviceId = input.readString();
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileId);
        dest.writeString(this.fileName);
        dest.writeLong(this.createTime);
        GalleryLog.printDFXLog("FileInfo");
        dest.writeString(this.albumId);
        dest.writeString(this.shareId);
        dest.writeString(this.hash);
        dest.writeLong(this.size);
        dest.writeString(this.source);
        dest.writeString(this.localThumbPath);
        dest.writeString(this.localBigThumbPath);
        dest.writeString(this.localRealPath);
        dest.writeString(this.videoThumbId);
        GalleryLog.printDFXLog("FileInfo");
        dest.writeInt(this.fileType);
        dest.writeString(this.expand);
        dest.writeLong(this.oversion);
        dest.writeInt(this.orientation);
        dest.writeString(this.fyuseAttach);
        dest.writeInt(this.duration);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longtitude);
        if (RecycleUtils.supportRecycle()) {
            dest.writeString(this.uniqueId);
            if (PhotoShareUtils.isGUIDSupport()) {
                dest.writeInt(this.fileAttribute);
                dest.writeString(this.deviceId);
            }
        }
    }
}
