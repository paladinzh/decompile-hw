package com.huawei.gallery.editor.filters.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;

public class CustMosaicDraw extends DrawStyle {
    int mBrushID;
    Context mContext;

    public CustMosaicDraw(int brushID, Context context) {
        this.mBrushID = brushID;
        this.mContext = context;
    }

    public BitmapShader getBrush(CustDrawBrushCache brushCache) {
        return brushCache.getBrush(this.mBrushID, this.mContext);
    }

    public void paint(Bitmap appliedMosaicBitmap, Canvas canvas, Matrix toScrMatrix, StrokeData sd, CustDrawBrushCache brushCache) {
        if (sd != null && sd.mPath != null && brushCache != null) {
            BitmapShader shader = getBrush(brushCache);
            if (shader == null) {
                GalleryLog.w("CustMosaicDraw", "shader is null");
                return;
            }
            Paint paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setStrokeCap(Cap.ROUND);
            paint.setStrokeJoin(Join.ROUND);
            paint.setAntiAlias(true);
            paint.setShader(shader);
            paint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius) * 2.0f);
            Path path = new Path();
            path.addPath(sd.mPath, toScrMatrix);
            canvas.drawPath(path, paint);
        }
    }
}
