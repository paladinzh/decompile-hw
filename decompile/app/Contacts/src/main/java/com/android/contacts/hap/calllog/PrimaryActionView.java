package com.android.contacts.hap.calllog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class PrimaryActionView extends ViewGroup {
    private CallLogListItemViews mCallLogListItemViews = null;
    private CheckBox mCheckBox;
    private int mItemMaxWidth;
    private View mPhoneCallDetailsView = null;
    private int mPreferredHeight;
    private int mPreferredWidth;
    private int mSecondaryActionViewPaddingEnd;
    private int mSecondaryActionViewPaddingStart;
    private int mSecondaryActionViewWidth;
    private View mShadowView;

    public PrimaryActionView(Context context) {
        super(context);
    }

    public PrimaryActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrimaryActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bindWidth(int dialerWidth, boolean isCallLogFragment, boolean isLandscape) {
        if (isCallLogFragment || !isLandscape) {
            this.mPreferredWidth = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_time_axis_child_content_default_width);
        } else {
            this.mPreferredWidth = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_time_axis_child_content_default_width_land);
        }
        this.mPreferredWidth = ContactDpiAdapter.getNewDpiFromDimen(this.mPreferredWidth);
        this.mPreferredHeight = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_time_axis_default_height);
        this.mSecondaryActionViewPaddingStart = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_secondary_action_icon_padding);
        this.mSecondaryActionViewWidth = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_secondaryaction_icon_width);
        this.mSecondaryActionViewPaddingEnd = getContext().getResources().getDimensionPixelSize(R.dimen.call_log_list_item_secondary_action_icon_padding_end);
        this.mItemMaxWidth = dialerWidth;
    }

    public int getViewMaxWidth() {
        return this.mItemMaxWidth;
    }

    public void bindViews(CallLogListItemViews views, View phoneCallDetailsView, CheckBox checkBox, View shadowView) {
        this.mCallLogListItemViews = views;
        this.mPhoneCallDetailsView = phoneCallDetailsView;
        this.mCheckBox = checkBox;
        this.mShadowView = shadowView;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height;
        int specWidth = this.mItemMaxWidth;
        int preferredWidth = this.mPreferredWidth;
        int preferredHeight = this.mPreferredHeight;
        View phoneCallDetailsView = this.mPhoneCallDetailsView;
        if (phoneCallDetailsView != null) {
            int detailsWidth = preferredWidth;
            phoneCallDetailsView.measure(MeasureSpec.makeMeasureSpec(preferredWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
        }
        int width = specWidth;
        if (phoneCallDetailsView != null) {
            height = phoneCallDetailsView.getMeasuredHeight();
        } else {
            height = preferredHeight;
        }
        height = Math.max(height, preferredHeight);
        View view = this.mCallLogListItemViews != null ? this.mCallLogListItemViews.secondaryActionViewLayout : null;
        int actionWidth = (this.mSecondaryActionViewWidth + this.mSecondaryActionViewPaddingStart) + this.mSecondaryActionViewPaddingEnd;
        if (view != null) {
            view.measure(MeasureSpec.makeMeasureSpec(actionWidth, 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
        }
        if (this.mCheckBox != null) {
            this.mCheckBox.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        }
        if (this.mShadowView != null) {
            this.mShadowView.measure(MeasureSpec.makeMeasureSpec(specWidth, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        }
        setMeasuredDimension(specWidth, height);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int lTopBound;
        boolean mIsMirror = CommonUtilMethods.isLayoutRTL();
        int height = bottom - top;
        int width = right - left;
        int bottomBound = height;
        int leftBound = getPaddingLeft();
        int rightBound = width - getPaddingRight();
        View phoneCallDetailsView = this.mPhoneCallDetailsView;
        if (phoneCallDetailsView != null) {
            if (mIsMirror) {
                int lrightBound = rightBound;
                lTopBound = ((height + 0) - phoneCallDetailsView.getMeasuredHeight()) / 2;
                phoneCallDetailsView.layout(rightBound - phoneCallDetailsView.getMeasuredWidth(), lTopBound, rightBound, phoneCallDetailsView.getMeasuredHeight() + lTopBound);
            } else {
                int i = leftBound;
                lTopBound = ((height + 0) - phoneCallDetailsView.getMeasuredHeight()) / 2;
                phoneCallDetailsView.layout(leftBound, lTopBound, leftBound + phoneCallDetailsView.getMeasuredWidth(), phoneCallDetailsView.getMeasuredHeight() + lTopBound);
            }
            setViewPivot(phoneCallDetailsView);
        }
        View secondaryActionView = this.mCallLogListItemViews != null ? this.mCallLogListItemViews.secondaryActionViewLayout : null;
        if (secondaryActionView != null) {
            if (mIsMirror) {
                i = leftBound;
                secondaryActionView.layout(leftBound, 0, leftBound + secondaryActionView.getMeasuredWidth(), height);
            } else {
                lTopBound = ((height + 0) - secondaryActionView.getMeasuredHeight()) / 2;
                lrightBound = rightBound;
                secondaryActionView.layout(rightBound - secondaryActionView.getMeasuredWidth(), 0, rightBound, height);
            }
            setViewPivot(secondaryActionView);
        }
        if (!(this.mCheckBox == null || secondaryActionView == null)) {
            lTopBound = ((height + 0) - this.mCheckBox.getMeasuredHeight()) / 2;
            if (mIsMirror) {
                i = leftBound - this.mSecondaryActionViewPaddingStart;
                this.mCheckBox.layout(i, lTopBound, i + secondaryActionView.getMeasuredWidth(), this.mCheckBox.getMeasuredHeight() + lTopBound);
            } else {
                lrightBound = rightBound + this.mSecondaryActionViewPaddingStart;
                this.mCheckBox.layout(lrightBound - secondaryActionView.getMeasuredWidth(), lTopBound, lrightBound, this.mCheckBox.getMeasuredHeight() + lTopBound);
            }
            setViewPivot(this.mCheckBox);
        }
        if (this.mShadowView != null) {
            this.mShadowView.layout(left, top, right, bottom);
        }
    }

    public boolean isLandScape() {
        if (getResources().getConfiguration().orientation == 2) {
            return true;
        }
        return false;
    }

    public void setScaleX(float scaleX) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView) || (this.mCallLogListItemViews != null && this.mCallLogListItemViews.phoneCallDetailsViews != null && view.equals(this.mCallLogListItemViews.phoneCallDetailsViews.timeAxisWidget)))) {
                view.setScaleX(scaleX);
            }
        }
    }

    public void setScaleY(float scaleY) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (!(view == null || view.equals(this.mShadowView) || (this.mCallLogListItemViews != null && this.mCallLogListItemViews.phoneCallDetailsViews != null && view.equals(this.mCallLogListItemViews.phoneCallDetailsViews.timeAxisWidget)))) {
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
}
