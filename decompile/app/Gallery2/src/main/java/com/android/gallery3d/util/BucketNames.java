package com.android.gallery3d.util;

import java.io.File;

public class BucketNames {
    public static final String DOCUMENT_RECTIFY = (formatPath(Constant.CAMERA_PATH) + "/DocRectify");

    private static String formatPath(String path) {
        if (path == null || !path.startsWith(File.separator)) {
            return path;
        }
        return path.substring(1);
    }
}
