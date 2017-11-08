package com.huawei.gallery.editor.filters.fx;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;

public class ImageFilterHuaweiMistFx extends ImageFilterFx {
    private FilterFxRepresentation mParameters = null;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterFxRepresentation) {
            this.mParameters = (FilterFxRepresentation) representation;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        FilterFxRepresentation parameters = this.mParameters;
        if (parameters == null || !EditorLoadLib.FILTERJNI_MIST_LOADED) {
            GalleryLog.d("ImageFilterHuaweiMistFx", "parameters:" + parameters + ", FILTERJNI_MIST_LOADED:" + EditorLoadLib.FILTERJNI_MIST_LOADED);
            return bitmap;
        }
        if (!getEnvironment().needsStop()) {
            nativeApplyFilterMist(bitmap, bitmap.getWidth(), bitmap.getHeight());
        }
        return bitmap;
    }
}
