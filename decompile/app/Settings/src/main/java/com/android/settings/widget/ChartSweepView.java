package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import com.android.internal.util.Preconditions;
import com.android.settings.R$styleable;

public class ChartSweepView extends View {
    private ChartAxis mAxis;
    private OnClickListener mClickListener;
    private Rect mContentOffset;
    private long mDragInterval;
    private int mFollowAxis;
    private int mLabelColor;
    private DynamicLayout mLabelLayout;
    private int mLabelMinSize;
    private float mLabelOffset;
    private float mLabelSize;
    private SpannableStringBuilder mLabelTemplate;
    private int mLabelTemplateRes;
    private long mLabelValue;
    private OnSweepListener mListener;
    private Rect mMargins;
    private float mNeighborMargin;
    private ChartSweepView[] mNeighbors;
    private Paint mOutlinePaint;
    private int mSafeRegion;
    private Drawable mSweep;
    private Point mSweepOffset;
    private Rect mSweepPadding;
    private int mTouchMode;
    private MotionEvent mTracking;
    private float mTrackingStart;
    private long mValidAfter;
    private ChartSweepView mValidAfterDynamic;
    private long mValidBefore;
    private ChartSweepView mValidBeforeDynamic;
    private long mValue;

    public interface OnSweepListener {
        void onSweep(ChartSweepView chartSweepView, boolean z);

        void requestEdit(ChartSweepView chartSweepView);
    }

    public ChartSweepView(Context context) {
        this(context, null);
    }

    public ChartSweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartSweepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSweepPadding = new Rect();
        this.mContentOffset = new Rect();
        this.mSweepOffset = new Point();
        this.mMargins = new Rect();
        this.mOutlinePaint = new Paint();
        this.mTouchMode = 0;
        this.mDragInterval = 1;
        this.mNeighbors = new ChartSweepView[0];
        this.mClickListener = new OnClickListener() {
            public void onClick(View v) {
                ChartSweepView.this.dispatchRequestEdit();
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ChartSweepView, defStyle, 0);
        int color = a.getColor(5, -16776961);
        setSweepDrawable(a.getDrawable(0), color);
        setFollowAxis(a.getInt(1, -1));
        setNeighborMargin((float) a.getDimensionPixelSize(2, 0));
        setSafeRegion(a.getDimensionPixelSize(6, 0));
        setLabelMinSize(a.getDimensionPixelSize(3, 0));
        setLabelTemplate(a.getResourceId(4, 0));
        setLabelColor(color);
        setBackgroundResource(2130837673);
        this.mOutlinePaint.setColor(-65536);
        this.mOutlinePaint.setStrokeWidth(1.0f);
        this.mOutlinePaint.setStyle(Style.STROKE);
        a.recycle();
        setClickable(true);
        setOnClickListener(this.mClickListener);
        setWillNotDraw(false);
    }

    void init(ChartAxis axis) {
        this.mAxis = (ChartAxis) Preconditions.checkNotNull(axis, "missing axis");
    }

    public void setNeighbors(ChartSweepView... neighbors) {
        this.mNeighbors = neighbors;
    }

    public int getFollowAxis() {
        return this.mFollowAxis;
    }

    public Rect getMargins() {
        return this.mMargins;
    }

    public void setDragInterval(long dragInterval) {
        this.mDragInterval = dragInterval;
    }

    private float getTargetInset() {
        if (this.mFollowAxis == 1) {
            return (((float) this.mSweepPadding.top) + (((float) ((this.mSweep.getIntrinsicHeight() - this.mSweepPadding.top) - this.mSweepPadding.bottom)) / 2.0f)) + ((float) this.mSweepOffset.y);
        }
        return (((float) this.mSweepPadding.left) + (((float) ((this.mSweep.getIntrinsicWidth() - this.mSweepPadding.left) - this.mSweepPadding.right)) / 2.0f)) + ((float) this.mSweepOffset.x);
    }

    public void addOnSweepListener(OnSweepListener listener) {
        this.mListener = listener;
    }

    private void dispatchOnSweep(boolean sweepDone) {
        if (this.mListener != null) {
            this.mListener.onSweep(this, sweepDone);
        }
    }

