package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.R$styleable;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

class ResizingTextView extends TextView {
    private float mDefaultLineSpacingExtra;
    private int mDefaultPaddingBottom;
    private int mDefaultPaddingTop;
    private int mDefaultTextSize;
    private boolean mDefaultsInitialized;
    private boolean mIsResized;
    private boolean mMaintainLineSpacing;
    private int mResizedPaddingAdjustmentBottom;
    private int mResizedPaddingAdjustmentTop;
    private int mResizedTextSize;
    private int mTriggerConditions;

    public ResizingTextView(Context ctx, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(ctx, attrs, defStyleAttr);
        this.mIsResized = false;
        this.mDefaultsInitialized = false;
        TypedArray a = ctx.obtainStyledAttributes(attrs, R$styleable.lbResizingTextView, defStyleAttr, defStyleRes);
        try {
            this.mTriggerConditions = a.getInt(R$styleable.lbResizingTextView_resizeTrigger, 1);
            this.mResizedTextSize = a.getDimensionPixelSize(R$styleable.lbResizingTextView_resizedTextSize, -1);
            this.mMaintainLineSpacing = a.getBoolean(R$styleable.lbResizingTextView_maintainLineSpacing, false);
            this.mResizedPaddingAdjustmentTop = a.getDimensionPixelOffset(R$styleable.lbResizingTextView_resizedPaddingAdjustmentTop, 0);
            this.mResizedPaddingAdjustmentBottom = a.getDimensionPixelOffset(R$styleable.lbResizingTextView_resizedPaddingAdjustmentBottom, 0);
        } finally {
            a.recycle();
        }
    }

    public ResizingTextView(Context ctx, AttributeSet attrs, int defStyleAttr) {
        this(ctx, attrs, defStyleAttr, 0);
    }

    public ResizingTextView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 16842884);
    }

    public ResizingTextView(Context ctx) {
        this(ctx, null);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!this.mDefaultsInitialized) {
            this.mDefaultTextSize = (int) getTextSize();
            this.mDefaultLineSpacingExtra = getLineSpacingExtra();
            this.mDefaultPaddingTop = getPaddingTop();
            this.mDefaultPaddingBottom = getPaddingBottom();
            this.mDefaultsInitialized = true;
        }
        setTextSize(0, (float) this.mDefaultTextSize);
        setLineSpacing(this.mDefaultLineSpacingExtra, getLineSpacingMultiplier());
        setPaddingTopAndBottom(this.mDefaultPaddingTop, this.mDefaultPaddingBottom);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean resizeText = false;
        Layout layout = getLayout();
        if (layout != null && (this.mTriggerConditions & 1) > 0) {
            int lineCount = layout.getLineCount();
            int maxLines = getMaxLines();
            if (maxLines > 1) {
                resizeText = lineCount == maxLines;
            }
        }
        int currentSizePx = (int) getTextSize();
        boolean z = false;
        if (resizeText) {
            if (!(this.mResizedTextSize == -1 || currentSizePx == this.mResizedTextSize)) {
                setTextSize(0, (float) this.mResizedTextSize);
                z = true;
            }
            float targetLineSpacingExtra = (this.mDefaultLineSpacingExtra + ((float) this.mDefaultTextSize)) - ((float) this.mResizedTextSize);
            if (this.mMaintainLineSpacing && getLineSpacingExtra() != targetLineSpacingExtra) {
                setLineSpacing(targetLineSpacingExtra, getLineSpacingMultiplier());
                z = true;
            }
            int paddingTop = this.mDefaultPaddingTop + this.mResizedPaddingAdjustmentTop;
            int paddingBottom = this.mDefaultPaddingBottom + this.mResizedPaddingAdjustmentBottom;
            if (!(getPaddingTop() == paddingTop && getPaddingBottom() == paddingBottom)) {
                setPaddingTopAndBottom(paddingTop, paddingBottom);
                z = true;
            }
        } else {
            if (!(this.mResizedTextSize == -1 || currentSizePx == this.mDefaultTextSize)) {
                setTextSize(0, (float) this.mDefaultTextSize);
                z = true;
            }
            if (this.mMaintainLineSpacing && getLineSpacingExtra() != this.mDefaultLineSpacingExtra) {
                setLineSpacing(this.mDefaultLineSpacingExtra, getLineSpacingMultiplier());
                z = true;
            }
            if (!(getPaddingTop() == this.mDefaultPaddingTop && getPaddingBottom() == this.mDefaultPaddingBottom)) {
                setPaddingTopAndBottom(this.mDefaultPaddingTop, this.mDefaultPaddingBottom);
                z = true;
            }
        }
        this.mIsResized = resizeText;
        if (z) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void setPaddingTopAndBottom(int paddingTop, int paddingBottom) {
        if (isPaddingRelative()) {
            setPaddingRelative(getPaddingStart(), paddingTop, getPaddingEnd(), paddingBottom);
        } else {
            setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), paddingBottom);
        }
    }
}
