package com.huawei.gallery.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.android.gallery3d.R;

public class QuickNavigation extends ImageView {
    private static final int mCurSdkVer = SystemProperties.getInt("ro.build.version.sdk", 21);
    private int mCurrentScreen;
    private boolean mIsRTLLocale = false;
    private Bitmap mNormalPoint;
    private Paint mPaint;
    private HwResolverView mResolverActivity;
    private Bitmap mSelectPoint;
    private int mStartPos;
    private int screenCount;
    private float startPosX;

    public QuickNavigation(Context context) {
        super(context);
    }

    public QuickNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public QuickNavigation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        this.mPaint = new Paint();
        Drawable normal = context.getResources().getDrawable(R.drawable.navigation_spot_off);
        Drawable select = context.getResources().getDrawable(R.drawable.navigation_spot_on);
        this.mNormalPoint = ((BitmapDrawable) normal).getBitmap();
        this.mSelectPoint = ((BitmapDrawable) select).getBitmap();
    }

    public void setDirection(int layoutDirection) {
    }

    public void setCurrentScreen(int mCurrentScreen) {
        this.mCurrentScreen = mCurrentScreen;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mResolverActivity != null) {
            super.onDraw(canvas);
            this.screenCount = this.mResolverActivity.getScreenCount();
            this.mPaint.setAntiAlias(true);
            this.mStartPos = startPosition();
            int startPosY = (getHeight() - this.mNormalPoint.getHeight()) >> 1;
            int highlightDotPos = (mCurSdkVer < 21 || !this.mIsRTLLocale) ? this.mCurrentScreen : (this.screenCount - this.mCurrentScreen) - 1;
            for (int i = 0; i < this.screenCount; i++) {
                this.startPosX = (float) position(this.mStartPos, i);
                if (i == highlightDotPos) {
                    canvas.drawBitmap(this.mSelectPoint, this.startPosX, (float) startPosY, this.mPaint);
                } else {
                    canvas.drawBitmap(this.mNormalPoint, this.startPosX, (float) startPosY, this.mPaint);
                }
            }
        }
    }

    private int position(int start, int i) {
        return ((this.mNormalPoint.getWidth() + 10) * i) + start;
    }

    private int startPosition() {
        int sc = this.screenCount;
        return (getWidth() - ((this.mNormalPoint.getWidth() + 10) * this.screenCount)) >> 1;
    }

    public void setResolverActivity(HwResolverView resolverActivity) {
        this.mResolverActivity = resolverActivity;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return false;
    }
}