    private void dispatchRequestEdit() {
        if (this.mListener != null) {
            this.mListener.requestEdit(this);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setFocusable(enabled);
        requestLayout();
    }

    public void setSweepDrawable(Drawable sweep, int color) {
        if (this.mSweep != null) {
            this.mSweep.setCallback(null);
            unscheduleDrawable(this.mSweep);
        }
        if (sweep != null) {
            sweep.setCallback(this);
            if (sweep.isStateful()) {
                sweep.setState(getDrawableState());
            }
            sweep.setVisible(getVisibility() == 0, false);
            this.mSweep = sweep;
            this.mSweep.setTint(color);
            sweep.getPadding(this.mSweepPadding);
        } else {
            this.mSweep = null;
        }
        invalidate();
    }

    public void setFollowAxis(int followAxis) {
        this.mFollowAxis = followAxis;
    }

    public void setLabelMinSize(int minSize) {
        this.mLabelMinSize = minSize;
        invalidateLabelTemplate();
    }

    public void setLabelTemplate(int resId) {
        this.mLabelTemplateRes = resId;
        invalidateLabelTemplate();
    }

    public void setLabelColor(int color) {
        this.mLabelColor = color;
        invalidateLabelTemplate();
    }

    private void invalidateLabelTemplate() {
        if (this.mLabelTemplateRes != 0) {
            CharSequence template = getResources().getText(this.mLabelTemplateRes);
            TextPaint paint = new TextPaint(1);
            paint.density = getResources().getDisplayMetrics().density;
            paint.setCompatibilityScaling(getResources().getCompatibilityInfo().applicationScale);
            paint.setColor(this.mLabelColor);
            paint.setShadowLayer(paint.density * 4.0f, 0.0f, 0.0f, Color.rgb(232, 232, 232));
            this.mLabelTemplate = new SpannableStringBuilder(template);
            this.mLabelLayout = new DynamicLayout(this.mLabelTemplate, paint, 1024, Alignment.ALIGN_RIGHT, 1.0f, 0.0f, false);
            invalidateLabel();
        } else {
            this.mLabelTemplate = null;
            this.mLabelLayout = null;
        }
        invalidate();
        requestLayout();
    }

    private void invalidateLabel() {
        if (this.mLabelTemplate == null || this.mAxis == null) {
            this.mLabelValue = this.mValue;
            return;
        }
        this.mLabelValue = this.mAxis.buildLabel(getResources(), this.mLabelTemplate, this.mValue);
        setContentDescription(this.mLabelTemplate);
        invalidateLabelOffset();
        invalidate();
    }

    public void invalidateLabelOffset() {
        float labelOffset = 0.0f;
        if (this.mFollowAxis == 1) {
            float margin;
            if (this.mValidAfterDynamic != null) {
                this.mLabelSize = Math.max(getLabelWidth(this), getLabelWidth(this.mValidAfterDynamic));
                margin = getLabelTop(this.mValidAfterDynamic) - getLabelBottom(this);
                if (margin < 0.0f) {
                    labelOffset = margin / 2.0f;
                }
            } else if (this.mValidBeforeDynamic != null) {
                this.mLabelSize = Math.max(getLabelWidth(this), getLabelWidth(this.mValidBeforeDynamic));
                margin = getLabelTop(this) - getLabelBottom(this.mValidBeforeDynamic);
                if (margin < 0.0f) {
                    labelOffset = (-margin) / 2.0f;
                }
            } else {
                this.mLabelSize = getLabelWidth(this);
            }
        }
        this.mLabelSize = Math.max(this.mLabelSize, (float) this.mLabelMinSize);
        if (labelOffset != this.mLabelOffset) {
            this.mLabelOffset = labelOffset;
            invalidate();
            if (this.mValidAfterDynamic != null) {
                this.mValidAfterDynamic.invalidateLabelOffset();
            }
            if (this.mValidBeforeDynamic != null) {
                this.mValidBeforeDynamic.invalidateLabelOffset();
            }
        }
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSweep != null) {
            this.mSweep.jumpToCurrentState();
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mSweep != null) {
            boolean z;
            Drawable drawable = this.mSweep;
            if (visibility == 0) {
                z = true;
            } else {
                z = false;
            }
            drawable.setVisible(z, false);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return who != this.mSweep ? super.verifyDrawable(who) : true;
    }

    public void setValue(long value) {
        this.mValue = value;
        invalidateLabel();
    }

    public long getValue() {
        return this.mValue;
    }

    public float getPoint() {
        if (isEnabled()) {
            return this.mAxis.convertToPoint(this.mValue);
        }
        return 0.0f;
    }

    public void setValidRange(long validAfter, long validBefore) {
        this.mValidAfter = validAfter;
        this.mValidBefore = validBefore;
    }

    public void setNeighborMargin(float neighborMargin) {
        this.mNeighborMargin = neighborMargin;
    }

    public void setSafeRegion(int safeRegion) {
        this.mSafeRegion = safeRegion;
    }

    public void setValidRangeDynamic(ChartSweepView validAfter, ChartSweepView validBefore) {
        this.mValidAfterDynamic = validAfter;
        this.mValidBeforeDynamic = validBefore;
    }

    public boolean isTouchCloserTo(MotionEvent eventInParent, ChartSweepView another) {
        return another.getTouchDistanceFromTarget(eventInParent) < getTouchDistanceFromTarget(eventInParent);
    }

    private float getTouchDistanceFromTarget(MotionEvent eventInParent) {
        if (this.mFollowAxis == 0) {
            return Math.abs(eventInParent.getX() - (getX() + getTargetInset()));
        }
        return Math.abs(eventInParent.getY() - (getY() + getTargetInset()));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        View parent = (View) getParent();
        switch (event.getAction()) {
            case 0:
                boolean acceptDrag;
                boolean acceptLabel;
                if (this.mFollowAxis == 1) {
                    acceptDrag = event.getX() > ((float) (getWidth() - (this.mSweepPadding.right * 8)));
                    acceptLabel = this.mLabelLayout != null ? event.getX() < ((float) this.mLabelLayout.getWidth()) : false;
                } else {
                    acceptDrag = event.getY() > ((float) (getHeight() - (this.mSweepPadding.bottom * 8)));
                    acceptLabel = this.mLabelLayout != null ? event.getY() < ((float) this.mLabelLayout.getHeight()) : false;
                }
                MotionEvent eventInParent = event.copy();
                eventInParent.offsetLocation((float) getLeft(), (float) getTop());
                for (ChartSweepView neighbor : this.mNeighbors) {
                    if (isTouchCloserTo(eventInParent, neighbor)) {
                        return false;
                    }
                }
                if (acceptDrag) {
                    if (this.mFollowAxis == 1) {
                        this.mTrackingStart = (float) (getTop() - this.mMargins.top);
                    } else {
                        this.mTrackingStart = (float) (getLeft() - this.mMargins.left);
                    }
                    this.mTracking = event.copy();
                    this.mTouchMode = 1;
                    if (!parent.isActivated()) {
                        parent.setActivated(true);
                    }
                    return true;
                } else if (acceptLabel) {
                    this.mTouchMode = 2;
                    return true;
                } else {
                    this.mTouchMode = 0;
                    return false;
                }
            case 1:
                if (this.mTouchMode == 2) {
                    performClick();
                } else if (this.mTouchMode == 1) {
                    this.mTrackingStart = 0.0f;
                    this.mTracking = null;
                    this.mValue = this.mLabelValue;
                    dispatchOnSweep(true);
                    setTranslationX(0.0f);
                    setTranslationY(0.0f);
                    requestLayout();
                }
                this.mTouchMode = 0;
                return true;
            case 2:
                if (this.mTouchMode == 2) {
                    return true;
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                Rect parentContent = getParentContentRect();
                Rect clampRect = computeClampRect(parentContent);
                if (clampRect.isEmpty()) {
                    return true;
                }
                long value;
                if (this.mFollowAxis == 1) {
                    float currentTargetY = (float) (getTop() - this.mMargins.top);
                    float clampedTargetY = MathUtils.constrain(this.mTrackingStart + (event.getRawY() - this.mTracking.getRawY()), (float) clampRect.top, (float) clampRect.bottom);
                    setTranslationY(clampedTargetY - currentTargetY);
                    value = this.mAxis.convertToValue(clampedTargetY - ((float) parentContent.top));
                } else {
                    float currentTargetX = (float) (getLeft() - this.mMargins.left);
                    float clampedTargetX = MathUtils.constrain(this.mTrackingStart + (event.getRawX() - this.mTracking.getRawX()), (float) clampRect.left, (float) clampRect.right);
                    setTranslationX(clampedTargetX - currentTargetX);
                    value = this.mAxis.convertToValue(clampedTargetX - ((float) parentContent.left));
                }
                setValue(WidgetExtUtils.getValueofDataUsage(value - (value % this.mDragInterval), this.mFollowAxis, this.mValidAfterDynamic));
                dispatchOnSweep(false);
                return true;
            default:
                return false;
        }
    }

    public void updateValueFromPosition() {
        Rect parentContent = getParentContentRect();
        if (this.mFollowAxis == 1) {
            setValue(this.mAxis.convertToValue((getY() - ((float) this.mMargins.top)) - ((float) parentContent.top)));
            return;
        }
        setValue(this.mAxis.convertToValue((getX() - ((float) this.mMargins.left)) - ((float) parentContent.left)));
    }

    public int shouldAdjustAxis() {
        return this.mAxis.shouldAdjustAxis(getValue());
    }

    private Rect getParentContentRect() {
        View parent = (View) getParent();
        return new Rect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getWidth() - parent.getPaddingRight(), parent.getHeight() - parent.getPaddingBottom());
    }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
    }

    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {
    }

