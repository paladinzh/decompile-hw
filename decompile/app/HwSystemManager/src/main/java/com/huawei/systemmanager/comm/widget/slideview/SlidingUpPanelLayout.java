package com.huawei.systemmanager.comm.widget.slideview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Interpolator;
import android.widget.ListView;
import android.widget.ScrollView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.slideview.PullView.ListViewPullView;
import com.huawei.systemmanager.comm.widget.slideview.PullView.ScrollViewWrapper;
import com.huawei.systemmanager.comm.widget.slideview.ViewDragHelper.Callback;
import com.huawei.systemmanager.util.HwLog;
import java.util.Locale;

public class SlidingUpPanelLayout extends ViewGroup {
    private static final /* synthetic */ int[] -com-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues = null;
    private static final float DEFAULT_ANCHOR_POINT = 1.0f;
    private static final int[] DEFAULT_ATTRS = new int[]{16842927};
    private static final boolean DEFAULT_CLIP_PANEL_FLAG = true;
    private static final int DEFAULT_FADE_COLOR = -1728053248;
    private static final int DEFAULT_MIN_FLING_VELOCITY = 400;
    private static final boolean DEFAULT_OVERLAY_FLAG = false;
    private static final int DEFAULT_PANEL_HEIGHT = 68;
    private static final int DEFAULT_PARALAX_OFFSET = 0;
    private static final int DEFAULT_SHADOW_HEIGHT = 4;
    private static PanelState DEFAULT_SLIDE_STATE = PanelState.COLLAPSED;
    private static final String TAG = SlidingUpPanelLayout.class.getSimpleName();
    private float mAnchorPoint;
    private float mChildScale;
    private boolean mClipPanel;
    private int mCoveredFadeColor;
    private final Paint mCoveredFadePaint;
    private final ViewDragHelper mDragHelper;
    private View mDragView;
    private int mDragViewResId;
    private boolean mFirstLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mIsLand;
    private boolean mIsSlidingUp;
    private boolean mIsSupportConfigurationChanged;
    private boolean mIsTouchEnabled;
    private boolean mIsTouchEnabledInPort;
    private boolean mIsUnableToDrag;
    private PanelState mLastNotDraggingSlideState;
    private View mMainView;
    private int mMinFlingVelocity;
    private int mOffsize;
    private boolean mOverlayContent;
    private int mPanelHeight;
    private int mPanelMarginTop;
    private PanelSlideListener mPanelSlideListener;
    private int mParallaxOffset;
    private PullView mPullView;
    private int mPullViewResId;
    private boolean mSaveInstance;
    private final Drawable mShadowDrawable;
    private int mShadowHeight;
    private boolean mShowShadow;
    private float mSlideOffset;
    private int mSlideRange;
    private PanelState mSlideState;
    private View mSlideableView;
    private PanelState mTargetSlideState;
    private final Rect mTmpRect;

    private class DragHelperCallback extends Callback {
        private DragHelperCallback() {
        }

        public boolean tryCaptureView(View child, int pointerId) {
            boolean z = false;
            if (SlidingUpPanelLayout.this.mIsUnableToDrag) {
                return false;
            }
            if (child == SlidingUpPanelLayout.this.mSlideableView) {
                z = true;
            }
            return z;
        }

        public void onViewDragStateChanged(int state) {
            if (SlidingUpPanelLayout.this.mDragHelper.getViewDragState() == 0) {
                SlidingUpPanelLayout.this.mSlideOffset = SlidingUpPanelLayout.this.computeSlideOffset(SlidingUpPanelLayout.this.mSlideableView.getTop());
                SlidingUpPanelLayout.this.applyParallaxForCurrentSlideOffset();
                if (SlidingUpPanelLayout.this.mSlideOffset == 1.0f) {
                    if (SlidingUpPanelLayout.this.mSlideState != PanelState.EXPANDED) {
                        SlidingUpPanelLayout.this.updateObscuredViewVisibility();
                        SlidingUpPanelLayout.this.mSlideState = PanelState.EXPANDED;
                        SlidingUpPanelLayout.this.dispatchOnPanelExpanded(SlidingUpPanelLayout.this.mSlideableView);
                    }
                } else if (SlidingUpPanelLayout.this.mSlideOffset == 0.0f) {
                    if (SlidingUpPanelLayout.this.mSlideState != PanelState.COLLAPSED) {
                        SlidingUpPanelLayout.this.mSlideState = PanelState.COLLAPSED;
                        SlidingUpPanelLayout.this.dispatchOnPanelCollapsed(SlidingUpPanelLayout.this.mSlideableView);
                    }
                } else if (SlidingUpPanelLayout.this.mSlideOffset < 0.0f) {
                    SlidingUpPanelLayout.this.mSlideState = PanelState.HIDDEN;
                    SlidingUpPanelLayout.this.mSlideableView.setVisibility(4);
                    SlidingUpPanelLayout.this.dispatchOnPanelHidden(SlidingUpPanelLayout.this.mSlideableView);
                } else if (SlidingUpPanelLayout.this.mSlideState != PanelState.ANCHORED) {
                    SlidingUpPanelLayout.this.updateObscuredViewVisibility();
                    SlidingUpPanelLayout.this.mSlideState = PanelState.ANCHORED;
                    SlidingUpPanelLayout.this.dispatchOnPanelAnchored(SlidingUpPanelLayout.this.mSlideableView);
                }
            }
        }

