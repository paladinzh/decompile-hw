package com.huawei.gallery.editor.sfb;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceColorRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import java.util.AbstractList;

public final class FaceEdit {
    private static boolean SUPPORT_NEW_FACEEDITOR = true;
    static boolean beautyLock = true;

    private static native int nativeArcSoftDelayUnInitialize(FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeArcSoftGetVersion(FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeArcSoftInitialize(Bitmap bitmap, FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeArcSoftUnInitialize(FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeCleanupBeautification(FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeCorrectBitmapArcSoft(Bitmap bitmap, FaceBeautifierParameter faceBeautifierParameter, int i);

    private static native int nativeCorrectBitmapSFB(Bitmap bitmap, FaceBeautifierParameter faceBeautifierParameter, int i);

    private static native int nativeDelayCleanupBeautification(FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativeDetection(Bitmap bitmap);

    private static native RectF[] nativeGetFaceInfo(Bitmap bitmap, RectF rectF);

    private static native int nativePrepareBeautification(Bitmap bitmap, FaceBeautifierParameter faceBeautifierParameter);

    private static native int nativePrepareEngine(FaceBeautifierParameter faceBeautifierParameter);

    private static Bitmap apply(Bitmap bitmap, FaceBeautifierParameter parameter) {
        if (!(bitmap == null || parameter == null || !parameter.hasModified())) {
            parameter.convertAllToSfbParameter();
            if (beautyLock) {
                beautyLock = false;
                if (EditorLoadLib.isArcSoftLoaded()) {
                    nativeCorrectBitmapArcSoft(bitmap, parameter, parameter.mSfbType);
                } else {
                    nativeCorrectBitmapSFB(bitmap, parameter, parameter.mSfbType);
                }
                beautyLock = true;
            }
        }
        return bitmap;
    }

    public static Bitmap apply(Bitmap src, AbstractList<FilterRepresentation> filters, FaceBeautifierParameter faceBeautifierPara) {
        if (faceBeautifierPara == null) {
            return src;
        }
        faceBeautifierPara.clearParameter();
        for (int i = 0; i < filters.size(); i++) {
            FilterRepresentation filterRepresentation = (FilterRepresentation) filters.get(i);
            if (filterRepresentation.getFilterType() == 5 && (filterRepresentation instanceof FilterFaceRepresentation)) {
                FilterFaceRepresentation faceRepresentation = (FilterFaceRepresentation) filterRepresentation;
                if (faceRepresentation instanceof FilterFaceColorRepresentation) {
                    faceBeautifierPara.updateParameter(faceRepresentation.getFaceType(), ((FilterFaceColorRepresentation) faceRepresentation).getValues());
                } else {
                    faceBeautifierPara.updateParameter(faceRepresentation.getFaceType(), faceRepresentation.getValue());
                }
            }
        }
        return apply(src, faceBeautifierPara);
    }

    public static int detect(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return -1;
        }
        return nativeDetection(bitmap);
    }

    public static int prepareBeautification(Bitmap bitmap, FaceBeautifierParameter faceBeautifierPara) {
        if (bitmap == null) {
            return -1;
        }
        int state = 0;
        if (beautyLock) {
            beautyLock = false;
            if (EditorLoadLib.isArcSoftLoaded()) {
                nativeArcSoftInitialize(bitmap, faceBeautifierPara);
                setSupportVersion(nativeArcSoftGetVersion(faceBeautifierPara));
            } else {
                nativePrepareEngine(faceBeautifierPara);
                state = nativePrepareBeautification(bitmap, faceBeautifierPara);
            }
            beautyLock = true;
        }
        return state;
    }

    public static int clearupBeautification(FaceBeautifierParameter faceBeautifierPara) {
        int state = 0;
        if (beautyLock) {
            beautyLock = false;
            if (EditorLoadLib.isArcSoftLoaded()) {
                state = nativeArcSoftUnInitialize(faceBeautifierPara);
            } else {
                state = nativeCleanupBeautification(faceBeautifierPara);
            }
            beautyLock = true;
        } else if (EditorLoadLib.isArcSoftLoaded()) {
            nativeArcSoftDelayUnInitialize(faceBeautifierPara);
        } else {
            nativeDelayCleanupBeautification(faceBeautifierPara);
        }
        return state;
    }

    public static RectF[] getFaceInfo(Bitmap bitmap, RectF rect) {
        if (!beautyLock) {
            return null;
        }
        beautyLock = false;
        RectF[] result = nativeGetFaceInfo(bitmap, rect);
        beautyLock = true;
        return result;
    }

    private static void setSupportVersion(int version) {
        boolean z = false;
        if (version > 0) {
            z = true;
        }
        SUPPORT_NEW_FACEEDITOR = z;
    }

    public static boolean getSupportVersion() {
        return SUPPORT_NEW_FACEEDITOR;
    }
}
