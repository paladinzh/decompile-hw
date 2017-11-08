package com.android.gallery3d.util;

import android.annotation.SuppressLint;
import com.android.gallery3d.common.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressLint({"DefaultLocale"})
public class FileUtils {
    private static final String LOG_TAG = FileUtils.class.getSimpleName();

    public static boolean deleteFile(File file) {
        int i = 0;
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }
        File[] listFiles = file.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            return true;
        }
        boolean success = true;
        int length = listFiles.length;
        while (i < length) {
            File childFile = listFiles[i];
            boolean isDir = childFile.isDirectory();
            boolean successThisFile = deleteFile(childFile);
            if (isDir) {
                successThisFile = successThisFile ? childFile.delete() : false;
            }
            if (!successThisFile) {
                GalleryLog.d(LOG_TAG, "Delete file " + childFile + "failed");
                success = false;
            }
            i++;
        }
        return success;
    }

    public static int createNewFileWithCatch(File file) {
        try {
            return file.createNewFile() ? 1 : 0;
        } catch (IOException e) {
            GalleryLog.d(LOG_TAG, "create new file failed " + file + "." + e.getMessage());
            return -1;
        }
    }

    private static boolean isRootNotExists(File root) {
        if (root == null || (!root.exists() && !root.mkdirs())) {
            return true;
        }
        return false;
    }

    public static File chooseFileInSequence(File root, String[] fileNameSequence, boolean isDir) {
        if (isRootNotExists(root)) {
            GalleryLog.d(LOG_TAG, "mkdir root file failed " + root);
            return null;
        }
        int i;
        File[] fileList = new File[fileNameSequence.length];
        for (i = 0; i < fileNameSequence.length; i++) {
            fileList[i] = new File(root, fileNameSequence[i]);
        }
        File selectFile = fileList[0];
        i = 0;
        while (i < fileNameSequence.length) {
            if (!fileList[i].exists() || (i > 0 && fileList[i].lastModified() <= fileList[i - 1].lastModified())) {
                selectFile = fileList[i];
                break;
            }
            i++;
        }
        GalleryLog.d(LOG_TAG, "The select output file is " + selectFile.getAbsolutePath());
        if (selectFile.exists() && !deleteFile(selectFile)) {
            return null;
        }
        if (isDir && !selectFile.exists() && !selectFile.mkdirs()) {
            GalleryLog.d(LOG_TAG, "create dir failed " + selectFile.getAbsolutePath());
            return null;
        } else if (isDir || createNewFileWithCatch(selectFile) > 0) {
            return selectFile;
        } else {
            return null;
        }
    }

    public static File getNoneDuplicateFile(File root, String suggestName) {
        int indexOfDot = suggestName.lastIndexOf(46);
        return getNoneDuplicateFile(root, indexOfDot > 0 ? suggestName.substring(0, indexOfDot) : suggestName, indexOfDot > 0 ? suggestName.substring(indexOfDot) : "");
    }

    public static File getNoneDuplicateFile(File root, String suggestName, String fileSuffix) {
        File file = new File(root, suggestName + fileSuffix);
        if (!file.exists()) {
            return file;
        }
        String format = "%s-%d%s";
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            file = new File(root, String.format("%s-%d%s", new Object[]{suggestName, Integer.valueOf(i), fileSuffix}));
            if (!file.exists()) {
                return file;
            }
        }
        GalleryLog.e(LOG_TAG, "All Possile is used");
        return null;
    }

    public static void skip(InputStream is, long skipCount) throws IOException {
        while (skipCount > 0) {
            long realSkipCount = is.skip(skipCount);
            if (realSkipCount > 0) {
                skipCount -= realSkipCount;
            } else {
                return;
            }
        }
    }

    public static void readDataToWrite(InputStream is, OutputStream os, byte[] buffer, long readCount) throws IOException {
        int bufferSize = buffer.length;
        while (readCount >= 0) {
            int realReadCount = is.read(buffer, 0, (int) Math.min(readCount, (long) bufferSize));
            if (realReadCount <= 0) {
                break;
            }
            os.write(buffer, 0, realReadCount);
            readCount -= (long) realReadCount;
        }
        os.flush();
    }

    public static boolean copyFileToNewFile(File oldFile, File newFile, int bufferSize) {
        Throwable th;
        Throwable th2;
        Closeable closeable = null;
        Closeable os = null;
        try {
            Closeable is = new BufferedInputStream(new FileInputStream(oldFile));
            try {
                Closeable os2 = new BufferedOutputStream(new FileOutputStream(newFile));
                try {
                    byte[] buffer = new byte[bufferSize];
                    while (true) {
                        int readCount = is.read(buffer);
                        if (readCount != -1) {
                            os2.write(buffer, 0, readCount);
                        } else {
                            Utils.closeSilently(is);
                            Utils.closeSilently(os2);
                            return true;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    os = os2;
                    closeable = is;
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(os);
                    throw th2;
                }
            } catch (Throwable th4) {
                th2 = th4;
                closeable = is;
                Utils.closeSilently(closeable);
                Utils.closeSilently(os);
                throw th2;
            }
        } catch (Throwable th5) {
            th = th5;
            GalleryLog.d(LOG_TAG, "copy file fail." + th.getMessage());
            Utils.closeSilently(closeable);
            Utils.closeSilently(os);
            return false;
        }
    }

    public static void readDataUtilEndToWrite(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        while (true) {
            int realReadCount = is.read(buffer);
            if (realReadCount != -1) {
                os.write(buffer, 0, realReadCount);
            } else {
                return;
            }
        }
    }
}
