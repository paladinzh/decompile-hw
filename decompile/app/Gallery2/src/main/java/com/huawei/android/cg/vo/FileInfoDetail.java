package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;

public class FileInfoDetail extends FileInfo {
    public static final Creator<FileInfoDetail> CREATOR = new Creator<FileInfoDetail>() {
        public FileInfoDetail createFromParcel(Parcel source) {
            return new FileInfoDetail(source);
        }

        public FileInfoDetail[] newArray(int size) {
            return new FileInfoDetail[size];
        }
    };
    private long addTime;
    private String ext1;
    private String ext2;
    private String ext3;
    private int fileStatus;
    private long finishTime;
    private long sizeProgress;

    public long getAddTime() {
        return this.addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public long getFinishTime() {
        return this.finishTime;
    }

    public int getFileStatus() {
        return this.fileStatus;
    }

    public String toString() {
        return "FileInfo [fileID=" + this.fileId + ", fileName=" + this.fileName + ", createTime=" + this.createTime + ", albumID=" + this.albumId + ", shareID=" + this.shareId + ", hash=" + this.hash + ", size=" + this.size + ", source=" + this.source + ", localThumbPath=" + this.localThumbPath + ", localBigThumbPath=" + this.localBigThumbPath + ", localRealPath=" + this.localRealPath + ", fileUrl=" + this.fileUrl + ", thumbUrl=" + this.thumbUrl + ", videoThumbId=" + this.videoThumbId + ", fileType =" + this.fileType + ", expand=" + this.expand + ", oversion=" + this.oversion + ", otype=" + this.otype + ", addTime=" + this.addTime + ", finishTime=" + this.finishTime + ", fileStatus=" + this.fileStatus + ", sizeProgress=" + this.sizeProgress + ", ext1=" + this.ext1 + ", ext2=" + this.ext2 + ", ext3=" + this.ext3 + "]";
    }

    private FileInfoDetail(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.fileId = in.readString();
        this.fileName = in.readString();
        this.createTime = in.readLong();
        this.albumId = in.readString();
        this.shareId = in.readString();
        this.hash = in.readString();
        this.size = in.readLong();
        this.source = in.readString();
        this.localThumbPath = in.readString();
        this.localBigThumbPath = in.readString();
        this.localRealPath = in.readString();
        this.videoThumbId = in.readString();
        this.fileType = in.readInt();
        this.expand = in.readString();
        this.oversion = in.readLong();
        this.addTime = in.readLong();
        this.finishTime = in.readLong();
        this.fileStatus = in.readInt();
        this.sizeProgress = in.readLong();
        this.ext1 = in.readString();
        this.ext2 = in.readString();
        this.ext3 = in.readString();
        if (RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport()) {
            this.uniqueId = in.readString();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileId);
        dest.writeString(this.fileName);
        dest.writeLong(this.createTime);
        dest.writeString(this.albumId);
        dest.writeString(this.shareId);
        dest.writeString(this.hash);
        GalleryLog.printDFXLog("FileInfoDetail");
        dest.writeLong(this.size);
        dest.writeString(this.source);
        dest.writeString(this.localThumbPath);
        dest.writeString(this.localBigThumbPath);
        dest.writeString(this.localRealPath);
        dest.writeString(this.videoThumbId);
        dest.writeInt(this.fileType);
        dest.writeString(this.expand);
        GalleryLog.printDFXLog("FileInfoDetail");
        dest.writeLong(this.oversion);
        dest.writeLong(this.addTime);
        dest.writeLong(this.finishTime);
        dest.writeInt(this.fileStatus);
        dest.writeLong(this.sizeProgress);
        dest.writeString(this.ext1);
        dest.writeString(this.ext2);
        dest.writeString(this.ext3);
        if (RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport()) {
            dest.writeString(this.uniqueId);
        }
    }
}
