package com.android.systemui.recents.views;

import android.app.ActivityManager.TaskThumbnailInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import com.android.systemui.R;
import com.android.systemui.recents.model.Task;

public class TaskViewThumbnail extends View {
    private static final ColorMatrix TMP_BRIGHTNESS_COLOR_MATRIX = new ColorMatrix();
    private static final ColorMatrix TMP_FILTER_COLOR_MATRIX = new ColorMatrix();
    private Paint mBgFillPaint;
    private BitmapShader mBitmapShader;
    private int mCornerRadius;
    @ExportedProperty(category = "recents")
    private float mDimAlpha;
    @ExportedProperty(category = "recents")
    private boolean mDisabledInSafeMode;
    private int mDisplayOrientation;
    private Rect mDisplayRect;
    private Paint mDrawPaint;
    private float mFullscreenThumbnailScale;
    @ExportedProperty(category = "recents")
    private boolean mInvisible;
    private LightingColorFilter mLightingColorFilter;
    private Matrix mScaleMatrix;
    private Task mTask;
    private View mTaskBar;
    @ExportedProperty(category = "recents")
    private Rect mTaskViewRect;
    private TaskThumbnailInfo mThumbnailInfo;
    @ExportedProperty(category = "recents")
    private Rect mThumbnailRect;
    @ExportedProperty(category = "recents")
    private float mThumbnailScale;

    public TaskViewThumbnail(Context context) {
        this(context, null);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDisplayOrientation = 0;
        this.mDisplayRect = new Rect();
        this.mTaskViewRect = new Rect();
        this.mThumbnailRect = new Rect();
        this.mScaleMatrix = new Matrix();
        this.mDrawPaint = new Paint();
        this.mBgFillPaint = new Paint();
        this.mLightingColorFilter = new LightingColorFilter(-1, 0);
        this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
        this.mDrawPaint.setFilterBitmap(true);
        this.mDrawPaint.setAntiAlias(true);
        this.mCornerRadius = getResources().getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mBgFillPaint.setColor(-1);
        this.mFullscreenThumbnailScale = context.getResources().getFraction(18022406, 1, 1);
    }

    public void onTaskViewSizeChanged(int width, int height) {
        if (this.mTaskViewRect.width() != width || this.mTaskViewRect.height() != height) {
            this.mTaskViewRect.set(0, 0, width, height);
            setLeftTopRightBottom(0, 0, width, height);
            updateThumbnailScale();
        }
    }

    protected void onDraw(Canvas canvas) {
        if (!this.mInvisible) {
            int viewWidth = this.mTaskViewRect.width();
            int viewHeight = this.mTaskViewRect.height();
            int thumbnailWidth = Math.min(viewWidth, (int) (((float) this.mThumbnailRect.width()) * this.mThumbnailScale));
            int thumbnailHeight = Math.min(viewHeight, (int) (((float) this.mThumbnailRect.height()) * this.mThumbnailScale));
            if (this.mBitmapShader == null || thumbnailWidth <= 0 || thumbnailHeight <= 0) {
                canvas.drawRoundRect(0.0f, 0.0f, (float) viewWidth, (float) viewHeight, (float) this.mCornerRadius, (float) this.mCornerRadius, this.mBgFillPaint);
            } else {
                int topOffset;
                if (this.mTaskBar != null) {
                    topOffset = this.mTaskBar.getHeight() - this.mCornerRadius;
                } else {
                    topOffset = 0;
                }
                if (thumbnailWidth < viewWidth) {
                    canvas.drawRoundRect((float) Math.max(0, thumbnailWidth - this.mCornerRadius), (float) topOffset, (float) viewWidth, (float) viewHeight, (float) this.mCornerRadius, (float) this.mCornerRadius, this.mBgFillPaint);
                }
                if (thumbnailHeight < viewHeight) {
                    canvas.drawRoundRect(0.0f, (float) Math.max(topOffset, thumbnailHeight - this.mCornerRadius), (float) viewWidth, (float) viewHeight, (float) this.mCornerRadius, (float) this.mCornerRadius, this.mBgFillPaint);
                }
                canvas.drawRoundRect(0.0f, (float) topOffset, (float) thumbnailWidth, (float) thumbnailHeight, (float) this.mCornerRadius, (float) this.mCornerRadius, this.mDrawPaint);
            }
        }
    }

    void setThumbnail(Bitmap bm, TaskThumbnailInfo thumbnailInfo) {
        if (bm != null) {
            bm.prepareToDraw();
            this.mBitmapShader = new BitmapShader(bm, TileMode.CLAMP, TileMode.CLAMP);
            this.mDrawPaint.setShader(this.mBitmapShader);
            this.mThumbnailRect.set(0, 0, bm.getWidth(), bm.getHeight());
            this.mThumbnailInfo = thumbnailInfo;
            updateThumbnailScale();
            return;
        }
        this.mBitmapShader = null;
        this.mDrawPaint.setShader(null);
        this.mThumbnailRect.setEmpty();
        this.mThumbnailInfo = null;
    }

