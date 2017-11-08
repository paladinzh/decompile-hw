package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;

public class EmuiMenuImage extends ImageView {
    int mDisableHint = 0;
    int mMaxIconSize = 0;

    public EmuiMenuImage(Context context) {
        super(context);
        initIconSize();
    }

    public EmuiMenuImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initIconSize();
    }

    public EmuiMenuImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initIconSize();
    }

    private void initIconSize() {
        this.mMaxIconSize = (int) ((24.0f * getResources().getDisplayMetrics().density) + 0.5f);
        setMaxWidth(this.mMaxIconSize);
        setMaxHeight(this.mMaxIconSize);
        setActivated(true);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isActivated()) {
            return super.onTouchEvent(event);
        }
        if (this.mDisableHint == 0) {
            return true;
        }
        switch (event.getAction()) {
            case 1:
            case 3:
            case 4:
                Toast.makeText(getContext(), this.mDisableHint, 0).show();
                break;
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        if (!isActivated()) {
            setAlpha(0.3f);
        } else if (isFocused() || isPressed()) {
            setAlpha(0.5f);
        } else {
            setAlpha(ContentUtil.FONT_SIZE_NORMAL);
        }
        super.onDraw(canvas);
    }
}
