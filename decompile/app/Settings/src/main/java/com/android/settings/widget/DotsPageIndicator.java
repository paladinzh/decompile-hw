package com.android.settings.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.Op;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnAttachStateChangeListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settings.R$styleable;
import java.util.Arrays;

public class DotsPageIndicator extends View implements OnPageChangeListener {
    public static final String TAG = DotsPageIndicator.class.getSimpleName();
    private long animDuration;
    private long animHalfDuration;
    private boolean attachedState;
    private final Path combinedUnselectedPath;
    float controlX1;
    float controlX2;
    float controlY1;
    float controlY2;
    private int currentPage;
    private float dotBottomY;
    private float[] dotCenterX;
    private float dotCenterY;
    private int dotDiameter;
    private float dotRadius;
    private float[] dotRevealFractions;
    private float dotTopY;
    float endX1;
    float endX2;
    float endY1;
    float endY2;
    private int gap;
    private float halfDotRadius;
    private final Interpolator interpolator;
    private AnimatorSet joiningAnimationSet;
    private ValueAnimator[] joiningAnimations;
    private float[] joiningFractions;
    private ValueAnimator moveAnimation;
    private OnPageChangeListener pageChangeListener;
    private int pageCount;
    private final RectF rectF;
    private PendingRetreatAnimator retreatAnimation;
    private float retreatingJoinX1;
    private float retreatingJoinX2;
    private PendingRevealAnimator[] revealAnimations;
    private int selectedColour;
    private boolean selectedDotInPosition;
    private float selectedDotX;
    private final Paint selectedPaint;
    private int unselectedColour;
    private final Path unselectedDotLeftPath;
    private final Path unselectedDotPath;
    private final Path unselectedDotRightPath;
    private final Paint unselectedPaint;
    private ViewPager viewPager;

    public abstract class StartPredicate {
        protected float thresholdValue;

        abstract boolean shouldStart(float f);

        public StartPredicate(float thresholdValue) {
            this.thresholdValue = thresholdValue;
        }
    }

    public class LeftwardStartPredicate extends StartPredicate {
        public LeftwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        boolean shouldStart(float currentValue) {
            return currentValue < this.thresholdValue;
        }
    }

    public abstract class PendingStartAnimator extends ValueAnimator {
        protected boolean hasStarted = false;
        protected StartPredicate predicate;

        public PendingStartAnimator(StartPredicate predicate) {
            this.predicate = predicate;
        }

        public void startIfNecessary(float currentValue) {
            if (!this.hasStarted && this.predicate.shouldStart(currentValue)) {
                start();
                this.hasStarted = true;
            }
        }
    }

