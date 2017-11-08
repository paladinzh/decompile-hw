package com.huawei.systemmanager.emui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;

public class ButtonTextView extends TextView {
    private static final String BUTTON_BACKGROUND = "@*androidhwext:drawable/dialog_btn_default_emui";

    public ButtonTextView(Context context) {
        super(context);
        setStyleForEmui();
    }

    public ButtonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStyleForEmui();
    }

    public ButtonTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setStyleForEmui();
    }

    private void setStyleForEmui() {
        Resources resources = getResources();
        if (resources != null) {
            int themeID = resources.getIdentifier(BUTTON_BACKGROUND, null, null);
            if (themeID != 0) {
                setBackground(resources.getDrawable(themeID));
            }
        }
    }
}
