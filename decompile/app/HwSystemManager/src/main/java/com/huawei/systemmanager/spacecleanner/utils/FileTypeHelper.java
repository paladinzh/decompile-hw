package com.huawei.systemmanager.spacecleanner.utils;

import android.media.MediaFile;
import android.media.MediaFile.MediaFileType;

public class FileTypeHelper {
    private static final String TAG = "FileTypeHelper";

    public static int getFileType(String filePath) {
        MediaFileType type = MediaFile.getFileType(filePath);
        if (type != null) {
            int fileType = type.fileType;
            if (MediaFile.isAudioFileType(fileType)) {
                return 1;
            }
            if (MediaFile.isVideoFileType(fileType)) {
                return 2;
            }
            if (MediaFile.isImageFileType(fileType)) {
                return 3;
            }
        }
        return HwMediaFile.getFileBigTypeByPath(filePath);
    }

    public static String getMimeTypeForFile(String filePath) {
        String mineType = MediaFile.getMimeTypeForFile(filePath);
        if (mineType == null) {
            return HwMediaFile.getMimeTypeForFile(filePath);
        }
        return mineType;
    }
}
