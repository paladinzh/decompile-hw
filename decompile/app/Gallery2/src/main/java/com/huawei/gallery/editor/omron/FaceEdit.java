package com.huawei.gallery.editor.omron;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import java.util.AbstractList;

public final class FaceEdit {
    private static native int correctBitmap(Bitmap bitmap, FaceBeautifierParameter faceBeautifierParameter);

    public static Bitmap apply(Bitmap bitmap, FaceBeautifierParameter parameter) {
        if (!(bitmap == null || parameter == null || !parameter.hasModified())) {
            parameter.convertAllToOmronParameter();
            correctBitmap(bitmap, parameter);
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
                faceBeautifierPara.updateParameter(faceRepresentation.getFaceType(), faceRepresentation.getValue());
            }
        }
        return apply(src, faceBeautifierPara);
    }
}
