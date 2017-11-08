package com.huawei.gallery.extfile;

import com.android.gallery3d.util.GalleryLog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

abstract class FileChecker<T> {
    protected abstract T onFileChecked(String str, T t) throws FileNotFoundException, IOException;

    FileChecker() {
    }

    public T checkFile(String filepath, T inout) {
        try {
            return onFileChecked(filepath, inout);
        } catch (FileNotFoundException e) {
            GalleryLog.i("FileChecker", "checkFile failed. " + e.getMessage());
            return null;
        } catch (IOException e2) {
            GalleryLog.i("FileChecker", "checkFile failed. " + e2.getMessage());
            return null;
        } catch (Exception e3) {
            GalleryLog.i("FileChecker", "checkFile failed. " + e3.getMessage());
            return null;
        }
    }

    protected static void close(RandomAccessFile file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                GalleryLog.i("FileChecker", "close RandomAccessFile failed. " + e.getMessage());
            }
        }
    }
}
