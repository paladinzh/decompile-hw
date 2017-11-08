package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import com.huawei.gallery.editor.sfb.FaceBeautifierParameter;
import com.huawei.gallery.editor.sfb.FaceEdit;

public class FaceSfbEditorStep extends FaceEditorStep {
    private FaceBeautifierParameter mFaceBeautifierParameter = new FaceBeautifierParameter();
    private FaceBeautifierParameter mQuickBeautyParameter = new FaceBeautifierParameter();

    public FaceBeautifierParameter getFaceBeautifierParameter() {
        return this.mFaceBeautifierParameter;
    }

    public FaceBeautifierParameter getQuickBeautyParameter() {
        return this.mQuickBeautyParameter;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        Bitmap bmp;
        if (environment.getQuality() == 2 && EditorLoadLib.isArcSoftLoaded()) {
            FaceBeautifierParameter faceBeautifierParameter = new FaceBeautifierParameter();
            FaceEdit.prepareBeautification(src, faceBeautifierParameter);
            bmp = FaceEdit.apply(src, this.mFaceFilterRepresentation, faceBeautifierParameter);
            FaceEdit.clearupBeautification(faceBeautifierParameter);
        } else {
            bmp = FaceEdit.apply(src, this.mFaceFilterRepresentation, this.mFaceBeautifierParameter);
        }
        return bmp;
    }

    public EditorStep copy() {
        FaceSfbEditorStep step = new FaceSfbEditorStep();
        for (FilterRepresentation representation : this.mFaceFilterRepresentation) {
            step.mFaceFilterRepresentation.add(representation.copy());
        }
        step.mFaceBeautifierParameter.set(this.mFaceBeautifierParameter);
        step.mQuickBeautyParameter.set(this.mQuickBeautyParameter);
        return step;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        FaceEdit.clearupBeautification(this.mFaceBeautifierParameter);
    }
}
