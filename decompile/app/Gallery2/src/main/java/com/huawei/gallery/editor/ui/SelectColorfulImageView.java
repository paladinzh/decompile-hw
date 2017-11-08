package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.gallery.util.ColorfulUtils;

public class SelectColorfulImageView extends ImageView {
    private boolean mFocus = false;
    private boolean mIsSelected = false;

    public SelectColorfulImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mFocus || this.mIsSelected != isSelected()) {
            this.mFocus = false;
            this.mIsSelected = isSelected();
            if (this.mIsSelected) {
                ColorfulUtils.decorateColorfulForImageView(getContext(), this);
            }
        }
        super.onDraw(canvas);
    }

    public void setImageResource(int resId) {
        this.mFocus = true;
        super.setImageResource(resId);
    }
}
