package com.huawei.watermark.wmutil;

import com.huawei.watermark.decoratorclass.WMLog;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WMZipUtil {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMZipUtil.class.getSimpleName());

    public static InputStream openZipEntryInputStream(String fileName, InputStream zipFileIs) {
        Exception e;
        if (zipFileIs == null) {
            return null;
        }
        int entries = 0;
        Closeable closeable = null;
        try {
            ZipInputStream zis = new ZipInputStream(zipFileIs);
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }
                String entryName = entry.getName();
                sanitizeFileName(entryName, ".");
                entries++;
                if (entries > 1024) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                try {
                    if (entryName.equals(fileName)) {
                        return zis;
                    }
                    zis.closeEntry();
                } catch (Exception e2) {
                    e = e2;
                    closeable = zis;
                }
            }
        } catch (Exception e3) {
            e = e3;
            WMLog.e(TAG, "openZipEntryInputStream got an Exception", e);
            WMFileUtil.closeSilently(closeable);
            return null;
        }
        return null;
    }

    private static String sanitizeFileName(String entryName, String intendedDir) throws Exception {
        if (entryName.contains("../")) {
            throw new Exception("unsecurity zipfiles!");
        }
        String canonicalPath = new File(intendedDir, entryName).getCanonicalPath();
        if (canonicalPath.startsWith(new File(intendedDir).getCanonicalPath())) {
            return canonicalPath;
        }
        throw new IllegalStateException("File is outside extraction target directory.");
    }
}
