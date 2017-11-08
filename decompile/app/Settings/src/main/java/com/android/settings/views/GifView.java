package com.android.settings.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.android.settings.R$styleable;

public class GifView extends ImageView {
    private int mCurrentAnimationTime;
    private int mDuration;
    private float mLeft;
    private Movie mMovie;
    private int mMovieResourceId;
    private long mMovieStart;
    private volatile boolean mPaused;
    private float mTop;
    private boolean mVisible;

    public GifView(Context context) {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDuration = 0;
        this.mCurrentAnimationTime = 0;
        this.mVisible = true;
        this.mPaused = false;
        setViewAttributes(context, attrs, defStyle);
    }

    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
        if (VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R$styleable.GifView, defStyle, 0);
        this.mMovieResourceId = array.getResourceId(0, -1);
        this.mPaused = array.getBoolean(1, false);
        array.recycle();
        if (this.mMovieResourceId != -1) {
            this.mMovie = Movie.decodeStream(getResources().openRawResource(this.mMovieResourceId));
        }
    }

    public void setMovieResource(int movieResId) {
        this.mMovieResourceId = movieResId;
        this.mMovie = Movie.decodeStream(getResources().openRawResource(this.mMovieResourceId));
        requestLayout();
    }

    public void resetMovieTime() {
        this.mMovieStart = 0;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mMovie != null) {
            setMeasuredDimension(this.mMovie.width(), this.mMovie.height());
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean z = false;
        super.onLayout(changed, l, t, r, b);
        this.mLeft = ((float) (getWidth() - this.mMovie.width())) / 2.0f;
        this.mTop = ((float) (getHeight() - this.mMovie.height())) / 2.0f;
        if (getVisibility() == 0) {
            z = true;
        }
        this.mVisible = z;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mMovie == null) {
            return;
        }
        if (this.mPaused) {
            drawMovieFrame(canvas);
            return;
        }
        updateAnimationTime();
        drawMovieFrame(canvas);
        invalidateView();
    }

    private void invalidateView() {
        if (!this.mVisible) {
            return;
        }
        if (VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    private void updateAnimationTime() {
        long now = SystemClock.uptimeMillis();
        if (this.mMovieStart == 0) {
            this.mMovieStart = now;
        }
        if (this.mDuration == 0) {
            this.mDuration = this.mMovie.duration();
            if (this.mDuration <= 0) {
                this.mDuration = 10700;
            }
        }
        this.mCurrentAnimationTime = (int) ((now - this.mMovieStart) % ((long) this.mDuration));
    }

    private void drawMovieFrame(Canvas canvas) {
        if (this.mMovie != null) {
            this.mLeft = ((float) (getWidth() - this.mMovie.width())) / 2.0f;
            this.mTop = ((float) (getHeight() - this.mMovie.height())) / 2.0f;
            this.mMovie.setTime(this.mCurrentAnimationTime);
            canvas.save(1);
            this.mMovie.draw(canvas, this.mLeft, this.mTop);
            canvas.restore();
        }
    }

    public void onScreenStateChanged(int screenState) {
        boolean z = true;
        super.onScreenStateChanged(screenState);
        if (screenState != 1) {
            z = false;
        }
        this.mVisible = z;
        invalidateView();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        boolean z = false;
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        invalidateView();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        invalidateView();
    }
}
