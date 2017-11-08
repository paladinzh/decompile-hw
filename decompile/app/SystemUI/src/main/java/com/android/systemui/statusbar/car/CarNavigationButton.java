package com.android.systemui.statusbar.car;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.keyguard.AlphaOptimizedImageButton;
import com.android.systemui.R;

public class CarNavigationButton extends RelativeLayout {
    private AlphaOptimizedImageButton mIcon;
    private AlphaOptimizedImageButton mMoreIcon;

    public CarNavigationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (AlphaOptimizedImageButton) findViewById(R.id.car_nav_button_icon);
        this.mIcon.setClickable(false);
        this.mIcon.setBackgroundColor(17170445);
        this.mIcon.setAlpha(0.7f);
        this.mMoreIcon = (AlphaOptimizedImageButton) findViewById(R.id.car_nav_button_more_icon);
        this.mMoreIcon.setClickable(false);
        this.mMoreIcon.setBackgroundColor(17170445);
        this.mMoreIcon.setVisibility(4);
        this.mMoreIcon.setImageDrawable(getContext().getDrawable(R.drawable.car_ic_arrow));
        this.mMoreIcon.setAlpha(0.7f);
    }

    public void setResources(Drawable icon) {
        this.mIcon.setImageDrawable(icon);
    }

    public void setSelected(boolean selected, boolean showMoreIcon) {
        int i = 4;
        if (selected) {
            AlphaOptimizedImageButton alphaOptimizedImageButton = this.mMoreIcon;
            if (showMoreIcon) {
                i = 0;
            }
            alphaOptimizedImageButton.setVisibility(i);
            this.mMoreIcon.setAlpha(1.0f);
            this.mIcon.setAlpha(1.0f);
            return;
        }
        this.mMoreIcon.setVisibility(4);
        this.mIcon.setAlpha(0.7f);
    }
}
