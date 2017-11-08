package tmsdk.fg.module.spacemanager;

import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class FileScanResult {
    public List<FileInfo> mBigFiles = new ArrayList();
    public List<FileMedia> mRadioFiles = new ArrayList();
    public List<FileMedia> mVideoFiles = new ArrayList();

    public long getBigFileSize() {
        long j = 0;
        for (FileInfo fileInfo : this.mBigFiles) {
            j = fileInfo.mSize + j;
        }
        return j;
    }

    public long getRadioSize() {
        long j = 0;
        for (FileMedia fileMedia : this.mRadioFiles) {
            j = fileMedia.mSize + j;
        }
        return j;
    }

    public long getVideoSize() {
        long j = 0;
        for (FileMedia fileMedia : this.mVideoFiles) {
            j = fileMedia.mSize + j;
        }
        return j;
    }
}
