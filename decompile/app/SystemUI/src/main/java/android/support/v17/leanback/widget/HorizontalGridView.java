package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class HorizontalGridView extends BaseGridView {
    private boolean mFadingHighEdge;
    private boolean mFadingLowEdge;
    private LinearGradient mHighFadeShader;
    private int mHighFadeShaderLength;
    private int mHighFadeShaderOffset;
    private LinearGradient mLowFadeShader;
    private int mLowFadeShaderLength;
    private int mLowFadeShaderOffset;
    private Bitmap mTempBitmapHigh;
    private Bitmap mTempBitmapLow;
    private Paint mTempPaint;
    private Rect mTempRect;

    public /* bridge */ /* synthetic */ boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return super.dispatchGenericFocusedEvent(event);
    }

    public /* bridge */ /* synthetic */ boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public /* bridge */ /* synthetic */ boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public /* bridge */ /* synthetic */ View focusSearch(int direction) {
        return super.focusSearch(direction);
    }

    public /* bridge */ /* synthetic */ int getChildDrawingOrder(int childCount, int i) {
        return super.getChildDrawingOrder(childCount, i);
    }

    public /* bridge */ /* synthetic */ int getSelectedPosition() {
        return super.getSelectedPosition();
    }

    public /* bridge */ /* synthetic */ boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering();
    }

    public /* bridge */ /* synthetic */ boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public /* bridge */ /* synthetic */ void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
    }

    public /* bridge */ /* synthetic */ void setGravity(int gravity) {
        super.setGravity(gravity);
    }

    public /* bridge */ /* synthetic */ void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        super.setOnChildViewHolderSelectedListener(listener);
    }

    public /* bridge */ /* synthetic */ void setRecyclerListener(RecyclerListener listener) {
        super.setRecyclerListener(listener);
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int position) {
        super.setSelectedPosition(position);
    }

    public /* bridge */ /* synthetic */ void setSelectedPositionSmooth(int position) {
        super.setSelectedPositionSmooth(position);
    }

    public /* bridge */ /* synthetic */ void setWindowAlignment(int windowAlignment) {
        super.setWindowAlignment(windowAlignment);
    }

    public HorizontalGridView(Context context) {
        this(context, null);
    }

    public HorizontalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTempPaint = new Paint();
        this.mTempRect = new Rect();
        this.mLayoutManager.setOrientation(0);
        initAttributes(context, attrs);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        initBaseGridViewAttributes(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.lbHorizontalGridView);
        setRowHeight(a);
        setNumRows(a.getInt(R$styleable.lbHorizontalGridView_numberOfRows, 1));
        a.recycle();
        updateLayerType();
        this.mTempPaint = new Paint();
        this.mTempPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }

    void setRowHeight(TypedArray array) {
        if (array.peekValue(R$styleable.lbHorizontalGridView_rowHeight) != null) {
            setRowHeight(array.getLayoutDimension(R$styleable.lbHorizontalGridView_rowHeight, 0));
        }
    }

    public void setNumRows(int numRows) {
        this.mLayoutManager.setNumRows(numRows);
        requestLayout();
    }

    public void setRowHeight(int height) {
        this.mLayoutManager.setRowHeight(height);
        requestLayout();
    }

    private boolean needsFadingLowEdge() {
        if (!this.mFadingLowEdge) {
            return false;
        }
        int c = getChildCount();
        for (int i = 0; i < c; i++) {
            if (this.mLayoutManager.getOpticalLeft(getChildAt(i)) < getPaddingLeft() - this.mLowFadeShaderOffset) {
                return true;
            }
        }
        return false;
    }

    private boolean needsFadingHighEdge() {
        if (!this.mFadingHighEdge) {
            return false;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (this.mLayoutManager.getOpticalRight(getChildAt(i)) > (getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset) {
                return true;
            }
        }
        return false;
    }

    private Bitmap getTempBitmapLow() {
        if (this.mTempBitmapLow != null && this.mTempBitmapLow.getWidth() == this.mLowFadeShaderLength) {
            if (this.mTempBitmapLow.getHeight() != getHeight()) {
            }
            return this.mTempBitmapLow;
        }
        this.mTempBitmapLow = Bitmap.createBitmap(this.mLowFadeShaderLength, getHeight(), Config.ARGB_8888);
        return this.mTempBitmapLow;
    }

    private Bitmap getTempBitmapHigh() {
        if (this.mTempBitmapHigh != null && this.mTempBitmapHigh.getWidth() == this.mHighFadeShaderLength) {
            if (this.mTempBitmapHigh.getHeight() != getHeight()) {
            }
            return this.mTempBitmapHigh;
        }
        this.mTempBitmapHigh = Bitmap.createBitmap(this.mHighFadeShaderLength, getHeight(), Config.ARGB_8888);
        return this.mTempBitmapHigh;
    }

    public void draw(Canvas canvas) {
        boolean needsFadingLow = needsFadingLowEdge();
        boolean needsFadingHigh = needsFadingHighEdge();
        if (!needsFadingLow) {
            this.mTempBitmapLow = null;
        }
        if (!needsFadingHigh) {
            this.mTempBitmapHigh = null;
        }
        if (needsFadingLow || needsFadingHigh) {
            int highEdge;
            Bitmap tempBitmap;
            int tmpSave;
            int lowEdge = this.mFadingLowEdge ? (getPaddingLeft() - this.mLowFadeShaderOffset) - this.mLowFadeShaderLength : 0;
            if (this.mFadingHighEdge) {
                highEdge = ((getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset) + this.mHighFadeShaderLength;
            } else {
                highEdge = getWidth();
            }
            int save = canvas.save();
            canvas.clipRect(lowEdge + (this.mFadingLowEdge ? this.mLowFadeShaderLength : 0), 0, highEdge - (this.mFadingHighEdge ? this.mHighFadeShaderLength : 0), getHeight());
            super.draw(canvas);
            canvas.restoreToCount(save);
            Canvas tmpCanvas = new Canvas();
            this.mTempRect.top = 0;
            this.mTempRect.bottom = getHeight();
            if (needsFadingLow && this.mLowFadeShaderLength > 0) {
                tempBitmap = getTempBitmapLow();
                tempBitmap.eraseColor(0);
                tmpCanvas.setBitmap(tempBitmap);
                tmpSave = tmpCanvas.save();
                tmpCanvas.clipRect(0, 0, this.mLowFadeShaderLength, getHeight());
                tmpCanvas.translate((float) (-lowEdge), 0.0f);
                super.draw(tmpCanvas);
                tmpCanvas.restoreToCount(tmpSave);
                this.mTempPaint.setShader(this.mLowFadeShader);
                tmpCanvas.drawRect(0.0f, 0.0f, (float) this.mLowFadeShaderLength, (float) getHeight(), this.mTempPaint);
                this.mTempRect.left = 0;
                this.mTempRect.right = this.mLowFadeShaderLength;
                canvas.translate((float) lowEdge, 0.0f);
                canvas.drawBitmap(tempBitmap, this.mTempRect, this.mTempRect, null);
                canvas.translate((float) (-lowEdge), 0.0f);
            }
            if (needsFadingHigh && this.mHighFadeShaderLength > 0) {
                tempBitmap = getTempBitmapHigh();
                tempBitmap.eraseColor(0);
                tmpCanvas.setBitmap(tempBitmap);
                tmpSave = tmpCanvas.save();
                tmpCanvas.clipRect(0, 0, this.mHighFadeShaderLength, getHeight());
                tmpCanvas.translate((float) (-(highEdge - this.mHighFadeShaderLength)), 0.0f);
                super.draw(tmpCanvas);
                tmpCanvas.restoreToCount(tmpSave);
                this.mTempPaint.setShader(this.mHighFadeShader);
                tmpCanvas.drawRect(0.0f, 0.0f, (float) this.mHighFadeShaderLength, (float) getHeight(), this.mTempPaint);
                this.mTempRect.left = 0;
                this.mTempRect.right = this.mHighFadeShaderLength;
                canvas.translate((float) (highEdge - this.mHighFadeShaderLength), 0.0f);
                canvas.drawBitmap(tempBitmap, this.mTempRect, this.mTempRect, null);
                canvas.translate((float) (-(highEdge - this.mHighFadeShaderLength)), 0.0f);
            }
            return;
        }
        super.draw(canvas);
    }

    private void updateLayerType() {
        if (this.mFadingLowEdge || this.mFadingHighEdge) {
            setLayerType(2, null);
            setWillNotDraw(false);
            return;
        }
        setLayerType(0, null);
        setWillNotDraw(true);
    }
}
