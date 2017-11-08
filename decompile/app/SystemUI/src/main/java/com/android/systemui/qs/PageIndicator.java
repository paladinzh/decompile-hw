package com.android.systemui.qs;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.android.systemui.R;
import java.util.ArrayList;

public class PageIndicator extends ViewGroup {
    private boolean mAnimating;
    private final Runnable mAnimationDone = new Runnable() {
        public void run() {
            PageIndicator.this.mAnimating = false;
            if (PageIndicator.this.mQueuedPositions.size() != 0) {
                PageIndicator.this.setPosition(((Integer) PageIndicator.this.mQueuedPositions.remove(0)).intValue());
            }
        }
    };
    private final int mPageDotWidth = ((int) (((float) this.mPageIndicatorWidth) * 0.4f));
    private final int mPageIndicatorHeight = ((int) this.mContext.getResources().getDimension(R.dimen.qs_page_indicator_height));
    private final int mPageIndicatorWidth = ((int) this.mContext.getResources().getDimension(R.dimen.qs_page_indicator_width));
    private int mPosition = -1;
    private final ArrayList<Integer> mQueuedPositions = new ArrayList();

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNumPages(int numPages) {
        setVisibility(numPages > 1 ? 0 : 4);
        if (this.mAnimating) {
            Log.w("PageIndicator", "setNumPages during animation");
        }
        while (numPages < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        while (numPages > getChildCount()) {
            ImageView v = new ImageView(this.mContext);
            v.setImageResource(R.drawable.minor_a_b);
            addView(v, new LayoutParams(this.mPageIndicatorWidth, this.mPageIndicatorHeight));
        }
        setIndex(this.mPosition >> 1);
    }

    public void setLocation(float location) {
        int i = 1;
        int index = (int) location;
        setContentDescription(getContext().getString(R.string.accessibility_quick_settings_page, new Object[]{Integer.valueOf(index + 1), Integer.valueOf(getChildCount())}));
        int i2 = index << 1;
        if (location == ((float) index)) {
            i = 0;
        }
        int position = i2 | i;
        int lastPosition = this.mPosition;
        if (this.mQueuedPositions.size() != 0) {
            lastPosition = ((Integer) this.mQueuedPositions.get(this.mQueuedPositions.size() - 1)).intValue();
        }
        if (position != lastPosition) {
            if (this.mAnimating) {
                this.mQueuedPositions.add(Integer.valueOf(position));
            } else {
                setPosition(position);
            }
        }
    }

    private void setPosition(int position) {
        if (isVisibleToUser() && Math.abs(this.mPosition - position) == 1) {
            animate(this.mPosition, position);
        } else {
            setIndex(position >> 1);
        }
        this.mPosition = position;
    }

    private void setIndex(int index) {
        int N = getChildCount();
        for (int i = 0; i < N; i++) {
            boolean z;
            ImageView v = (ImageView) getChildAt(i);
            v.setTranslationX(0.0f);
            v.setImageResource(R.drawable.major_a_b);
            if (i == index) {
                z = true;
            } else {
                z = false;
            }
            v.setAlpha(getAlpha(z));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void animate(int from, int to) {
        boolean fromTransition;
        boolean isAState;
        int firstIndex;
        int secondIndex;
        ImageView first;
        ImageView second;
        int fromIndex = from >> 1;
        int toIndex = to >> 1;
        setIndex(fromIndex);
        if ((from & 1) != 0) {
            fromTransition = true;
        } else {
            fromTransition = false;
        }
        if (!fromTransition) {
            if (from < to) {
            }
            isAState = false;
            firstIndex = Math.min(fromIndex, toIndex);
            secondIndex = Math.max(fromIndex, toIndex);
            if (secondIndex == firstIndex) {
                secondIndex++;
            }
            first = (ImageView) getChildAt(firstIndex);
            second = (ImageView) getChildAt(secondIndex);
            if (first == null && second != null) {
                second.setTranslationX(first.getX() - second.getX());
                playAnimation(first, getTransition(fromTransition, isAState, false));
                first.setAlpha(getAlpha(false));
                playAnimation(second, getTransition(fromTransition, isAState, true));
                second.setAlpha(getAlpha(true));
                this.mAnimating = true;
                return;
            }
        }
        isAState = true;
        firstIndex = Math.min(fromIndex, toIndex);
        secondIndex = Math.max(fromIndex, toIndex);
        if (secondIndex == firstIndex) {
            secondIndex++;
        }
        first = (ImageView) getChildAt(firstIndex);
        second = (ImageView) getChildAt(secondIndex);
        if (first == null) {
        }
    }

    private float getAlpha(boolean isMajor) {
        return isMajor ? 1.0f : 0.3f;
    }

    private void playAnimation(ImageView imageView, int res) {
        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getContext().getDrawable(res);
        imageView.setImageDrawable(avd);
        avd.forceAnimationOnUI();
        avd.start();
        postDelayed(this.mAnimationDone, 250);
    }

    private int getTransition(boolean fromB, boolean isMajorAState, boolean isMajor) {
        if (isMajor) {
            if (fromB) {
                if (isMajorAState) {
                    return R.drawable.major_b_a_animation;
                }
                return R.drawable.major_b_c_animation;
            } else if (isMajorAState) {
                return R.drawable.major_a_b_animation;
            } else {
                return R.drawable.major_c_b_animation;
            }
        } else if (fromB) {
            if (isMajorAState) {
                return R.drawable.minor_b_c_animation;
            }
            return R.drawable.minor_b_a_animation;
        } else if (isMajorAState) {
            return R.drawable.minor_c_b_animation;
        } else {
            return R.drawable.minor_a_b_animation;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int N = getChildCount();
        if (N == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthChildSpec = MeasureSpec.makeMeasureSpec(this.mPageIndicatorWidth, 1073741824);
        int heightChildSpec = MeasureSpec.makeMeasureSpec(this.mPageIndicatorHeight, 1073741824);
        for (int i = 0; i < N; i++) {
            getChildAt(i).measure(widthChildSpec, heightChildSpec);
        }
        setMeasuredDimension(((this.mPageIndicatorWidth - this.mPageDotWidth) * N) + this.mPageDotWidth, this.mPageIndicatorHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int N = getChildCount();
        if (N != 0) {
            for (int i = 0; i < N; i++) {
                int left = (this.mPageIndicatorWidth - this.mPageDotWidth) * i;
                getChildAt(i).layout(left, 0, this.mPageIndicatorWidth + left, this.mPageIndicatorHeight);
            }
        }
    }
}
