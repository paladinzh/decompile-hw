package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.omron.FaceBeautifierParameter;
import com.huawei.gallery.editor.omron.FaceEdit;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;

public class FaceOmronEditorStep extends FaceEditorStep {
    private FaceBeautifierParameter mFaceBeautifierParameter = new FaceBeautifierParameter();

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        return FaceEdit.apply(src, this.mFaceFilterRepresentation, this.mFaceBeautifierParameter);
    }

    public EditorStep copy() {
        FaceOmronEditorStep step = new FaceOmronEditorStep();
        for (FilterRepresentation representation : this.mFaceFilterRepresentation) {
            step.mFaceFilterRepresentation.add(representation.copy());
        }
        step.mFaceBeautifierParameter.set(this.mFaceBeautifierParameter);
        return step;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        this.mFaceBeautifierParameter.clearParameter();
    }
}
