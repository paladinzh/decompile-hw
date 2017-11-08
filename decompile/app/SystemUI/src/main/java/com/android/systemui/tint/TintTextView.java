package com.android.systemui.tint;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.Observable;
import java.util.Observer;

public class TintTextView extends TextView implements Observer {
    private int mColor = -1275068417;
    private boolean mIsResever = true;

    public TintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TintTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintTextView(Context context) {
        super(context);
    }

    protected void onAttachedToWindow() {
        TintManager.getInstance().addObserver(this);
        setColorByTintManager();
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        TintManager.getInstance().deleteObserver(this);
        super.onDetachedFromWindow();
    }

    public void update(Observable o, Object arg) {
        if (getVisibility() == 0) {
            setColorByTintManager();
        }
    }

    public void setColorByTintManager() {
        if (isResever() && getVisibility() == 0) {
            int tintColor = TintManager.getInstance().getIconColorByType("statusBarType", this.mColor);
            Log.d("TintTextView", String.format("#%08X", new Object[]{Integer.valueOf(tintColor)}) + " " + this);
            super.setTextColor(tintColor);
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == 0 && !TintUtil.isAnyParentNodeInvisible(this)) {
            setColorByTintManager();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    public void setTextColor(int color) {
        this.mColor = color;
        if (isResever()) {
            color = TintManager.getInstance().getIconColorByType("statusBarType", this.mColor);
        }
        super.setTextColor(color);
    }

    public boolean isResever() {
        return this.mIsResever;
    }

    public void setIsResever(boolean isResever) {
        this.mIsResever = isResever;
    }
}
