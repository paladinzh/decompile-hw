package com.android.contacts.detail;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TransformableImageView extends ImageView {
    public TransformableImageView(Context context) {
        super(context);
    }

    public TransformableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransformableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = getDrawable();
        if (drawable != null) {
            float scale;
            int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
            Matrix drawMatrix = new Matrix();
            int dwidth = drawable.getIntrinsicWidth();
            int dheight = drawable.getIntrinsicHeight();
            int vwidth = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
            int vheight = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
            float dx = 0.0f;
            float dy = 0.0f;
            if (dwidth * vheight > vwidth * dheight) {
                scale = ((float) vheight) / ((float) dheight);
                dx = (((float) vwidth) - (((float) dwidth) * scale)) * 0.5f;
            } else {
                scale = ((float) vwidth) / ((float) dwidth);
                dy = (((float) vheight) - (((float) dheight) * scale)) * 0.5f;
            }
            drawMatrix.setScale(scale, scale);
            drawMatrix.postTranslate((float) Float.valueOf(dx + 0.5f).intValue(), (float) Float.valueOf(dy + 0.5f).intValue());
            canvas.concat(drawMatrix);
            drawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }
}
