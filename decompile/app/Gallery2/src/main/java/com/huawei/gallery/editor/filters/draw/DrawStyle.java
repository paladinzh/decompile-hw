package com.huawei.gallery.editor.filters.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;

public abstract class DrawStyle {
    public abstract void paint(Bitmap bitmap, Canvas canvas, Matrix matrix, StrokeData strokeData, CustDrawBrushCache custDrawBrushCache);
}