        public void onViewCaptured(View capturedChild, int activePointerId) {
            SlidingUpPanelLayout.this.setAllChildrenVisible();
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            SlidingUpPanelLayout.this.onPanelDragged(top);
            SlidingUpPanelLayout.this.invalidate();
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            float direction;
            int target;
            if (SlidingUpPanelLayout.this.mIsSlidingUp) {
                direction = -yvel;
            } else {
                direction = yvel;
            }
            if (direction > 0.0f) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(1.0f);
            } else if (direction < 0.0f) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(0.0f);
            } else if (SlidingUpPanelLayout.this.mAnchorPoint != 1.0f && SlidingUpPanelLayout.this.mSlideOffset >= (SlidingUpPanelLayout.this.mAnchorPoint + 1.0f) / 2.0f) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(1.0f);
            } else if (SlidingUpPanelLayout.this.mAnchorPoint == 1.0f && SlidingUpPanelLayout.this.mSlideOffset >= 0.5f) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(1.0f);
            } else if (SlidingUpPanelLayout.this.mAnchorPoint != 1.0f && SlidingUpPanelLayout.this.mSlideOffset >= SlidingUpPanelLayout.this.mAnchorPoint) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(SlidingUpPanelLayout.this.mAnchorPoint);
            } else if (SlidingUpPanelLayout.this.mAnchorPoint == 1.0f || SlidingUpPanelLayout.this.mSlideOffset < SlidingUpPanelLayout.this.mAnchorPoint / 2.0f) {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(0.0f);
            } else {
                target = SlidingUpPanelLayout.this.computePanelTopPosition(SlidingUpPanelLayout.this.mAnchorPoint);
            }
            SlidingUpPanelLayout.this.mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), target);
            SlidingUpPanelLayout.this.invalidate();
        }

        public int getViewVerticalDragRange(View child) {
            return SlidingUpPanelLayout.this.mSlideRange;
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            int collapsedTop = SlidingUpPanelLayout.this.computePanelTopPosition(0.0f);
            int expandedTop = SlidingUpPanelLayout.this.computePanelTopPosition(1.0f);
            if (SlidingUpPanelLayout.this.mIsSlidingUp) {
                return Math.min(Math.max(top, expandedTop), collapsedTop);
            }
            return Math.min(Math.max(top, collapsedTop), expandedTop);
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        private static final int[] ATTRS = new int[]{16843137};

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            c.obtainStyledAttributes(attrs, ATTRS).recycle();
        }
    }

    public enum PanelState {
        EXPANDED,
        COLLAPSED,
        ANCHORED,
        HIDDEN,
        DRAGGING
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        PanelState mSlideState;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            try {
                this.mSlideState = (PanelState) Enum.valueOf(PanelState.class, in.readString());
            } catch (IllegalArgumentException e) {
                this.mSlideState = PanelState.COLLAPSED;
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            if (this.mSlideState == null) {
                HwLog.e(SlidingUpPanelLayout.TAG, "mSlideState is null");
            } else {
                out.writeString(this.mSlideState.toString());
            }
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues() {
        if (-com-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues != null) {
            return -com-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues;
        }
        int[] iArr = new int[PanelState.values().length];
        try {
            iArr[PanelState.ANCHORED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PanelState.COLLAPSED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PanelState.DRAGGING.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PanelState.EXPANDED.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PanelState.HIDDEN.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues = iArr;
        return iArr;
    }

    public void setPanelMarginTop(int panelMarginTop) {
        this.mPanelMarginTop = panelMarginTop;
        requestLayout();
    }

    public SlidingUpPanelLayout(Context context) {
        this(context, null);
    }

    public SlidingUpPanelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingUpPanelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY;
        this.mCoveredFadeColor = DEFAULT_FADE_COLOR;
        this.mCoveredFadePaint = new Paint();
        this.mPanelHeight = -1;
        this.mShadowHeight = -1;
        this.mParallaxOffset = -1;
        this.mOverlayContent = false;
        this.mClipPanel = true;
        this.mDragViewResId = -1;
        this.mPullViewResId = -1;
        this.mPanelMarginTop = -1;
        this.mShowShadow = true;
        this.mSaveInstance = true;
        this.mSlideState = DEFAULT_SLIDE_STATE;
        this.mTargetSlideState = DEFAULT_SLIDE_STATE;
        this.mLastNotDraggingSlideState = null;
        this.mIsSupportConfigurationChanged = true;
        this.mIsTouchEnabled = true;
        this.mAnchorPoint = 1.0f;
        this.mFirstLayout = true;
        this.mTmpRect = new Rect();
        this.mIsTouchEnabledInPort = true;
        this.mOffsize = 0;
        if (isInEditMode()) {
            this.mShadowDrawable = null;
            this.mDragHelper = null;
            return;
        }
        boolean isSupportOrientation;
        if (attrs != null) {
            TypedArray defAttrs = context.obtainStyledAttributes(attrs, DEFAULT_ATTRS);
            if (defAttrs != null) {
                setGravity(defAttrs.getInt(0, 0));
                defAttrs.recycle();
            }
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingUpPanelLayout);
            if (ta != null) {
                this.mPanelHeight = ta.getDimensionPixelSize(0, -1);
                this.mShadowHeight = ta.getDimensionPixelSize(1, -1);
                this.mParallaxOffset = ta.getDimensionPixelSize(2, -1);
                this.mMinFlingVelocity = ta.getInt(4, DEFAULT_MIN_FLING_VELOCITY);
                this.mCoveredFadeColor = ta.getColor(3, DEFAULT_FADE_COLOR);
                this.mDragViewResId = ta.getResourceId(5, -1);
                this.mOverlayContent = ta.getBoolean(6, false);
                this.mClipPanel = ta.getBoolean(7, true);
                this.mAnchorPoint = ta.getFloat(8, 1.0f);
                this.mSlideState = PanelState.values()[ta.getInt(10, DEFAULT_SLIDE_STATE.ordinal())];
                this.mShowShadow = ta.getBoolean(11, true);
                this.mPanelMarginTop = ta.getDimensionPixelSize(12, -1);
                this.mPullViewResId = ta.getResourceId(13, -1);
                this.mIsTouchEnabled = ta.getBoolean(14, true);
                this.mChildScale = ta.getFloat(15, 0.5f);
                ta.recycle();
            }
        }
        this.mIsTouchEnabledInPort = this.mIsTouchEnabled;
        float density = context.getResources().getDisplayMetrics().density;
        if (this.mPanelHeight == -1 && this.mPanelMarginTop == -1) {
            this.mPanelHeight = (int) ((68.0f * density) + 0.5f);
        }
        if (this.mShadowHeight == -1) {
            this.mShadowHeight = (int) ((4.0f * density) + 0.5f);
        }
        if (this.mParallaxOffset == -1) {
            this.mParallaxOffset = (int) (0.0f * density);
        }
        if (this.mShadowHeight <= 0) {
            this.mShadowDrawable = null;
        } else if (this.mIsSlidingUp) {
            this.mShadowDrawable = getResources().getDrawable(R.drawable.po_above_shadow);
        } else {
            this.mShadowDrawable = getResources().getDrawable(R.drawable.po_below_shadow);
        }
        if (getResources().getConfiguration().orientation == 2) {
            isSupportOrientation = Utility.isSupportOrientation();
        } else {
            isSupportOrientation = false;
        }
        this.mIsLand = isSupportOrientation;
        setWillNotDraw(false);
        this.mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        this.mDragHelper.setMinVelocity(((float) this.mMinFlingVelocity) * density);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.mDragViewResId != -1) {
            setDragView(findViewById(this.mDragViewResId));
        }
        findAndSetPullView();
    }

    public void setGravity(int gravity) {
        if (gravity == 48 || gravity == 80) {
            this.mIsSlidingUp = gravity == 80;
            if (!this.mFirstLayout) {
                requestLayout();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("gravity must be set to either top or bottom");
    }

    public void setCoveredFadeColor(int color) {
        this.mCoveredFadeColor = color;
        invalidate();
    }

    public int getCoveredFadeColor() {
        return this.mCoveredFadeColor;
    }

    public void setTouchEnabled(boolean enabled) {
        this.mIsTouchEnabled = enabled;
    }

    public boolean isTouchEnabled() {
        return (!this.mIsTouchEnabled || this.mSlideableView == null || this.mSlideState == PanelState.HIDDEN) ? false : true;
    }

    public void setPanelHeight(int val) {
        if (getPanelHeight() != val) {
            this.mPanelHeight = val;
            if (!this.mFirstLayout) {
                requestLayout();
            }
            if (getPanelState() == PanelState.COLLAPSED) {
                smoothToBottom();
                invalidate();
            }
        }
    }

    protected void smoothToBottom() {
        this.mTargetSlideState = this.mSlideState;
        smoothSlideTo(0.0f, 0);
    }

    public int getShadowHeight() {
        return this.mShadowHeight;
    }

    public void setShadowHeight(int val) {
        this.mShadowHeight = val;
        if (!this.mFirstLayout) {
            invalidate();
        }
    }

    public int getPanelHeight() {
        return this.mPanelHeight;
    }

    public int getCurrentParalaxOffset() {
        int offset = (int) (((float) this.mParallaxOffset) * Math.max(this.mSlideOffset, 0.0f));
        return this.mIsSlidingUp ? -offset : offset;
    }

    public void setParalaxOffset(int val) {
        this.mParallaxOffset = val;
        if (!this.mFirstLayout) {
            requestLayout();
        }
    }

    public int getMinFlingVelocity() {
        return this.mMinFlingVelocity;
    }

    public void setMinFlingVelocity(int val) {
        this.mMinFlingVelocity = val;
    }

    public void setPanelSlideListener(PanelSlideListener listener) {
        this.mPanelSlideListener = listener;
    }

    public void setDragView(View dragView) {
        if (this.mDragView != null) {
            this.mDragView.setOnClickListener(null);
        }
        this.mDragView = dragView;
        if (this.mDragView != null) {
            this.mDragView.setClickable(true);
            this.mDragView.setFocusable(false);
            this.mDragView.setFocusableInTouchMode(false);
        }
    }

    public View getDragView() {
        return this.mDragView;
    }

    public void setDragView(int dragViewResId) {
        this.mDragViewResId = dragViewResId;
        setDragView(findViewById(dragViewResId));
    }

    public void setAnchorPoint(float anchorPoint) {
        if (anchorPoint > 0.0f && anchorPoint <= 1.0f) {
            this.mAnchorPoint = anchorPoint;
        }
    }

    public float getAnchorPoint() {
        return this.mAnchorPoint;
    }

    public void setOverlayed(boolean overlayed) {
        this.mOverlayContent = overlayed;
    }

    public boolean isOverlayed() {
        return this.mOverlayContent;
    }

    public void setClipPanel(boolean clip) {
        this.mClipPanel = clip;
    }

    public boolean isClipPanel() {
        return this.mClipPanel;
    }

    void dispatchOnPanelSlide(View panel) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelSlide(panel, this.mSlideOffset);
        }
    }

    void dispatchOnPanelExpanded(View panel) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelExpanded(panel);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelCollapsed(View panel) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelCollapsed(panel);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelAnchored(View panel) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelAnchored(panel);
        }
        sendAccessibilityEvent(32);
    }

    void dispatchOnPanelHidden(View panel) {
        if (this.mPanelSlideListener != null) {
            this.mPanelSlideListener.onPanelHidden(panel);
        }
        sendAccessibilityEvent(32);
    }

    void updateObscuredViewVisibility() {
        if (getChildCount() != 0) {
            int left;
            int vis;
            int leftBound = getPaddingLeft();
            int rightBound = getWidth() - getPaddingRight();
            int topBound = getPaddingTop();
            int bottomBound = getHeight() - getPaddingBottom();
            int bottom;
            int top;
            int right;
            if (this.mSlideableView == null || !hasOpaqueBackground(this.mSlideableView)) {
                bottom = 0;
                top = 0;
                right = 0;
                left = 0;
            } else {
                left = this.mSlideableView.getLeft();
                right = this.mSlideableView.getRight();
                top = this.mSlideableView.getTop();
                bottom = this.mSlideableView.getBottom();
            }
            View child = getChildAt(0);
            int clampedChildLeft = Math.max(leftBound, child.getLeft());
            int clampedChildTop = Math.max(topBound, child.getTop());
            int clampedChildRight = Math.min(rightBound, child.getRight());
            int clampedChildBottom = Math.min(bottomBound, child.getBottom());
            if (clampedChildLeft < left || clampedChildTop < top || clampedChildRight > right || clampedChildBottom > bottom) {
                vis = 0;
            } else {
                vis = 4;
            }
            child.setVisibility(vis);
        }
    }

    void setAllChildrenVisible() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 4) {
                child.setVisibility(0);
            }
        }
    }

    private static boolean hasOpaqueBackground(View v) {
        Drawable bg = v.getBackground();
        if (bg == null || bg.getOpacity() != -1) {
            return false;
        }
        return true;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mFirstLayout = true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != 1073741824) {
            throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
        } else if (heightMode != 1073741824) {
            throw new IllegalStateException("Height must have an exact value or MATCH_PARENT");
        } else {
            int childCount = getChildCount();
            if (childCount != 2) {
                throw new IllegalStateException("Sliding up panel layout must have exactly 2 children!");
            }
            this.mMainView = getChildAt(0);
            this.mSlideableView = getChildAt(1);
            if (this.mDragView == null) {
                setDragView(this.mSlideableView);
                findAndSetPullView();
            }
            int layoutHeight = (heightSize - getPaddingTop()) - getPaddingBottom();
            if (this.mPanelMarginTop >= 0) {
                this.mPanelHeight = layoutHeight - this.mPanelMarginTop;
            }
            int layoutWidth = (widthSize - getPaddingLeft()) - getPaddingRight();
            for (int i = 0; i < childCount; i++) {
                mesuareChild(getChildAt(i), i, layoutHeight, layoutWidth);
            }
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    private void mesuareChild(View child, int i, int layoutHeight, int layoutWidth) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (child.getVisibility() != 8 || i != 0) {
            int childWidthSpec;
            int childHeightSpec;
            int height = layoutHeight;
            int width = layoutWidth;
            if (this.mIsLand) {
                int landWidth = (int) (((float) layoutWidth) * this.mChildScale);
                if (child == this.mSlideableView) {
                    landWidth = (int) (((float) layoutWidth) * (1.0f - this.mChildScale));
                }
                width = landWidth;
            }
            if (child == this.mMainView) {
                if (!(this.mOverlayContent || this.mSlideState == PanelState.HIDDEN)) {
                    height = layoutHeight - this.mPanelHeight;
                }
                width -= lp.leftMargin + lp.rightMargin;
            } else if (child == this.mSlideableView) {
                height = layoutHeight - lp.topMargin;
            }
            int childheight = this.mIsLand ? layoutHeight : height;
            if (lp.width == -2) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE);
            } else if (lp.width == -1) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(width, 1073741824);
            } else {
                childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, 1073741824);
            }
            if (lp.height == -2) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(childheight, Integer.MIN_VALUE);
            } else if (lp.height == -1) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(childheight, 1073741824);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, 1073741824);
            }
            child.measure(childWidthSpec, childHeightSpec);
            if (child == this.mSlideableView) {
                this.mSlideRange = this.mSlideableView.getMeasuredHeight() - this.mPanelHeight;
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        if (this.mFirstLayout) {
            switch (-getcom-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues()[this.mSlideState.ordinal()]) {
                case 1:
                    this.mSlideOffset = this.mAnchorPoint;
                    break;
                case 3:
                    this.mSlideOffset = 1.0f;
                    break;
                case 4:
                    int i;
                    int computePanelTopPosition = computePanelTopPosition(0.0f);
                    if (this.mIsSlidingUp) {
                        i = this.mPanelHeight;
                    } else {
                        i = -this.mPanelHeight;
                    }
                    this.mSlideOffset = computeSlideOffset(computePanelTopPosition + i);
                    break;
                default:
                    this.mSlideOffset = 0.0f;
                    break;
            }
        }
        if (this.mIsLand) {
            this.mIsTouchEnabled = false;
            this.mDragHelper.cancel();
            int landChildTop = t + paddingTop;
            int landChildBottom = b - getPaddingBottom();
            if (getChildCount() == 2) {
                this.mMainView.setY((float) landChildTop);
                if (isRtlLocale()) {
                    this.mMainView.layout((int) (((float) (l + r)) * (1.0f - this.mChildScale)), landChildTop, r - getPaddingRight(), landChildBottom);
                    this.mSlideableView.layout(l + paddingLeft, landChildTop, (int) (((float) (l + r)) * (1.0f - this.mChildScale)), landChildBottom);
                } else {
                    this.mMainView.layout(l + paddingLeft, landChildTop, (int) (((float) (l + r)) * this.mChildScale), landChildBottom);
                    this.mSlideableView.layout((int) (((float) (l + r)) * this.mChildScale), landChildTop, r - getPaddingRight(), landChildBottom);
                }
            }
        } else {
            this.mIsTouchEnabled = this.mIsTouchEnabledInPort;
            int i2 = 0;
            while (i2 < childCount) {
                View child = getChildAt(i2);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!(child.getVisibility() == 8 && (i2 == 0 || this.mFirstLayout))) {
                    int childHeight = child.getMeasuredHeight();
                    int childTop = paddingTop;
                    if (child == this.mSlideableView) {
                        childTop = computePanelTopPosition(this.mSlideOffset);
                    }
                    if (!(this.mIsSlidingUp || child != this.mMainView || this.mOverlayContent)) {
                        childTop = computePanelTopPosition(this.mSlideOffset) + this.mSlideableView.getMeasuredHeight();
                    }
                    int childLeft = paddingLeft + lp.leftMargin;
                    child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + childHeight);
                }
                i2++;
            }
        }
        if (this.mFirstLayout && !this.mIsLand) {
            updateObscuredViewVisibility();
        }
        if (!this.mIsLand) {
            applyParallaxForCurrentSlideOffset();
        }
        this.mFirstLayout = false;
    }

    public void setOffsize(int offsize) {
        this.mOffsize = offsize;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) {
            this.mFirstLayout = true;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (checkActionCancel(action)) {
            this.mDragHelper.cancel();
            return false;
        }
        float x = ev.getX();
        float y = ev.getY();
        boolean interceptTap = false;
        switch (action) {
            case 0:
                this.mIsUnableToDrag = false;
                this.mInitialMotionX = x;
                this.mInitialMotionY = y;
                break;
            case 2:
                float dy = y - this.mInitialMotionY;
                float adx = Math.abs(x - this.mInitialMotionX);
                float ady = Math.abs(dy);
                int dragSlop = this.mDragHelper.getTouchSlop();
                if (ady < ((float) dragSlop)) {
                    if (adx > ((float) dragSlop)) {
                        return false;
                    }
                } else if (!checkPullView((int) x, (int) y)) {
                    interceptTap = isDragViewUnder((int) x, (int) y);
                    if ((ady <= ((float) dragSlop) || adx <= ady) && isDragViewUnder((int) this.mInitialMotionX, (int) this.mInitialMotionY)) {
                        break;
                    }
                    this.mDragHelper.cancel();
                    this.mIsUnableToDrag = true;
                    return false;
                } else if (dy > 0.0f) {
                    return isPullViewTop();
                } else {
                    return isPullViewContentFit();
                }
                break;
        }
        boolean shouldIntercept = false;
        try {
            shouldIntercept = this.mDragHelper.shouldInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (shouldIntercept) {
            interceptTap = true;
        }
        return interceptTap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkActionCancel(int action) {
        return (isEnabled() && isTouchEnabled() && ((!this.mIsUnableToDrag || action == 0) && action != 3 && action != 1)) ? false : true;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || !isTouchEnabled()) {
            return super.onTouchEvent(ev);
        }
        try {
            this.mDragHelper.processTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isDragViewUnder(int x, int y) {
        boolean z = true;
        if (this.mDragView == null) {
            return false;
        }
        int[] viewLocation = new int[2];
        this.mDragView.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        if (screenX < viewLocation[0] || screenX >= viewLocation[0] + this.mDragView.getWidth() || screenY < viewLocation[1]) {
            z = false;
        } else if (screenY >= viewLocation[1] + this.mDragView.getHeight()) {
            z = false;
        }
        return z;
    }

    private int computePanelTopPosition(float slideOffset) {
        int slidingViewHeight = this.mSlideableView != null ? this.mSlideableView.getMeasuredHeight() : 0;
        int slidePixelOffset = (int) (((float) this.mSlideRange) * slideOffset);
        if (this.mIsSlidingUp) {
            return ((getMeasuredHeight() - getPaddingBottom()) - this.mPanelHeight) - slidePixelOffset;
        }
        return ((getPaddingTop() - slidingViewHeight) + this.mPanelHeight) + slidePixelOffset;
    }

    private float computeSlideOffset(int topPosition) {
        int topBoundCollapsed = computePanelTopPosition(0.0f);
        if (this.mIsSlidingUp) {
            return adjustSlideOffset(((float) (topBoundCollapsed - topPosition)) / ((float) this.mSlideRange));
        }
        return ((float) (topPosition - topBoundCollapsed)) / ((float) this.mSlideRange);
    }

    public PanelState getPanelState() {
        return this.mSlideState;
    }

    public void setPanelState(PanelState state) {
        if (state == null || state == PanelState.DRAGGING) {
            throw new IllegalArgumentException("Panel state cannot be null or DRAGGING.");
        }
        this.mTargetSlideState = state;
        if (isEnabled() && ((this.mFirstLayout || this.mSlideableView != null) && state != this.mSlideState)) {
            if (!this.mFirstLayout) {
                if (this.mSlideState == PanelState.HIDDEN) {
                    this.mSlideableView.setVisibility(0);
                    requestLayout();
                }
                switch (-getcom-huawei-systemmanager-comm-widget-slideview-SlidingUpPanelLayout$PanelStateSwitchesValues()[state.ordinal()]) {
                    case 1:
                        smoothSlideTo(this.mAnchorPoint, 0);
                        break;
                    case 2:
                        smoothSlideTo(0.0f, 0);
                        break;
                    case 3:
                        smoothSlideTo(1.0f, 0);
                        break;
                    case 4:
                        int i;
                        int computePanelTopPosition = computePanelTopPosition(0.0f);
                        if (this.mIsSlidingUp) {
                            i = this.mPanelHeight;
                        } else {
                            i = -this.mPanelHeight;
                        }
                        smoothSlideTo(computeSlideOffset(computePanelTopPosition + i), 0);
                        break;
                    default:
                        HwLog.e(TAG, "unknow state:" + state);
                        break;
                }
            }
            this.mSlideState = state;
        }
    }

    @SuppressLint({"NewApi"})
    private void applyParallaxForCurrentSlideOffset() {
        if (this.mParallaxOffset > 0 && !this.mIsLand) {
            this.mMainView.setTranslationY((float) getCurrentParalaxOffset());
        }
    }

    private void onPanelDragged(int newTop) {
        this.mLastNotDraggingSlideState = this.mSlideState;
        this.mSlideState = PanelState.DRAGGING;
        this.mSlideOffset = computeSlideOffset(newTop);
        applyParallaxForCurrentSlideOffset();
        dispatchOnPanelSlide(this.mSlideableView);
        LayoutParams lp = (LayoutParams) this.mMainView.getLayoutParams();
        int defaultHeight = ((getHeight() - getPaddingBottom()) - getPaddingTop()) - this.mPanelHeight;
        if (this.mSlideOffset <= 0.0f && !this.mOverlayContent) {
            lp.height = this.mIsSlidingUp ? newTop - getPaddingBottom() : ((getHeight() - getPaddingBottom()) - this.mSlideableView.getMeasuredHeight()) - newTop;
            this.mMainView.requestLayout();
        } else if (lp.height != defaultHeight && !this.mOverlayContent) {
            lp.height = defaultHeight;
            this.mMainView.requestLayout();
        }
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        int save = canvas.save(2);
        if (this.mIsLand) {
            int childClipTop = getTop() + getPaddingTop();
            int childClipBottom = getBottom() - getPaddingBottom();
            int childmMainViewClipLeft = getLeft() + getPaddingLeft();
            int childClipBetween = ((getLeft() + getRight()) + getPaddingLeft()) - getPaddingRight();
            int childSlidViewClipRight = getRight() - getPaddingRight();
            if (isRtlLocale()) {
                if (child == this.mMainView) {
                    canvas.clipRect(((float) childClipBetween) * (1.0f - this.mChildScale), (float) childClipTop, (float) childSlidViewClipRight, (float) childClipBottom);
                } else {
                    canvas.clipRect((float) childmMainViewClipLeft, (float) childClipTop, ((float) childClipBetween) * (1.0f - this.mChildScale), (float) childClipBottom);
                }
            } else if (child == this.mMainView) {
                canvas.clipRect((float) childmMainViewClipLeft, (float) childClipTop, ((float) childClipBetween) * this.mChildScale, (float) childClipBottom);
            } else {
                canvas.clipRect(((float) childClipBetween) * this.mChildScale, (float) childClipTop, (float) childSlidViewClipRight, (float) childClipBottom);
            }
            result = super.drawChild(canvas, child, drawingTime);
        } else if (this.mSlideableView != child) {
            canvas.getClipBounds(this.mTmpRect);
            if (!this.mOverlayContent) {
                if (this.mIsSlidingUp) {
                    this.mTmpRect.bottom = Math.min(this.mTmpRect.bottom, this.mSlideableView.getTop());
                } else {
                    this.mTmpRect.top = Math.max(this.mTmpRect.top, this.mSlideableView.getBottom());
                }
            }
            if (this.mClipPanel) {
                canvas.clipRect(this.mTmpRect);
            }
            result = super.drawChild(canvas, child, drawingTime);
            if (this.mCoveredFadeColor != 0 && this.mSlideOffset > 0.0f) {
                this.mCoveredFadePaint.setColor((((int) (((float) ((this.mCoveredFadeColor & ViewCompat.MEASURED_STATE_MASK) >>> 24)) * this.mSlideOffset)) << 24) | (this.mCoveredFadeColor & ViewCompat.MEASURED_SIZE_MASK));
                canvas.drawRect(this.mTmpRect, this.mCoveredFadePaint);
            }
        } else {
            result = super.drawChild(canvas, child, drawingTime);
        }
        canvas.restoreToCount(save);
        return result;
    }

    boolean smoothSlideTo(float slideOffset, int velocity) {
        if (!isEnabled()) {
            return false;
        }
        if (!this.mDragHelper.smoothSlideViewTo(this.mSlideableView, this.mSlideableView.getLeft(), computePanelTopPosition(slideOffset) - this.mOffsize)) {
            return false;
        }
        setAllChildrenVisible();
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }

    public void computeScroll() {
        if (this.mDragHelper != null && this.mDragHelper.continueSettling(true)) {
            if (isEnabled()) {
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                this.mDragHelper.abort();
            }
        }
    }

    public void draw(Canvas c) {
        super.draw(c);
        if (this.mShadowDrawable != null && this.mShowShadow) {
            int top;
            int bottom;
            int right = this.mSlideableView.getRight();
            if (this.mIsSlidingUp) {
                top = this.mSlideableView.getTop() - this.mShadowHeight;
                bottom = this.mSlideableView.getTop();
            } else {
                top = this.mSlideableView.getBottom();
                bottom = this.mSlideableView.getBottom() + this.mShadowHeight;
            }
            this.mShadowDrawable.setBounds(this.mSlideableView.getLeft(), top, right, bottom);
            this.mShadowDrawable.draw(c);
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()) {
                    if (canScroll(child, true, dx, (x + scrollX) - child.getLeft(), (y + scrollY) - child.getTop())) {
                        return true;
                    }
                }
            }
        }
        return checkV ? ViewCompat.canScrollHorizontally(v, -dx) : false;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? super.checkLayoutParams(p) : false;
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (!this.mSaveInstance) {
            return superState;
        }
        SavedState ss = new SavedState(superState);
        if (this.mSlideState != PanelState.DRAGGING) {
            ss.mSlideState = this.mSlideState;
        } else {
            ss.mSlideState = this.mLastNotDraggingSlideState;
        }
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            this.mSlideState = ss.mSlideState;
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private void findAndSetPullView() {
        if (this.mPullViewResId != -1 && this.mDragView != null) {
            View view = this.mDragView.findViewById(this.mPullViewResId);
            if (view != null) {
                if (view instanceof ListView) {
                    setPullView(new ListViewPullView((ListView) view));
                } else if (view instanceof ScrollView) {
                    setPullView(new ScrollViewWrapper((ScrollView) view));
                } else {
                    HwLog.e(TAG, "illeage pull view type!");
                }
            }
        }
    }

    public void setPullView(PullView pv) {
        this.mPullView = pv;
    }

    private boolean isPullViewTop() {
        if (this.mPullView == null) {
            return true;
        }
        return this.mPullView.isContentTop();
    }

    private boolean isPullViewContentFit() {
        if (this.mPullView == null) {
            return true;
        }
        return this.mPullView.isContentFit();
    }

    private boolean checkPullView(int x, int y) {
        if (this.mPullView != null && isExpanded() && this.mDragHelper.isViewUnder(this.mPullView.getView(), x, y)) {
            return true;
        }
        return false;
    }

    public void expandPane() {
        setPanelState(PanelState.EXPANDED);
    }

    public void collapsePane() {
        setPanelState(PanelState.COLLAPSED);
    }

    public boolean isExpanded() {
        return this.mSlideState == PanelState.EXPANDED;
    }

    public boolean isCollapse() {
        return this.mSlideState == PanelState.COLLAPSED;
    }

    public void setShowShadow(boolean show) {
        this.mShowShadow = show;
        invalidate();
    }

    public void setNeedSaveInstance(boolean saveInstance) {
        this.mSaveInstance = saveInstance;
    }

    @Deprecated
    public void setSlidingEnabled(boolean enable) {
        setTouchEnabled(enable);
    }

    public void setSupportConfigurationChange(boolean supportCfgChanged) {
        this.mIsSupportConfigurationChanged = supportCfgChanged;
        if (!this.mIsSupportConfigurationChanged) {
            this.mIsLand = false;
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mDragHelper.setInterpolator(getContext(), interpolator);
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        return (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ur")) ? true : isLayoutRtl();
    }

    private float adjustSlideOffset(float slideOffset) {
        return Math.min(slideOffset, 1.0f);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsSupportConfigurationChanged) {
            this.mIsLand = newConfig.orientation == 2;
            if (this.mDragHelper.getViewDragState() == 2) {
                this.mDragHelper.abort();
                setAllChildrenVisible();
                if (this.mSlideState != this.mTargetSlideState) {
                    this.mSlideState = this.mTargetSlideState;
                }
                invalidate();
            }
        }
    }
}
