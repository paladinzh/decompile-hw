package com.huawei.systemmanager.emui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;

public class SectionTextView extends TextView {
    private static final String SECTOR_TEXT_COLOR = "@*androidhwext:color/emui_label_primary_text";

    public SectionTextView(Context context) {
        super(context);
        setStyleForEmui();
    }

    public SectionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStyleForEmui();
    }

    public SectionTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setStyleForEmui();
    }

    private void setStyleForEmui() {
        Resources resources = getResources();
        if (resources == null || resources.getIdentifier(SECTOR_TEXT_COLOR, null, null) != 0) {
        }
    }
}
