package com.huawei.gallery.editor.watermark;

import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.io.FilenameFilter;

public class GalleryWMUtils {
    public static boolean checkWMZipExist() {
        long start = System.currentTimeMillis();
        File watermarkDir = new File("system/watermark/wm");
        if (!watermarkDir.isDirectory()) {
            return false;
        }
        String[] fileList = watermarkDir.list(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename != null ? filename.endsWith("zip") : false;
            }
        });
        boolean found = fileList != null && fileList.length > 0;
        GalleryLog.d("GalleryWMUtils", "check watermark zip time:" + (System.currentTimeMillis() - start) + "ms, found:" + found);
        return found;
    }
}
