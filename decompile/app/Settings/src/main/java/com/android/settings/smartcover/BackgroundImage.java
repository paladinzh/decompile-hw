package com.android.settings.smartcover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import java.util.Locale;

public class BackgroundImage extends ImageView {
    private int mLabeOffset = 0;
    private int mLabelIconHeight = 0;
    private int mLabelIconWidth = 0;
    private Bitmap mThemeMark = null;
    private int mThemeType = -1;

    public BackgroundImage(Context context) {
        super(context);
    }

    public BackgroundImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BackgroundImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mLabelIconWidth = getMeasuredWidth() - ((int) getContext().getResources().getDimension(2131559146));
        this.mLabelIconHeight = (int) getContext().getResources().getDimension(2131559145);
        this.mLabeOffset = (int) getContext().getResources().getDimension(2131559144);
        if (!Locale.getDefault().getLanguage().equals(Locale.CHINA.getLanguage())) {
            this.mLabeOffset -= 3;
        }
    }

    public void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            Log.e("BackgroundImage", "fail to draw");
        }
        if (canvas != null && this.mThemeMark != null) {
            canvas.drawBitmap(this.mThemeMark, (float) (this.mLabelIconWidth - this.mThemeMark.getWidth()), (float) this.mLabelIconHeight, null);
        }
    }

    private Bitmap conversionDrawable(Drawable drawable) {
        BitmapDrawable bitmapDrawable = null;
        if (drawable instanceof BitmapDrawable) {
            bitmapDrawable = (BitmapDrawable) drawable;
        }
        if (bitmapDrawable != null) {
            return bitmapDrawable.getBitmap();
        }
        return null;
    }

    public void setThemeType(int themeType) {
        this.mThemeType = themeType;
        int resId = 0;
        switch (this.mThemeType) {
            case 8:
                resId = 2130838528;
                break;
        }
        if (resId == 0 || getResources() == null) {
            this.mThemeMark = null;
        } else {
            this.mThemeMark = conversionDrawable(getResources().getDrawable(resId));
        }
    }
}
