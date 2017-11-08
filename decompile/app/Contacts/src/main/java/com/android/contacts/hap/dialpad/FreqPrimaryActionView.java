package com.android.contacts.hap.dialpad;

import android.content.Context;
import android.os.Trace;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import com.android.contacts.dialpad.DialpadFragment.AllContactListItemViews;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class FreqPrimaryActionView extends ViewGroup {
    private boolean isVisible;
    private AllContactListItemViews mCallLogListItemViews = null;
    private int mDetailsViewMeasureEnd;
    private int mDetailsViewMeasureStart;
    private View mDivider;
    private int mDividerHeight;
    private int mItemMaxWidth;
    private View mPhoneCallDetailsView = null;
    private int mPreferredHeight;
    private int mSecondaryActionViewPaddingEnd;
    private int mSecondaryActionViewPaddingStart;
    private int mSecondaryActionViewWidth;
    private View mShadowView;

    public FreqPrimaryActionView(Context context) {
        super(context);
    }

    public FreqPrimaryActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreqPrimaryActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bindWidth(int itemMaxWidth) {
        this.mPreferredHeight = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_time_axis_default_height);
        this.mDetailsViewMeasureStart = getContext().getResources().getDimensionPixelSize(R.dimen.contact_list_item_indent);
        this.mDetailsViewMeasureEnd = getContext().getResources().getDimensionPixelSize(R.dimen.freq_call_list_item_margin_end);
        this.mSecondaryActionViewPaddingStart = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_secondary_action_icon_padding);
        this.mSecondaryActionViewWidth = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_secondaryaction_icon_width);
        this.mSecondaryActionViewPaddingEnd = this.mSecondaryActionViewPaddingStart;
        this.mDividerHeight = getContext().getResources().getDimensionPixelSize(R.dimen.contact_list_divider_height);
        this.mItemMaxWidth = itemMaxWidth;
        if (this.mItemMaxWidth <= 0) {
            this.mItemMaxWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        }
    }

    public void bindViews(AllContactListItemViews views, View phoneCallDetailsView, View shadowView, View divider) {
        this.mCallLogListItemViews = views;
        this.mPhoneCallDetailsView = phoneCallDetailsView;
        this.mShadowView = shadowView;
        this.mDivider = divider;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        Trace.traceBegin(8, "FreqPrimaryActionView onMeasure");
        int specWidth = this.mItemMaxWidth;
        int preferredHeight = this.mPreferredHeight;
        View phoneCallDetailsView = this.mPhoneCallDetailsView;
        if (phoneCallDetailsView != null) {
            phoneCallDetailsView.measure(MeasureSpec.makeMeasureSpec(this.mItemMaxWidth - (this.mDetailsViewMeasureStart + this.mDetailsViewMeasureEnd), 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
        }
        View view = (this.mCallLogListItemViews == null || this.mCallLogListItemViews.mSecondaryActionViewLayout == null) ? null : this.mCallLogListItemViews.mSecondaryActionViewLayout;
        int actionWidth = (this.mSecondaryActionViewWidth + this.mSecondaryActionViewPaddingStart) + this.mSecondaryActionViewPaddingEnd;
        if (view != null) {
            view.measure(MeasureSpec.makeMeasureSpec(actionWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
        }
        View divider = this.mDivider;
        if (divider != null) {
            divider.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(this.mDividerHeight, 1073741824));
        }
        int width = specWidth;
        if (phoneCallDetailsView != null) {
            height = phoneCallDetailsView.getMeasuredHeight();
        } else {
            height = preferredHeight;
        }
        height = Math.max(height, preferredHeight);
        if (this.mShadowView != null) {
            this.mShadowView.measure(MeasureSpec.makeMeasureSpec(specWidth, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        }
        setMeasuredDimension(specWidth, height);
        Trace.traceEnd(8);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean mIsMirror = CommonUtilMethods.isLayoutRTL();
        int height = bottom - top;
        int width = right - left;
        int bottomBound = height;
        int leftBound = getPaddingLeft();
        int rightBound = width - getPaddingRight();
        View phoneCallDetailsView = this.mPhoneCallDetailsView;
        if (phoneCallDetailsView != null) {
            int lTopBound;
            if (mIsMirror) {
                int lrightBound = rightBound - this.mDetailsViewMeasureStart;
                lTopBound = ((height + 0) - phoneCallDetailsView.getMeasuredHeight()) / 2;
                phoneCallDetailsView.layout(lrightBound - phoneCallDetailsView.getMeasuredWidth(), lTopBound, lrightBound, phoneCallDetailsView.getMeasuredHeight() + lTopBound);
            } else {
                int lLeftBound = leftBound + this.mDetailsViewMeasureStart;
                lTopBound = ((height + 0) - phoneCallDetailsView.getMeasuredHeight()) / 2;
                phoneCallDetailsView.layout(lLeftBound, lTopBound, lLeftBound + phoneCallDetailsView.getMeasuredWidth(), phoneCallDetailsView.getMeasuredHeight() + lTopBound);
            }
        }
        View secondaryActionView = (this.mCallLogListItemViews == null || this.mCallLogListItemViews.mSecondaryActionViewLayout == null) ? null : this.mCallLogListItemViews.mSecondaryActionViewLayout;
        if (secondaryActionView != null) {
            if (mIsMirror) {
                lLeftBound = leftBound;
                secondaryActionView.layout(leftBound, 0, leftBound + secondaryActionView.getMeasuredWidth(), height);
            } else {
                lrightBound = rightBound;
                secondaryActionView.layout(rightBound - secondaryActionView.getMeasuredWidth(), 0, rightBound, height);
            }
            setViewPivot(secondaryActionView);
        }
        View divider = this.mDivider;
        if (divider != null) {
            divider.layout(leftBound, height - divider.getMeasuredHeight(), rightBound, height);
            divider.setVisibility(getDividerisVisible() ? 8 : 0);
        }
        if (this.mShadowView != null) {
            this.mShadowView.layout(left, top, right, bottom);
        }
    }

    public void setScaleX(float scaleX) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView))) {
                view.setScaleX(scaleX);
            }
        }
    }

    public void setScaleY(float scaleY) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView))) {
                view.setScaleY(scaleY);
            }
        }
    }

    private void setViewPivot(View view) {
        if (view != null) {
            view.setPivotX((float) ((getMeasuredWidth() / 2) - view.getLeft()));
            view.setPivotY(getRotationY());
        }
    }

    public void setDividerisVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    private boolean getDividerisVisible() {
        return this.isVisible;
    }
}