    public class PendingRetreatAnimator extends PendingStartAnimator {
        public PendingRetreatAnimator(int was, int now, int steps, StartPredicate predicate) {
            float initialX1;
            float finalX1;
            float initialX2;
            float finalX2;
            super(predicate);
            setDuration(DotsPageIndicator.this.animHalfDuration);
            setInterpolator(DotsPageIndicator.this.interpolator);
            if (now > was) {
                initialX1 = Math.min(DotsPageIndicator.this.dotCenterX[was], DotsPageIndicator.this.selectedDotX) - DotsPageIndicator.this.dotRadius;
            } else {
                initialX1 = DotsPageIndicator.this.dotCenterX[now] - DotsPageIndicator.this.dotRadius;
            }
            if (now > was) {
                finalX1 = DotsPageIndicator.this.dotCenterX[now] - DotsPageIndicator.this.dotRadius;
            } else {
                finalX1 = DotsPageIndicator.this.dotCenterX[now] - DotsPageIndicator.this.dotRadius;
            }
            if (now > was) {
                initialX2 = DotsPageIndicator.this.dotCenterX[now] + DotsPageIndicator.this.dotRadius;
            } else {
                initialX2 = Math.max(DotsPageIndicator.this.dotCenterX[was], DotsPageIndicator.this.selectedDotX) + DotsPageIndicator.this.dotRadius;
            }
            if (now > was) {
                finalX2 = DotsPageIndicator.this.dotCenterX[now] + DotsPageIndicator.this.dotRadius;
            } else {
                finalX2 = DotsPageIndicator.this.dotCenterX[now] + DotsPageIndicator.this.dotRadius;
            }
            DotsPageIndicator.this.revealAnimations = new PendingRevealAnimator[steps];
            final int[] dotsToHide = new int[steps];
            int i;
            if (initialX1 != finalX1) {
                setFloatValues(new float[]{initialX1, finalX1});
                for (i = 0; i < steps; i++) {
                    DotsPageIndicator.this.revealAnimations[i] = new PendingRevealAnimator(was + i, new RightwardStartPredicate(DotsPageIndicator.this.dotCenterX[was + i]));
                    dotsToHide[i] = was + i;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        DotsPageIndicator.this.retreatingJoinX1 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        DotsPageIndicator.this.postInvalidateOnAnimation();
                        for (PendingRevealAnimator pendingReveal : DotsPageIndicator.this.revealAnimations) {
                            pendingReveal.startIfNecessary(DotsPageIndicator.this.retreatingJoinX1);
                        }
                    }
                });
            } else {
                setFloatValues(new float[]{initialX2, finalX2});
                for (i = 0; i < steps; i++) {
                    DotsPageIndicator.this.revealAnimations[i] = new PendingRevealAnimator(was - i, new LeftwardStartPredicate(DotsPageIndicator.this.dotCenterX[was - i]));
                    dotsToHide[i] = was - i;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        DotsPageIndicator.this.retreatingJoinX2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        DotsPageIndicator.this.postInvalidateOnAnimation();
                        for (PendingRevealAnimator pendingReveal : DotsPageIndicator.this.revealAnimations) {
                            pendingReveal.startIfNecessary(DotsPageIndicator.this.retreatingJoinX2);
                        }
                    }
                });
            }
            addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    DotsPageIndicator.this.cancelJoiningAnimations();
                    DotsPageIndicator.this.clearJoiningFractions();
                    for (int dot : dotsToHide) {
                        DotsPageIndicator.this.setDotRevealFraction(dot, 1.0E-5f);
                    }
                    DotsPageIndicator.this.retreatingJoinX1 = initialX1;
                    DotsPageIndicator.this.retreatingJoinX2 = initialX2;
                    DotsPageIndicator.this.postInvalidateOnAnimation();
                }

                public void onAnimationEnd(Animator animation) {
                    DotsPageIndicator.this.retreatingJoinX1 = -1.0f;
                    DotsPageIndicator.this.retreatingJoinX2 = -1.0f;
                    DotsPageIndicator.this.postInvalidateOnAnimation();
                }
            });
        }
    }

    public class PendingRevealAnimator extends PendingStartAnimator {
        private final int dot;

        public PendingRevealAnimator(int dot, StartPredicate predicate) {
            super(predicate);
            this.dot = dot;
            setFloatValues(new float[]{1.0E-5f, 1.0f});
            setDuration(DotsPageIndicator.this.animHalfDuration);
            setInterpolator(DotsPageIndicator.this.interpolator);
            addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DotsPageIndicator.this.setDotRevealFraction(PendingRevealAnimator.this.dot, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    DotsPageIndicator.this.setDotRevealFraction(PendingRevealAnimator.this.dot, 0.0f);
                    DotsPageIndicator.this.postInvalidateOnAnimation();
                }
            });
        }
    }

    public class RightwardStartPredicate extends StartPredicate {
        public RightwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        boolean shouldStart(float currentValue) {
            return currentValue > this.thresholdValue;
        }
    }

    public DotsPageIndicator(Context context) {
        this(context, null, 0);
    }

    public DotsPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotsPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int scaledDensity = (int) context.getResources().getDisplayMetrics().scaledDensity;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R$styleable.DotsPageIndicator, defStyle, 0);
        this.dotDiameter = typedArray.getDimensionPixelSize(0, scaledDensity * 8);
        this.dotRadius = (float) (this.dotDiameter / 2);
        this.halfDotRadius = this.dotRadius / 2.0f;
        this.gap = typedArray.getDimensionPixelSize(1, scaledDensity * 12);
        this.animDuration = (long) typedArray.getInteger(2, 400);
        this.animHalfDuration = this.animDuration / 2;
        this.unselectedColour = typedArray.getColor(3, -2130706433);
        this.selectedColour = typedArray.getColor(4, -1);
        typedArray.recycle();
        this.unselectedPaint = new Paint(1);
        this.unselectedPaint.setColor(this.unselectedColour);
        this.selectedPaint = new Paint(1);
        this.selectedPaint.setColor(this.selectedColour);
        if (VERSION.SDK_INT >= 21) {
            this.interpolator = AnimationUtils.loadInterpolator(context, 17563661);
        } else {
            this.interpolator = AnimationUtils.loadInterpolator(context, 17432580);
        }
        this.combinedUnselectedPath = new Path();
        this.unselectedDotPath = new Path();
        this.unselectedDotLeftPath = new Path();
        this.unselectedDotRightPath = new Path();
        this.rectF = new RectF();
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                DotsPageIndicator.this.attachedState = true;
            }

            public void onViewDetachedFromWindow(View v) {
                DotsPageIndicator.this.attachedState = false;
            }
        });
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);
        setPageCount(viewPager.getAdapter().getCount());
        viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                DotsPageIndicator.this.setPageCount(DotsPageIndicator.this.viewPager.getAdapter().getCount());
            }
        });
        setCurrentPageImmediate();
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.pageChangeListener = onPageChangeListener;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    public void onPageSelected(int position) {
        if (this.attachedState) {
            setSelectedPage(position);
        } else {
            setCurrentPageImmediate();
        }
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageSelected(position);
        }
    }

    public void onPageScrollStateChanged(int state) {
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void setPageCount(int pages) {
        this.pageCount = pages;
        calculateDotPositions();
        resetState();
    }

    private void calculateDotPositions() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        float startLeft = ((float) (((((getWidth() - getPaddingRight()) - left) - getRequiredWidth()) / 2) + left)) + this.dotRadius;
        this.dotCenterX = new float[this.pageCount];
        for (int i = 0; i < this.pageCount; i++) {
            this.dotCenterX[i] = ((float) ((this.dotDiameter + this.gap) * i)) + startLeft;
        }
        this.dotTopY = (float) top;
        this.dotCenterY = ((float) top) + this.dotRadius;
        this.dotBottomY = (float) (this.dotDiameter + top);
        setCurrentPageImmediate();
    }

    private void setCurrentPageImmediate() {
        if (this.viewPager != null) {
            this.currentPage = this.viewPager.getCurrentItem();
        } else {
            this.currentPage = 0;
        }
        if (this.pageCount > 0) {
            this.selectedDotX = this.dotCenterX[this.currentPage];
        }
    }

    private void resetState() {
        if (this.pageCount > 0) {
            this.joiningFractions = new float[(this.pageCount - 1)];
            Arrays.fill(this.joiningFractions, 0.0f);
            this.dotRevealFractions = new float[this.pageCount];
            Arrays.fill(this.dotRevealFractions, 0.0f);
            this.retreatingJoinX1 = -1.0f;
            this.retreatingJoinX2 = -1.0f;
            this.selectedDotInPosition = true;
        }
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
        calculateDotPositions();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        setMeasuredDimension(width, height);
        calculateDotPositions();
    }

    public void clearAnimation() {
        super.clearAnimation();
        if (VERSION.SDK_INT >= 16) {
            cancelRunningAnimations();
        }
    }

    private int getDesiredHeight() {
        return (getPaddingTop() + this.dotDiameter) + getPaddingBottom();
    }

    private int getRequiredWidth() {
        return (this.pageCount * this.dotDiameter) + ((this.pageCount - 1) * this.gap);
    }

    private int getDesiredWidth() {
        return (getPaddingLeft() + getRequiredWidth()) + getPaddingRight();
    }

    protected void onDraw(Canvas canvas) {
        if (this.viewPager != null && this.pageCount != 0) {
            drawUnselected(canvas);
            drawSelected(canvas);
        }
    }

    private void drawUnselected(Canvas canvas) {
        this.combinedUnselectedPath.rewind();
        int page = 0;
        while (page < this.pageCount) {
            int nextXIndex = page == this.pageCount + -1 ? page : page + 1;
            if (VERSION.SDK_INT >= 21) {
                this.combinedUnselectedPath.op(getUnselectedPath(page, this.dotCenterX[page], this.dotCenterX[nextXIndex], page == this.pageCount + -1 ? -1.0f : this.joiningFractions[page], this.dotRevealFractions[page]), Op.UNION);
            } else {
                canvas.drawCircle(this.dotCenterX[page], this.dotCenterY, this.dotRadius, this.unselectedPaint);
            }
            page++;
        }
        if (this.retreatingJoinX1 != -1.0f && VERSION.SDK_INT >= 21) {
            this.combinedUnselectedPath.op(getRetreatingJoinPath(), Op.UNION);
        }
        canvas.drawPath(this.combinedUnselectedPath, this.unselectedPaint);
    }

    private Path getUnselectedPath(int page, float centerX, float nextCenterX, float joiningFraction, float dotRevealFraction) {
        this.unselectedDotPath.rewind();
        if ((joiningFraction == 0.0f || joiningFraction == -1.0f) && dotRevealFraction == 0.0f && !(page == this.currentPage && this.selectedDotInPosition)) {
            this.unselectedDotPath.addCircle(this.dotCenterX[page], this.dotCenterY, this.dotRadius, Direction.CW);
        }
        if (joiningFraction > 0.0f && joiningFraction < 0.5f && this.retreatingJoinX1 == -1.0f) {
            this.unselectedDotLeftPath.rewind();
            this.unselectedDotLeftPath.moveTo(centerX, this.dotBottomY);
            this.rectF.set(centerX - this.dotRadius, this.dotTopY, this.dotRadius + centerX, this.dotBottomY);
            this.unselectedDotLeftPath.arcTo(this.rectF, 90.0f, 180.0f, true);
            this.endX1 = (this.dotRadius + centerX) + (((float) this.gap) * joiningFraction);
            this.endY1 = this.dotCenterY;
            this.controlX1 = this.halfDotRadius + centerX;
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1;
            this.controlY2 = this.endY1 - this.halfDotRadius;
            this.unselectedDotLeftPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = centerX;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1;
            this.controlY1 = this.endY1 + this.halfDotRadius;
            this.controlX2 = this.halfDotRadius + centerX;
            this.controlY2 = this.dotBottomY;
            this.unselectedDotLeftPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            if (VERSION.SDK_INT >= 21) {
                this.unselectedDotPath.op(this.unselectedDotLeftPath, Op.UNION);
            }
            this.unselectedDotRightPath.rewind();
            this.unselectedDotRightPath.moveTo(nextCenterX, this.dotBottomY);
            this.rectF.set(nextCenterX - this.dotRadius, this.dotTopY, this.dotRadius + nextCenterX, this.dotBottomY);
            this.unselectedDotRightPath.arcTo(this.rectF, 90.0f, -180.0f, true);
            this.endX1 = (nextCenterX - this.dotRadius) - (((float) this.gap) * joiningFraction);
            this.endY1 = this.dotCenterY;
            this.controlX1 = nextCenterX - this.halfDotRadius;
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1;
            this.controlY2 = this.endY1 - this.halfDotRadius;
            this.unselectedDotRightPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = nextCenterX;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1;
            this.controlY1 = this.endY1 + this.halfDotRadius;
            this.controlX2 = this.endX2 - this.halfDotRadius;
            this.controlY2 = this.dotBottomY;
            this.unselectedDotRightPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            if (VERSION.SDK_INT >= 21) {
                this.unselectedDotPath.op(this.unselectedDotRightPath, Op.UNION);
            }
        }
        if (joiningFraction > 0.5f && joiningFraction < 1.0f && this.retreatingJoinX1 == -1.0f) {
            this.unselectedDotPath.moveTo(centerX, this.dotBottomY);
            this.rectF.set(centerX - this.dotRadius, this.dotTopY, this.dotRadius + centerX, this.dotBottomY);
            this.unselectedDotPath.arcTo(this.rectF, 90.0f, 180.0f, true);
            this.endX1 = (this.dotRadius + centerX) + ((float) (this.gap / 2));
            this.endY1 = this.dotCenterY - (this.dotRadius * joiningFraction);
            this.controlX1 = this.endX1 - (this.dotRadius * joiningFraction);
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1 - ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY2 = this.endY1;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = nextCenterX;
            this.endY2 = this.dotTopY;
            this.controlX1 = this.endX1 + ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY1 = this.endY1;
            this.controlX2 = this.endX1 + (this.dotRadius * joiningFraction);
            this.controlY2 = this.dotTopY;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            this.rectF.set(nextCenterX - this.dotRadius, this.dotTopY, this.dotRadius + nextCenterX, this.dotBottomY);
            this.unselectedDotPath.arcTo(this.rectF, 270.0f, 180.0f, true);
            this.endY1 = this.dotCenterY + (this.dotRadius * joiningFraction);
            this.controlX1 = this.endX1 + (this.dotRadius * joiningFraction);
            this.controlY1 = this.dotBottomY;
            this.controlX2 = this.endX1 + ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY2 = this.endY1;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = centerX;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1 - ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY1 = this.endY1;
            this.controlX2 = this.endX1 - (this.dotRadius * joiningFraction);
            this.controlY2 = this.endY2;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
        }
        if (joiningFraction == 1.0f && this.retreatingJoinX1 == -1.0f) {
            this.rectF.set(centerX - this.dotRadius, this.dotTopY, this.dotRadius + nextCenterX, this.dotBottomY);
            this.unselectedDotPath.addRoundRect(this.rectF, this.dotRadius, this.dotRadius, Direction.CW);
        }
        if (dotRevealFraction > 1.0E-5f) {
            this.unselectedDotPath.addCircle(centerX, this.dotCenterY, this.dotRadius * dotRevealFraction, Direction.CW);
        }
        return this.unselectedDotPath;
    }

    private Path getRetreatingJoinPath() {
        this.unselectedDotPath.rewind();
        this.rectF.set(this.retreatingJoinX1, this.dotTopY, this.retreatingJoinX2, this.dotBottomY);
        this.unselectedDotPath.addRoundRect(this.rectF, this.dotRadius, this.dotRadius, Direction.CW);
        return this.unselectedDotPath;
    }

    private void drawSelected(Canvas canvas) {
        canvas.drawCircle(this.selectedDotX, this.dotCenterY, this.dotRadius, this.selectedPaint);
    }

    private void setSelectedPage(int now) {
        if (now != this.currentPage && this.pageCount != 0) {
            int was = this.currentPage;
            this.currentPage = now;
            if (VERSION.SDK_INT >= 16) {
                cancelRunningAnimations();
                int steps = Math.abs(now - was);
                this.moveAnimation = createMoveSelectedAnimator(this.dotCenterX[now], was, now, steps);
                this.joiningAnimations = new ValueAnimator[steps];
                int i = 0;
                while (i < steps) {
                    this.joiningAnimations[i] = createJoiningAnimator(now > was ? was + i : (was - 1) - i, ((long) i) * (this.animDuration / 8));
                    i++;
                }
                this.moveAnimation.start();
                startJoiningAnimations();
            } else {
                setCurrentPageImmediate();
                invalidate();
            }
        }
    }

    private ValueAnimator createMoveSelectedAnimator(float moveTo, int was, int now, int steps) {
        StartPredicate rightwardStartPredicate;
        ValueAnimator moveSelected = ValueAnimator.ofFloat(new float[]{this.selectedDotX, moveTo});
        if (now > was) {
            rightwardStartPredicate = new RightwardStartPredicate(moveTo - ((moveTo - this.selectedDotX) * 0.25f));
        } else {
            rightwardStartPredicate = new LeftwardStartPredicate(((this.selectedDotX - moveTo) * 0.25f) + moveTo);
        }
        this.retreatAnimation = new PendingRetreatAnimator(was, now, steps, rightwardStartPredicate);
        moveSelected.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DotsPageIndicator.this.selectedDotX = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                DotsPageIndicator.this.retreatAnimation.startIfNecessary(DotsPageIndicator.this.selectedDotX);
                DotsPageIndicator.this.postInvalidateOnAnimation();
            }
        });
        moveSelected.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                DotsPageIndicator.this.selectedDotInPosition = false;
            }

            public void onAnimationEnd(Animator animation) {
                DotsPageIndicator.this.selectedDotInPosition = true;
            }
        });
        moveSelected.setStartDelay(this.selectedDotInPosition ? this.animDuration / 4 : 0);
        moveSelected.setDuration((this.animDuration * 3) / 4);
        moveSelected.setInterpolator(this.interpolator);
        return moveSelected;
    }

    private ValueAnimator createJoiningAnimator(final int leftJoiningDot, long startDelay) {
        ValueAnimator joining = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        joining.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DotsPageIndicator.this.setJoiningFraction(leftJoiningDot, valueAnimator.getAnimatedFraction());
            }
        });
        joining.setDuration(this.animHalfDuration);
        joining.setStartDelay(startDelay);
        joining.setInterpolator(this.interpolator);
        return joining;
    }

    private void setJoiningFraction(int leftDot, float fraction) {
        this.joiningFractions[leftDot] = fraction;
        postInvalidateOnAnimation();
    }

    private void clearJoiningFractions() {
        Arrays.fill(this.joiningFractions, 0.0f);
        postInvalidateOnAnimation();
    }

    private void setDotRevealFraction(int dot, float fraction) {
        this.dotRevealFractions[dot] = fraction;
        postInvalidateOnAnimation();
    }

    private void cancelRunningAnimations() {
        cancelMoveAnimation();
        cancelJoiningAnimations();
        cancelRetreatAnimation();
        cancelRevealAnimations();
        resetState();
    }

    private void cancelMoveAnimation() {
        if (this.moveAnimation != null && this.moveAnimation.isRunning()) {
            this.moveAnimation.cancel();
        }
    }

    private void startJoiningAnimations() {
        this.joiningAnimationSet = new AnimatorSet();
        this.joiningAnimationSet.playTogether(this.joiningAnimations);
        this.joiningAnimationSet.start();
    }

    private void cancelJoiningAnimations() {
        if (this.joiningAnimationSet != null && this.joiningAnimationSet.isRunning()) {
            this.joiningAnimationSet.cancel();
        }
    }

    private void cancelRetreatAnimation() {
        if (this.retreatAnimation != null && this.retreatAnimation.isRunning()) {
            this.retreatAnimation.cancel();
        }
    }

    private void cancelRevealAnimations() {
        if (this.revealAnimations != null) {
            for (PendingRevealAnimator reveal : this.revealAnimations) {
                reveal.cancel();
            }
        }
    }
}
