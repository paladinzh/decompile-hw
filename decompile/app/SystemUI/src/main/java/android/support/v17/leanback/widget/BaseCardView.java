package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R$attr;
import android.support.v17.leanback.R$integer;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import java.util.ArrayList;

public class BaseCardView extends FrameLayout {
    private static final int[] LB_PRESSED_STATE_SET = new int[]{16842919};
    private final int mActivatedAnimDuration;
    private Animation mAnim;
    private final Runnable mAnimationTrigger;
    private int mCardType;
    private boolean mDelaySelectedAnim;
    private ArrayList<View> mExtraViewList;
    private int mExtraVisibility;
    private float mInfoAlpha;
    private float mInfoOffset;
    private ArrayList<View> mInfoViewList;
    private float mInfoVisFraction;
    private int mInfoVisibility;
    private ArrayList<View> mMainViewList;
    private int mMeasuredHeight;
    private int mMeasuredWidth;
    private final int mSelectedAnimDuration;
    private int mSelectedAnimationDelay;

    private class InfoAlphaAnimation extends Animation {
        private float mDelta;
        private float mStartValue;

        public InfoAlphaAnimation(float start, float end) {
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoAlpha = this.mStartValue + (this.mDelta * interpolatedTime);
            for (int i = 0; i < BaseCardView.this.mInfoViewList.size(); i++) {
                ((View) BaseCardView.this.mInfoViewList.get(i)).setAlpha(BaseCardView.this.mInfoAlpha);
            }
        }
    }

    private class InfoHeightAnimation extends Animation {
        private float mDelta;
        private float mStartValue;

        public InfoHeightAnimation(float start, float end) {
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoVisFraction = this.mStartValue + (this.mDelta * interpolatedTime);
            BaseCardView.this.requestLayout();
        }
    }

    private class InfoOffsetAnimation extends Animation {
        private float mDelta;
        private float mStartValue;

