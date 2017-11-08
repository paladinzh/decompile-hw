package com.huawei.gallery.editor.filters.fx;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;

public class ImageFilterMorphoFx extends ImageFilterFx {
    private FilterMorphoFxRepresentation mParameters = null;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterMorphoFxRepresentation) {
            this.mParameters = (FilterMorphoFxRepresentation) representation;
        }
    }

    public FilterMorphoFxRepresentation getParameters() {
        return this.mParameters;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null || !EditorLoadLib.FILTERJNI_MORPHO_LOADED) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int stride = w * 4;
        if (!(this.mParameters.getEffectName() == null || getEnvironment().needsStop())) {
            synchronized (ImageFilterFx.FILTER_LOCK) {
                nativeApplyFilterMorpho(bitmap, this.mParameters.getEffectName(), w, h, stride, this.mParameters.getParameter());
            }
        }
        return bitmap;
    }
}