    void updateThumbnailPaintFilter() {
        if (!this.mInvisible) {
            int mul = (int) ((1.0f - this.mDimAlpha) * 255.0f);
            if (this.mBitmapShader == null) {
                int grey = mul;
                this.mDrawPaint.setColorFilter(null);
                this.mDrawPaint.setColor(Color.argb(255, mul, mul, mul));
            } else if (this.mDisabledInSafeMode) {
                TMP_FILTER_COLOR_MATRIX.setSaturation(0.0f);
                float scale = 1.0f - this.mDimAlpha;
                float[] mat = TMP_BRIGHTNESS_COLOR_MATRIX.getArray();
                mat[0] = scale;
                mat[6] = scale;
                mat[12] = scale;
                mat[4] = this.mDimAlpha * 255.0f;
                mat[9] = this.mDimAlpha * 255.0f;
                mat[14] = this.mDimAlpha * 255.0f;
                TMP_FILTER_COLOR_MATRIX.preConcat(TMP_BRIGHTNESS_COLOR_MATRIX);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(TMP_FILTER_COLOR_MATRIX);
                this.mDrawPaint.setColorFilter(filter);
                this.mBgFillPaint.setColorFilter(filter);
            } else {
                this.mLightingColorFilter.setColorMultiply(Color.argb(255, mul, mul, mul));
                this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
                this.mDrawPaint.setColor(-1);
                this.mBgFillPaint.setColorFilter(this.mLightingColorFilter);
            }
            if (!this.mInvisible) {
                invalidate();
            }
        }
    }

    public void updateThumbnailScale() {
        this.mThumbnailScale = 1.0f;
        if (this.mBitmapShader != null) {
            boolean isStackTask = !this.mTask.isFreeformTask() || this.mTask.bounds == null;
            if (this.mTaskViewRect.isEmpty() || this.mThumbnailInfo == null || this.mThumbnailInfo.taskWidth == 0 || this.mThumbnailInfo.taskHeight == 0) {
                this.mThumbnailScale = 0.0f;
            } else if (isStackTask) {
                float invThumbnailScale = 1.0f / this.mFullscreenThumbnailScale;
                if (this.mDisplayOrientation != 1) {
                    this.mThumbnailScale = invThumbnailScale;
                } else if (this.mThumbnailInfo.screenOrientation == 1) {
                    this.mThumbnailScale = ((float) this.mTaskViewRect.width()) / ((float) this.mThumbnailRect.width());
                } else {
                    this.mThumbnailScale = (((float) this.mTaskViewRect.width()) / ((float) this.mDisplayRect.width())) * invThumbnailScale;
                }
            } else {
                this.mThumbnailScale = Math.min(((float) this.mTaskViewRect.width()) / ((float) this.mThumbnailRect.width()), ((float) this.mTaskViewRect.height()) / ((float) this.mThumbnailRect.height()));
            }
            this.mScaleMatrix.setScale(this.mThumbnailScale, this.mThumbnailScale);
            this.mBitmapShader.setLocalMatrix(this.mScaleMatrix);
        }
        if (!this.mInvisible) {
            invalidate();
        }
    }

    void updateClipToTaskBar(View taskBar) {
        this.mTaskBar = taskBar;
        invalidate();
    }

    void updateThumbnailVisibility(int clipBottom) {
        boolean invisible = this.mTaskBar != null && getHeight() - clipBottom <= this.mTaskBar.getHeight();
        if (invisible != this.mInvisible) {
            this.mInvisible = invisible;
            if (!this.mInvisible) {
                updateThumbnailPaintFilter();
            }
        }
    }

    public void setDimAlpha(float dimAlpha) {
        this.mDimAlpha = dimAlpha;
        updateThumbnailPaintFilter();
    }

    void bindToTask(Task t, boolean disabledInSafeMode, int displayOrientation, Rect displayRect) {
        this.mTask = t;
        this.mDisabledInSafeMode = disabledInSafeMode;
        this.mDisplayOrientation = displayOrientation;
        this.mDisplayRect.set(displayRect);
        if (t.colorBackground != 0) {
            this.mBgFillPaint.setColor(t.colorBackground);
        }
    }

    void onTaskDataLoaded(TaskThumbnailInfo thumbnailInfo) {
        if (this.mTask.thumbnail != null) {
            setThumbnail(this.mTask.thumbnail, thumbnailInfo);
        } else {
            setThumbnail(null, null);
        }
    }

    void unbindFromTask() {
        this.mTask = null;
        setThumbnail(null, null);
    }
}
