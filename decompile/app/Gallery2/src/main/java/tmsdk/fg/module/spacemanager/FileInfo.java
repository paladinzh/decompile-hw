package tmsdk.fg.module.spacemanager;

import android.text.TextUtils;
import java.io.File;
import tmsdk.common.tcc.QFile;

/* compiled from: Unknown */
public class FileInfo implements Cloneable {
    public static final int TYPE_BIGFILE = 3;
    public static final int TYPE_RADIO = 1;
    public static final int TYPE_VIDEO = 2;
    private long MB = -1;
    private long MC = -1;
    private long mModifyTime = -1;
    public String mPath;
    public long mSize;
    public String mSrcName;
    public int type = 3;

    public boolean delFile() {
        return !TextUtils.isEmpty(this.mPath) ? new QFile(this.mPath).deleteSelf() : false;
    }

    public String getFileName() {
        return (TextUtils.isEmpty(this.mPath) || this.mPath.endsWith(File.separator)) ? null : this.mPath.substring(this.mPath.lastIndexOf(File.separator) + 1, this.mPath.length());
    }

    public long getmAccessTime() {
        if (-1 == this.MC) {
            if (TextUtils.isEmpty(this.mPath)) {
                return -1;
            }
            QFile qFile = new QFile(this.mPath);
            qFile.fillExtraInfo();
            this.MB = qFile.createTime;
            this.mModifyTime = qFile.modifyTime;
            this.MC = qFile.accessTime;
        }
        return this.MC;
    }

    public long getmCreateTime() {
        if (TextUtils.isEmpty(this.mPath)) {
            return -1;
        }
        if (-1 == this.MB) {
            QFile qFile = new QFile(this.mPath);
            qFile.fillExtraInfo();
            this.MB = qFile.createTime;
            this.mModifyTime = qFile.modifyTime;
            this.MC = qFile.accessTime;
        }
        return this.MB;
    }

    public long getmModifyTime() {
        if (-1 == this.mModifyTime) {
            if (TextUtils.isEmpty(this.mPath)) {
                return -1;
            }
            QFile qFile = new QFile(this.mPath);
            qFile.fillExtraInfo();
            this.MB = qFile.createTime;
            this.mModifyTime = qFile.modifyTime;
            this.MC = qFile.accessTime;
        }
        return this.mModifyTime;
    }

    public String toString() {
        return this.mPath + " :: " + this.mSize + " :: " + this.mSrcName;
    }
}
