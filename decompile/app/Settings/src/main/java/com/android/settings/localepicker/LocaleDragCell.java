package com.android.settings.localepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

class LocaleDragCell extends RelativeLayout {
    private CheckBox mCheckbox;
    private ImageView mDragHandle;
    private TextView mLabel;
    private LinearLayout mLinearlayout;
    private TextView mLocalized;
    private ImageView mMiniLabel;

    public LocaleDragCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLabel = (TextView) findViewById(2131886728);
        this.mLocalized = (TextView) findViewById(2131886761);
        this.mMiniLabel = (ImageView) findViewById(2131886762);
        this.mCheckbox = (CheckBox) findViewById(2131886163);
        this.mDragHandle = (ImageView) findViewById(2131886760);
        this.mLinearlayout = (LinearLayout) findViewById(2131886759);
    }

    public void setShowHandle(boolean showHandle) {
        this.mDragHandle.setAlpha(showHandle ? 255 : 51);
        invalidate();
        requestLayout();
    }

    public void setShowCheckbox(boolean showCheckbox) {
        if (showCheckbox) {
            this.mCheckbox.setVisibility(0);
            this.mLabel.setVisibility(4);
        } else {
            this.mCheckbox.setVisibility(4);
            this.mLabel.setVisibility(0);
        }
        invalidate();
        requestLayout();
    }

    public void setChecked(boolean checked) {
        this.mCheckbox.setChecked(checked);
    }

    public void setShowMiniLabel(boolean showMiniLabel) {
        this.mMiniLabel.setVisibility(showMiniLabel ? 0 : 8);
        invalidate();
        requestLayout();
    }

    public void setMiniLabel(int mResOK, boolean isShow) {
        this.mMiniLabel.setImageResource(mResOK);
        this.mMiniLabel.setVisibility(isShow ? 0 : 8);
        invalidate();
    }

    public void setLabelAndDescription(String labelText, String description, boolean showColor) {
        this.mLabel.setText(labelText);
        this.mLabel.setTextColor(getResources().getColor(2131427603));
        if (showColor) {
            this.mLabel.setTextColor(getResources().getColor(2131427540));
        }
        this.mCheckbox.setText(labelText);
        this.mLabel.setContentDescription(description);
        this.mCheckbox.setContentDescription(description);
        invalidate();
    }

    public void setLocalized(boolean localized) {
        int i = 0;
        int oneTextMargin = getResources().getDimensionPixelSize(2131559014);
        int doubleTextMargin = getResources().getDimensionPixelSize(2131559015);
        LayoutParams layoutParams = (LayoutParams) this.mLinearlayout.getLayoutParams();
        if (localized) {
            layoutParams.setMargins(0, oneTextMargin, 0, oneTextMargin);
        } else {
            layoutParams.setMargins(0, doubleTextMargin, 0, doubleTextMargin);
        }
        this.mLinearlayout.setLayoutParams(layoutParams);
        TextView textView = this.mLocalized;
        if (localized) {
            i = 8;
        }
        textView.setVisibility(i);
        invalidate();
    }

    public ImageView getDragHandle() {
        return this.mDragHandle;
    }

    public CheckBox getCheckbox() {
        return this.mCheckbox;
    }
}
