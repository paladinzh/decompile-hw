package com.huawei.gallery.refocus.app;

import android.graphics.Point;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.RandomAccessFile;

public abstract class AbsRefocusPhoto {
    protected int mEDoFPhotoLen = 0;
    protected RandomAccessFile mFile;
    protected int mFileLen;
    protected String mFileName;
    protected Point mFocusPoint;
    protected boolean mIsRefocusPhoto;
    protected int mOrientation;
    protected int mPhotoHeight = 0;
    protected int mPhotoWidth = 0;

    public AbsRefocusPhoto(String filePath, int photoWidth, int PhotoHeight) {
        this.mFileName = filePath;
        this.mPhotoWidth = photoWidth;
        this.mPhotoHeight = PhotoHeight;
    }

    public void resizePhoto(int photoWidth, int photoHeight) {
        if (this.mPhotoWidth != photoWidth || this.mPhotoHeight != photoHeight) {
            this.mPhotoWidth = photoWidth;
            this.mPhotoHeight = photoHeight;
        }
    }

    protected static byte[] intToByteArray(int value, byte[] out, int offset) {
        if (out == null || out.length < 4) {
            GalleryLog.e("AbsRefocusPhoto", "the length of byteArray must be bigger than 3 and byteArray");
            return out;
        }
        for (int i = 0; i < 4; i++) {
            out[offset + i] = (byte) ((value >> (i * 8)) & 255);
        }
        return out;
    }

    protected static void closeSilently(Closeable file) {
        if (file != null) {
            try {
                file.close();
            } catch (Throwable t) {
                GalleryLog.w("AbsRefocusPhoto", "fail to close." + t.getMessage());
            }
        }
    }

    protected static boolean simplyCheckIsJpegPhoto(byte[] photoData) {
        return photoData != null && photoData.length >= 4 && photoData[0] == (byte) -1 && photoData[1] == (byte) -40;
    }

    protected Point transformToPreviewCoordinate(Point point, int orientation) {
        int tmp_x = point.x;
        int tmp_y = point.y;
        switch (orientation % 360) {
            case WMComponent.ORI_90 /*90*/:
                point.x = tmp_y;
                point.y = this.mPhotoWidth - tmp_x;
                break;
            case 180:
                point.x = this.mPhotoWidth - tmp_x;
                point.y = this.mPhotoHeight - tmp_y;
                break;
            case 270:
                point.x = this.mPhotoHeight - tmp_y;
                point.y = tmp_x;
                break;
        }
        GalleryLog.i("AbsRefocusPhoto", "point coordinate before transformToPreviewCoordinate(): " + tmp_x + ", " + tmp_y);
        GalleryLog.i("AbsRefocusPhoto", "point coordinate after transformToPreviewCoordinate(): " + point.x + ", " + point.y);
        return point;
    }

    protected Point transformToPhotoCoordinate(Point point, int orientation) {
        int tmp_x = point.x;
        int tmp_y = point.y;
        switch (orientation % 360) {
            case WMComponent.ORI_90 /*90*/:
                point.x = this.mPhotoWidth - tmp_y;
                point.y = tmp_x;
                break;
            case 180:
                point.x = this.mPhotoWidth - tmp_x;
                point.y = this.mPhotoHeight - tmp_y;
                break;
            case 270:
                point.x = tmp_y;
                point.y = this.mPhotoHeight - tmp_x;
                break;
        }
        GalleryLog.i("AbsRefocusPhoto", "point coordinate before transformToPhotoCoordinate() : " + tmp_x + ", " + tmp_y);
        GalleryLog.i("AbsRefocusPhoto", "point coordinate after transformToPhotoCoordinate() : " + point.x + ", " + point.y);
        return point;
    }
}
