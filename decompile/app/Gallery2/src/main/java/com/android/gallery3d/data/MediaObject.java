package com.android.gallery3d.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.manager.parse.WMConfig;

public abstract class MediaObject {
    private static long sVersionSerial = 0;
    protected long mDataVersion;
    protected final Path mPath;

    public interface PanoramaSupportCallback {
        void panoramaInfoAvailable(MediaObject mediaObject, boolean z, boolean z2);
    }

    public MediaObject(Path path, long version) {
        path.setObject(this);
        this.mPath = path;
        this.mDataVersion = version;
    }

    public Path getPath() {
        return this.mPath;
    }

    public int getSupportedOperations() {
        return 0;
    }

    public int getVirtualFlags() {
        return 0;
    }

    public void delete() {
        throw new UnsupportedOperationException();
    }

    public boolean delete(int flag) {
        delete();
        return true;
    }

    public void recycle(SQLiteDatabase db, Bundle data) {
        throw new UnsupportedOperationException();
    }

    public void rotate(int degrees) {
        throw new UnsupportedOperationException();
    }

    public Uri getContentUri() {
        GalleryLog.e("MediaObject", "Class " + getClass().getName() + "should implement getContentUri.");
        GalleryLog.e("MediaObject", "The object was created from path: " + getPath());
        throw new UnsupportedOperationException();
    }

    public Uri getPlayUri() {
        throw new UnsupportedOperationException();
    }

    public int getMediaType() {
        return 1;
    }

    public boolean Import() {
        throw new UnsupportedOperationException();
    }

    public MediaDetails getDetails() {
        return new MediaDetails();
    }

    public long getDataVersion() {
        return this.mDataVersion;
    }

    public int getCacheFlag() {
        return 0;
    }

    public void cache(int flag) {
        throw new UnsupportedOperationException();
    }

    public static synchronized long nextVersionNumber() {
        long j;
        synchronized (MediaObject.class) {
            j = sVersionSerial + 1;
            sVersionSerial = j;
        }
        return j;
    }

    public static boolean isImageTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length >= 2) {
            return "image".equals(name[1]);
        }
        throw new IllegalArgumentException(path.toString());
    }

    public static boolean isVideoTypeFromPath(Path path) {
        String[] name = path.split();
        if (name.length >= 2) {
            return "video".equals(name[1]);
        }
        throw new IllegalArgumentException(path.toString());
    }

    public static int getTypeFromString(String s) {
        if (WMConfig.SUPPORTALL.equals(s)) {
            return 6;
        }
        if ("image".equals(s)) {
            return 2;
        }
        if ("video".equals(s)) {
            return 4;
        }
        throw new IllegalArgumentException(s);
    }

    public void hide() {
        throw new UnsupportedOperationException();
    }

    public void show() {
        throw new UnsupportedOperationException();
    }

    public void editPhotoShare(Context context) {
        throw new UnsupportedOperationException();
    }

    public void moveOUT() {
        throw new UnsupportedOperationException();
    }

    public void moveIN() {
        throw new UnsupportedOperationException();
    }

    public boolean rename(String newName) {
        throw new UnsupportedOperationException();
    }

    public boolean isPhotoSharePreView() {
        return false;
    }

    public boolean isPhotoShareUploadFailItem() {
        return false;
    }

    public void removeFromStoryAlbum(String code) {
        throw new UnsupportedOperationException();
    }
}
