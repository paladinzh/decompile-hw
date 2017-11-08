package com.huawei.gallery.editor.omron;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.android.gallery3d.util.GalleryLog;

public class FaceDetectionIMP {
    private FaceDetection mFaceDetection = FaceDetection.create();
    private FaceDetectionResult mFaceDetectionResult;
    private int mFaceNum = 0;
    private Bitmap mSource;

    public FaceDetectionIMP(Bitmap bitmap) {
        this.mSource = bitmap;
        if (this.mFaceDetection == null) {
            GalleryLog.e("FaceDetectionIMP", "create face detection failed");
            return;
        }
        this.mFaceDetection.setAngle(getanNonTrackingAngle(), 16846865);
        this.mFaceDetection.setFaceSizeRange(getMinfacesize(), getMaxfacesize());
        this.mFaceDetection.setMode(0);
        this.mFaceDetectionResult = FaceDetectionResult.createResult(10, 0);
        if (this.mFaceDetectionResult == null) {
            this.mFaceDetection.delete();
            this.mFaceDetection = null;
            GalleryLog.e("FaceDetectionIMP", "create face detaection result failed");
        }
    }

    private int[] getanNonTrackingAngle() {
        return new int[]{8402947, 0, 0};
    }

    private int getMinfacesize() {
        if (this.mSource == null) {
            return 20;
        }
        int nMinFaceSize = (((this.mSource.getWidth() > this.mSource.getHeight() ? this.mSource.getHeight() : this.mSource.getWidth()) / 10) / 10) * 10;
        if (nMinFaceSize <= 20) {
            nMinFaceSize = 20;
        }
        return nMinFaceSize;
    }

    private int getMaxfacesize() {
        if (this.mSource == null) {
            return 1920;
        }
        int nMaxFaceSize = this.mSource.getWidth() > this.mSource.getHeight() ? this.mSource.getWidth() : this.mSource.getHeight();
        if (nMaxFaceSize > 1920) {
            nMaxFaceSize = 1920;
        }
        return nMaxFaceSize;
    }

    public void destroy() {
        if (this.mFaceDetectionResult != null) {
            this.mFaceDetectionResult.deleteResult();
            this.mFaceDetectionResult = null;
        }
        if (this.mFaceDetection != null) {
            this.mFaceDetection.delete();
            this.mFaceDetection = null;
        }
    }

    public int detect() {
        if (this.mFaceDetection == null || this.mFaceDetectionResult == null || this.mSource == null) {
            GalleryLog.e("FaceDetectionIMP", "detect failed:not init");
            return -1;
        }
        this.mFaceDetectionResult.clearResult();
        int result = this.mFaceDetection.detection(this.mSource, 1, this.mFaceDetectionResult);
        if (result != 0) {
            GalleryLog.e("FaceDetectionIMP", "detect failed result:" + result);
            return -1;
        }
        this.mFaceNum = this.mFaceDetectionResult.getFaceCount();
        if (this.mFaceNum > 0) {
            return this.mFaceNum;
        }
        GalleryLog.d("FaceDetectionIMP", "getFaceCount:" + this.mFaceNum);
        return -1;
    }

    public RectF[] getFaceInfo() {
        if (this.mFaceDetection == null || this.mFaceDetectionResult == null || this.mSource == null) {
            GalleryLog.e("FaceDetectionIMP", "detect failed:not init");
            return new RectF[0];
        }
        this.mFaceDetectionResult.clearResult();
        return this.mFaceDetection.detection(this.mSource, 1, this.mFaceDetectionResult, new RectF());
    }
}
