package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.huawei.mms.util.ResEx;

public class EmuiMenuText extends TextView {
    int mDisableHint = 0;
    int mMaxIconSize = 0;
    int mMenuId;

    public EmuiMenuText(Context context) {
        super(context);
        initIconSize();
    }

    public EmuiMenuText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initIconSize();
    }

    public EmuiMenuText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initIconSize();
    }

    private void initIconSize() {
        this.mMaxIconSize = (int) ((32.0f * getResources().getDisplayMetrics().density) + 0.5f);
        setDisplayMaxLines();
    }

    private void setDisplayMaxLines() {
        setSingleLine(false);
        setMaxLines(2);
        setEllipsize(TruncateAt.valueOf("END"));
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setActivated(true);
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

    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    protected void onDraw(Canvas canvas) {
        if (!isActivated()) {
            setAlpha(0.3f);
        } else if (isPressed() || isFocused()) {
            setAlpha(0.5f);
        } else {
            setAlpha(ContentUtil.FONT_SIZE_NORMAL);
        }
        super.onDraw(canvas);
    }

    public void updateMenu(int cmd, int strId, int iconId) {
        setText(strId);
        setIcon(iconId);
        this.mMenuId = cmd;
    }

    public void setMenuId(int menuId) {
        this.mMenuId = menuId;
    }

    public int getMenuId() {
        return this.mMenuId;
    }

    public void setIcon(Drawable icon) {
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > this.mMaxIconSize) {
                height = (int) (((float) height) * (((float) this.mMaxIconSize) / ((float) width)));
                width = this.mMaxIconSize;
            }
            if (height > this.mMaxIconSize) {
                width = (int) (((float) width) * (((float) this.mMaxIconSize) / ((float) height)));
                height = this.mMaxIconSize;
            }
            icon.setBounds(0, 0, width, height);
            setCompoundDrawables(null, icon, null, null);
        }
    }

    public void setIcon(int resId) {
        setIcon(ResEx.self().getStateListDrawable(getContext(), resId));
    }
}
