package com.huawei.gallery.photoshare.uploadservice;

import com.huawei.android.cg.vo.FileInfo;
import java.util.ArrayList;

public class WaitUploadGeneralFile {
    private ArrayList<FileInfo> mFileInfoList = new ArrayList();

    public void addFileInfo(FileInfo fileInfo) {
        this.mFileInfoList.add(fileInfo);
    }

    public ArrayList<FileInfo> getFileInfoList() {
        return this.mFileInfoList;
    }
}
