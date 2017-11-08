package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.android.gallery3d.R;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.VirtualEarthProjection;

public class ApertureLeafDrawer {
    private static float LEAF_ALPHA = 0.65f;
    private static float RADIUS_MAX_RATIO = 0.95f;
    private static float RADIUS_MIN_RATIO = 0.08f;
    private static int SIZE = 6;
    private double X1;
    private double Y1;
    private float mCurrentRadius;
    private float mLeafRadius;
    private float x1;
    private PorterDuffXfermode xferMode = new PorterDuffXfermode(Mode.DST_IN);
    private float y1;

    public ApertureLeafDrawer(Context context) {
        this.mLeafRadius = (float) context.getResources().getDimensionPixelSize(R.dimen.aperture_leaf_circle_diameter);
        this.X1 = ((double) this.mLeafRadius) * Math.cos((((double) ((((float) ((SIZE - 2) * 180)) / ((float) SIZE)) / 2.0f)) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude);
        this.Y1 = ((double) this.mLeafRadius) * Math.sin((((double) ((((float) ((SIZE - 2) * 180)) / ((float) SIZE)) / 2.0f)) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude);
    }

    public void drawApertureLeaf(Canvas canvas, float dragRatio, int centerX, int centerY, int alpha) {
        int paintAlpha = (int) (((float) alpha) * LEAF_ALPHA);
        this.mCurrentRadius = (((RADIUS_MAX_RATIO - RADIUS_MIN_RATIO) * this.mLeafRadius) * dragRatio) + (RADIUS_MIN_RATIO * this.mLeafRadius);
        if (this.mCurrentRadius > this.mLeafRadius) {
            this.mCurrentRadius = this.mLeafRadius;
        }
        canvas.saveLayer(null, new Paint(), 3);
        Paint paint2 = new Paint();
        paint2.setColor(-1);
        paint2.setStyle(Style.FILL);
        paint2.setAntiAlias(true);
        this.x1 = ((this.mCurrentRadius * ((float) this.X1)) / this.mLeafRadius) - (MapConfig.MIN_ZOOM / ((float) Math.sin(((double) ((BitmapDescriptorFactory.HUE_CYAN - (((float) ((SIZE - 2) * 180)) / ((float) SIZE))) / BitmapDescriptorFactory.HUE_CYAN)) * 3.141592653589793d)));
        this.y1 = (this.mCurrentRadius * ((float) this.Y1)) / this.mLeafRadius;
        RectF rf1 = new RectF((((float) centerX) + this.x1) - (this.mLeafRadius * 2.0f), (((float) centerY) - this.y1) - (this.mLeafRadius * 2.0f), (((float) centerX) + this.x1) + (this.mLeafRadius * 2.0f), (((float) centerY) - this.y1) + (this.mLeafRadius * 2.0f));
        canvas.save();
        for (int i = 0; i < SIZE; i++) {
            canvas.rotate(360.0f / ((float) SIZE), (float) centerX, (float) centerY);
            canvas.drawArc(rf1, -180.0f, BitmapDescriptorFactory.HUE_CYAN - (((float) ((SIZE - 2) * 180)) / ((float) SIZE)), true, paint2);
        }
        paint2.setXfermode(this.xferMode);
        canvas.saveLayer(null, paint2, 3);
        Paint paint3 = new Paint();
        paint3.setColor(-65536);
        paint3.setAntiAlias(true);
        paint3.setStyle(Style.FILL);
        paint3.setAlpha(paintAlpha);
        canvas.drawCircle((float) centerX, (float) centerY, this.mLeafRadius, paint3);
        canvas.restore();
    }
}
