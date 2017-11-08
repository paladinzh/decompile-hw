package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.google.android.gms.R;

public class EmuiAvatarImage extends FrameLayout {
    private ImageView mPhoto;

    public EmuiAvatarImage(Context context) {
        super(context);
    }

    public EmuiAvatarImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmuiAvatarImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPhoto = (ImageView) findViewById(R.id.photo);
    }

    public void setImageDrawable(Drawable avatar) {
        this.mPhoto.setScaleType(ScaleType.CENTER_CROP);
        this.mPhoto.setImageDrawable(avatar);
    }

    public void setOnClickListener(OnClickListener l) {
        this.mPhoto.setOnClickListener(l);
    }

    public void setClickable(boolean clickable) {
        this.mPhoto.setClickable(clickable);
        this.mPhoto.setEnabled(clickable);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mPhoto.dispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