    private long getValidAfterDynamic() {
        ChartSweepView dynamic = this.mValidAfterDynamic;
        return (dynamic == null || !dynamic.isEnabled()) ? Long.MIN_VALUE : dynamic.getValue();
    }

    private long getValidBeforeDynamic() {
        ChartSweepView dynamic = this.mValidBeforeDynamic;
        return (dynamic == null || !dynamic.isEnabled()) ? Long.MAX_VALUE : dynamic.getValue();
    }

    private Rect computeClampRect(Rect parentContent) {
        Rect rect = buildClampRect(parentContent, this.mValidAfter, this.mValidBefore, 0.0f);
        if (!rect.intersect(buildClampRect(parentContent, getValidAfterDynamic(), getValidBeforeDynamic(), this.mNeighborMargin))) {
            rect.setEmpty();
        }
        return rect;
    }

    private Rect buildClampRect(Rect parentContent, long afterValue, long beforeValue, float margin) {
        if (this.mAxis instanceof InvertedChartAxis) {
            long temp = beforeValue;
            beforeValue = afterValue;
            afterValue = temp;
        }
        boolean afterValid = (afterValue == Long.MIN_VALUE || afterValue == Long.MAX_VALUE) ? false : true;
        boolean beforeValid = (beforeValue == Long.MIN_VALUE || beforeValue == Long.MAX_VALUE) ? false : true;
        float afterPoint = this.mAxis.convertToPoint(afterValue) + margin;
        float beforePoint = this.mAxis.convertToPoint(beforeValue) - margin;
        Rect clampRect = new Rect(parentContent);
        if (this.mFollowAxis == 1) {
            if (beforeValid) {
                clampRect.bottom = clampRect.top + ((int) beforePoint);
            }
            if (afterValid) {
                clampRect.top = (int) (((float) clampRect.top) + afterPoint);
            }
        } else {
            if (beforeValid) {
                clampRect.right = clampRect.left + ((int) beforePoint);
            }
            if (afterValid) {
                clampRect.left = (int) (((float) clampRect.left) + afterPoint);
            }
        }
        return clampRect;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mSweep.isStateful()) {
            this.mSweep.setState(getDrawableState());
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isEnabled() || this.mLabelLayout == null) {
            this.mSweepOffset.x = 0;
            this.mSweepOffset.y = 0;
            setMeasuredDimension(this.mSweep.getIntrinsicWidth(), this.mSweep.getIntrinsicHeight());
        } else {
            int sweepHeight = this.mSweep.getIntrinsicHeight();
            int templateHeight = this.mLabelLayout.getHeight();
            this.mSweepOffset.x = 0;
            this.mSweepOffset.y = 0;
            this.mSweepOffset.y = (int) (((float) (templateHeight / 2)) - getTargetInset());
            setMeasuredDimension(this.mSweep.getIntrinsicWidth(), Math.max(sweepHeight, templateHeight));
        }
        if (this.mFollowAxis == 1) {
            int targetHeight = (this.mSweep.getIntrinsicHeight() - this.mSweepPadding.top) - this.mSweepPadding.bottom;
            this.mMargins.top = -(this.mSweepPadding.top + (targetHeight / 2));
            this.mMargins.bottom = 0;
            this.mMargins.left = -this.mSweepPadding.left;
            this.mMargins.right = this.mSweepPadding.right;
        } else {
            int targetWidth = (this.mSweep.getIntrinsicWidth() - this.mSweepPadding.left) - this.mSweepPadding.right;
            this.mMargins.left = -(this.mSweepPadding.left + (targetWidth / 2));
            this.mMargins.right = 0;
            this.mMargins.top = -this.mSweepPadding.top;
            this.mMargins.bottom = this.mSweepPadding.bottom;
        }
        this.mContentOffset.set(0, 0, 0, 0);
        int widthBefore = getMeasuredWidth();
        int heightBefore = getMeasuredHeight();
        int offset;
        Rect rect;
        if (this.mFollowAxis == 0) {
            int widthAfter = widthBefore * 3;
            setMeasuredDimension(widthAfter, heightBefore);
            this.mContentOffset.left = (widthAfter - widthBefore) / 2;
            offset = this.mSweepPadding.bottom * 2;
            rect = this.mContentOffset;
            rect.bottom -= offset;
            rect = this.mMargins;
            rect.bottom += offset;
        } else {
            int heightAfter = heightBefore * 2;
            setMeasuredDimension(widthBefore, heightAfter);
            this.mContentOffset.offset(0, (heightAfter - heightBefore) / 2);
            offset = this.mSweepPadding.right * 2;
            rect = this.mContentOffset;
            rect.right -= offset;
            rect = this.mMargins;
            rect.right += offset;
        }
        this.mSweepOffset.offset(this.mContentOffset.left, this.mContentOffset.top);
        this.mMargins.offset(-this.mSweepOffset.x, -this.mSweepOffset.y);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        invalidateLabelOffset();
    }

    protected void onDraw(Canvas canvas) {
        int labelSize;
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (!isEnabled() || this.mLabelLayout == null) {
            labelSize = 0;
        } else {
            int count = canvas.save();
            canvas.translate(((float) this.mContentOffset.left) + (this.mLabelSize - 1024.0f), ((float) this.mContentOffset.top) + this.mLabelOffset);
            this.mLabelLayout.draw(canvas);
            canvas.restoreToCount(count);
            labelSize = ((int) this.mLabelSize) + this.mSafeRegion;
        }
        if (this.mFollowAxis == 1) {
            this.mSweep.setBounds(labelSize, this.mSweepOffset.y, this.mContentOffset.right + width, this.mSweepOffset.y + this.mSweep.getIntrinsicHeight());
        } else {
            this.mSweep.setBounds(this.mSweepOffset.x, labelSize, this.mSweepOffset.x + this.mSweep.getIntrinsicWidth(), this.mContentOffset.bottom + height);
        }
        this.mSweep.draw(canvas);
    }

    public static float getLabelTop(ChartSweepView view) {
        return view.getY() + ((float) view.mContentOffset.top);
    }

    public static float getLabelBottom(ChartSweepView view) {
        return getLabelTop(view) + ((float) view.mLabelLayout.getHeight());
    }

    public static float getLabelWidth(ChartSweepView view) {
        return Layout.getDesiredWidth(view.mLabelLayout.getText(), view.mLabelLayout.getPaint());
    }
}
