package android.support.v17.leanback.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$drawable;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;

public class PagingIndicator extends View {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Property<Dot, Float> DOT_ALPHA = new Property<Dot, Float>(Float.class, "alpha") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getAlpha());
        }

        public void set(Dot dot, Float value) {
            dot.setAlpha(value.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_DIAMETER = new Property<Dot, Float>(Float.class, "diameter") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getDiameter());
        }

        public void set(Dot dot, Float value) {
            dot.setDiameter(value.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_TRANSLATION_X = new Property<Dot, Float>(Float.class, "translation_x") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getTranslationX());
        }

        public void set(Dot dot, Float value) {
            dot.setTranslationX(value.floatValue());
        }
    };
    private final AnimatorSet mAnimator;
    private Bitmap mArrow;
    private final int mArrowDiameter;
    private final int mArrowGap;
    private final int mArrowRadius;
    private final Rect mArrowRect;
    private final float mArrowToBgRatio;
    private final Paint mBgPaint;
    private int mCurrentPage;
    private int mDotCenterY;
    private final int mDotDiameter;
    @ColorInt
    private final int mDotFgSelectColor;
    private final int mDotGap;
    private final int mDotRadius;
    private int[] mDotSelectedNextX;
    private int[] mDotSelectedPrevX;
    private int[] mDotSelectedX;
    private Dot[] mDots;
    private final Paint mFgPaint;
    private final AnimatorSet mHideAnimator;
    private boolean mIsLtr;
    private int mPageCount;
    private int mPreviousPage;
    private final int mShadowRadius;
    private final AnimatorSet mShowAnimator;

    public class Dot {
        float mAlpha;
        float mArrowImageRadius;
        float mCenterX;
        float mDiameter;
        float mDirection;
        @ColorInt
        int mFgColor;
        float mLayoutDirection;
        float mRadius;
        float mTranslationX;
        final /* synthetic */ PagingIndicator this$0;

        void select() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = (float) this.this$0.mArrowDiameter;
            this.mRadius = (float) this.this$0.mArrowRadius;
            this.mArrowImageRadius = this.mRadius * this.this$0.mArrowToBgRatio;
            this.mAlpha = 1.0f;
            adjustAlpha();
        }

        void deselect() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = (float) this.this$0.mDotDiameter;
            this.mRadius = (float) this.this$0.mDotRadius;
            this.mArrowImageRadius = this.mRadius * this.this$0.mArrowToBgRatio;
            this.mAlpha = 0.0f;
            adjustAlpha();
        }

        public void adjustAlpha() {
            this.mFgColor = Color.argb(Math.round(this.mAlpha * 255.0f), Color.red(this.this$0.mDotFgSelectColor), Color.green(this.this$0.mDotFgSelectColor), Color.blue(this.this$0.mDotFgSelectColor));
        }

        public float getAlpha() {
            return this.mAlpha;
        }

        public void setAlpha(float alpha) {
            this.mAlpha = alpha;
            adjustAlpha();
            this.this$0.invalidate();
        }

        public float getTranslationX() {
            return this.mTranslationX;
        }

        public void setTranslationX(float translationX) {
            this.mTranslationX = (this.mDirection * translationX) * this.mLayoutDirection;
            this.this$0.invalidate();
        }

        public float getDiameter() {
            return this.mDiameter;
        }

        public void setDiameter(float diameter) {
            this.mDiameter = diameter;
            this.mRadius = diameter / 2.0f;
            this.mArrowImageRadius = (diameter / 2.0f) * this.this$0.mArrowToBgRatio;
            this.this$0.invalidate();
        }

        void draw(Canvas canvas) {
            float centerX = this.mCenterX + this.mTranslationX;
            canvas.drawCircle(centerX, (float) this.this$0.mDotCenterY, this.mRadius, this.this$0.mBgPaint);
            if (this.mAlpha > 0.0f) {
                this.this$0.mFgPaint.setColor(this.mFgColor);
                canvas.drawCircle(centerX, (float) this.this$0.mDotCenterY, this.mRadius, this.this$0.mFgPaint);
                canvas.drawBitmap(this.this$0.mArrow, this.this$0.mArrowRect, new Rect((int) (centerX - this.mArrowImageRadius), (int) (((float) this.this$0.mDotCenterY) - this.mArrowImageRadius), (int) (this.mArrowImageRadius + centerX), (int) (((float) this.this$0.mDotCenterY) + this.mArrowImageRadius)), null);
            }
        }

        void onRtlPropertiesChanged() {
            this.mLayoutDirection = this.this$0.mIsLtr ? 1.0f : -1.0f;
        }
    }

    public PagingIndicator(Context context) {
        this(context, null, 0);
    }

    public PagingIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagingIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAnimator = new AnimatorSet();
        Resources res = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.PagingIndicator, defStyle, 0);
        this.mDotRadius = getDimensionFromTypedArray(typedArray, R$styleable.PagingIndicator_dotRadius, R$dimen.lb_page_indicator_dot_radius);
        this.mDotDiameter = this.mDotRadius * 2;
        this.mArrowRadius = getDimensionFromTypedArray(typedArray, R$styleable.PagingIndicator_arrowRadius, R$dimen.lb_page_indicator_arrow_radius);
        this.mArrowDiameter = this.mArrowRadius * 2;
        this.mDotGap = getDimensionFromTypedArray(typedArray, R$styleable.PagingIndicator_dotToDotGap, R$dimen.lb_page_indicator_dot_gap);
        this.mArrowGap = getDimensionFromTypedArray(typedArray, R$styleable.PagingIndicator_dotToArrowGap, R$dimen.lb_page_indicator_arrow_gap);
        int bgColor = getColorFromTypedArray(typedArray, R$styleable.PagingIndicator_dotBgColor, R$color.lb_page_indicator_dot);
        this.mBgPaint = new Paint(1);
        this.mBgPaint.setColor(bgColor);
        this.mDotFgSelectColor = getColorFromTypedArray(typedArray, R$styleable.PagingIndicator_arrowBgColor, R$color.lb_page_indicator_arrow_background);
        typedArray.recycle();
        this.mIsLtr = res.getConfiguration().getLayoutDirection() == 0;
        int shadowColor = res.getColor(R$color.lb_page_indicator_arrow_shadow);
        this.mShadowRadius = res.getDimensionPixelSize(R$dimen.lb_page_indicator_arrow_shadow_radius);
        this.mFgPaint = new Paint(1);
        int shadowOffset = res.getDimensionPixelSize(R$dimen.lb_page_indicator_arrow_shadow_offset);
        this.mFgPaint.setShadowLayer((float) this.mShadowRadius, (float) shadowOffset, (float) shadowOffset, shadowColor);
        this.mArrow = loadArrow();
        this.mArrowRect = new Rect(0, 0, this.mArrow.getWidth(), this.mArrow.getHeight());
        this.mArrowToBgRatio = ((float) this.mArrow.getWidth()) / ((float) this.mArrowDiameter);
        this.mShowAnimator = new AnimatorSet();
        this.mShowAnimator.playTogether(new Animator[]{createDotAlphaAnimator(0.0f, 1.0f), createDotDiameterAnimator((float) (this.mDotRadius * 2), (float) (this.mArrowRadius * 2)), createDotTranslationXAnimator()});
        this.mHideAnimator = new AnimatorSet();
        this.mHideAnimator.playTogether(new Animator[]{createDotAlphaAnimator(1.0f, 0.0f), createDotDiameterAnimator((float) (this.mArrowRadius * 2), (float) (this.mDotRadius * 2)), createDotTranslationXAnimator()});
        this.mAnimator.playTogether(new Animator[]{this.mShowAnimator, this.mHideAnimator});
        setLayerType(1, null);
    }

    private int getDimensionFromTypedArray(TypedArray typedArray, int attr, int defaultId) {
        return typedArray.getDimensionPixelOffset(attr, getResources().getDimensionPixelOffset(defaultId));
    }

    private int getColorFromTypedArray(TypedArray typedArray, int attr, int defaultId) {
        return typedArray.getColor(attr, getResources().getColor(defaultId));
    }

    private Bitmap loadArrow() {
        Bitmap arrow = BitmapFactory.decodeResource(getResources(), R$drawable.lb_ic_nav_arrow);
        if (this.mIsLtr) {
            return arrow;
        }
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), matrix, false);
    }

    private Animator createDotAlphaAnimator(float from, float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_ALPHA, new float[]{from, to});
        animator.setDuration(167);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    private Animator createDotDiameterAnimator(float from, float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_DIAMETER, new float[]{from, to});
        animator.setDuration(417);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    private Animator createDotTranslationXAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_TRANSLATION_X, new float[]{(float) ((-this.mArrowGap) + this.mDotGap), 0.0f});
        animator.setDuration(417);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    private void calculateDotPositions() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int requiredWidth = getRequiredWidth();
        int mid = (left + right) / 2;
        this.mDotSelectedX = new int[this.mPageCount];
        this.mDotSelectedPrevX = new int[this.mPageCount];
        this.mDotSelectedNextX = new int[this.mPageCount];
        int i;
        if (this.mIsLtr) {
            int startLeft = mid - (requiredWidth / 2);
            this.mDotSelectedX[0] = ((this.mDotRadius + startLeft) - this.mDotGap) + this.mArrowGap;
            this.mDotSelectedPrevX[0] = this.mDotRadius + startLeft;
            this.mDotSelectedNextX[0] = ((this.mDotRadius + startLeft) - (this.mDotGap * 2)) + (this.mArrowGap * 2);
            for (i = 1; i < this.mPageCount; i++) {
                this.mDotSelectedX[i] = this.mDotSelectedPrevX[i - 1] + this.mArrowGap;
                this.mDotSelectedPrevX[i] = this.mDotSelectedPrevX[i - 1] + this.mDotGap;
                this.mDotSelectedNextX[i] = this.mDotSelectedX[i - 1] + this.mArrowGap;
            }
        } else {
            int startRight = mid + (requiredWidth / 2);
            this.mDotSelectedX[0] = ((startRight - this.mDotRadius) + this.mDotGap) - this.mArrowGap;
            this.mDotSelectedPrevX[0] = startRight - this.mDotRadius;
            this.mDotSelectedNextX[0] = ((startRight - this.mDotRadius) + (this.mDotGap * 2)) - (this.mArrowGap * 2);
            for (i = 1; i < this.mPageCount; i++) {
                this.mDotSelectedX[i] = this.mDotSelectedPrevX[i - 1] - this.mArrowGap;
                this.mDotSelectedPrevX[i] = this.mDotSelectedPrevX[i - 1] - this.mDotGap;
                this.mDotSelectedNextX[i] = this.mDotSelectedX[i - 1] - this.mArrowGap;
            }
        }
        this.mDotCenterY = this.mArrowRadius + top;
        adjustDotPosition();
    }

    @VisibleForTesting
    int getPageCount() {
        return this.mPageCount;
    }

    @VisibleForTesting
    int[] getDotSelectedX() {
        return this.mDotSelectedX;
    }

    @VisibleForTesting
    int[] getDotSelectedLeftX() {
        return this.mDotSelectedPrevX;
    }

    @VisibleForTesting
    int[] getDotSelectedRightX() {
        return this.mDotSelectedNextX;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        int width;
        int desiredHeight = getDesiredHeight();
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case Integer.MIN_VALUE:
                height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
                break;
            case 1073741824:
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
            default:
                height = desiredHeight;
                break;
        }
        int desiredWidth = getDesiredWidth();
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case Integer.MIN_VALUE:
                width = Math.min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec));
                break;
            case 1073741824:
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
            default:
                width = desiredWidth;
                break;
        }
        setMeasuredDimension(width, height);
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        setMeasuredDimension(width, height);
        calculateDotPositions();
    }

    private int getDesiredHeight() {
        return ((getPaddingTop() + this.mArrowDiameter) + getPaddingBottom()) + this.mShadowRadius;
    }

    private int getRequiredWidth() {
        return ((this.mDotRadius * 2) + (this.mArrowGap * 2)) + ((this.mPageCount - 3) * this.mDotGap);
    }

    private int getDesiredWidth() {
        return (getPaddingLeft() + getRequiredWidth()) + getPaddingRight();
    }

    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < this.mPageCount; i++) {
            this.mDots[i].draw(canvas);
        }
    }

    private void adjustDotPosition() {
        int i;
        float f = -1.0f;
        for (i = 0; i < this.mCurrentPage; i++) {
            float f2;
            this.mDots[i].deselect();
            Dot dot = this.mDots[i];
            if (i == this.mPreviousPage) {
                f2 = -1.0f;
            } else {
                f2 = 1.0f;
            }
            dot.mDirection = f2;
            this.mDots[i].mCenterX = (float) this.mDotSelectedPrevX[i];
        }
        this.mDots[this.mCurrentPage].select();
        Dot dot2 = this.mDots[this.mCurrentPage];
        if (this.mPreviousPage >= this.mCurrentPage) {
            f = 1.0f;
        }
        dot2.mDirection = f;
        this.mDots[this.mCurrentPage].mCenterX = (float) this.mDotSelectedX[this.mCurrentPage];
        for (i = this.mCurrentPage + 1; i < this.mPageCount; i++) {
            this.mDots[i].deselect();
            this.mDots[i].mDirection = 1.0f;
            this.mDots[i].mCenterX = (float) this.mDotSelectedNextX[i];
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        boolean isLtr = layoutDirection == 0;
        if (this.mIsLtr != isLtr) {
            this.mIsLtr = isLtr;
            this.mArrow = loadArrow();
            if (this.mDots != null) {
                for (Dot dot : this.mDots) {
                    dot.onRtlPropertiesChanged();
                }
            }
            calculateDotPositions();
            invalidate();
        }
    }
}
