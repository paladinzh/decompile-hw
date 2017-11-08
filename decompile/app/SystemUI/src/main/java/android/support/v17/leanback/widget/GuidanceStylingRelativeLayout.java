package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$styleable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class GuidanceStylingRelativeLayout extends RelativeLayout {
    private float mTitleKeylinePercent;

    public GuidanceStylingRelativeLayout(Context context) {
        this(context, null);
    }

    public GuidanceStylingRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuidanceStylingRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(R$styleable.LeanbackGuidedStepTheme);
        this.mTitleKeylinePercent = ta.getFloat(R$styleable.LeanbackGuidedStepTheme_guidedStepKeyline, 40.0f);
        ta.recycle();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        TextView mTitleView = (TextView) getRootView().findViewById(R$id.guidance_title);
        TextView mBreadcrumbView = (TextView) getRootView().findViewById(R$id.guidance_breadcrumb);
        TextView mDescriptionView = (TextView) getRootView().findViewById(R$id.guidance_description);
        ImageView mIconView = (ImageView) getRootView().findViewById(R$id.guidance_icon);
        int mTitleKeylinePixels = (int) ((((float) getMeasuredHeight()) * this.mTitleKeylinePercent) / 100.0f);
        if (mTitleView != null && mTitleView.getParent() == this) {
            int offset = (((mTitleKeylinePixels - (-mTitleView.getPaint().getFontMetricsInt().top)) - mBreadcrumbView.getMeasuredHeight()) - mTitleView.getPaddingTop()) - mBreadcrumbView.getTop();
            if (mBreadcrumbView != null && mBreadcrumbView.getParent() == this) {
                mBreadcrumbView.offsetTopAndBottom(offset);
            }
            mTitleView.offsetTopAndBottom(offset);
            if (mDescriptionView != null && mDescriptionView.getParent() == this) {
                mDescriptionView.offsetTopAndBottom(offset);
            }
        }
        if (mIconView != null && mIconView.getParent() == this && mIconView.getDrawable() != null) {
            mIconView.offsetTopAndBottom(mTitleKeylinePixels - (mIconView.getMeasuredHeight() / 2));
        }
    }
}