        public InfoOffsetAnimation(float start, float end) {
            this.mStartValue = start;
            this.mDelta = end - start;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            BaseCardView.this.mInfoOffset = this.mStartValue + (this.mDelta * interpolatedTime);
            BaseCardView.this.requestLayout();
        }
    }

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "MAIN"), @IntToString(from = 1, to = "INFO"), @IntToString(from = 2, to = "EXTRA")})
        public int viewType = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R$styleable.lbBaseCardView_Layout);
            this.viewType = a.getInt(R$styleable.lbBaseCardView_Layout_layout_viewType, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.viewType = source.viewType;
        }
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R$attr.baseCardViewStyle);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mInfoAlpha = 1.0f;
        this.mAnimationTrigger = new Runnable() {
            public void run() {
                BaseCardView.this.animateInfoOffset(true);
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.lbBaseCardView, defStyleAttr, 0);
        try {
            this.mCardType = a.getInteger(R$styleable.lbBaseCardView_cardType, 0);
            Drawable cardForeground = a.getDrawable(R$styleable.lbBaseCardView_cardForeground);
            if (cardForeground != null) {
                setForeground(cardForeground);
            }
            Drawable cardBackground = a.getDrawable(R$styleable.lbBaseCardView_cardBackground);
            if (cardBackground != null) {
                setBackground(cardBackground);
            }
            this.mInfoVisibility = a.getInteger(R$styleable.lbBaseCardView_infoVisibility, 1);
            this.mExtraVisibility = a.getInteger(R$styleable.lbBaseCardView_extraVisibility, 2);
            if (this.mExtraVisibility < this.mInfoVisibility) {
                this.mExtraVisibility = this.mInfoVisibility;
            }
            this.mSelectedAnimationDelay = a.getInteger(R$styleable.lbBaseCardView_selectedAnimationDelay, getResources().getInteger(R$integer.lb_card_selected_animation_delay));
            this.mSelectedAnimDuration = a.getInteger(R$styleable.lbBaseCardView_selectedAnimationDuration, getResources().getInteger(R$integer.lb_card_selected_animation_duration));
            this.mActivatedAnimDuration = a.getInteger(R$styleable.lbBaseCardView_activatedAnimationDuration, getResources().getInteger(R$integer.lb_card_activated_animation_duration));
            this.mDelaySelectedAnim = true;
            this.mMainViewList = new ArrayList();
            this.mInfoViewList = new ArrayList();
            this.mExtraViewList = new ArrayList();
            this.mInfoOffset = 0.0f;
            this.mInfoVisFraction = 0.0f;
        } finally {
            a.recycle();
        }
    }

    public void setActivated(boolean activated) {
        if (activated != isActivated()) {
            super.setActivated(activated);
            applyActiveState(isActivated());
        }
    }

    public void setSelected(boolean selected) {
        if (selected != isSelected()) {
            super.setSelected(selected);
            applySelectedState(isSelected());
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        this.mMeasuredWidth = 0;
        this.mMeasuredHeight = 0;
        int state = 0;
        int mainHeight = 0;
        int infoHeight = 0;
        int extraHeight = 0;
        findChildrenViews();
        int unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, 0);
        for (i = 0; i < this.mMainViewList.size(); i++) {
            View mainView = (View) this.mMainViewList.get(i);
            if (mainView.getVisibility() != 8) {
                measureChild(mainView, unspecifiedSpec, unspecifiedSpec);
                this.mMeasuredWidth = Math.max(this.mMeasuredWidth, mainView.getMeasuredWidth());
                mainHeight += mainView.getMeasuredHeight();
                state = View.combineMeasuredStates(state, mainView.getMeasuredState());
            }
        }
        setPivotX((float) (this.mMeasuredWidth / 2));
        setPivotY((float) (mainHeight / 2));
        int cardWidthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
        if (hasInfoRegion()) {
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                View infoView = (View) this.mInfoViewList.get(i);
                if (infoView.getVisibility() != 8) {
                    measureChild(infoView, cardWidthMeasureSpec, unspecifiedSpec);
                    if (this.mCardType != 1) {
                        infoHeight += infoView.getMeasuredHeight();
                    }
                    state = View.combineMeasuredStates(state, infoView.getMeasuredState());
                }
            }
            if (hasExtraRegion()) {
                for (i = 0; i < this.mExtraViewList.size(); i++) {
                    View extraView = (View) this.mExtraViewList.get(i);
                    if (extraView.getVisibility() != 8) {
                        measureChild(extraView, cardWidthMeasureSpec, unspecifiedSpec);
                        extraHeight += extraView.getMeasuredHeight();
                        state = View.combineMeasuredStates(state, extraView.getMeasuredState());
                    }
                }
            }
        }
        boolean infoAnimating = hasInfoRegion() && this.mInfoVisibility == 2;
        this.mMeasuredHeight = (int) ((((float) extraHeight) + ((infoAnimating ? ((float) infoHeight) * this.mInfoVisFraction : (float) infoHeight) + ((float) mainHeight))) - (infoAnimating ? 0.0f : this.mInfoOffset));
        setMeasuredDimension(View.resolveSizeAndState((this.mMeasuredWidth + getPaddingLeft()) + getPaddingRight(), widthMeasureSpec, state), View.resolveSizeAndState((this.mMeasuredHeight + getPaddingTop()) + getPaddingBottom(), heightMeasureSpec, state << 16));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int i;
        float currBottom = (float) getPaddingTop();
        for (i = 0; i < this.mMainViewList.size(); i++) {
            View mainView = (View) this.mMainViewList.get(i);
            if (mainView.getVisibility() != 8) {
                mainView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) mainView.getMeasuredHeight()) + currBottom));
                currBottom += (float) mainView.getMeasuredHeight();
            }
        }
        if (hasInfoRegion()) {
            float infoHeight = 0.0f;
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                infoHeight += (float) ((View) this.mInfoViewList.get(i)).getMeasuredHeight();
            }
            if (this.mCardType == 1) {
                currBottom -= infoHeight;
                if (currBottom < 0.0f) {
                    currBottom = 0.0f;
                }
            } else if (this.mCardType != 2) {
                currBottom -= this.mInfoOffset;
            } else if (this.mInfoVisibility == 2) {
                infoHeight *= this.mInfoVisFraction;
            }
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                View infoView = (View) this.mInfoViewList.get(i);
                if (infoView.getVisibility() != 8) {
                    int viewHeight = infoView.getMeasuredHeight();
                    if (((float) viewHeight) > infoHeight) {
                        viewHeight = (int) infoHeight;
                    }
                    infoView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) viewHeight) + currBottom));
                    currBottom += (float) viewHeight;
                    infoHeight -= (float) viewHeight;
                    if (infoHeight <= 0.0f) {
                        break;
                    }
                }
            }
            if (hasExtraRegion()) {
                for (i = 0; i < this.mExtraViewList.size(); i++) {
                    View extraView = (View) this.mExtraViewList.get(i);
                    if (extraView.getVisibility() != 8) {
                        extraView.layout(getPaddingLeft(), (int) currBottom, this.mMeasuredWidth + getPaddingLeft(), (int) (((float) extraView.getMeasuredHeight()) + currBottom));
                        currBottom += (float) extraView.getMeasuredHeight();
                    }
                }
            }
        }
        onSizeChanged(0, 0, right - left, bottom - top);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mAnimationTrigger);
        cancelAnimations();
        this.mInfoOffset = 0.0f;
        this.mInfoVisFraction = 0.0f;
    }

    private boolean hasInfoRegion() {
        return this.mCardType != 0;
    }

    private boolean hasExtraRegion() {
        return this.mCardType == 3;
    }

    private boolean isRegionVisible(int regionVisibility) {
        boolean z = false;
        switch (regionVisibility) {
            case 0:
                return true;
            case 1:
                return isActivated();
            case 2:
                if (isActivated()) {
                    z = isSelected();
                }
                return z;
            default:
                return false;
        }
    }

    private void findChildrenViews() {
        this.mMainViewList.clear();
        this.mInfoViewList.clear();
        this.mExtraViewList.clear();
        int count = getChildCount();
        boolean infoVisible = isRegionVisible(this.mInfoVisibility);
        boolean extraVisible = hasExtraRegion() && this.mInfoOffset > 0.0f;
        if (this.mCardType == 2 && this.mInfoVisibility == 2) {
            infoVisible = infoVisible && this.mInfoVisFraction > 0.0f;
        }
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.viewType == 1) {
                    this.mInfoViewList.add(child);
                    child.setVisibility(infoVisible ? 0 : 8);
                } else if (lp.viewType == 2) {
                    this.mExtraViewList.add(child);
                    child.setVisibility(extraVisible ? 0 : 8);
                } else {
                    this.mMainViewList.add(child);
                    child.setVisibility(0);
                }
            }
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] s = super.onCreateDrawableState(extraSpace);
        int N = s.length;
        boolean pressed = false;
        boolean enabled = false;
        for (int i = 0; i < N; i++) {
            if (s[i] == 16842919) {
                pressed = true;
            }
            if (s[i] == 16842910) {
                enabled = true;
            }
        }
        if (pressed && enabled) {
            return View.PRESSED_ENABLED_STATE_SET;
        }
        if (pressed) {
            return LB_PRESSED_STATE_SET;
        }
        if (enabled) {
            return View.ENABLED_STATE_SET;
        }
        return View.EMPTY_STATE_SET;
    }

    private void applyActiveState(boolean active) {
        if (hasInfoRegion() && this.mInfoVisibility <= 1) {
            setInfoViewVisibility(active);
        }
        if (hasExtraRegion() && this.mExtraVisibility > 1) {
        }
    }

    private void setInfoViewVisibility(boolean visible) {
        int i;
        if (this.mCardType == 3) {
            if (visible) {
                for (i = 0; i < this.mInfoViewList.size(); i++) {
                    ((View) this.mInfoViewList.get(i)).setVisibility(0);
                }
                return;
            }
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                ((View) this.mInfoViewList.get(i)).setVisibility(8);
            }
            for (i = 0; i < this.mExtraViewList.size(); i++) {
                ((View) this.mExtraViewList.get(i)).setVisibility(8);
            }
            this.mInfoOffset = 0.0f;
        } else if (this.mCardType == 2) {
            if (this.mInfoVisibility == 2) {
                animateInfoHeight(visible);
                return;
            }
            for (i = 0; i < this.mInfoViewList.size(); i++) {
                int i2;
                View view = (View) this.mInfoViewList.get(i);
                if (visible) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                view.setVisibility(i2);
            }
        } else if (this.mCardType == 1) {
            animateInfoAlpha(visible);
        }
    }

    private void applySelectedState(boolean focused) {
        removeCallbacks(this.mAnimationTrigger);
        if (this.mCardType == 3) {
            if (!focused) {
                animateInfoOffset(false);
            } else if (this.mDelaySelectedAnim) {
                postDelayed(this.mAnimationTrigger, (long) this.mSelectedAnimationDelay);
            } else {
                post(this.mAnimationTrigger);
                this.mDelaySelectedAnim = true;
            }
        } else if (this.mInfoVisibility == 2) {
            setInfoViewVisibility(focused);
        }
    }

    private void cancelAnimations() {
        if (this.mAnim != null) {
            this.mAnim.cancel();
            this.mAnim = null;
        }
    }

    private void animateInfoOffset(boolean shown) {
        cancelAnimations();
        int extraHeight = 0;
        if (shown) {
            int widthSpec = MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, 0);
            for (int i = 0; i < this.mExtraViewList.size(); i++) {
                View extraView = (View) this.mExtraViewList.get(i);
                extraView.setVisibility(0);
                extraView.measure(widthSpec, heightSpec);
                extraHeight = Math.max(extraHeight, extraView.getMeasuredHeight());
            }
        }
        float f = this.mInfoOffset;
        if (!shown) {
            extraHeight = 0;
        }
        this.mAnim = new InfoOffsetAnimation(f, (float) extraHeight);
        this.mAnim.setDuration((long) this.mSelectedAnimDuration);
        this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (BaseCardView.this.mInfoOffset == 0.0f) {
                    for (int i = 0; i < BaseCardView.this.mExtraViewList.size(); i++) {
                        ((View) BaseCardView.this.mExtraViewList.get(i)).setVisibility(8);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    private void animateInfoHeight(boolean shown) {
        cancelAnimations();
        int extraHeight = 0;
        if (shown) {
            int widthSpec = MeasureSpec.makeMeasureSpec(this.mMeasuredWidth, 1073741824);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, 0);
            for (int i = 0; i < this.mExtraViewList.size(); i++) {
                View extraView = (View) this.mExtraViewList.get(i);
                extraView.setVisibility(0);
                extraView.measure(widthSpec, heightSpec);
                extraHeight = Math.max(extraHeight, extraView.getMeasuredHeight());
            }
        }
        this.mAnim = new InfoHeightAnimation(this.mInfoVisFraction, shown ? 1.0f : 0.0f);
        this.mAnim.setDuration((long) this.mSelectedAnimDuration);
        this.mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (BaseCardView.this.mInfoOffset == 0.0f) {
                    for (int i = 0; i < BaseCardView.this.mExtraViewList.size(); i++) {
                        ((View) BaseCardView.this.mExtraViewList.get(i)).setVisibility(8);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    private void animateInfoAlpha(boolean shown) {
        cancelAnimations();
        if (shown) {
            for (int i = 0; i < this.mInfoViewList.size(); i++) {
                ((View) this.mInfoViewList.get(i)).setVisibility(0);
            }
        }
        this.mAnim = new InfoAlphaAnimation(this.mInfoAlpha, shown ? 1.0f : 0.0f);
        this.mAnim.setDuration((long) this.mActivatedAnimDuration);
        this.mAnim.setInterpolator(new DecelerateInterpolator());
        this.mAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (((double) BaseCardView.this.mInfoAlpha) == 0.0d) {
                    for (int i = 0; i < BaseCardView.this.mInfoViewList.size(); i++) {
                        ((View) BaseCardView.this.mInfoViewList.get(i)).setVisibility(8);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(this.mAnim);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public String toString() {
        return super.toString();
    }
}
