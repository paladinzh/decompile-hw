package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;

public class HwTintImageView extends ImageView {
    private Drawable mDrawable = null;
    private Drawable mDrawableWithTrans = null;
    private float mEndMargin = 0.0f;
    private float mIconWidth;
    private float mStartMargin = 0.0f;
    private float mViewWidth;

    public HwTintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mStartMargin = getResources().getDimension(R$dimen.fyuse_icon_small_margin_with_title);
        this.mEndMargin = getResources().getDimension(R$dimen.fyuse_icon_margin_with_title);
        this.mDrawableWithTrans = getResources().getDrawable(R$drawable.ic_unlock_fyuse_flag_icon_trans);
        this.mDrawable = getResources().getDrawable(R$drawable.ic_unlock_fyuse_flag_icon);
        this.mIconWidth = getResources().getDimension(R$dimen.fyuse_icon_width);
        this.mViewWidth = getResources().getDimension(R$dimen.fyuse_icon_view_width);
    }

    public void refreshImageDrawble(float dy, int h) {
        if (getVisibility() == 0 && h != 0) {
            float offset = Math.abs(((this.mEndMargin - this.mStartMargin) * dy) / ((float) h));
            boolean isRtlLocale = isRtlLocale();
            if (isRtlLocale) {
                offset = (this.mViewWidth - this.mIconWidth) - offset;
            }
            setTranslationX(offset);
            if (Math.abs(getTranslationX()) == this.mEndMargin - this.mStartMargin || (isRtlLocale && Math.abs(getTranslationX()) == (this.mViewWidth - this.mIconWidth) - (this.mEndMargin - this.mStartMargin))) {
                setImageDrawable(this.mDrawableWithTrans);
            } else {
                setImageDrawable(this.mDrawable);
            }
            invalidate();
        }
    }
}
