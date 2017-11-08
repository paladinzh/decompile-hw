package com.huawei.gallery.editor.filters.fx;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;

public class ImageFilterHuaweiCommonFx extends ImageFilterFx {
    private FilterHuaweiCommonFxRepresentation mParameters = null;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterHuaweiCommonFxRepresentation) {
            this.mParameters = (FilterHuaweiCommonFxRepresentation) representation;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        FilterHuaweiCommonFxRepresentation parameters = this.mParameters;
        if (parameters == null || !EditorLoadLib.FILTERJNI_LOADED) {
            GalleryLog.d("ImageFilterHuaweiCommonFx", "parameters:" + parameters + ", FILTERJNI_LOADED:" + EditorLoadLib.FILTERJNI_LOADED);
            return bitmap;
        }
        if (!getEnvironment().needsStop()) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            synchronized (FILTER_LOCK) {
                nativeApplyFilter2(bitmap, w, h, parameters.getFilterId(), parameters.getParameter().mStrengthParameter);
            }
        }
        return bitmap;
    }
}
