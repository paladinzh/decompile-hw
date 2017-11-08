package com.huawei.gallery.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HwImageView extends ImageView {
    private Drawable mNewDrawable;
    private Drawable mOldDrawable;

    public HwImageView(Context context) {
        super(context);
    }

    public HwImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (getDrawable() != null) {
            if (isPressed() || isSelected()) {
                if (this.mNewDrawable == null) {
                    Canvas canvas = new Canvas();
                    Bitmap mainImage = drawableToBitmap(getDrawable());
                    Paint paint = new Paint();
                    paint.setColorFilter(new PorterDuffColorFilter(2130706432, Mode.SRC_ATOP));
                    Bitmap result = Bitmap.createBitmap(mainImage.getWidth(), mainImage.getHeight(), Config.ARGB_8888);
                    canvas.setBitmap(result);
                    canvas.drawBitmap(mainImage, 0.0f, 0.0f, paint);
                    this.mOldDrawable = getDrawable();
                    setImageBitmap(result);
                    this.mNewDrawable = getDrawable();
                } else {
                    setImageDrawable(this.mNewDrawable);
                }
            } else if (this.mOldDrawable == null) {
                this.mOldDrawable = getDrawable();
            } else {
                setImageDrawable(this.mOldDrawable);
            }
            invalidate();
        }
    }

    public void setForceSelected(boolean sel) {
        super.setSelected(sel);
        refreshDrawableState();
    }
}
