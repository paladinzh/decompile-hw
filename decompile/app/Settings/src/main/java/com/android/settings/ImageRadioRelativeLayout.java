package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class ImageRadioRelativeLayout extends RelativeLayout implements Checkable {
    private RadioButton mRadioButton;

    public ImageRadioRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        updateView();
    }

    private void updateView() {
        int childCount = getChildCount();
        for (int idx = 0; idx < childCount; idx++) {
            View view = getChildAt(idx);
            if (view != null && (view instanceof RadioButton)) {
                this.mRadioButton = (RadioButton) view;
            }
        }
    }

    public void setChecked(boolean checked) {
        if (this.mRadioButton == null) {
            Log.w("ImageRadioRelativeLayout", "setChecked but radiobutton is null.");
        } else {
            this.mRadioButton.setChecked(checked);
        }
    }

    public boolean isChecked() {
        if (this.mRadioButton != null) {
            return this.mRadioButton.isChecked();
        }
        Log.w("ImageRadioRelativeLayout", "isChecked but radiobutton is null.");
        return false;
    }

    public void toggle() {
        if (this.mRadioButton == null) {
            Log.w("ImageRadioRelativeLayout", "toggle but radiobutton is null.");
        } else {
            this.mRadioButton.toggle();
        }
    }
}
