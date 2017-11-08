package com.huawei.gallery.voiceimage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.android.gallery3d.R;
import com.huawei.gallery.app.plugin.PhotoExtraButtonOverlay;
import com.huawei.watermark.manager.parse.WMElement;

public class VoiceImageOverlay extends PhotoExtraButtonOverlay {
    private Paint mPaint = new Paint();
    private float mPaint_Stroke_Width;
    private int mRadius;
    private float mSweepAngle;
    private RectF mTargetRect;

    public VoiceImageOverlay(Context context) {
        this.mRadius = context.getResources().getDimensionPixelSize(R.dimen.voice_photo_button_circle_radius);
        this.mPaint_Stroke_Width = (((float) context.getResources().getDimensionPixelSize(R.dimen.voice_photo_button_circle_width)) * WMElement.CAMERASIZEVALUE1B1) / 2.0f;
        this.mPaint.setColor(context.getResources().getColor(R.color.voice_photo_button_color));
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(this.mPaint_Stroke_Width);
        this.mPaint.setAntiAlias(true);
        this.mTargetRect = new RectF();
    }

    protected void onDraw(Resources res, Canvas canvas) {
        Rect dstRect = canvas.getClipBounds();
        this.mPaint.setAlpha(76);
        canvas.drawCircle((float) dstRect.centerX(), (float) dstRect.centerY(), (float) this.mRadius, this.mPaint);
        if (this.mSweepAngle != 0.0f) {
            this.mPaint.setAlpha(255);
            this.mTargetRect.set((float) (dstRect.centerX() - this.mRadius), (float) (dstRect.centerY() - this.mRadius), (float) (dstRect.centerX() + this.mRadius), (float) (dstRect.centerY() + this.mRadius));
            canvas.drawArc(this.mTargetRect, BitmapDescriptorFactory.HUE_VIOLET, this.mSweepAngle, false, this.mPaint);
        }
    }

    public void setSweepAngle(float sweepAngle) {
        this.mSweepAngle = sweepAngle;
    }
}
