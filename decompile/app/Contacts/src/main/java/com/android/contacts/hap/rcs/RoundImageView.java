package com.android.contacts.hap.rcs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import com.android.contacts.R$styleable;

public class RoundImageView extends ImageView {
    private Paint mBitmapPaint;
    private BitmapShader mBitmapShader;
    private int mBorderRadius;
    private Matrix mMatrix;
    private RectF mViewRectF;

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBitmapPaint = new Paint();
        this.mBitmapPaint.setAntiAlias(true);
        this.mMatrix = new Matrix();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.RoundImageView);
        this.mBorderRadius = a.getDimensionPixelSize(0, (int) TypedValue.applyDimension(1, 8.0f, getResources().getDisplayMetrics()));
        a.recycle();
    }

    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bmp = drawableToBitamp(drawable);
            if (bmp.getWidth() > 0 && bmp.getHeight() > 0) {
                this.mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
                float scale = Math.max((((float) getWidth()) * 1.0f) / ((float) bmp.getWidth()), (((float) getHeight()) * 1.0f) / ((float) bmp.getHeight()));
                this.mMatrix.setScale(scale, scale);
                this.mBitmapShader.setLocalMatrix(this.mMatrix);
                this.mBitmapPaint.setShader(this.mBitmapShader);
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        if (getDrawable() != null) {
            setUpShader();
            canvas.drawRoundRect(this.mViewRectF, (float) this.mBorderRadius, (float) this.mBorderRadius, this.mBitmapPaint);
        }
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mViewRectF = new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
    }

    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state_instance", super.onSaveInstanceState());
        bundle.putInt("state_border_radius", this.mBorderRadius);
        return bundle;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable("state_instance"));
            this.mBorderRadius = bundle.getInt("state_border_radius");
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
