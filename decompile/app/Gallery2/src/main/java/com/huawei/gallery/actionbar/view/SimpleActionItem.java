package com.huawei.gallery.actionbar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.util.ImmersionUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class SimpleActionItem extends ImageButton implements ActionItem {
    private Action mAction;
    private int mStyle = 0;
    private String mText = null;

    public SimpleActionItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAction(Action action) {
        if (action == Action.NONE) {
            setVisibility(4);
            this.mAction = Action.NONE;
            return;
        }
        if (getVisibility() != 0) {
            setVisibility(0);
        }
        this.mAction = action;
        ImmersionUtils.setImageViewSrcImmersionStyle(this, action.iconResID, action.iconWhiteResID, this.mStyle, true);
        this.mText = getResources().getString(action.textResID);
        setContentDescription(this.mText);
    }

    private void setItemView(boolean isPressed, boolean isEnable) {
        float alpha = isEnable ? isPressed ? 0.5f : WMElement.CAMERASIZEVALUE1B1 : 0.3f;
        setAlpha(alpha);
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        setItemView(pressed, isEnabled());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setItemView(isPressed(), enabled);
    }

    public Action getAction() {
        return this.mAction;
    }

    public View asView() {
        return this;
    }

    public void applyStyle(int style) {
        this.mStyle = style;
        if (this.mAction != null) {
            setAction(this.mAction);
        }
    }
}
