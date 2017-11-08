package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.systemui.R;

public class DismissView extends StackScrollerDecorView {
    private DismissViewButton mDismissButton;

    public DismissView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected View findContentView() {
        return findViewById(R.id.dismiss_text);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDismissButton = (DismissViewButton) findContentView();
    }

    public void setOnButtonClickListener(OnClickListener listener) {
        this.mContent.setOnClickListener(listener);
    }

    public boolean isOnEmptySpace(float touchX, float touchY) {
        if (touchX < this.mContent.getX() || touchX > this.mContent.getX() + ((float) this.mContent.getWidth()) || touchY < this.mContent.getY() || touchY > this.mContent.getY() + ((float) this.mContent.getHeight())) {
            return true;
        }
        return false;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDismissButton.setText(R.string.clear_all_notifications_text);
    }

    public boolean isButtonVisible() {
        return this.mDismissButton.getAlpha() != 0.0f;
    }
}
