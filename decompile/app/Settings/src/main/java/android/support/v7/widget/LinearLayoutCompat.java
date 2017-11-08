package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class LinearLayoutCompat extends ViewGroup {
    private boolean mBaselineAligned;
    private int mBaselineAlignedChildIndex;
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mGravity;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    private int mOrientation;
    private int mShowDividers;
    private int mTotalLength;
    private boolean mUseLargestChild;
    private float mWeightSum;

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity;
        public float weight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = -1;
            TypedArray a = c.obtainStyledAttributes(attrs, R$styleable.LinearLayoutCompat_Layout);
            this.weight = a.getFloat(R$styleable.LinearLayoutCompat_Layout_android_layout_weight, 0.0f);
            this.gravity = a.getInt(R$styleable.LinearLayoutCompat_Layout_android_layout_gravity, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
            this.gravity = -1;
        }
    }

    public LinearLayoutCompat(Context context) {
        this(context, null);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearLayoutCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R$styleable.LinearLayoutCompat, defStyleAttr, 0);
        int index = a.getInt(R$styleable.LinearLayoutCompat_android_orientation, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        index = a.getInt(R$styleable.LinearLayoutCompat_android_gravity, -1);
        if (index >= 0) {
            setGravity(index);
        }
        boolean baselineAligned = a.getBoolean(R$styleable.LinearLayoutCompat_android_baselineAligned, true);
        if (!baselineAligned) {
            setBaselineAligned(baselineAligned);
        }
        this.mWeightSum = a.getFloat(R$styleable.LinearLayoutCompat_android_weightSum, -1.0f);
        this.mBaselineAlignedChildIndex = a.getInt(R$styleable.LinearLayoutCompat_android_baselineAlignedChildIndex, -1);
        this.mUseLargestChild = a.getBoolean(R$styleable.LinearLayoutCompat_measureWithLargestChild, false);
        setDividerDrawable(a.getDrawable(R$styleable.LinearLayoutCompat_divider));
        this.mShowDividers = a.getInt(R$styleable.LinearLayoutCompat_showDividers, 0);
        this.mDividerPadding = a.getDimensionPixelSize(R$styleable.LinearLayoutCompat_dividerPadding, 0);
        a.recycle();
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void setDividerDrawable(Drawable divider) {
        boolean z = false;
        if (divider != this.mDivider) {
            this.mDivider = divider;
            if (divider != null) {
                this.mDividerWidth = divider.getIntrinsicWidth();
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            }
            if (divider == null) {
                z = true;
            }
            setWillNotDraw(z);
            requestLayout();
        }
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == 1) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    void drawDividersVertical(Canvas canvas) {
        int count = getVirtualChildCount();
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                drawHorizontalDivider(canvas, (child.getTop() - ((LayoutParams) child.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            int bottom;
            child = getVirtualChildAt(count - 1);
            if (child == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = child.getBottom() + ((LayoutParams) child.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    void drawDividersHorizontal(Canvas canvas) {
        LayoutParams lp;
        int count = getVirtualChildCount();
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int i = 0;
        while (i < count) {
            int position;
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = child.getRight() + lp.rightMargin;
                } else {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                }
                drawVerticalDivider(canvas, position);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            child = getVirtualChildAt(count - 1);
            if (child != null) {
                lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                } else {
                    position = child.getRight() + lp.rightMargin;
                }
            } else if (isLayoutRtl) {
                position = getPaddingLeft();
            } else {
                position = (getWidth() - getPaddingRight()) - this.mDividerWidth;
            }
            drawVerticalDivider(canvas, position);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int top) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, top, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + top);
        this.mDivider.draw(canvas);
    }

    void drawVerticalDivider(Canvas canvas, int left) {
        this.mDivider.setBounds(left, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + left, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public void setBaselineAligned(boolean baselineAligned) {
        this.mBaselineAligned = baselineAligned;
    }

    public int getBaseline() {
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() <= this.mBaselineAlignedChildIndex) {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
        View child = getChildAt(this.mBaselineAlignedChildIndex);
        int childBaseline = child.getBaseline();
        if (childBaseline != -1) {
            int childTop = this.mBaselineChildTop;
            if (this.mOrientation == 1) {
                int majorGravity = this.mGravity & 112;
                if (majorGravity != 48) {
                    switch (majorGravity) {
                        case 16:
                            childTop += ((((getBottom() - getTop()) - getPaddingTop()) - getPaddingBottom()) - this.mTotalLength) / 2;
                            break;
                        case 80:
                            childTop = ((getBottom() - getTop()) - getPaddingBottom()) - this.mTotalLength;
                            break;
                    }
                }
            }
            return (((LayoutParams) child.getLayoutParams()).topMargin + childTop) + childBaseline;
        } else if (this.mBaselineAlignedChildIndex == 0) {
            return -1;
        } else {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
        }
    }

    View getVirtualChildAt(int index) {
        return getChildAt(index);
    }

    int getVirtualChildCount() {
        return getChildCount();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected boolean hasDividerBeforeChildAt(int childIndex) {
        boolean z = true;
        if (childIndex == 0) {
            if ((this.mShowDividers & 1) == 0) {
                z = false;
            }
            return z;
        } else if (childIndex == getChildCount()) {
            if ((this.mShowDividers & 4) == 0) {
                z = false;
            }
            return z;
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            boolean hasVisibleViewBefore = false;
            for (int i = childIndex - 1; i >= 0; i--) {
                if (getChildAt(i).getVisibility() != 8) {
                    hasVisibleViewBefore = true;
                    break;
                }
            }
            return hasVisibleViewBefore;
        }
    }

    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int childHeight;
        int measuredWidth;
        this.mTotalLength = 0;
        int maxWidth = 0;
        int childState = 0;
        int alternativeMaxWidth = 0;
        int weightedMaxWidth = 0;
        boolean allFillParent = true;
        float totalWeight = 0.0f;
        int count = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchWidth = false;
        boolean skippedMeasure = false;
        int baselineChildIndex = this.mBaselineAlignedChildIndex;
        boolean useLargestChild = this.mUseLargestChild;
        int largestChildHeight = Integer.MIN_VALUE;
        int i = 0;
        while (i < count) {
            LayoutParams lp;
            int totalLength;
            boolean matchWidthLocally;
            int margin;
            View child = getVirtualChildAt(i);
            if (child == null) {
                this.mTotalLength += measureNullChild(i);
            } else if (child.getVisibility() == 8) {
                i += getChildrenSkipCount(child, i);
            } else {
                if (hasDividerBeforeChildAt(i)) {
                    this.mTotalLength += this.mDividerHeight;
                }
                lp = (LayoutParams) child.getLayoutParams();
                totalWeight += lp.weight;
                if (heightMode == 1073741824 && lp.height == 0 && lp.weight > 0.0f) {
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (lp.topMargin + totalLength) + lp.bottomMargin);
                    skippedMeasure = true;
                } else {
                    int oldHeight = Integer.MIN_VALUE;
                    if (lp.height == 0 && lp.weight > 0.0f) {
                        oldHeight = 0;
                        lp.height = -2;
                    }
                    measureChildBeforeLayout(child, i, widthMeasureSpec, 0, heightMeasureSpec, totalWeight == 0.0f ? this.mTotalLength : 0);
                    if (oldHeight != Integer.MIN_VALUE) {
                        lp.height = oldHeight;
                    }
                    childHeight = child.getMeasuredHeight();
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + childHeight) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                    if (useLargestChild) {
                        largestChildHeight = Math.max(childHeight, largestChildHeight);
                    }
                }
                if (baselineChildIndex >= 0 && baselineChildIndex == i + 1) {
                    this.mBaselineChildTop = this.mTotalLength;
                }
                if (i >= baselineChildIndex || lp.weight <= 0.0f) {
                    matchWidthLocally = false;
                    if (widthMode != 1073741824 && lp.width == -1) {
                        matchWidth = true;
                        matchWidthLocally = true;
                    }
                    margin = lp.leftMargin + lp.rightMargin;
                    measuredWidth = child.getMeasuredWidth() + margin;
                    maxWidth = Math.max(maxWidth, measuredWidth);
                    childState = ViewUtils.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
                    allFillParent = allFillParent && lp.width == -1;
                    if (lp.weight > 0.0f) {
                        if (!matchWidthLocally) {
                            margin = measuredWidth;
                        }
                        weightedMaxWidth = Math.max(weightedMaxWidth, margin);
                    } else {
                        if (!matchWidthLocally) {
                            margin = measuredWidth;
                        }
                        alternativeMaxWidth = Math.max(alternativeMaxWidth, margin);
                    }
                    i += getChildrenSkipCount(child, i);
                } else {
                    throw new RuntimeException("A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.");
                }
            }
            i++;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(count)) {
            this.mTotalLength += this.mDividerHeight;
        }
        if (useLargestChild && (heightMode == Integer.MIN_VALUE || heightMode == 0)) {
            this.mTotalLength = 0;
            i = 0;
            while (i < count) {
                child = getVirtualChildAt(i);
                if (child == null) {
                    this.mTotalLength += measureNullChild(i);
                } else if (child.getVisibility() == 8) {
                    i += getChildrenSkipCount(child, i);
                } else {
                    lp = (LayoutParams) child.getLayoutParams();
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildHeight) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                }
                i++;
            }
        }
        this.mTotalLength += getPaddingTop() + getPaddingBottom();
        int heightSizeAndState = ViewCompat.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumHeight()), heightMeasureSpec, 0);
        int delta = (heightSizeAndState & 16777215) - this.mTotalLength;
        if (skippedMeasure || (delta != 0 && totalWeight > 0.0f)) {
            float weightSum = this.mWeightSum > 0.0f ? this.mWeightSum : totalWeight;
            this.mTotalLength = 0;
            for (i = 0; i < count; i++) {
                child = getVirtualChildAt(i);
                if (child.getVisibility() != 8) {
                    lp = (LayoutParams) child.getLayoutParams();
                    float childExtra = lp.weight;
                    if (childExtra > 0.0f) {
                        int share = (int) ((((float) delta) * childExtra) / weightSum);
                        weightSum -= childExtra;
                        delta -= share;
                        int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, ((getPaddingLeft() + getPaddingRight()) + lp.leftMargin) + lp.rightMargin, lp.width);
                        if (lp.height == 0 && heightMode == 1073741824) {
                            if (share <= 0) {
                                share = 0;
                            }
                            child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(share, 1073741824));
                        } else {
                            childHeight = child.getMeasuredHeight() + share;
                            if (childHeight < 0) {
                                childHeight = 0;
                            }
                            child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(childHeight, 1073741824));
                        }
                        childState = ViewUtils.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child) & -256);
                    }
                    margin = lp.leftMargin + lp.rightMargin;
                    measuredWidth = child.getMeasuredWidth() + margin;
                    maxWidth = Math.max(maxWidth, measuredWidth);
                    matchWidthLocally = widthMode != 1073741824 ? lp.width == -1 : false;
                    if (!matchWidthLocally) {
                        margin = measuredWidth;
                    }
                    alternativeMaxWidth = Math.max(alternativeMaxWidth, margin);
                    allFillParent = allFillParent && lp.width == -1;
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((child.getMeasuredHeight() + totalLength) + lp.topMargin) + lp.bottomMargin) + getNextLocationOffset(child));
                }
            }
            this.mTotalLength += getPaddingTop() + getPaddingBottom();
        } else {
            alternativeMaxWidth = Math.max(alternativeMaxWidth, weightedMaxWidth);
            if (useLargestChild && heightMode != 1073741824) {
                for (i = 0; i < count; i++) {
                    child = getVirtualChildAt(i);
                    if (!(child == null || child.getVisibility() == 8 || ((LayoutParams) child.getLayoutParams()).weight <= 0.0f)) {
                        child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(largestChildHeight, 1073741824));
                    }
                }
            }
        }
        if (!(allFillParent || widthMode == 1073741824)) {
            maxWidth = alternativeMaxWidth;
        }
        setMeasuredDimension(ViewCompat.resolveSizeAndState(Math.max(maxWidth + (getPaddingLeft() + getPaddingRight()), getSuggestedMinimumWidth()), widthMeasureSpec, childState), heightSizeAndState);
        if (matchWidth) {
            forceUniformWidth(count, heightMeasureSpec);
        }
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int totalLength;
        int childWidth;
        boolean matchHeightLocally;
        int childBaseline;
        int widthSizeAndState;
        int delta;
        float weightSum;
        float childExtra;
        int share;
        int childHeightMeasureSpec;
        this.mTotalLength = 0;
        int maxHeight = 0;
        int childState = 0;
        int alternativeMaxHeight = 0;
        int weightedMaxHeight = 0;
        boolean allFillParent = true;
        float totalWeight = 0.0f;
        int count = getVirtualChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchHeight = false;
        boolean skippedMeasure = false;
        if (this.mMaxAscent == null || this.mMaxDescent == null) {
            this.mMaxAscent = new int[4];
            this.mMaxDescent = new int[4];
        }
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent[3] = -1;
        maxDescent[2] = -1;
        maxDescent[1] = -1;
        maxDescent[0] = -1;
        boolean baselineAligned = this.mBaselineAligned;
        boolean useLargestChild = this.mUseLargestChild;
        boolean isExactly = widthMode == 1073741824;
        int largestChildWidth = Integer.MIN_VALUE;
        int i = 0;
        while (i < count) {
            LayoutParams lp;
            int margin;
            int childHeight;
            int i2;
            View child = getVirtualChildAt(i);
            if (child == null) {
                this.mTotalLength += measureNullChild(i);
            } else if (child.getVisibility() == 8) {
                i += getChildrenSkipCount(child, i);
            } else {
                if (hasDividerBeforeChildAt(i)) {
                    this.mTotalLength += this.mDividerWidth;
                }
                lp = (LayoutParams) child.getLayoutParams();
                totalWeight += lp.weight;
                if (widthMode == 1073741824 && lp.width == 0 && lp.weight > 0.0f) {
                    if (isExactly) {
                        this.mTotalLength += lp.leftMargin + lp.rightMargin;
                    } else {
                        totalLength = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength, (lp.leftMargin + totalLength) + lp.rightMargin);
                    }
                    if (baselineAligned) {
                        int freeSpec = MeasureSpec.makeMeasureSpec(0, 0);
                        child.measure(freeSpec, freeSpec);
                    } else {
                        skippedMeasure = true;
                    }
                } else {
                    int oldWidth = Integer.MIN_VALUE;
                    if (lp.width == 0 && lp.weight > 0.0f) {
                        oldWidth = 0;
                        lp.width = -2;
                    }
                    measureChildBeforeLayout(child, i, widthMeasureSpec, totalWeight == 0.0f ? this.mTotalLength : 0, heightMeasureSpec, 0);
                    if (oldWidth != Integer.MIN_VALUE) {
                        lp.width = oldWidth;
                    }
                    childWidth = child.getMeasuredWidth();
                    if (isExactly) {
                        this.mTotalLength += ((lp.leftMargin + childWidth) + lp.rightMargin) + getNextLocationOffset(child);
                    } else {
                        totalLength = this.mTotalLength;
                        this.mTotalLength = Math.max(totalLength, (((totalLength + childWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                    }
                    if (useLargestChild) {
                        largestChildWidth = Math.max(childWidth, largestChildWidth);
                    }
                }
                matchHeightLocally = false;
                if (heightMode != 1073741824 && lp.height == -1) {
                    matchHeight = true;
                    matchHeightLocally = true;
                }
                margin = lp.topMargin + lp.bottomMargin;
                childHeight = child.getMeasuredHeight() + margin;
                childState = ViewUtils.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
                if (baselineAligned) {
                    childBaseline = child.getBaseline();
                    if (childBaseline != -1) {
                        if (lp.gravity < 0) {
                            i2 = this.mGravity;
                        } else {
                            i2 = lp.gravity;
                        }
                        int index = (((i2 & 112) >> 4) & -2) >> 1;
                        maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                        maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                    }
                }
                maxHeight = Math.max(maxHeight, childHeight);
                allFillParent = allFillParent && lp.height == -1;
                if (lp.weight > 0.0f) {
                    if (!matchHeightLocally) {
                        margin = childHeight;
                    }
                    weightedMaxHeight = Math.max(weightedMaxHeight, margin);
                } else {
                    if (!matchHeightLocally) {
                        margin = childHeight;
                    }
                    alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
                }
                i += getChildrenSkipCount(child, i);
            }
            i++;
        }
        if (this.mTotalLength > 0 && hasDividerBeforeChildAt(count)) {
            this.mTotalLength += this.mDividerWidth;
        }
        if (maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1) {
            if (maxAscent[3] != -1) {
            }
            if (useLargestChild && (widthMode == Integer.MIN_VALUE || widthMode == 0)) {
                this.mTotalLength = 0;
                i = 0;
                while (i < count) {
                    child = getVirtualChildAt(i);
                    if (child == null) {
                        this.mTotalLength += measureNullChild(i);
                    } else if (child.getVisibility() != 8) {
                        i += getChildrenSkipCount(child, i);
                    } else {
                        lp = (LayoutParams) child.getLayoutParams();
                        if (isExactly) {
                            totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                        } else {
                            this.mTotalLength += ((lp.leftMargin + largestChildWidth) + lp.rightMargin) + getNextLocationOffset(child);
                        }
                    }
                    i++;
                }
            }
            this.mTotalLength += getPaddingLeft() + getPaddingRight();
            widthSizeAndState = ViewCompat.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
            delta = (widthSizeAndState & 16777215) - this.mTotalLength;
            if (!skippedMeasure || (delta != 0 && totalWeight > 0.0f)) {
                weightSum = this.mWeightSum <= 0.0f ? this.mWeightSum : totalWeight;
                maxAscent[3] = -1;
                maxAscent[2] = -1;
                maxAscent[1] = -1;
                maxAscent[0] = -1;
                maxDescent[3] = -1;
                maxDescent[2] = -1;
                maxDescent[1] = -1;
                maxDescent[0] = -1;
                maxHeight = -1;
                this.mTotalLength = 0;
                for (i = 0; i < count; i++) {
                    child = getVirtualChildAt(i);
                    if (!(child == null || child.getVisibility() == 8)) {
                        lp = (LayoutParams) child.getLayoutParams();
                        childExtra = lp.weight;
                        if (childExtra > 0.0f) {
                            share = (int) ((((float) delta) * childExtra) / weightSum);
                            weightSum -= childExtra;
                            delta -= share;
                            childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin, lp.height);
                            if (lp.width == 0 || widthMode != 1073741824) {
                                childWidth = child.getMeasuredWidth() + share;
                                if (childWidth < 0) {
                                    childWidth = 0;
                                }
                                child.measure(MeasureSpec.makeMeasureSpec(childWidth, 1073741824), childHeightMeasureSpec);
                            } else {
                                if (share <= 0) {
                                    share = 0;
                                }
                                child.measure(MeasureSpec.makeMeasureSpec(share, 1073741824), childHeightMeasureSpec);
                            }
                            childState = ViewUtils.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child) & -16777216);
                        }
                        if (isExactly) {
                            totalLength = this.mTotalLength;
                            this.mTotalLength = Math.max(totalLength, (((child.getMeasuredWidth() + totalLength) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                        } else {
                            this.mTotalLength += ((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child);
                        }
                        matchHeightLocally = heightMode == 1073741824 ? lp.height != -1 : false;
                        margin = lp.topMargin + lp.bottomMargin;
                        childHeight = child.getMeasuredHeight() + margin;
                        maxHeight = Math.max(maxHeight, childHeight);
                        if (!matchHeightLocally) {
                            margin = childHeight;
                        }
                        alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
                        allFillParent = allFillParent && lp.height == -1;
                        if (baselineAligned) {
                            childBaseline = child.getBaseline();
                            if (childBaseline == -1) {
                                if (lp.gravity >= 0) {
                                    i2 = this.mGravity;
                                } else {
                                    i2 = lp.gravity;
                                }
                                index = (((i2 & 112) >> 4) & -2) >> 1;
                                maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                                maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                            }
                        }
                    }
                }
                this.mTotalLength += getPaddingLeft() + getPaddingRight();
                if (maxAscent[1] == -1 && maxAscent[0] == -1 && maxAscent[2] == -1) {
                    if (maxAscent[3] != -1) {
                    }
                }
                maxHeight = Math.max(maxHeight, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent[3], Math.max(maxDescent[0], Math.max(maxDescent[1], maxDescent[2]))));
            } else {
                alternativeMaxHeight = Math.max(alternativeMaxHeight, weightedMaxHeight);
                if (useLargestChild && widthMode != 1073741824) {
                    for (i = 0; i < count; i++) {
                        child = getVirtualChildAt(i);
                        if (!(child == null || child.getVisibility() == 8 || ((LayoutParams) child.getLayoutParams()).weight <= 0.0f)) {
                            child.measure(MeasureSpec.makeMeasureSpec(largestChildWidth, 1073741824), MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), 1073741824));
                        }
                    }
                }
            }
            if (!(allFillParent || heightMode == 1073741824)) {
                maxHeight = alternativeMaxHeight;
            }
            setMeasuredDimension((-16777216 & childState) | widthSizeAndState, ViewCompat.resolveSizeAndState(Math.max(maxHeight + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), heightMeasureSpec, childState << 16));
            if (matchHeight) {
                forceUniformHeight(count, widthMeasureSpec);
            }
        }
        maxHeight = Math.max(maxHeight, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent[3], Math.max(maxDescent[0], Math.max(maxDescent[1], maxDescent[2]))));
        this.mTotalLength = 0;
        i = 0;
        while (i < count) {
            child = getVirtualChildAt(i);
            if (child == null) {
                this.mTotalLength += measureNullChild(i);
            } else if (child.getVisibility() != 8) {
                lp = (LayoutParams) child.getLayoutParams();
                if (isExactly) {
                    totalLength = this.mTotalLength;
                    this.mTotalLength = Math.max(totalLength, (((totalLength + largestChildWidth) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
                } else {
                    this.mTotalLength += ((lp.leftMargin + largestChildWidth) + lp.rightMargin) + getNextLocationOffset(child);
                }
            } else {
                i += getChildrenSkipCount(child, i);
            }
            i++;
        }
        this.mTotalLength += getPaddingLeft() + getPaddingRight();
        widthSizeAndState = ViewCompat.resolveSizeAndState(Math.max(this.mTotalLength, getSuggestedMinimumWidth()), widthMeasureSpec, 0);
        delta = (widthSizeAndState & 16777215) - this.mTotalLength;
        if (skippedMeasure) {
        }
        if (this.mWeightSum <= 0.0f) {
        }
        maxAscent[3] = -1;
        maxAscent[2] = -1;
        maxAscent[1] = -1;
        maxAscent[0] = -1;
        maxDescent[3] = -1;
        maxDescent[2] = -1;
        maxDescent[1] = -1;
        maxDescent[0] = -1;
        maxHeight = -1;
        this.mTotalLength = 0;
        for (i = 0; i < count; i++) {
            child = getVirtualChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            childExtra = lp.weight;
            if (childExtra > 0.0f) {
                share = (int) ((((float) delta) * childExtra) / weightSum);
                weightSum -= childExtra;
                delta -= share;
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin, lp.height);
                if (lp.width == 0) {
                }
                childWidth = child.getMeasuredWidth() + share;
                if (childWidth < 0) {
                    childWidth = 0;
                }
                child.measure(MeasureSpec.makeMeasureSpec(childWidth, 1073741824), childHeightMeasureSpec);
                childState = ViewUtils.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child) & -16777216);
            }
            if (isExactly) {
                totalLength = this.mTotalLength;
                this.mTotalLength = Math.max(totalLength, (((child.getMeasuredWidth() + totalLength) + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child));
            } else {
                this.mTotalLength += ((child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin) + getNextLocationOffset(child);
            }
            if (heightMode == 1073741824) {
            }
            margin = lp.topMargin + lp.bottomMargin;
            childHeight = child.getMeasuredHeight() + margin;
            maxHeight = Math.max(maxHeight, childHeight);
            if (matchHeightLocally) {
                margin = childHeight;
            }
            alternativeMaxHeight = Math.max(alternativeMaxHeight, margin);
            if (!allFillParent) {
            }
            if (baselineAligned) {
                childBaseline = child.getBaseline();
                if (childBaseline == -1) {
                    if (lp.gravity >= 0) {
                        i2 = lp.gravity;
                    } else {
                        i2 = this.mGravity;
                    }
                    index = (((i2 & 112) >> 4) & -2) >> 1;
                    maxAscent[index] = Math.max(maxAscent[index], childBaseline);
                    maxDescent[index] = Math.max(maxDescent[index], childHeight - childBaseline);
                }
            }
        }
        this.mTotalLength += getPaddingLeft() + getPaddingRight();
        if (maxAscent[3] != -1) {
            maxHeight = Math.max(maxHeight, Math.max(maxAscent[3], Math.max(maxAscent[0], Math.max(maxAscent[1], maxAscent[2]))) + Math.max(maxDescent[3], Math.max(maxDescent[0], Math.max(maxDescent[1], maxDescent[2]))));
        }
        maxHeight = alternativeMaxHeight;
        setMeasuredDimension((-16777216 & childState) | widthSizeAndState, ViewCompat.resolveSizeAndState(Math.max(maxHeight + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight()), heightMeasureSpec, childState << 16));
        if (matchHeight) {
            forceUniformHeight(count, widthMeasureSpec);
        }
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    int getChildrenSkipCount(View child, int index) {
        return 0;
    }

    int measureNullChild(int childIndex) {
        return 0;
    }

    void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    int getLocationOffset(View child) {
        return 0;
    }

    int getNextLocationOffset(View child) {
        return 0;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mOrientation == 1) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    void layoutVertical(int left, int top, int right, int bottom) {
        int childTop;
        int paddingLeft = getPaddingLeft();
        int width = right - left;
        int childRight = width - getPaddingRight();
        int childSpace = (width - paddingLeft) - getPaddingRight();
        int count = getVirtualChildCount();
        int minorGravity = this.mGravity & 8388615;
        switch (this.mGravity & 112) {
            case 16:
                childTop = getPaddingTop() + (((bottom - top) - this.mTotalLength) / 2);
                break;
            case 80:
                childTop = ((getPaddingTop() + bottom) - top) - this.mTotalLength;
                break;
            default:
                childTop = getPaddingTop();
                break;
        }
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (child == null) {
                childTop += measureNullChild(i);
            } else if (child.getVisibility() != 8) {
                int childLeft;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                switch (GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this)) & 7) {
                    case 1:
                        childLeft = ((((childSpace - childWidth) / 2) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                        break;
                    case 5:
                        childLeft = (childRight - childWidth) - lp.rightMargin;
                        break;
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                        break;
                }
                if (hasDividerBeforeChildAt(i)) {
                    childTop += this.mDividerHeight;
                }
                childTop += lp.topMargin;
                setChildFrame(child, childLeft, childTop + getLocationOffset(child), childWidth, childHeight);
                childTop += (lp.bottomMargin + childHeight) + getNextLocationOffset(child);
                i += getChildrenSkipCount(child, i);
            }
            i++;
        }
    }

    void layoutHorizontal(int left, int top, int right, int bottom) {
        int childLeft;
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int paddingTop = getPaddingTop();
        int height = bottom - top;
        int childBottom = height - getPaddingBottom();
        int childSpace = (height - paddingTop) - getPaddingBottom();
        int count = getVirtualChildCount();
        int majorGravity = this.mGravity & 8388615;
        int minorGravity = this.mGravity & 112;
        boolean baselineAligned = this.mBaselineAligned;
        int[] maxAscent = this.mMaxAscent;
        int[] maxDescent = this.mMaxDescent;
        switch (GravityCompat.getAbsoluteGravity(majorGravity, ViewCompat.getLayoutDirection(this))) {
            case 1:
                childLeft = getPaddingLeft() + (((right - left) - this.mTotalLength) / 2);
                break;
            case 5:
                childLeft = ((getPaddingLeft() + right) - left) - this.mTotalLength;
                break;
            default:
                childLeft = getPaddingLeft();
                break;
        }
        int start = 0;
        int dir = 1;
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }
        int i = 0;
        while (i < count) {
            int childIndex = start + (dir * i);
            View child = getVirtualChildAt(childIndex);
            if (child == null) {
                childLeft += measureNullChild(childIndex);
            } else if (child.getVisibility() != 8) {
                int childTop;
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int childBaseline = -1;
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (baselineAligned && lp.height != -1) {
                    childBaseline = child.getBaseline();
                }
                int gravity = lp.gravity;
                if (gravity < 0) {
                    gravity = minorGravity;
                }
                switch (gravity & 112) {
                    case 16:
                        childTop = ((((childSpace - childHeight) / 2) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                        break;
                    case 48:
                        childTop = paddingTop + lp.topMargin;
                        if (childBaseline != -1) {
                            childTop += maxAscent[1] - childBaseline;
                            break;
                        }
                        break;
                    case 80:
                        childTop = (childBottom - childHeight) - lp.bottomMargin;
                        if (childBaseline != -1) {
                            childTop -= maxDescent[2] - (child.getMeasuredHeight() - childBaseline);
                            break;
                        }
                        break;
                    default:
                        childTop = paddingTop;
                        break;
                }
                if (hasDividerBeforeChildAt(childIndex)) {
                    childLeft += this.mDividerWidth;
                }
                childLeft += lp.leftMargin;
                setChildFrame(child, childLeft + getLocationOffset(child), childTop, childWidth, childHeight);
                childLeft += (lp.rightMargin + childWidth) + getNextLocationOffset(child);
                i += getChildrenSkipCount(child, childIndex);
            }
            i++;
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }

    public void setOrientation(int orientation) {
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            requestLayout();
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((8388615 & gravity) == 0) {
                gravity |= 8388611;
            }
            if ((gravity & 112) == 0) {
                gravity |= 48;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        if (VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityEvent(event);
            event.setClassName(LinearLayoutCompat.class.getName());
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        if (VERSION.SDK_INT >= 14) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(LinearLayoutCompat.class.getName());
        }
    }
}
